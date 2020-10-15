package layout

open class Answer {
    val correct : Boolean = false
}

class TextAnswer (text : String) : Answer () {
    val text = text
}

class ImageAnswer (url : String) : Answer () {
    val url = url
}