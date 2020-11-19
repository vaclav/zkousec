package cz.dobris.zkousec.domain

import android.util.Log
import cz.dobris.zkousec.db.SessionEntity
import layout.Answer
import layout.Question
import layout.QuestionPack
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.random.Random

class TestSession(
    qp: QuestionPack,
    initializer: SessionInitializer = AllQuestionsInitializer(),
    val answerHandler: AnswerHandler = SimpleAnswerHandler(),
    private val toProcess: MutableList<QuestionStatus> = initializer.initialize(qp),
    private val answeredCorrectly: MutableList<QuestionStatus> = mutableListOf<QuestionStatus>(),
    private val answeredIncorrectly: MutableList<QuestionStatus> = mutableListOf<QuestionStatus>()
) {
    val id = qp.id

    init { }

    //  ----------------- Business methods

    class QuestionStatus(val question: Question, var given : Int = 0, var answeredCorrectly : Int = 0, var answeredIncorrectly : Int = 0) {}

    fun correctlyAnsweredQuestions(): List<QuestionStatus> = answeredCorrectly

    fun incorrectlyAnsweredQuestions(): List<QuestionStatus> = answeredIncorrectly

    fun remainingQuestions() = toProcess.size

    fun completedQuestions() = answeredCorrectly.size + answeredIncorrectly.size

    fun totalQuestions() = remainingQuestions() + completedQuestions()

    fun nextQuestion() =
        if (remainingQuestions() > 0) toProcess.first() else throw IllegalStateException("The are no more questions to process.")

    fun evaluateAnswer(answer: Answer) {
        val q = nextQuestion()
        if (!q.question.answers.contains(answer)) {
            throw IllegalArgumentException("Cannot evaluate the answer as it does not belong to the current question.")
        }
        answerHandler.handle(q, answer, this)
    }

    //  ----------------- Database persistence code

    fun toSessionEntity(): SessionEntity {
        val toProcessString = toProcess.map { encodeQuestionStatus(it) }.joinToString()
        val answeredCorrectlyString = answeredCorrectly.map { encodeQuestionStatus(it) }.joinToString()
        val answeredIncorrectlyString = answeredIncorrectly.map { encodeQuestionStatus(it) }.joinToString()
        return SessionEntity(id, answerHandler.javaClass.name, toProcessString, answeredCorrectlyString, answeredIncorrectlyString)
    }

    private fun encodeQuestionStatus(it: QuestionStatus) =
        "${it.question.position}:${it.given}:${it.answeredCorrectly}:${it.answeredIncorrectly}"

    companion object {
        fun fromSessionEntity(qp: QuestionPack, entity: SessionEntity): TestSession {
            val ah =
                if (entity.answerHandler == "SimpleAnswerHandler") SimpleAnswerHandler() else RetryIncorrectAnswerHandler()
            val toProcess = parseList(qp, entity.toProcess)
            val answeredCorrectly = parseList(qp, entity.answeredCorrectly)
            val answeredIncorrectly = parseList(qp, entity.answeredIncorrectly)
            return TestSession(qp, AllQuestionsInitializer(), ah, toProcess, answeredCorrectly, answeredIncorrectly)
        }

        private fun parseList(qp: QuestionPack, entries: String?): MutableList<QuestionStatus> {
            Log.d("Zkousec", "Data read: " + entries)
            if (entries != null && entries.trim().length > 0) {
                val l = mutableListOf<QuestionStatus>()
                l.addAll(entries.split(",").map {
                    val data = it.split(":")
                    if (data.size<4) throw IllegalArgumentException("Broken data for " + qp.id + ": " + entries)
                    QuestionStatus(qp.questions[data[0].trim().toInt()], data[1].trim().toInt(), data[2].trim().toInt(), data[3].trim().toInt())
                })
                return l
            } else return mutableListOf()
        }
    }

    //  ----------------- Strategies for session initialization

    interface SessionInitializer {
        fun initialize(qp: QuestionPack): MutableList<QuestionStatus>
    }

    class AllQuestionsInitializer :
        SessionInitializer {
        override fun initialize(qp: QuestionPack): MutableList<QuestionStatus> {
            val statuses = mutableListOf<QuestionStatus>()
            val count = qp.questions.size
            for (i in 0..count - 1) {
                statuses.add(
                    QuestionStatus(
                        qp.questions[i]
                    )
                )
            }
            return statuses
        }
    }

    class SubsetQuestionsInitializer(val testSize: Int = Int.MAX_VALUE) :
        SessionInitializer {
        //TODO test for 0 or negative values
        override fun initialize(qp: QuestionPack): MutableList<QuestionStatus> {
            val statuses = mutableListOf<QuestionStatus>()
            val count = if (testSize < qp.questions.size) testSize else qp.questions.size
            val random = Random(System.currentTimeMillis())
            val alreadyIncludedQuestionIndexes = mutableListOf<Int>()
            for (i in 0..count - 1) {
                var nextInt = random.nextInt(qp.questions.size)
                while (alreadyIncludedQuestionIndexes.contains(nextInt)) nextInt =
                    random.nextInt(qp.questions.size)
                alreadyIncludedQuestionIndexes.add(nextInt)
                statuses.add(
                    QuestionStatus(
                        qp.questions[nextInt]
                    )
                )
            }
            return statuses
        }
    }

    //  ----------------- Strategies for answer handling

    interface AnswerHandler {
        fun handle(q: QuestionStatus, a: Answer, session: TestSession)
    }

    class SimpleAnswerHandler : AnswerHandler {
        override fun handle(q: QuestionStatus, a: Answer, session: TestSession) {
            session.toProcess.remove(q)
            q.given = +1
            if (a.correct) {
                session.answeredCorrectly.add(q)
                q.answeredCorrectly = +1
            } else {
                session.answeredIncorrectly.add(q)
                q.answeredIncorrectly = +1
            }
        }
    }

    class RetryIncorrectAnswerHandler(val numberOfRetries: Int = Int.MAX_VALUE) :
        AnswerHandler {
        //TODO test for 0 or negative values
        val random = Random(System.currentTimeMillis())

        override fun handle(q: QuestionStatus, a: Answer, session: TestSession) {
            session.toProcess.remove(q)
            q.given = +1
            if (a.correct) {
                session.answeredCorrectly.add(q)
                q.answeredCorrectly = +1
            } else {
                q.answeredIncorrectly = +1
                if (q.answeredIncorrectly < numberOfRetries) {
                    val nextInt = random.nextInt(session.toProcess.size)
                    session.toProcess.add(nextInt, q)
                } else {
                    session.answeredIncorrectly.add(q)
                }
            }
        }
    }
}