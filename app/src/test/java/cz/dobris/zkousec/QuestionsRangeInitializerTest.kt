package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
import org.junit.Assert
import org.junit.Test

class QuestionsRangeInitializerTest {

    private fun rangeInitializeSesionTest(rangeStart: Int, rangeEnd: Int) {
        val session1 = TestSession(TestHelper.treeElementQP, TestSession.RangeQuestionsInitializer(rangeStart,rangeEnd))
        Assert.assertEquals(rangeEnd-rangeStart+1, session1.remainingQuestions())
        Assert.assertEquals(TestHelper.treeElementQP.questions[rangeStart-1], session1.nextQuestion().question)
    }

    @Test
    fun subsetInitializerTestAll() {
        rangeInitializeSesionTest(1,3)
    }
    @Test
    fun subsetInitializerTestFirst() {
        rangeInitializeSesionTest(1,1)
    }
    @Test
    fun subsetInitializerTestLast() {
        rangeInitializeSesionTest(3,3)
    }
    @Test
    fun subsetInitializerTestFirstTwoLast() {
        rangeInitializeSesionTest(1,2)
    }
    @Test
    fun subsetInitializerTestFail1() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.RangeQuestionsInitializer(-3, 2))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}

    }
    @Test
    fun subsetInitializerTestFail2() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.RangeQuestionsInitializer(0, 2))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}

    }
    @Test
    fun subsetInitializerTestFail3() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.RangeQuestionsInitializer(1, -2))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}

    }
    @Test
    fun subsetInitializerTestFail4() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.RangeQuestionsInitializer(1, 0))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}

    }
    @Test
    fun subsetInitializerTestFail5() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.RangeQuestionsInitializer(2, 1))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}

    }
}