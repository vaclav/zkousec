package cz.dobris.zkousec

import layout.Answer
import layout.Question
import layout.QuestionPack
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class QuestionPackParser {

    val nameSpace = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun readQuestionPack(parser: XmlPullParser): QuestionPack {
        val questions = mutableListOf<Question>()
        var id = ""
        var description = ""
        var version = ""

        parser.require(XmlPullParser.START_TAG, nameSpace, "testing")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "id" -> id = readValue(parser, "id")
                "description" -> description = readValue(parser, "description")
                "version" -> version = readValue(parser, "version")
                "questions" -> questions.add(readQuestions(parser))
                else -> skip(parser)
            }
        }
        val qp = QuestionPack(id, description, version, questions)
        return qp
    }

    private fun readQuestions(parser: XmlPullParser): Question {
       // TODO("Not yet implemented")
        skip(parser)
        return Question("Q1", emptyList())
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readValue(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, nameSpace, tagName)
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, nameSpace, tagName)
        return title
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}