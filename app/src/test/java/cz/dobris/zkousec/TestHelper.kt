package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
import layout.Answer
import layout.Question
import layout.QuestionPack
import org.junit.Assert
import java.lang.Exception
import java.lang.IllegalArgumentException

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
            Assert.assertEquals("Error in total questions", total, session.totalQuestions())
            Assert.assertEquals("Error in remaining questions", remaining, session.remainingQuestions())
            Assert.assertEquals("Error in completed questions", completed, session.completedQuestions())
            Assert.assertEquals("Error in correct questions", correct, session.correctlyAnsweredQuestions().size)
            Assert.assertEquals("Error in incorrect questions", incorrect, session.incorrectlyAnsweredQuestions().size)
        }

        fun findAnswer(q: Question, correct: Boolean) : Answer {
            for (answer in q.answers) {
                if (answer.correct == correct)
                    return answer
            }
            throw IllegalArgumentException ("Question '${q.text}', position: ${q.position} has no ${if (correct) "right" else "wrong"} answers")
        }

        fun evaluateAnswer(session: TestSession, correct: Boolean) {
            session.evaluateAnswer(findAnswer(session.nextQuestion().question, correct))
        }
    }
}