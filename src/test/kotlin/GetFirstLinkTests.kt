import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.net.URL

class GetFirstLinkTests {
	val klaxon = Klaxon()

	val targets = listOf(
		mapOf(
			"title" to "Fire_pump",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Fire_pump",
			"fl" to "Pump"
		),
		mapOf(
			"title" to "Lise_Meitner",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Lise_Meitner",
			"fl" to "Physicist"
		),
		mapOf(
			"title" to "Tachisme",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Tachisme",
			"fl" to ""
		),
		mapOf(
			"title" to "1189",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=1189",
			"fl" to "January_21"
		),
		mapOf(
			"title" to "Swedish_Lapland",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Swedish_Lapland",
			"fl" to "Provinces_of_Sweden"
		),
		mapOf(
			"title" to "Black_Panther_Party",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Black_Panther_Party",
			"fl" to "Socialist"
		),
		mapOf(
			"title" to "Javier_Valentín",
			"wiki" to "en"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Javier_Valent%C3%ADn",
			"fl" to "Catcher"
		),
		mapOf(
			"title" to "Frank_Herbert",
			"wiki" to "en"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Frank_Herbert",
			"fl" to "List_of_science_fiction_authors"
		),
		mapOf(
			"title" to "Ernest_Cline",
			"wiki" to "en"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Ernest_Cline",
			"fl" to "Ready_Player_One_(film)"
		),
		mapOf(
			"title" to "John_Corabi",
			"wiki" to "en"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=John_Corabi",
			"fl" to "The_Scream_(band)"
		),
		mapOf(
			"title" to "&",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=%26",
			"fl" to "Ampersand"
		),
		mapOf(
			"title" to "0s",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=0s",
			"fl" to "Regent"
		)
	)

	@BeforeEach
	fun setup() {
		articlesHolder = ArticlesHolder(5)
	}

	//todo document-- which of these tests tests what?
	@Test
	fun createEdge() {
		targets.forEach{
			val wikiRequest = createWikiRequestFromTitle(it.first.getValue("title"), it.first.getValue("wiki"))
			val target = WikiRequest(
				URL(it.second.getValue("url")),
				::createEdge
			). apply { this.callbackArguments["title"] = it.first.getValue("title") }
			wikiRequest shouldBeEqualTo target
			val (URL, callback) = wikiRequest
			val parsed: JsonObject = klaxon.parseJsonObject(StringReader(URL.readText()))
			callback(parsed, wikiRequest.callbackArguments)
			articlesHolder[it.first.getValue("title")].firstLink?.title shouldBeEqualTo it.second.getValue("fl")
		}
	}

//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Lise_Meitner"), wiki = "simple") shouldBeEqualTo "Physicist"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Tachisme"), wiki = "simple") shouldBeEqualTo ""}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("1189"), wiki = "simple") shouldBeEqualTo "January_21"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Swedish_Lapland"), wiki = "simple") shouldBeEqualTo "Provinces_of_Sweden"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Black_Panther_Party"), wiki = "simple") shouldBeIn  arrayOf("Socialist", "Socialism")}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Javier_Valentín"), wiki = "en") shouldBeEqualTo "Catcher"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Frank_Herbert"), wiki = "en") shouldBeEqualTo "List_of_science_fiction_authors"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("Ernest_Cline"), wiki = "en") shouldBeEqualTo "Ready_Player_One_(film)"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("John_Corabi"), wiki = "en") shouldBeEqualTo "The_Scream_(band)"}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("&"), wiki = "simple") shouldBeIn arrayOf("Ampersand", "Logogram")}
//	fun createEdgeN() { val wikiRequest = createWikiRequestFromTitle("0s"), wiki = "simple") shouldBeEqualTo "Regent"
}