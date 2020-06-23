import org.amshove.kluent.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ArticleTests {

	private var articleBuilder = ArticleBuilder()

	@BeforeEach
	fun reset() {
		articlesHolder = ArticlesHolder(5)
		articleBuilder = ArticleBuilder()
	}

	@Test
	fun basicArticleTests() {
		//todo document and expand


		//some super basic tests
		articleBuilder.title = "abc"
		articleBuilder.title shouldBeEqualTo "abc"
		articleBuilder.reset()
		articleBuilder.title shouldBeEqualTo null


		articleBuilder.firstLink = "def"
		articleBuilder.title = "ghk"
		articleBuilder.isEndArticle = true
		articlesHolder["ghk"] = articleBuilder.build()
		//test that reset is working AND called after .build
		articleBuilder.title shouldBeEqualTo null
		articleBuilder.firstLink shouldBeEqualTo null
		articleBuilder.isEndArticle shouldBeEqualTo false


		articlesHolder["def"].firstLink = "ghk"
		articlesHolder["ghk"] shouldBeEqualTo articlesHolder["ghk"]


		//check that trying to build without title should blow up
		invoking { articleBuilder.build() } shouldThrow AnyException

		//test parent propagation
		articlesHolder["lmn"].firstLink = "pqr"

		articlesHolder["pqr"].parents shouldContain "lmn"

		//test linked to end article propagation
		articlesHolder["pqr"].firstLink = "xyz"
		articlesHolder["xyz"].firstLink = "def"
		articlesHolder["def"].firstLink = "stu"


		articlesHolder["stu"].isLinkedToEndArticle = true
		articlesHolder["lmn"].isLinkedToEndArticle `should be equal to` true

		//make sure we don't get infinite recursion with parents
		articlesHolder["stu"].isLinkedToEndArticle = false
		articlesHolder["lmn"].isLinkedToEndArticle `should be equal to` false
		articlesHolder["stu"].firstLink = "lmn"
		articlesHolder["stu"].isLinkedToEndArticle = true
		articlesHolder["lmn"].isLinkedToEndArticle `should be equal to` true
	}

	@Test
	fun serializationTest() {
		//consider expanding
		//<editor-fold desc="Set up the articlesHolder">
		//some super basic tests
		articlesHolder["lmn"].firstLink = "pqr"
		articlesHolder["pqr"].firstLink = "xyz"
		articlesHolder["xyz"].firstLink = "def"
		articlesHolder["def"].firstLink = "stu"
		articlesHolder["stu"].isLinkedToEndArticle = true
		articlesHolder["stu"].isLinkedToEndArticle = false
		articlesHolder["stu"].firstLink = "lmn"
		articlesHolder["stu"].isLinkedToEndArticle = true
		//</editor-fold>

		articlesHolder.save("./ahTest.ser")
		val a = articlesHolder
		val b = ArticlesHolder.load("./ahTest.ser")
		val c = a==b
		c shouldBeEqualTo true
		//a shouldBeEqualTo b this fails. that's dumb
	}
}