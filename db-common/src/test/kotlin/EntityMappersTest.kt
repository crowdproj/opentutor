package com.gitlab.sszuev.flashcards.common

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class EntityMappersTest {

    companion object {
        private fun assertSplitWords(expectedSize: Int, givenString: String) {
            val actual1: List<String> = splitIntoWords(givenString)
            Assertions.assertEquals(expectedSize, actual1.size)
            actual1.forEach { assertPhrasePart(it) }
            Assertions.assertEquals(expectedSize, splitIntoWords(givenString).size)
            val actual2: List<String> = splitIntoWords(givenString)
            Assertions.assertEquals(actual1, actual2)
        }

        private fun assertPhrasePart(s: String) {
            Assertions.assertFalse(s.isEmpty())
            Assertions.assertFalse(s.startsWith(" "))
            Assertions.assertFalse(s.endsWith(" "))
            if (!s.contains("(") || !s.contains(")")) {
                Assertions.assertFalse(s.contains(","), "For string '$s'")
            }
        }
    }

    @Test
    fun testSplitIntoWords() {
        assertSplitWords(0, " ")
        assertSplitWords(1, "a.  bb.xxx;yyy")
        assertSplitWords(6, "a,  ew,ewere;errt,&oipuoirwe,ор43ыфю,,,q,,")
        assertSplitWords(10, "mmmmmmmm, uuuuuu, uuu (sss, xzxx, aaa), ddd, sss, q, www,ooo , ppp, sss. in zzzzz")
        assertSplitWords(3, "s s s s (smth l.), d (smth=d., smth=g) x, (&,?)x y")
    }
}