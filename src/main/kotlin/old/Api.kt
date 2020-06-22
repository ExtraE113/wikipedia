package old

import WIKI
import articleBuilder
import articlesHolder
import com.beust.klaxon.Klaxon
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.io.StringReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


//fun core() {
//	var r = URL(old.getApiURLBase)
//	val klaxon = Klaxon()
//	var parsed = klaxon.parseJsonObject(StringReader(r.readText()))
//
//	processPagesFromJsonObjectPageList(parsed)
//
//	while (old.canContinue(parsed)) {
//		r =
//			URL(
//				old.getApiURLBase + old.continueArguments(parsed)
//			)
//		println("!!!!!!! " + r.query)
//		parsed = klaxon.parseJsonObject(StringReader(r.readText()))
//		processPagesFromJsonObjectPageList(parsed)
//	}
//
//	old.getArticlesHolder.save()
//
//}





//fun processPagesFromJsonObjectPageList(parsed: JsonObject) {
//	//pages is a JsonObject of all of the pages/articles that we can iterate through
//
//	val pages: JsonObject = (parsed["query"] as JsonObject).obj("pages")!!
//	pages.forEach {
//		println(
//			it.key + ": " +
//					old.makeArticle((it.value as JsonObject).map["title"] as String).toString()
//		)
//	}
//}


//makes an article given a title and adds it to old.getArticlesHolder
//also makes the article it links to
//todo should also probably be modified to work with whatever structure we end up using based on this
//	(https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html#actors)
fun makeArticle(title: String): Article? {
	val titleOfArticleLinkedTo: String =
		getLinkFromTitle(title) //todo is there a more concise equally descriptive name?

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

//gets the first link given the title of a page (string)
//todo if the result isn't in either the queue or the articleHolder normalize the title (https://www.mediawiki.org/wiki/API:Query#Example_2:_Title_normalization)
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
	return "" //todo got to be a better way of doing this
}