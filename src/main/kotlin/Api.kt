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


//<editor-fold desc="Set up constants">
val WIKI = "simple"

val articleBuilder = ArticleBuilder()
val articlesHolder = ArticlesHolder(500)

val debugRunShort = false
val debugURLBase =
	"https://simple.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=25&gapto=.tj"
val apiURLBase =
	if (!debugRunShort) "https://$WIKI.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=max" else debugURLBase
//</editor-fold>

fun main() {
	try {
		core()
	} catch (e: Throwable) {
		e.printStackTrace()
	}

}

//todo parse json better/cleaner

fun core() {
	var r = URL(apiURLBase)
	val klaxon = Klaxon()
	var parsed = klaxon.parseJsonObject(StringReader(r.readText()))

	processPagesFromJsonObjectPageList(parsed)

	while (canContinue(parsed)) {
		r =
			URL("$apiURLBase&continue=${(parsed["continue"] as JsonObject)["continue"]}&gapcontinue=${(parsed["continue"] as JsonObject)["gapcontinue"]}") //black magic json processing todo clean up
		parsed = klaxon.parseJsonObject(StringReader(r.readText()))
		processPagesFromJsonObjectPageList(parsed)
	}

	articlesHolder.save()

}

//checks if there is a continue block in the response, if there is then we can continue because there are more results
private fun canContinue(parsed: JsonObject) = parsed["continue"] != null

fun processPagesFromJsonObjectPageList(parsed: JsonObject) {
	//pages is a JsonObject of all of the pages/articles that we can iterate through
	val pages: JsonObject = (parsed["query"] as JsonObject).obj("pages")!!
	pages.forEach {
		println(
			it.key + " " +
					makeArticle((it.value as JsonObject).map["title"] as String).toString()
		)
	}
}


fun makeArticle(title: String): Article? {
	val titleOfArticleLinkedTo: String =
		getLinkFromTitle(title) //todo is there a more concise equally descriptive name?

	return if (articlesHolder.containsKey(title)) {
		articlesHolder[title]!!.firstLink = articlesHolder[titleOfArticleLinkedTo]
		articlesHolder[title]
	} else {
		//todo make sure articleBuilder isn't being used anywhere
		articleBuilder.reset()
		articleBuilder.title = title
		articleBuilder.firstLink = articlesHolder[titleOfArticleLinkedTo]
		val art = articleBuilder.build()
		articlesHolder[art.title] = art
		articlesHolder[art.title]!!
	}
}


fun getLinkFromTitle(title: String, wiki: String = WIKI): String {
	val link = "https://$wiki.wikipedia.org/w/api.php?action=parse&format=json&page=${URLEncoder.encode(
		title,
		StandardCharsets.UTF_8.toString()
	)}"
	val klaxon = Klaxon()
	val parsed = klaxon.parseJsonObject(StringReader(URL(link).readText()))
	val html = parsed.obj("parse")!!.obj("text")!!["*"]!!

	val doc: Node = Jsoup.parse(html.toString()).body().getElementsByClass("mw-parser-output")[0]


	return extractWikiLink(getFirstLink(doc))
}


fun getFirstLink(doc: Node): Element {
	doc.childNodes()
		.filter { it is Element && (it.tagName() in listOf("p", "ul", "ol") || it.attr("class") == "redirectMsg") }
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
						Regex("""/wiki/.+""").matches(it1.attr("href"))
					) {
						return it1
					}
				}
			}

			//<editor-fold desc="heh">
			var druggie = false
			do line
			while (druggie)
			//</editor-fold>

		}
	return Element("a")
}

fun parentIsNotIllegal(parent: Element) =
	parent.tagName() != "i" /*parent is not italics*/ && "reference" !in parent.classNames() /*parent is not a reference class*/

fun extractWikiLink(link: Element): String {
	assert(link.tagName() == "a")

	//makes sure link is a internal wikilink
	assert(link.attr("href").isEmpty() || Regex("""/wiki/.+""").matches(link.attr("href")))

	if (link.attr("href") != null && link.attr("href").length > 6)
		return link.attr("href")
			.substring(6) //after the /wiki/
			.replace("--__OPEN-PAREN__--", "(")
			.replace("--__CLOSE-PAREN__--", ")")
			.split("#")[0] //ignore anchors
	return ""
}