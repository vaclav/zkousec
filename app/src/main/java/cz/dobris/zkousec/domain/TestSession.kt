package cz.dobris.zkousec.domain

import android.util.Log
import cz.dobris.zkousec.db.SessionEntity
import layout.Answer
import layout.Question
import layout.QuestionPack
import java.time.*
import java.util.*
import kotlin.random.Random

class TestSession(
    val qp: QuestionPack,
    initializer: SessionInitializer = AllQuestionsInitializer(),
    val answerHandler: AnswerHandler = SimpleAnswerHandler(),
    val learnMode: Boolean = true,
    val firstQuestionIndex: Int = 1,
    private val toProcess: MutableList<QuestionStatus> = initializer.initialize(qp),
    private val answeredCorrectly: MutableList<QuestionStatus> = mutableListOf<QuestionStatus>(),
    private val answeredIncorrectly: MutableList<QuestionStatus> = mutableListOf<QuestionStatus>()
) {
    val id = qp.fileName
    var lastUsed: LocalDateTime = LocalDateTime.now()

    init {
    }

    //  ----------------- Business methods

    class QuestionStatus(val question: Question, var given: Int = 0, var answeredCorrectly: Int = 0, var answeredIncorrectly: Int = 0) {}

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
        Log.d("Zkousec", "Is learn mode (write):" + learnMode)
        val toProcessString = toProcess.map { encodeQuestionStatus(it) }.joinToString()
        val answeredCorrectlyString = answeredCorrectly.map { encodeQuestionStatus(it) }.joinToString()
        val answeredIncorrectlyString = answeredIncorrectly.map { encodeQuestionStatus(it) }.joinToString()

        lastUsed = LocalDateTime.now()
        val zdt: ZonedDateTime = ZonedDateTime.of(lastUsed, ZoneId.systemDefault())
        val date: Long = zdt.toInstant().toEpochMilli()
        return SessionEntity(id,
            answerHandler.javaClass.name,
            toProcessString,
            answeredCorrectlyString,
            answeredIncorrectlyString,
            learnMode,
            date,
            firstQuestionIndex)
    }

    private fun encodeQuestionStatus(it: QuestionStatus) =
        "${it.question.position}:${it.given}:${it.answeredCorrectly}:${it.answeredIncorrectly}"

    companion object {
        fun fromSessionEntity(qp: QuestionPack, entity: SessionEntity): TestSession {
            Log.d("Zkousec", "Is learn mode (read):" + entity.learnMode)
            val isLearnMode = if (entity.learnMode == null) false else entity.learnMode
            val ah =
                if (entity.answerHandler != null && entity.answerHandler.contains("SimpleAnswerHandler")) SimpleAnswerHandler()
                else RetryIncorrectAnswerHandler()
            val firstIndex = if (entity.firstQuestionIndex!=null) entity.firstQuestionIndex else 1
            val toProcess = parseList(qp, entity.toProcess)
            val answeredCorrectly = parseList(qp, entity.answeredCorrectly)
            val answeredIncorrectly = parseList(qp, entity.answeredIncorrectly)
            val testSession = TestSession(qp, AllQuestionsInitializer(), ah, isLearnMode, firstIndex, toProcess, answeredCorrectly, answeredIncorrectly)
            val lastUsed = entity.lastUsed
            testSession.lastUsed = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastUsed!!), TimeZone.getDefault().toZoneId());
            return testSession
        }

        private fun parseList(qp: QuestionPack, entries: String?): MutableList<QuestionStatus> {
            if (entries != null && entries.trim().length > 0) {
                val l = mutableListOf<QuestionStatus>()
                l.addAll(entries.split(",").map {
                    val data = it.split(":")
                    if (data.size < 4) throw IllegalArgumentException("Broken data for " + qp.id + ": " + entries)
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
        override fun initialize(qp: QuestionPack): MutableList<QuestionStatus> {
            val statuses = mutableListOf<QuestionStatus>()
            if (testSize <= 0) throw IllegalArgumentException("TestSize must be > 1")
            val count = if (testSize <= qp.questions.size) testSize else throw IllegalArgumentException("QuestionPack is smaller than testSize")
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

    class RangeQuestionsInitializer(val rangeStart: Int = 1, val rangeEnd: Int = 1) :
        SessionInitializer {
        override fun initialize(qp: QuestionPack): MutableList<QuestionStatus> {
            val statuses = mutableListOf<QuestionStatus>()
            if (rangeStart < 1) throw IllegalArgumentException("Range start must be > 0")
            if (rangeEnd < 1) throw IllegalArgumentException("Range end must be > 0")
            if (rangeEnd < rangeStart) throw IllegalArgumentException("Range end must be greater than range start")
            if (rangeStart > qp.questions.size) throw IllegalArgumentException("QuestionPack is smaller than range start")
            if (rangeEnd > qp.questions.size) throw IllegalArgumentException("QuestionPack is smaller than range end")

            val random = Random(System.currentTimeMillis())
            val alreadyIncludedQuestionIndexes = mutableListOf<Int>()
            for (i in (rangeStart - 1)..rangeEnd - 1) {
                statuses.add(QuestionStatus(qp.questions[i]))
            }
            return statuses
        }
    }

    //  ----------------- Strategies for answer handling

    interface AnswerHandler {
        fun handle(q: TestSession.QuestionStatus, a: Answer, session: TestSession)
    }

    class SimpleAnswerHandler : AnswerHandler {
        override fun handle(q: TestSession.QuestionStatus, a: Answer, session: TestSession) {
            session.toProcess.remove(q)
            q.given += 1
            if (a.correct) {
                session.answeredCorrectly.add(q)
                q.answeredCorrectly += 1
            } else {
                session.answeredIncorrectly.add(q)
                q.answeredIncorrectly += 1
            }
        }
    }

    class RetryIncorrectAnswerHandler(val numberOfRetries: Int = Int.MAX_VALUE) :
        AnswerHandler {
        val random = Random(System.currentTimeMillis())

        override fun handle(q: TestSession.QuestionStatus, a: Answer, session: TestSession) {
            session.toProcess.remove(q)
            q.given += 1
            if (a.correct) {
                session.answeredCorrectly.add(q)
                q.answeredCorrectly += 1
            } else {
                q.answeredIncorrectly += 1
                if (q.answeredIncorrectly < numberOfRetries) {
                    val nextInt = if (session.toProcess.size > 0) random.nextInt(session.toProcess.size) else 0
                    session.toProcess.add(nextInt, q)
                } else {
                    session.answeredIncorrectly.add(q)
                }
            }
        }
    }
}