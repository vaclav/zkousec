package layout

open class Answer(val correct: Boolean = false) {

}

class TextualAnswer (correct: Boolean, val text : String) : Answer (correct) {
    override fun toString(): String = text
}

class ImageAnswer (val url : String) : Answer () {

}