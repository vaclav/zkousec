package cz.dobris.zkousec

import cz.dobris.zkousec.domain.TestSession
import org.junit.Assert
import org.junit.Test
import java.lang.Exception

class QuestionsInitializerTest {

    private fun subsetInitializeSesionTest(subsetSize: Int, expectedSize: Int) {
        val session1 = TestSession(TestHelper.treeElementQP, TestSession.SubsetQuestionsInitializer(subsetSize))
        Assert.assertEquals(expectedSize, session1.remainingQuestions())
    }

    @Test
    fun subsetInitializerTest1() {
        subsetInitializeSesionTest(3,3)
    }
    @Test
    fun subsetInitializerTest2() {
        subsetInitializeSesionTest(2,2)
    }
    @Test
    fun subsetInitializerTest3() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.SubsetQuestionsInitializer(10))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}

    }
    @Test
    fun subsetInitializerTest4() {
        try {
            val session1 = TestSession(TestHelper.treeElementQP, TestSession.SubsetQuestionsInitializer(-5))
            Assert.fail()
        }catch (ignore:IllegalArgumentException){}
    }


}