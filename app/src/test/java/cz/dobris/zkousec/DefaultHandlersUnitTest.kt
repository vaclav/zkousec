package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
import layout.Answer
import layout.Question
import layout.QuestionPack
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DefaultHandlersUnitTest {
    val qp = QuestionPack("test", "Test", "1.0", listOf(
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

    @Test
    fun simpleScenario() {
//        assertEquals(4, 2 + 2)
        val session = TestSession("file", qp)
        val nextQuestion1 = session.nextQuestion()
        val nextQuestion2 = session.nextQuestion()
        assert(nextQuestion1 == nextQuestion2)
        session.evaluateAnswer(nextQuestion1.question.answers[0])
        TestHelper.assertSizes(session, 3, 2, 1, 1, 0)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())

        val nextQuestion3 = session.nextQuestion()
        assert(nextQuestion1 != nextQuestion3)
        session.evaluateAnswer(nextQuestion3.question.answers[0])
        TestHelper.assertSizes(session, 3, 1, 2, 1, 1)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())
        assert(nextQuestion3 == session.incorrectlyAnsweredQuestions().first())

        val nextQuestion4 = session.nextQuestion()
        try {
            session.evaluateAnswer(nextQuestion3.question.answers[2])
            assert(false)
        } catch (t: Throwable) { }

        session.evaluateAnswer(nextQuestion4.question.answers[2])
        TestHelper.assertSizes(session, 3, 0, 3, 2, 1)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())
        assert(nextQuestion4 == session.correctlyAnsweredQuestions().last())
        assert(nextQuestion3 == session.incorrectlyAnsweredQuestions().first())

        try {
            session.nextQuestion()
            assert(false)
        } catch (t: Throwable) { }
    }

    @Test
    fun doubleCallTo_evaluateAnswer() {
        val session = TestSession("file", qp)
        val nextQuestion1 = session.nextQuestion()
        session.evaluateAnswer(nextQuestion1.question.answers[0])
        try {
            session.evaluateAnswer(nextQuestion1.question.answers[1])
            assert(false)
        } catch (t: Throwable) {}
        TestHelper.assertSizes(session, 3, 2, 1, 1, 0)
        assert(nextQuestion1 == session.correctlyAnsweredQuestions().first())

    }
}