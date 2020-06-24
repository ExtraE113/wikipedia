import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.io.StringReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val requestQueue: PriorityQueueNoDuplicates<WikiRequest> = PriorityQueueNoDuplicates()

fun main() {
	requestQueue.add(
		WikiRequest(
			URL(apiURLBase),
			::addAllPagesQueryResultsToQueue,
			100
		)
	)

	val klaxon = Klaxon()

	var counter = 0


	while (true) {
		if (requestQueue.isNotEmpty()) {
			val next: WikiRequest = requestQueue.poll()
			println(next)
			val (URL, callback) = next
			val parsed: JsonObject = klaxon.parseJsonObject(StringReader(URL.readText()))
			callback(parsed, next.callbackArguments)
			counter++
			if (counter % 100 == 0) {
				articlesHolder.save(path = "./ah$counter.ser")
			}
		} else {
			articlesHolder.save(path = "./ah$counter.ser")
			break
		}
	}
}

/**
 * Callback function for [WikiRequest]s.
 *
 * To be used generating the initial queue. First request added by hand with this callback.
 *
 * 1. If there is a continue block (ie we can continue), add the continue request to the queue with priority 100
 * 2. Add all the received items to the queue so we can get back their parsed HTML
 *
 * @see WikiRequest
 */

fun addAllPagesQueryResultsToQueue(parsed: JsonObject, passthroughArguments: Map<String, Any>) {

	//checks if there is a continue block in the response, if there is then we can continue because there are more results
	//todo test
	fun canContinue(parsed: JsonObject) = parsed["continue"] != null


	//builds the continue block part of the url that we request
	//todo should build up dynamically depending on whats in the continue block (see api documentation for continue), but this works for now
	//todo test
	fun continueArguments(parsed: JsonObject) =
		"&continue=${(parsed["continue"] as JsonObject)["continue"]}&gapcontinue=${
		URLEncoder.encode(
			(parsed["continue"] as JsonObject)["gapcontinue"].toString(),
			StandardCharsets.UTF_8.toString()
		)
		}" //black magic json processing todo clean up


	//step 1
	if (canContinue(parsed)) {
		requestQueue.add(
			WikiRequest(
				URL(apiURLBase + continueArguments(parsed)), ::addAllPagesQueryResultsToQueue, 100
			)
		)
	}

	//step 2
	requestQueue.addAll(
		createWikiRequestsFromParsedJsonObjectPageList(parsed)
	)


}

/**
 * Callback function for [WikiRequest]s.
 *
 * To be used after a request for a parsed-to-HTML page
 *
 * First gets out wikilink, then extracts linked to article name, then creates relevant edge in articleholder
 * (as a side effect it will create the articles if they don't already exist)
 *
 * @param [passthroughArguments] "title" -> String
 * @see WikiRequest
 */
fun createEdge(response: JsonObject, passthroughArguments: Map<String, Any>) {
	fun getLinkElement(doc: Node): Element {
		doc.childNodes()
			.filter {
				it is Element && (it.tagName() in listOf(
					"p",
					"ul",
					"ol"
				) /* todo expand*/ || it.attr("class") == "redirectMsg")
			}
			.forEach { topLevelChildren ->
				var stringRepresentation = topLevelChildren.toString()
				val item = Jsoup.parseBodyFragment(stringRepresentation)
				val links = item.select("a[href]")
				for ((index, it) in links.withIndex()) {
					links[index] = it.attr(
						"href",
						it.attr("href").replace("(", "--__OPEN-PAREN__--").replace(")", "--__CLOSE-PAREN__--")
					)
				}

				stringRepresentation = item.toString()
				//todo very likely very buggy, improve, test, bug fig
				//optimize
				stringRepresentation = stringRepresentation.replace(Regex("""\(.*?\)"""), "")
				val line = Jsoup.parseBodyFragment(stringRepresentation)

				val linksNotParend: Elements = line.select("a[href]")
				if (linksNotParend.size > 0) {

					fun parentOrChildIsNotIllegal(parent: Element) =
						parent.tagName() != "i" /*parent is not italics*/ && "reference" !in parent.classNames() /*not a footnote i.e. parent is not a reference class*/

					linksNotParend.forEach { it1 ->
						var parent = it1.parent()
						var foundIllegalParent: Boolean
						//iterate until either we run out of parents or find an illegal parent
						while (parentOrChildIsNotIllegal(parent) && parent.hasParent()) {
							parent = parent.parent()
						}

						//if we stopped iterating because we found an illegal parent, then the current parent is illegal
						//so we can set foundIllegalParent
						foundIllegalParent = !parentOrChildIsNotIllegal(parent)

						var foundIllegalChild = false

						fun lookForIllegalChildren(children: Elements) {
							for (it in children) {
								if (!parentOrChildIsNotIllegal(it)) {
									foundIllegalChild = true
									break
								}
								lookForIllegalChildren(it.children())
							}
						}

						lookForIllegalChildren(it1.children())

						/*
						 *	and if we didn't find an illegal parent or child, assuming it meets a few other conditions
						 *	namely, is not a red link, is an internal wikilink
						 *	then return this
						*/
						if (!it1.attr("href").contains("redlink=1") &&
							!foundIllegalParent && !foundIllegalChild &&
							Regex("""/wiki/.+""").matches(it1.attr("href")) &&
							!it1.attr("href").startsWith("/wiki/File:") //todo test
						) {
							return it1
						}
					}
				}

			}
		return Element("a")
	}


	//makes an article given a title and adds it to old.getArticlesHolder
	//also makes the article it links to
	//todo should also probably be modified to work with whatever structure we end up using based on this
	//	(https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#actors)
	fun makeArticle(title: String, titleOfArticleLinkedTo: String) {
		articlesHolder[title].firstLink = titleOfArticleLinkedTo
	}
	if (response.obj("error")?.get("code").toString() == "missingtitle"){
		makeArticle(passthroughArguments["title"].toString(), "")
		return
	}
	val html = response.obj("parse")!!.obj("text")!!["*"]!!
	val doc: Node = Jsoup.parse(html.toString()).body().getElementsByClass("mw-parser-output")[0]

	val linkElement = getLinkElement(doc)

//	if (linkElement.tagName() != "a"){
//		throw Exception("Something has gone wrong-- getLinkElement didn't return an 'a' tag") //todo replace with flag for manual review
//	}
//
//	if (linkElement.attr("href").isEmpty() || !Regex("""/wiki/.+""").matches(linkElement.attr("href"))){
//		throw Exception("Something has gone wrong-- getLinkElement didn't return an internal wikilink") //todo replace with flag for manual review
//	}

	makeArticle(
		passthroughArguments["title"] as String,
		if (linkElement.attr("href") != null && linkElement.attr("href").length > 6)
			linkElement.attr("href")
				.substring(6) //after the /wiki/
				.replace("--__OPEN-PAREN__--", "(")
				.replace("--__CLOSE-PAREN__--", ")")
				.split("#")[0] //ignore anchors
		else "" /*todo got to be a better way of doing this*/
	)


}