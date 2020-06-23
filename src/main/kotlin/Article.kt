import java.io.*

class Article(
	var parents: HashSet<String>,
	val title: String
) : Serializable{
	var isEndArticle = false
		set(value) {
			field = value
			isLinkedToEndArticle = true
		}
	var firstLink: String? = null
		set(value) {
			articlesHolder[field]?.parents?.remove(title)
			field = value
			articlesHolder[value]?.parents?.add(title)
			isLinkedToEndArticle = isEndArticle || articlesHolder[firstLink]?.isLinkedToEndArticle ?: false
		}
	var isLinkedToEndArticle: Boolean = (articlesHolder[firstLink]?.isLinkedToEndArticle ?: false) || isEndArticle
		//REMEMBER: THIS IS RECURSIVE. THIS IS LIKELY WHERE FUTURE INFINITE RECURSION ERRORS ARE COMING FROM, EVEN THOUGH IT DOESN'T LOOK RECURSIVE.
		set(value) {
			field = value
			parents.forEach {
				if (articlesHolder[it].isLinkedToEndArticle != value)
					articlesHolder[it].isLinkedToEndArticle = value
			}
		}

	init {
		//necessary to call setter code. Initializations do not call setter code, but this is an assignment (even though it looks like initialization.)
		isLinkedToEndArticle = isLinkedToEndArticle
	}


	override fun toString(): String {
		return "$title (links to ${firstLink}) [parents: $parents]"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Article

		if (other.parents != this.parents) return false

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

	var firstLink: String? = null
	var parents: HashSet<String> = HashSet()
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
			out.writeObject(articlesHolder)
			out.close()
			fileOut.close()
			System.out.printf("Serialized data is saved in $path")
		} catch (i: IOException) {
			i.printStackTrace()
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ArticlesHolder

		if(this.toList().sortedBy { it.first } != other.toList().sortedBy { it.first }) {
			println(this.toList().sortedBy { it.first })
			println(other.toList().sortedBy { it.first })
			return false
		}

		return true
	}

	override fun hashCode(): Int {
		return super.hashCode()
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
				println(out)
			} catch (i: IOException) {
				i.printStackTrace()

			} catch (c: ClassNotFoundException) {
				println("ArticleHolder class not found")
				c.printStackTrace()
			}
			return out!!
		}
	}

}