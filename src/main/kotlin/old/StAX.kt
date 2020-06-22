package old

import ArticleBuilder
import java.io.*
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory


fun main() {
    val xmlInputFactory = XMLInputFactory.newInstance()
    val reader =
        xmlInputFactory.createXMLEventReader(FileInputStream("D:\\wiki-simple\\simplewiki-20170820-pages-meta-current.xml\\simplewiki-20170820-pages-meta-current.xml"))
    // loop through

    var count = 0

    //Get to beginning of pages list
    while (reader.hasNext()) {
        val nextEvent = reader.nextEvent()
        if (nextEvent.isEndElement && nextEvent.asEndElement().name.localPart.equals("siteinfo")) {
            break
        }
    }

    while (reader.hasNext()) {
        val nextEvent = reader.nextEvent()
        if (nextEvent.isStartElement) {
            val startElement = nextEvent.asStartElement()
            if (startElement.name.localPart.equals("page", ignoreCase = true)) {
				readPage(reader)
                count++
                if (count >= 100)
                    break
            }
        }
    }
}

/**
 * Reads one page element.
 * This method should only be called imediately after a page tag is found.
 * TODO claim/lock the XMLEventReader so we don't try to read twice at once and get screwed.
 * TODO change to not void after return type is implemented
 * TODO change to not assume reader.hasNextEvent() true when expected for better error handling and stability
 */
private fun readPage(reader: XMLEventReader) {
    var xmlEvent = reader.nextEvent()
    val articleBuilder: ArticleBuilder = ArticleBuilder().reset() //(reset call technically not required)


    // while we're in the <page> element
    // technically speaking, just until we reach the end of a page element.
    // Assumes we're at the beginning of a page element when the old.readPage method is called.
    while (!(xmlEvent.isEndElement && xmlEvent.asEndElement().name.localPart.equals("page", ignoreCase = true))) {

        if (xmlEvent.isStartElement) {
            val tagType = xmlEvent.asStartElement().name.localPart
            when (tagType) { //case switch statement handling tag types; TODO consider recursion for nested XML. For now doesn't matter.
                "title" -> {
                    xmlEvent = reader.nextEvent()
                    if (xmlEvent.isCharacters) {
                        articleBuilder.title = xmlEvent.asCharacters().toString()
                        println(articleBuilder.title)
                    } else {
                        error("Unexpected XML")
                    }
                }
//                "text" -> {
//                    old.getFirstQualifyingLink(reader)
//                }
            }
        }
        xmlEvent = reader.nextEvent() // move on to the next xml event...
    }
    //TODO return the created graph node based on the extracted and modified xml data
}

//TODO implement
//TODO is using string builder really any faster?
private fun getFirstQualifyingLink(reader: XMLEventReader) {
    var xmlEvent = reader.nextEvent()
    //iterate through page so we can skip to the end of the page
    while (!xmlEvent.isEndElement || !xmlEvent.asEndElement().name.localPart.equals("text", ignoreCase = true)) {
        xmlEvent = reader.nextEvent()
    }

}