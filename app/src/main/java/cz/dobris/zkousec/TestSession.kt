package cz.dobris.zkousec

import layout.Answer
import layout.Question
import layout.QuestionPack
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.random.Random

class TestSession(qp:QuestionPack, initializer : SessionInitializer = AllQuestionsInitializer(), answerHandler : AnswerHandler = SimpleAnswerHandler()) {
    private val toProcess = initializer.initialize(qp)
    private val answeredCorrectly = mutableListOf<QuestionStatus>()
    private val answeredIncorrectly = mutableListOf<QuestionStatus>()
    init {    }

    fun correctlyAnsweredQuestions() : List<QuestionStatus> = answeredCorrectly
    fun incorrectlyAnsweredQuestions() : List<QuestionStatus> = answeredIncorrectly
    fun remainingQuestions() = toProcess.size
    fun completedQuestions() = answeredCorrectly.size + answeredIncorrectly.size
    fun totalQuestions() = remainingQuestions() + completedQuestions()
    fun nextQuestion() = if (remainingQuestions()>0) toProcess.first() else throw IllegalStateException("The are no more questions to process.")
    fun evaluateAnswer(answer: Answer) {
        val q = nextQuestion()
        if(!q.question.answers.contains(answer)) {
            throw IllegalArgumentException("Cannot evaluate the answer as it does not belong to the current question.")
        }
        toProcess.remove(q)
        q.given=+1
        if(answer.correct) {
            answeredCorrectly.add(q)
            q.answeredCorrectly=+1
        } else {
            answeredIncorrectly.add(q)
            q.answeredIncorrectly=+1
        }
    }

    class QuestionStatus(val question: Question) {
        var given = 0
        var answeredCorrectly = 0
        var answeredIncorrectly = 0
    }

    interface SessionInitializer {
        fun initialize(qp: QuestionPack) : MutableList<QuestionStatus>
    }

    class AllQuestionsInitializer : SessionInitializer {
        override fun initialize(qp: QuestionPack): MutableList<QuestionStatus> {
            val statuses = mutableListOf<QuestionStatus>()
            val count = qp.questions.size
            for(i in 0..count-1) {
                statuses.add(QuestionStatus(qp.questions[i]))
            }
            return statuses
        }
    }

    class SubsetQuestionsInitializer(val testSize : Int = Int.MAX_VALUE) : SessionInitializer {
        //TODO test for 0 or negative values
        override fun initialize(qp: QuestionPack): MutableList<QuestionStatus> {
            val statuses = mutableListOf<QuestionStatus>()
            val count = if (testSize<qp.questions.size) testSize else qp.questions.size
            val random = Random(System.currentTimeMillis())
            val alreadyIncludedQuestionIndexes = mutableListOf<Int>()
            for(i in 0..count-1) {
                var nextInt = random.nextInt(qp.questions.size)
                while (alreadyIncludedQuestionIndexes.contains(nextInt)) nextInt = random.nextInt(qp.questions.size)
                alreadyIncludedQuestionIndexes.add(nextInt)
                statuses.add(QuestionStatus(qp.questions[nextInt]))
            }
            return statuses
        }
    }

    interface AnswerHandler {
        fun handle(q: QuestionStatus, a: Answer, session: TestSession)
    }

    class SimpleAnswerHandler : AnswerHandler {
        override fun handle(q: QuestionStatus, a: Answer, session: TestSession) {
            session.toProcess.remove(q)
            q.given=+1
            if(a.correct) {
                session.answeredCorrectly.add(q)
                q.answeredCorrectly=+1
            } else {
                session.answeredIncorrectly.add(q)
                q.answeredIncorrectly=+1
            }
        }
    }

    class RetryIncorrectAnswerHandler(val numberOfRetries : Int = Int.MAX_VALUE) : AnswerHandler {
        //TODO test for 0 or negative values
        val random = Random(System.currentTimeMillis())

        override fun handle(q: QuestionStatus, a: Answer, session: TestSession) {
            session.toProcess.remove(q)
            q.given=+1
            if(a.correct) {
                session.answeredCorrectly.add(q)
                q.answeredCorrectly=+1
            } else {
                q.answeredIncorrectly=+1
                if (q.answeredIncorrectly<numberOfRetries) {
                    val nextInt = random.nextInt(session.toProcess.size)
                    session.toProcess.add(nextInt, q)
                } else {
                    session.answeredIncorrectly.add(q)
                }
            }
        }
    }
}