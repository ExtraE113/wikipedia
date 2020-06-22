import org.amshove.kluent.*
import org.junit.jupiter.api.Test

class ArticleTests {

	private val articlesHolder = ArticlesHolder(5)
	private val articleBuilder = ArticleBuilder()

	@Test
	fun basicArticleTests() {
		//todo document and expand


		//some super basic tests
		articleBuilder.title = "abc"
		articleBuilder.title shouldBeEqualTo "abc"
		articleBuilder.reset()
		articleBuilder.title shouldBeEqualTo null


		articleBuilder.firstLink = articlesHolder["def"]
		articleBuilder.title = "ghk"
        articleBuilder.isEndArticle = true
		articlesHolder["ghk"] = articleBuilder.build()
        //test that reset is working AND called after .build
		articleBuilder.title shouldBeEqualTo null
        articleBuilder.firstLink shouldBeEqualTo null
        articleBuilder.isEndArticle shouldBeEqualTo false

		val targetArticle = Article(HashSet(), "ghk").apply { isEndArticle = true }
		targetArticle.firstLink = articlesHolder["def"]

		articlesHolder["ghk"] shouldBeEqualTo targetArticle

		//check that trying to build without title should blow up
		invoking {  articleBuilder.build() } shouldThrow AnyException

		//test parent propagation
		articlesHolder["lmn"].firstLink = articlesHolder["pqr"]

		articlesHolder["pqr"].parents shouldContain articlesHolder["lmn"]

		//test linked to end article propagation
		articlesHolder["pqr"].firstLink = articlesHolder["xyz"]
		articlesHolder["xyz"].firstLink = articlesHolder["def"]
		articlesHolder["def"].firstLink = articlesHolder["stu"]


		articlesHolder["stu"].isLinkedToEndArticle = true
		articlesHolder["lmn"].isLinkedToEndArticle `should be equal to` true

		//make sure we don't get infinite recursion with parents
		articlesHolder["stu"].isLinkedToEndArticle = false
		articlesHolder["lmn"].isLinkedToEndArticle `should be equal to` false
		articlesHolder["stu"].firstLink = articlesHolder["lmn"]
		articlesHolder["stu"].isLinkedToEndArticle = true
		articlesHolder["lmn"].isLinkedToEndArticle `should be equal to` true
	}

	@Test
	fun serializationTest() {
		//consider expanding
		articlesHolder.save("./ahTest.ser")
		ArticlesHolder.load("./ahTest.ser") shouldBeEqualTo articlesHolder
	}
}