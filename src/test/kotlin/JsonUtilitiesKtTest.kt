import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.net.URL

internal class JsonUtilitiesKtTest {

	@BeforeEach
	fun setUp() {
	}

	//fragile
	//fixme
	@Test
	fun createWikiRequestsFromParsedHtml() {

		val target = HashSet<WikiRequest>()

		target.add(WikiRequest(URL("https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=%24NT"), ::createEdge).apply { this.callbackArguments["title"] = "\$NT"; this.priority = 10})
		target.add(WikiRequest(URL("https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=%22Mother%22+Wright"), ::createEdge).apply { this.callbackArguments["title"] = "\"Mother\" Wright"; this.priority = 9})
		target.add(WikiRequest(URL("https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=%22Sensational%22+Sherri"), ::createEdge).apply { this.callbackArguments["title"] = "\"Sensational\" Sherri"; this.priority = 8 })
		target.add(WikiRequest(URL("https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=%22Captain%22+Lou+Albano"), ::createEdge).apply { this.callbackArguments["title"] = "\"Captain\" Lou Albano"; this.priority = 7 })
		target.add(WikiRequest(URL("https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=%21%21%21"), ::createEdge).apply { this.callbackArguments["title"] =  "!!!"; this.priority = 6})


		val klaxon = Klaxon()
		val parsed: JsonObject = klaxon.parseJsonObject(StringReader(URL(apiURLBase).readText()))
		val result = createWikiRequestsFromParsedHtml(parsed).filter { it.callbackArguments["title"] in listOf("\$NT", "\"Mother\" Wright", "\"Sensational\" Sherri", "\"Captain\" Lou Albano", "!!!") }
		result.forEach{
			target.contains(it) shouldBeEqualTo true
		}

		target.forEach{
			result.contains(it) shouldBeEqualTo true
		}
	}
}