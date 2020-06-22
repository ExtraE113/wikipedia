import com.beust.klaxon.JsonObject
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.HashSet

//given a JsonObject (as returned by the api and parsed by klaxon when polling for all pages) return a LinkedHashSet
//of all of the page titles in that request
fun createWikiRequestsFromParsedHtml(JsonObjectPageList: JsonObject): HashSet<WikiRequest> {
	val pages: JsonObject = (JsonObjectPageList["query"] as JsonObject).obj("pages")!!
	val out = HashSet<WikiRequest>()
	pages.forEach {
		out.add(
			WikiRequest(
				URL(
					"https://$WIKI.wikipedia.org/w/api.php?action=parse&format=json&page=${URLEncoder.encode(
						((it.value as JsonObject).map["title"] as String),
						StandardCharsets.UTF_8.toString()
					)}"
				), ::getFirstLink
			).apply { this.callbackArguments["title"] = (it.value as JsonObject).map["title"] as String } //todo test
		)
	}
	return out
}