import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeIn
import org.junit.jupiter.api.Test

class GetFirstLinkTests{
    @Test
    fun getFirstLinkFromTitleTest() {
        getLinkFromTitle("Fire_pump", wiki = "simple") shouldBeEqualTo "Pump"
        getLinkFromTitle("Lise_Meitner", wiki = "simple") shouldBeEqualTo "Physicist"
        getLinkFromTitle("Tachisme", wiki = "simple") shouldBeEqualTo ""
        getLinkFromTitle("1189", wiki = "simple") shouldBeEqualTo "January_21"
        getLinkFromTitle("Swedish_Lapland", wiki = "simple") shouldBeEqualTo "Provinces_of_Sweden"
        getLinkFromTitle("Black_Panther_Party", wiki = "simple") shouldBeIn  arrayOf("Socialist", "Socialism")
        getLinkFromTitle("Javier_Valentín", wiki = "en") shouldBeEqualTo "Catcher"
        getLinkFromTitle("Frank_Herbert", wiki = "en") shouldBeEqualTo "List_of_science_fiction_authors"
        getLinkFromTitle("Ernest_Cline", wiki = "en") shouldBeEqualTo "Ready_Player_One_(film)"
        getLinkFromTitle("John_Corabi", wiki = "en") shouldBeEqualTo "The_Scream_(band)"
        getLinkFromTitle("&", wiki = "simple") shouldBeIn arrayOf("Ampersand", "Logogram")
        getLinkFromTitle("0s", wiki = "simple") shouldBeEqualTo "Regent"

    }
}