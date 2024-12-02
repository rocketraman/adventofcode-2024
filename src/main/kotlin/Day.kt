import org.junit.jupiter.api.Test
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

typealias Lines = List<String>

abstract class Day {
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

  @Test
  fun part1(): Unit = solve("part1.txt", ::part1)

  @Test
  fun part2(): Unit = solve("part2.txt", ::part2)
}
