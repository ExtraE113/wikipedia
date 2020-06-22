import com.beust.klaxon.JsonObject
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.HashSet

//given a JsonObject (as returned by the api and parsed by klaxon when polling for all pages) return a LinkedHashSet
//of all of the page titles in that request
fun createWikiRequestsFromParsedJsonObjectPageList(JsonObjectPageList: JsonObject): HashSet<WikiRequest> {
	val pages: JsonObject = (JsonObjectPageList["query"] as JsonObject).obj("pages")!!
	val out = HashSet<WikiRequest>()
	pages.forEach {
		out.add(
			createWikiRequestFromTitle(getTitleFromJson(it))
		)
	}
	return out
}

fun createWikiRequestFromTitle(title: String, wiki: String = WIKI):WikiRequest {
	return WikiRequest(
		URL(
			"https://$wiki.wikipedia.org/w/api.php?action=parse&format=json&page=${URLEncoder.encode(
				title,
				StandardCharsets.UTF_8.toString()
			)}"
		), ::createEdge
	).apply { this.callbackArguments["title"] = title }
}

private fun getTitleFromJson(it: Map.Entry<String, Any?>) =
	((it.value as JsonObject).map["title"] as String)