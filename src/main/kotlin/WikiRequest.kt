import com.beust.klaxon.JsonObject
import java.io.Serializable
import java.lang.System.currentTimeMillis
import java.net.URL

//todo document
data class WikiRequest(val url: URL, val callback: (JsonObject, Map<String, Any>) -> Unit) : Serializable,
	Comparable<WikiRequest> {
	var priority: Int = 10

	var callbackArguments = mutableMapOf<String, Any>()
	var created = currentTimeMillis()


	constructor(url: URL, callback: (JsonObject, Map<String, Any>) -> Unit, priority: Int) : this(url, callback) {
		this.priority = priority
	}

	//todo test
	override fun compareTo(other: WikiRequest): Int {
		return if (other.priority != this.priority) {
			other.priority - this.priority
		} else {
			other.created.compareTo(this.created)
		}
	}

	override fun equals(other: Any?): Boolean {
		return other is WikiRequest && other.url == url && other.callback == callback && other.callbackArguments == callbackArguments
	}

	//todo hashcode
}