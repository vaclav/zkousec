package layout

class Question (val position : Int, val text : String, val answers : List<Answer>) {
    var answeredCorrectly : Boolean = false
}