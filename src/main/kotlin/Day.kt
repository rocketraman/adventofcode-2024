@file:Suppress("FunctionName")

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.*
import kotlin.time.measureTimedValue

typealias Lines = List<String>
typealias Example = Pair<String, Any>

abstract class Day {
  interface Examples {
    val EMPTY: List<Example?>
      get() = listOf(null)

    val part1Examples: List<Example?>
    val part2Examples: List<Example?>
  }

  private val year = Calendar.getInstance().get(Calendar.YEAR)
  private val day by lazy {
    this::class.java.simpleName.removePrefix("Day")
  }

  abstract fun part1(input: Lines): Any
  abstract fun part2(input: Lines): Any

  private fun solve(inputFileName: String, solution: (Lines) -> Any) {
    val lines = this::class.java.getResource(inputFileName)?.toURI()?.let(::File)?.readLines()
      ?: error("$inputFileName not found")
    val (answer, duration) = measureTimedValue { solution(lines) }
    val time = duration.toComponents { seconds, nanoseconds -> "${seconds}s ${nanoseconds}ns" }
    pushToClipboard(answer.toString())
    println(
      """
      Solution took ${duration.inWholeNanoseconds}ns or $time.
        Input: $inputFileName
        Answer: $answer
        Answer copied to clipboard. 
        Don't forget to submit it at https://adventofcode.com/$year/day/$day
     """.trimIndent()
    )
  }

  private fun pushToClipboard(data: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(data), null)
  }

  @ParameterizedTest
  @FieldSource("part1Examples")
  fun `part1 example`(example: Example?) {
    if (example == null) return
    val input = example.first.trimIndent()
    val expected = example.second
    val actual = part1(input.lines())
    assert(actual == expected)
  }

  @ParameterizedTest
  @FieldSource("part2Examples")
  fun `part2 example`(example: Example?) {
    if (example == null) return
    val input = example.first.trimIndent()
    val expected = example.second
    val actual = part2(input.lines())
    assert(actual == expected)
  }

  @Test
  fun part1(): Unit = solve("part1.txt", ::part1)

  @Test
  fun part2(): Unit = solve("part2.txt", ::part2)
}
