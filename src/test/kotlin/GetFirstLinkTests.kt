import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.net.URL

class GetFirstLinkTests {
	private val klaxon = Klaxon()

	//todo document-- which of these tests tests what?
	private val targets = listOf(
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
		),
		//tests for italic or partially italic children
		mapOf(
			"title" to "Crash_Bandicoot",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=Crash_Bandicoot",
			"fl" to "Crash_Bandicoot_(character)"
		),
		//we shouldn't need this but just in case
		//tests for artices that don't exist
		mapOf(
			"title" to "afieapnfeaponfeapnfei",
			"wiki" to "simple"
		) to mapOf(
			"url" to "https://simple.wikipedia.org/w/api.php?action=parse&format=json&page=afieapnfeaponfeapnfei",
			"fl" to "--__Not-An-Article__--" //todo fix this kind of thing (or at least log collisions)
		)
	)

	@BeforeEach
	fun setup() {
		articlesHolder = ArticlesHolder(5)
	}


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
			articlesHolder[it.first.getValue("title")].firstLink shouldBeEqualTo it.second.getValue("fl")
		}
	}
}