import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import khttp.get
import com.beust.klaxon.Klaxon
import java.io.StringReader

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.lang.Error
import java.lang.NullPointerException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//todo use only java.net.url (not khttp)
//  remove khttp from gradle

val WIKI = "simple"

val articleBuilder = ArticleBuilder()
val articlesHolder = ArticlesHolder(50)

fun core() {
	var r = get("http://$WIKI.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=max")
	val klaxon = Klaxon()
	var parsed = klaxon.parseJsonObject(StringReader(r.text))

	var pages = (parsed["query"] as JsonObject).obj("pages")!!
	pages.forEach {
        println(
			it.key +
					makeArticle((pages[it.key] as JsonObject).map["title"] as String).toString()
		)
	}

    while (parsed["continue"] != null) {
        r =
            get("https://$WIKI.wikipedia.org/w/api.php?action=query&format=json&prop=&generator=allpages&gapmaxsize=100&gaplimit=max&continue=${(parsed["continue"] as JsonObject)["continue"]}&gapcontinue=${(parsed["continue"] as JsonObject)["gapcontinue"]}")
        parsed = klaxon.parseJsonObject(StringReader(r.text))
        pages = (parsed["query"] as JsonObject).obj("pages")!!
        pages.forEach {
            println(
                it.key +
                        makeArticle((pages[it.key] as JsonObject).map["title"] as String).toString()
            )
        }
    }


}

fun main() {
    try {
        core()
    }
    catch (e: Throwable) {
        println("debugme")
    }

}

fun makeArticle(title: String):Article? {
    val titleOfArticleLinkedTo:String
    //todo there's got to be a more elegant way to do this
    //  basically this just allows for the possibility that there are articles that are listed by allarticles generator (or whatever its called) that don't acutally exist
    //  no idea why this would happen but it does
    // fixme this is fundamentally broken and needs to be debugged

    try {
        titleOfArticleLinkedTo =
            getLinkFromTitle(title) //todo is there a more concise equally descriptive name?
    } catch (e:Throwable){
        return null
    }

    if(articlesHolder.containsKey(title)){
        articlesHolder[title]?.firstLink = articlesHolder[titleOfArticleLinkedTo]
        return articlesHolder[title ]
    }

    articleBuilder.reset()
    articleBuilder.title = title
    articleBuilder.firstLink = articlesHolder[titleOfArticleLinkedTo]
    val art = articleBuilder.build()
    articlesHolder[art.title] = art
    return articlesHolder[art.title]!!
}


fun getLinkFromTitle(title: String, wiki: String = WIKI): String {
    val link = "https://$wiki.wikipedia.org/w/api.php?action=parse&format=json&page=${URLEncoder.encode(
        title,
        StandardCharsets.UTF_8.toString()
    )}"
    val r = URL(link).readText()
    val klaxon = Klaxon()
    val parsed = klaxon.parseJsonObject(StringReader(r))
    var html:Any? = null
    if(title == "102564")
        println("de")
    try {
        if (parsed.obj("error")?.get("code") == "missingtitle")
            throw Exception("no article with that name")
        html = parsed.obj("parse")!!.obj("text")!!["*"]!!
        html!!
    }catch (e:NullPointerException){
        println("debug me 2")
    }

    var doc: Node = Jsoup.parse(html.toString()).body().getElementsByClass("mw-parser-output")[0]


    return extractWikiLink(getFirstLink(doc))
}

fun extractWikiLink(link: Element): String {
    assert(link is Element && link.tagName() == "a")
    if(link.attr("href") != null && link.attr("href").length > 6)
        return link.attr("href").substring(6).replace("--__OPEN-PAREN__--", "(").replace("--__CLOSE-PAREN__--", ")")
    return ""
}



fun getFirstLink(doc: Node): Element {
    doc.childNodes().filter { it is Element && (it.tagName()  in listOf("p", "ul", "ol") || it.attr("class") == "redirectMsg") }.forEach { topLevelChildren ->
		var stringRepresentation = topLevelChildren.toString()
        val item = Jsoup.parseBodyFragment(stringRepresentation)
        val links = item.select("a[href]")
        for ((index, it) in links.withIndex()) {
            links[index] = it.attr("href", it.attr("href").replace("(", "--__OPEN-PAREN__--").replace(")", "--__CLOSE-PAREN__--"))
        }

        stringRepresentation = item.toString()
		//split by () and get even only-- filters out anything between parens. probably not perfect, but works pretty well
		//for one thing, it ~~could be~~ *is* messed up by () in <a>
		//todo consider fix
        //todo very likely very buggy, improve, test, bug fig
        //optimize
		stringRepresentation = stringRepresentation.replace(Regex("\\(.*?\\)"), "")
//        StringEscapeUtils.unescapeHtml4(stringRepresentation).split("(", ")").filterIndexed { index, _ -> index % 2 ==0 }.forEach{ itabc ->
            val line = Jsoup.parseBodyFragment(stringRepresentation)

			val linksNotParend: Elements = line.select("a[href]")

			if (linksNotParend.size > 0) {

                linksNotParend.forEach { it1 ->
                    var parent = it1.parent()
                    while (parent.tagName() != "i"){
                        if (parent.hasParent()){
                            parent = parent.parent()
                        } else {
                            break
                        }
                    }
                    if(parent.tagName() != "i" && !it1.attr("href").contains("redlink=1")){
                        return it1
                    }
                }
            }

			//heh
			var druggie = false
			do line
				while (druggie)

		}

	//}
    return Element("a")
}

