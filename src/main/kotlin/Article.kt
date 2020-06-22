import java.io.*

class Article(
	var parents: HashSet<Article>,
	val title: String
) : Serializable{
	var isEndArticle = false
		set(value) {
			field = value
			isLinkedToEndArticle = true
		}
	var firstLink: Article? = null
		set(value) {
			field?.parents?.remove(this)
			field = value
			value?.parents?.add(this)
			isLinkedToEndArticle = isEndArticle || firstLink?.isLinkedToEndArticle ?: false
		}
	var isLinkedToEndArticle: Boolean = (firstLink?.isLinkedToEndArticle ?: false) || isEndArticle
		//REMEMBER: THIS IS RECURSIVE. THIS IS LIKELY WHERE FUTURE INFINITE RECURSION ERRORS ARE COMING FROM, EVEN THOUGH IT DOESN'T LOOK RECURSIVE.
		set(value) {
			field = value
			parents.forEach {
				if (it.isLinkedToEndArticle != value)
					it.isLinkedToEndArticle = value
			}
		}

	init {
		//necessary to call setter code. Initializations do not call setter code, but this is an assignment (even though it looks like initialization.)
		isLinkedToEndArticle = isLinkedToEndArticle
	}


	override fun toString(): String {
		return "$title (links to ${firstLink?.title})"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Article

		if (parents != other.parents) return false
		if (title != other.title) return false
		if (isEndArticle != other.isEndArticle) return false
		if (firstLink != other.firstLink) return false
		if (isLinkedToEndArticle != other.isLinkedToEndArticle) return false

		return true
	}

	override fun hashCode(): Int {
		return this.toString().hashCode()
	}


}

class ArticleBuilder : Serializable {

	var firstLink: Article? = null
	var parents: HashSet<Article> = HashSet()
	var isEndArticle = false
	var title:String? = null

	init {
		reset() //insurance
	}


	fun reset(): ArticleBuilder {
		firstLink = null
		parents = HashSet()
		isEndArticle = false
		title = null
		return this
	}



	fun build(): Article {
		if(title == null)
			error("You must provide a title for all new articles.")
		val art = Article(parents, title!!)
		art.isLinkedToEndArticle = isEndArticle
		art.isEndArticle = isEndArticle
		art.firstLink = firstLink
		reset()
		return art
	}
}



class ArticlesHolder(initialCapacity: Int) : HashMap<String, Article>(initialCapacity), Serializable{
	private val articleBuilder = ArticleBuilder()
	override fun get(key: String): Article {
		return if (super.containsKey(key))
			super.get(key)!!
		else {
			println("creating new article $key")
			articleBuilder.reset()
			articleBuilder.title = key
			super.put(
				key, articleBuilder.build()
			)
			articleBuilder.reset()
			super.get(key)!!
		}
	}
	fun save(path: String = "./ah.ser") {
		try {
			val fileOut = FileOutputStream(path)
			val out = ObjectOutputStream(fileOut)
			out.writeObject(articlesHolder) //todo is this right?
			out.close()
			fileOut.close()
			System.out.printf("Serialized data is saved in $path")
		} catch (i: IOException) {
			i.printStackTrace()
		}
	}
	companion object {
		fun load(path:String = "./ah.ser"): ArticlesHolder {
			var out: ArticlesHolder? = null
			try {
				val fileIn = FileInputStream(path)
				val `in` = ObjectInputStream(fileIn)
				out = `in`.readObject() as ArticlesHolder
				`in`.close()
				fileIn.close()
			} catch (i: IOException) {
				i.printStackTrace()

			} catch (c: ClassNotFoundException) {
				println("ArticleHolder class not found")
				c.printStackTrace()
			}
			return out!!
		}

		//todo test
		//todo take object as input instead of serialized object
		fun toCSV(inputPath: String = "./ah.ser" , outputPath: String = "./ah/"){
			val ah = load(inputPath)
			val elements = File(outputPath + "elements.csv")
			val connections = File(outputPath + "connections.csv")
			var counter = 0
			ah.forEach {
				fun sanitizeForCSV(string: String?): String? {
					return string?.replace("\n", "newline")?.replace(",", "comma")
				}
				elements.appendText("${sanitizeForCSV(it.value.title)}\n")
				connections.appendText(("${sanitizeForCSV(it.value.title)},${sanitizeForCSV(it.value.firstLink?.title)}\n"))
				counter++
				if (counter % 1000 == 0){
					println((counter.toDouble() / ah.size) * 100)
				}
			}
		}
	}

}