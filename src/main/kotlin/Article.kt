class Article(
    var parents: ArrayList<Article>,
    val title: String,
    isDirectlyLinkedToEndArticle: Boolean = false
) {
    var firstLink: Article? = null
    set(value) {
        field?.parents?.remove(this)
        field = value
        value?.parents?.add(this)
    }
    var isLinkedToEndArticle: Boolean = isDirectlyLinkedToEndArticle || (firstLink?.isLinkedToEndArticle ?: false)
        //REMEMBER: THIS IS RECURSIVE. THIS IS LIKELY WHERE FUTURE INFINITE RECURSION ERRORS ARE COMING FROM, EVEN THOUGH IT DOESN'T LOOK RECURSIVE.
        set(value) {
            field = value
            parents.forEach {
                //if there's a change propagate, otherwise move on. should prevent infinite recursion and give slight performance boost
                if (it.isLinkedToEndArticle != isLinkedToEndArticle)
                    it.isLinkedToEndArticle = value
            }
        }

    init {
        //necessary to call setter code. Initializations do not call setter code, but this is an assignment (even though it looks like initialization.
        isLinkedToEndArticle = isLinkedToEndArticle
    }
}

class ArticleBuilder {

    var firstLink: Article? = null
    var parents: ArrayList<Article> = ArrayList()
    var isDirectlyLinkedToEndArticle = false
    var title:String? = null

    init {
        reset() //insurance
    }


    fun reset(): ArticleBuilder {
        firstLink = null
        parents = ArrayList()
        isDirectlyLinkedToEndArticle = false
        title = null
        return this
    }



    fun build(): Article {
        if(title == null)
            error("You must provide a title for all new articles.")
        val art = Article(parents, title!!, isDirectlyLinkedToEndArticle)
        art.firstLink = firstLink
        reset()
        return art
    }
}



class ArticlesHolder(initialCapacity: Int) : HashMap<String, Article>(initialCapacity) {
    private val articleBuilder = ArticleBuilder()
    override fun get(key: String): Article? {
        return if (super.containsKey(key))
            super.get(key)
        else {
            println("creating new article $key")
            articleBuilder.reset()
            articleBuilder.title = key
            super.put(
                key, articleBuilder.build()
            )
            articleBuilder.reset()
            super.get(key)
        }
    }
}