import com.beust.klaxon.JsonObject
import khttp.get
import com.beust.klaxon.Klaxon
import java.io.StringReader

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val WIKI = "simple"

fun main() {
	val r = get("http://$WIKI.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=max")
	val klaxon = Klaxon()
	val parsed = klaxon.parseJsonObject(StringReader(r.text))


	val pages = (parsed["query"] as JsonObject).obj("pages")!!
	pages.forEach {
		println(
			it.key +
					getLinkFromTitle((pages[it.key] as JsonObject).map["title"] as String).toString()
		)
	}

    //println(getLinkFromTitle("Tachisme"))

}

fun getLinkFromTitle(title: String, wiki: String = WIKI): String {
    val link = "http://$wiki.wikipedia.org/w/api.php?action=parse&format=json&page=${URLEncoder.encode(
        title,
        StandardCharsets.UTF_8.toString()
    )}"
    val r = get(link)
    val klaxon = Klaxon()
    val parsed = klaxon.parseJsonObject(StringReader(r.text))
    val html = parsed.obj("parse")!!.obj("text")!!["*"]!!

    var doc: Node = Jsoup.parse(html.toString()).body().getElementsByClass("mw-parser-output")[0]


    return extractWikiLink(getFirstLink(doc))
}

fun extractWikiLink(link: Element): String {
    assert(link is Element && link.tagName() == "a")
    return link.attr("href").substring(6)
}



fun getFirstLink(doc: Node): Element {
    doc.childNodes().filter { it is Element && (it.tagName()  in listOf("p", "ul", "ol") || it.attr("class") == "redirectMsg") }.forEach { topLevelChildren ->
		val stringRepresentation = topLevelChildren.toString()

		//split by () and get even only-- filters out anything between parens. probably not perfect, but works pretty well
		//for one thing, it could be messed up by () in <a>
		//todo consider fix
        //todo very likely very buggy, improve, test, bug fig
        //todo inefficient
		stringRepresentation.split("(", ")").filterIndexed { index, _ -> index % 2 ==0 }.forEach{
			val line = Jsoup.parseBodyFragment(it)

			val links: Elements = line.select("a[href]")

			if (links.size > 0) {

                links.forEach { it1 ->
                    var parent = it1.parent()
                    while (parent.tagName() != "i"){
                        if (parent.hasParent()){
                            parent = parent.parent()
                        } else {
                            break
                        }
                    }
                    if(parent.tagName() != "i"){
                        return it1
                    }
                }
            }

			//heh
			var druggie = false
			do line
				while (druggie)

		}

	}
    return Element("a")
}

