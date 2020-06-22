import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import old.*
import old.canContinue
import old.continueArguments
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.io.StringReader
import java.lang.Exception
import java.net.URL


//<editor-fold desc="Set up constants">
val WIKI = "simple"

//these two probably don't belong here. todo move
val articleBuilder = ArticleBuilder()
val articlesHolder = ArticlesHolder(150_000)

val debugRunShort = true
val debugURLBase =
	"https://simple.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=25&gapto=.tj"
val apiURLBase =
	if (!debugRunShort) "https://$WIKI.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=max" else debugURLBase
//</editor-fold>

var requestQueue: PriorityQueueNoDuplicates<WikiRequest> = PriorityQueueNoDuplicates()

fun main() {
	requestQueue.add(
		WikiRequest(
			URL(apiURLBase),
			::addListRequestResultsToQueue,
			100
		)
	)

	val klaxon = Klaxon()

	var counter = 0


	while (true) {
		if(requestQueue.isNotEmpty()) {
			val next: WikiRequest = requestQueue.poll()
			println(next)
			val (URL, callback) = next
			val parsed: JsonObject = klaxon.parseJsonObject(StringReader(URL.readText()))
			callback(parsed, next.callbackArguments)
			counter++
			if (counter % 100 == 0) {
				articlesHolder.save(path = "./ah$counter.ser")
			}
		} else{
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

fun addListRequestResultsToQueue(parsed: JsonObject, passthroughArguments: Map<String, Any>) { //todo name better

	//step 1
	if (canContinue(parsed)) {
		requestQueue.add(
			WikiRequest(
				URL(apiURLBase + continueArguments(parsed)), ::addListRequestResultsToQueue, 100
			)
		)
	}

	//step 2
	requestQueue.addAll(
		createWikiRequestsFromParsedHtml(parsed)
	)


}

/**
 * Callback function for [WikiRequest]s.
 *
 * To be used after a request for a parsed-to-HTML page
 *
 * First gets out wikilink, then extracts linked to article name, then creates relevant articles in articleholder
 *
 * @param [passthroughArguments] "title" -> String
 * @see WikiRequest
 */
fun getFirstLink(response: JsonObject, passthroughArguments: Map<String, Any>) {
	fun getLinkElement(doc: Node): Element {
		doc.childNodes()
			.filter { it is Element && (it.tagName() in listOf("p", "ul", "ol") /* todo expand*/ || it.attr("class") == "redirectMsg") }
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

					linksNotParend.forEach { it1 ->
						var parent = it1.parent()
						//iterate until either we run out of parents or find an illegal parent
						while (parentIsNotIllegal(parent) && parent.hasParent()) {
							parent = parent.parent()
						}
						/*
						and if we stopped iterating because we ran out (not because the one we stopped on is illegal)
						return this link, assuming it meets a few other conditions
							namely, is not a red link, is an internal wikilink
						*/
						if (!it1.attr("href").contains("redlink=1") &&
							parentIsNotIllegal(parent) &&
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
	fun makeArticle(title: String, titleOfArticleLinkedTo: String): Article? {
		//todo all this behavior probably belongs in the ArticleHolder class
		//	or at least should return something instead of modifying as a side effect
		return if (articlesHolder.containsKey(title)) {
			articlesHolder[title]!!.firstLink = articlesHolder[titleOfArticleLinkedTo]
			articlesHolder[title]
		} else {
			//todo make sure old.getArticleBuilder isn't being used anywhere
			articleBuilder.reset()
			articleBuilder.title = title
			articleBuilder.firstLink = articlesHolder[titleOfArticleLinkedTo]
			val art = articleBuilder.build()
			articlesHolder[art.title] = art
			articlesHolder[art.title]!!
		}
	}

	val html = response.obj("parse")!!.obj("text")!!["*"]!!
	val doc: Node = Jsoup.parse(html.toString()).body().getElementsByClass("mw-parser-output")[0]

	val linkElement = getLinkElement(doc)

	if (linkElement.tagName() != "a"){
		throw Exception("Something has gone wrong-- getLinkElement didn't return an 'a' tag") //todo replace with flag for manual review
	}

	if (linkElement.attr("href").isEmpty() || !Regex("""/wiki/.+""").matches(linkElement.attr("href"))){
		throw Exception("Something has gone wrong-- getLinkElement didn't return an internal wikilink") //todo replace with flag for manual review
	}

	makeArticle (
		passthroughArguments["title"] as String,
		if(linkElement.attr("href") != null && linkElement.attr("href").length > 6)
		 linkElement.attr("href")
			.substring(6) //after the /wiki/
			.replace("--__OPEN-PAREN__--", "(")
			.replace("--__CLOSE-PAREN__--", ")")
			.split("#")[0] //ignore anchors
	 else "" /*todo got to be a better way of doing this*/
	)



}