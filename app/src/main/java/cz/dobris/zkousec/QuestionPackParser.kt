package cz.dobris.zkousec

import android.util.Log
import layout.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.StringBuilder

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
                "questions" -> questions.addAll(readQuestions(parser))
                else -> skip(parser)
            }
        }
        val qp = QuestionPack(id, description, version, questions)
        return qp
    }

    private fun readQuestions(parser: XmlPullParser): List<Question> {
        parser.require(XmlPullParser.START_TAG, nameSpace, "questions")
        val questions = mutableListOf<Question>()
        parser.nextTag()
        while(parser.name=="question") {
            questions.add(readQuestion(parser))
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, nameSpace, "questions")
        return questions
    }

    private fun readQuestion(parser: XmlPullParser): Question {
        parser.require(XmlPullParser.START_TAG, nameSpace, "question")
        val s = StringBuilder()
        parser.nextTag()
        while(parser.name=="part") {
            parser.require(XmlPullParser.START_TAG, nameSpace, "part")
            parser.nextTag()
            if (parser.name == "text") {
                s.append(readValue(parser, "text"))
                s.append(" ")
            } else {
                //TODO handle img parts
                skip(parser)
            }
            parser.nextTag()
            parser.require(XmlPullParser.END_TAG, nameSpace, "part")
            parser.nextTag()
        }

        val answers = mutableListOf<Answer>()
        parser.require(XmlPullParser.START_TAG, nameSpace, "answers")
        parser.nextTag()
        while(parser.name=="a") {
            parser.require(XmlPullParser.START_TAG, nameSpace, "a")
            val correct = parser.attributeCount > 0 && parser.getAttributeValue(0) == "true"
            answers.add(TextualAnswer(correct, readText(parser)))
            parser.require(XmlPullParser.END_TAG, nameSpace, "a")
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, nameSpace, "answers")
        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, nameSpace, "question")
        return Question(s.toString(), answers)
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