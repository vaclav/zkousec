package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
import layout.Answer
import layout.Question
import layout.QuestionPack
import org.junit.Assert

class TestHelper {
    companion object {
        val treeElementQP = QuestionPack("test", "Test", "1.0", listOf(
            Question(0, "what?", listOf(
                Answer(true),
                Answer(false),
                Answer(false)
            )),
            Question(1,"who?", listOf(
                Answer(false),
                Answer(true),
                Answer(false)
            )),
            Question(2,"when?", listOf(
                Answer(false),
                Answer(false),
                Answer(true)
            ))
        ))


        fun assertSizes(session: TestSession, total: Int, remaining: Int, completed: Int, correct: Int, incorrect: Int) {
            Assert.assertEquals(total, session.totalQuestions())
            Assert.assertEquals(remaining, session.remainingQuestions())
            Assert.assertEquals(completed, session.completedQuestions())
            Assert.assertEquals(correct, session.correctlyAnsweredQuestions().size)
            Assert.assertEquals(incorrect, session.incorrectlyAnsweredQuestions().size)
        }
    }
}