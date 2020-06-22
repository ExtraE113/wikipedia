package old

import apiURLBase
import articlesHolder
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.io.StringReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.LinkedHashSet


//todo test
fun main() {
	println("beginning")
	val queue  = buildInitialQueue()

	//todo listen to the stream of updates
	println("Queue created")
	//Yusuke_Sato
	while (queue.isNotEmpty() || articlesHolder.any { it.value.firstLink == null }) {//todo this is inelegant
		while (queue.isNotEmpty()) {
			makeArticle(queue.poll())
		}

		articlesHolder.forEach {
			if(it.value.firstLink==null && !queue.contains(it.value.title)){
				queue.add(it.value.title)
			}
		}
	}
	articlesHolder.save("./ahTestNewIsSame.ser")
	println("done!")
}

//this builds up the queue of elements at the beginning of the run.
//it uses Queries to the MediaWiki instance of our choice (as specified with old.getApiURLBase)
//and uses the all pages generator
//todo parse json better/cleaner
internal fun buildInitialQueue() :  Queue<String> {
	var r = URL(apiURLBase)
	val klaxon = Klaxon()
	var parsed = klaxon.parseJsonObject(StringReader(r.readText()))
	var queue : Queue<String> = LinkedList()
	//todo this is inelegant, ugly, slow, and at the very least is behavior that should be moved to a superclass
	//todo also, the queue should really be an actor (https://kotlinlang.org/docs/reference/coroutines/shared-mutable-state-and-concurrency.html) when this switches to concurrency.
	extractPageTitles(parsed).forEach {
		if(!queue.contains(it)){
			queue.add(it)
		}
	}
	//checks if there is a continue block in the response, if there is then we can continue because there are more results
	//todo test
	fun canContinue(parsed: JsonObject) = parsed["continue"] != null


	//builds the continue block part of the url that we request
	//todo should build up dynamically depending on whats in the continue block (see api documentation for continue), but this works for now
	//todo test
	fun continueArguments(parsed: JsonObject) =
		"&continue=${(parsed["continue"] as JsonObject)["continue"]}&gapcontinue=${
		URLEncoder.encode(
			(parsed["continue"] as JsonObject)["gapcontinue"].toString(),
			StandardCharsets.UTF_8.toString()
		)
		}" //black magic json processing todo clean up

	while (canContinue(parsed)) {
		r =
			URL(
				apiURLBase + continueArguments(parsed)
			)
		parsed = klaxon.parseJsonObject(StringReader(r.readText()))

		queue.addAll(extractPageTitles(parsed))
	}

	return queue
}


//given a JsonObject (as returned by the api and parsed by klaxon when polling for all pages) return a LinkedHashSet of
// all of the page titles in that request
//todo test
fun extractPageTitles(JsonObjectPageList: JsonObject): LinkedHashSet<String> {
	val pages: JsonObject = (JsonObjectPageList["query"] as JsonObject).obj("pages")!!
	val out = LinkedHashSet<String>()
	pages.forEach { out.add((it.value as JsonObject).map["title"] as String) }
	return out
}
