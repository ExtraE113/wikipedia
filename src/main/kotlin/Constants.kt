const val WIKI = "simple"

//this probably doesn't belong here. todo move
var articlesHolder = ArticlesHolder(150_000)

const val debugRunShort = false
val debugURLBase =
	"https://$WIKI.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=25&gapto=.tj"
val apiURLBase =
	if (!debugRunShort) "https://$WIKI.wikipedia.org/w/api.php?action=query&format=json&generator=allpages&gaplimit=max" else debugURLBase
