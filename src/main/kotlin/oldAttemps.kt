import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

//import org.apache.commons.lang3.StringUtils
//import org.jsoup.nodes.Element
//import org.jsoup.nodes.Node
//import org.jsoup.nodes.TextNode
//
//fun getFirstLinkOld(doc: Node): Element {
//    var parens = 0
//    //we don't need to check italics because they'd be nested nodes and we don't need to check those
//    doc.childNodes().forEach {
//        if (it is Element && it.tagName() == "p") {
//            it.childNodes().forEach { child ->
//                fun explore(doc: Node): Element? {
//                    if (doc is TextNode) {
//                        parens += StringUtils.countMatches(doc.toString(), "(") - StringUtils.countMatches(
//                            doc.toString(),
//                            ")"
//                        )
//                    } else if (doc is Element && doc.tagName() == "a" && parens == 0 && !doc.attr("href").toString()
//                            .contains("redlink=1") && doc.attr("href").toString().substring(0, 6) == "/wiki/"
//                    ) {
//                        return doc
//                    } else if (doc is Element && doc.tagName() != "i") {
//                        doc.childNodes().forEach { it2 ->
//                            explore(it2)
//                        }
//                    }
//                    return null
//                }
//                val result = explore(child)
//                if(result != null){
//                    return result
//                }
//            }
//        }
//    }
//    System.err.println("No link found")
//    return Element("a")
//}
//
//fun getFirstLink(doc: Node): Element {
//    doc.childNodes().filter { it is Element && (it.tagName() == "p" || it.attr("class") == "redirectMsg")}.forEach{
//        var parens = 0
//        fun explore(item: Node): Element {
//
//            fun processTextNode(textNode: TextNode) {
//                parens += StringUtils.countMatches(textNode.toString(), "(") -
//                        StringUtils.countMatches(textNode.toString(), ")")
//            }
//
//            fun processLink(link: Element): Boolean {
//                assert(link.tagName() == "a")
//                if(link.attr("href").toString().contains("redlink=1") || (doc.attr("href").toString().length < 6 || doc.attr("href").toString().substring(0, 6) != "/wiki/")){
//                    return false
//                } else {
//                    link.childNodes().forEach { linkContents ->
//                        if (linkContents is TextNode){
//                            if (linkContents.toString().contains('(')) // check that there are no parens in the link text
//                                println("failed at ( check")
//                            return false
//                        } else if (linkContents is Element){
//                            fun seekItalics(child: Node):Boolean { //recursively check that there are no italics in the link text
//                                child.childNodes().forEach{linkContentChild ->
//                                    if(linkContentChild is Element && linkContentChild.tagName() == "i"){
//                                        println("failed at italics check")
//                                        return false
//                                    }
//                                    seekItalics(linkContentChild)
//                                }
//                                return  true
//                            }
//                            return seekItalics(linkContents)
//                        }
//                    }
//                    return true //as long as those checks pass, return true.
//                }
//            }
//
//            fun shouldExploreChildren(candidate: Element): Boolean {
//                return candidate.tagName() != "i" && parens == 0
//            }
//
//
//            if(item is Element && item.tagName() == "a"){
//                if (processLink(item))
//                    println(item)
//            } else if(item is TextNode){
//                processTextNode(item)
//
//            } else if (item is Element) {
//                if (shouldExploreChildren(item)) { //explore all child nodes
//                    item.childNodes().forEach { it2 ->
//                        if (it2.childNodes().size == 0)
//                            println(it2)
//                        explore(it2)
//                    }
//                }
//            }
//            return Element("a")
//        }
//        explore(it)
//    }
//    return Element("a")
//}
//


////all top level nodes
//var parens = 0
//var inItalics = false
//fun process(element: Node) {
//    element.childNodes().forEach { it1 ->
//        if(it1 is TextNode){
//            parens += StringUtils.countMatches(it1.toString(), "(") -
//                    StringUtils.countMatches(it1.toString(), ")")
//        } else if (it1 is Element){
//            if(it1.tagName() == "a" && parens==0 && !inItalics){
//                println(it1)
//            } else if(it1.tagName() == "i"){
//                inItalics = true
//            }
//        }
//    }
//
//}
//process(topLevelChildren)
//}