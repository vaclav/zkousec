package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
import org.junit.Assert
import org.junit.Test

class RetryIncorrectAnswerHandlerUnitTest {

    @Test
    fun infiniteRetries () {
        val session = TestSession(TestHelper.treeElementQP, answerHandler = TestSession.RetryIncorrectAnswerHandler ())
        val nextQuestion1 = session.nextQuestion()
        session.evaluateAnswer(nextQuestion1.question.answers[1]) //incorrect
        TestHelper.assertSizes(session, 3, 3, 0, 0, 0)
        assert(nextQuestion1.answeredIncorrectly == 1)

        for (i in 0..100) {
            val nextQuestion = session.nextQuestion()
            session.evaluateAnswer(TestHelper.findAnswer(nextQuestion.question, false))
            TestHelper.assertSizes(session, 3, 3, 0, 0, 0)
        }

        val nextQuestion3 = session.nextQuestion()
        session.evaluateAnswer (TestHelper.findAnswer(nextQuestion3.question, true))
        TestHelper.assertSizes(session, 3, 2, 1, 1, 0)
        assert(nextQuestion3.answeredCorrectly == 1)

        for (i in 0..100) {
            val nextQuestion = session.nextQuestion()
            session.evaluateAnswer(TestHelper.findAnswer(nextQuestion.question, false))
            TestHelper.assertSizes(session, 3, 2, 1, 1, 0)
            assert(nextQuestion != nextQuestion3)
        }

        val nextQuestion4 = session.nextQuestion()
        session.evaluateAnswer (TestHelper.findAnswer(nextQuestion4.question, true))
        TestHelper.assertSizes(session, 3, 1, 2, 2, 0)

        val nextQuestion5 = session.nextQuestion()
        session.evaluateAnswer (TestHelper.findAnswer(nextQuestion5.question, true))
        TestHelper.assertSizes(session, 3, 0, 3, 3, 0)

        try {
            session.nextQuestion()
            assert(false)
        } catch (t: Throwable) {}
    }

    @Test
    fun noRetries () {
        val session = TestSession (TestHelper.treeElementQP, answerHandler = TestSession.RetryIncorrectAnswerHandler (0))

        val nextQuestion1 = session.nextQuestion()
        session.evaluateAnswer(nextQuestion1.question.answers[0])
        TestHelper.assertSizes(session, 3, 2, 1, 1, 0)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())

        val nextQuestion2 = session.nextQuestion()
        assert(nextQuestion1 != nextQuestion2)
        session.evaluateAnswer(nextQuestion2.question.answers[0])
        TestHelper.assertSizes(session, 3, 1, 2, 1, 1)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())
        assert(nextQuestion2 == session.incorrectlyAnsweredQuestions().first())

        val nextQuestion3 = session.nextQuestion()
        session.evaluateAnswer(nextQuestion3.question.answers[2])
        TestHelper.assertSizes(session, 3, 0, 3, 2, 1)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())
        assert(nextQuestion3 == session.correctlyAnsweredQuestions().last())
        assert(nextQuestion2 == session.incorrectlyAnsweredQuestions().first())

        try {
            session.nextQuestion()
            assert(false)
        } catch (t: Throwable) { }
    }

    @Test
    fun someRetries () {
        val session = TestSession (TestHelper.treeElementQP, answerHandler = TestSession.RetryIncorrectAnswerHandler (4))
        val nextQuestion1 = session.nextQuestion()
        TestHelper.evaluateAnswer(session, false)

        for (j in 1..3) {
            for (i in 0..100) {
                val nextQuestion = session.nextQuestion()
                if (nextQuestion == nextQuestion1) {
                    TestHelper.evaluateAnswer(session, false)
                    break
                }
                TestHelper.evaluateAnswer(session, true)
                if (i == 100) assert(false)
            }
        }
        Assert.assertEquals(4, nextQuestion1.given)
        while (session.remainingQuestions() > 0) {
            val nextQuestion = session.nextQuestion()
            TestHelper.evaluateAnswer(session, true)
        }
        TestHelper.assertSizes(session, 3, 0, 3, 2, 1)
    }
}