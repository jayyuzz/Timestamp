package com.example

import com.example.util.HashUtil
import com.example.util.WitaTimeUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testSha256Calculation() {
    val sampleText = "BOS-SMKN3-KOTAMOBAGU-2026"
    val hash = HashUtil.calculateSha256(sampleText.toByteArray(Charsets.UTF_8))
    assertEquals(64, hash.length)
    assertTrue(hash.matches(Regex("^[0-9a-f]{64}$")))
  }

  @Test
  fun testWitaTimeFormatting() {
    val timestamp = 1793600000000L
    val formatted = WitaTimeUtil.formatWita(timestamp)
    assertTrue(formatted.contains("WITA"))

    val digitalTime = WitaTimeUtil.getCurrentWitaTimeOnly()
    assertTrue(digitalTime.contains("WITA"))
    assertTrue(digitalTime.matches(Regex("^\\d{2}:\\d{2}:\\d{2} WITA$")))

    val dms = WitaTimeUtil.toDmsFormat(0.7303, 124.3168)
    assertTrue(dms.contains("N"))
    assertTrue(dms.contains("E"))
  }
}
