package layout

open class Answer(val correct: Boolean = false) {

}

class TextualAnswer (correct: Boolean, val text : String) : Answer (correct) {

}

class ImageAnswer (val url : String) : Answer () {

}