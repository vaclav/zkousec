package cz.dobris.zkousec

import org.junit.Assert

class TestHelper {
    companion object {
        fun assertSizes(session: TestSession, total: Int, remaining: Int, completed: Int, correct: Int, incorrect: Int) {
            Assert.assertEquals(total, session.totalQuestions())
            Assert.assertEquals(remaining, session.remainingQuestions())
            Assert.assertEquals(completed, session.completedQuestions())
            Assert.assertEquals(correct, session.correctlyAnsweredQuestions().size)
            Assert.assertEquals(incorrect, session.incorrectlyAnsweredQuestions().size)
        }
    }
}