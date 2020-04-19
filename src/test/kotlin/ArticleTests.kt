import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ArticleTests {
	@Test
	fun ArticleTests() {
		//todo document and expand

		val articlesHolder = ArticlesHolder(5)
		val articleBuilder = ArticleBuilder()

		//some super basic tests
		articleBuilder.title = "abc"
		articleBuilder.title shouldBeEqualTo "abc"
		articleBuilder.reset()
		articleBuilder.title shouldBeEqualTo null


		articleBuilder.firstLink = articlesHolder["def"]
		articleBuilder.title = "ghk"
        articleBuilder.isDirectlyLinkedToEndArticle = true
		articlesHolder["ghk"] = articleBuilder.build()
        //test that reset is working AND called after .build
		articleBuilder.title shouldBeEqualTo null
        articleBuilder.firstLink shouldBeEqualTo null
        articleBuilder.isDirectlyLinkedToEndArticle shouldBeEqualTo false

		val targetArticle = Article(ArrayList(), "ghk", true)
		targetArticle.firstLink = articlesHolder["def"]

        //test equality and builder NOTE: checks that parents are in the same order for equality, but this isn't strictly-speaking necessary
		articlesHolder["ghk"] shouldBeEqualTo targetArticle

		//todo test parent code!!
	}
}