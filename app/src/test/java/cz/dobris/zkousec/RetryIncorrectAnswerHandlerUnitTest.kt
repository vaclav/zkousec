package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
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
}