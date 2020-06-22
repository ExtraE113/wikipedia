import com.beust.klaxon.JsonObject
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.net.URL

internal class WikiRequestTest {

	private lateinit var primaryConstructor: WikiRequest
	private lateinit var secondaryConstructor: WikiRequest
	private lateinit var differentURL: WikiRequest
	private lateinit var differentFunction: WikiRequest
	private lateinit var differentCallbackArguments: WikiRequest
	private lateinit var createdLate: WikiRequest

	private val funForTesting = fun (_: JsonObject, _: Map<String, Any>)  {}
	private val diffFunForTesting = fun (_: JsonObject, _: Map<String, Any>)  { print("hello")}


	@BeforeEach
	fun setUp() {
		primaryConstructor = WikiRequest(URL("https://www.example.com"), funForTesting)
		secondaryConstructor = WikiRequest(URL("https://www.example.com"), funForTesting, 15)
		differentURL = WikiRequest(URL("https://example.com/different"), funForTesting)
		differentFunction = WikiRequest(URL("https://www.example.com"), diffFunForTesting)
		differentCallbackArguments = primaryConstructor.copy().apply { callbackArguments["Key"] = "Value" }
	}

	@Test
	fun getPriority() {
		primaryConstructor.priority `should be equal to` 10
		secondaryConstructor.priority `should be equal to` 15
	}

	@Test
	fun setPriority() {
		primaryConstructor.priority `should be equal to` 10
		primaryConstructor.priority = 12
		primaryConstructor.priority `should be equal to` 12

		secondaryConstructor.priority `should be equal to` 15
		secondaryConstructor.priority = 13
		secondaryConstructor.priority `should be equal to` 13
	}

	@Test
	fun compareTo() {
		Thread.sleep(1000)
		createdLate = WikiRequest(URL("https://example.com"), funForTesting)


		(secondaryConstructor > primaryConstructor) `should be` false
		(secondaryConstructor < primaryConstructor) `should be` true

		(createdLate > primaryConstructor) `should be` false
		(createdLate < primaryConstructor) `should be` true

		(secondaryConstructor > createdLate) `should be` false
		(secondaryConstructor < createdLate) `should be` true

		//remember... ik it seems backwards
		("a" > "b") `should be` false
	}

	@Test
	fun equals(){ primaryConstructor `should be equal to` primaryConstructor
		primaryConstructor `should be equal to` secondaryConstructor

		primaryConstructor `should not be equal to` differentURL
		primaryConstructor `should not be equal to` differentFunction

		primaryConstructor `should not be equal to` differentCallbackArguments
	}
}