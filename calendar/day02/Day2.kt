package day02

import Day
import Lines
import kotlin.math.abs

class Day2 : Day() {
  override fun part1(input: Lines): Any {
    return parseInput(input).count(::passPredicate)
  }

  override fun part2(input: Lines): Any {
    return parseInput(input).count { reports ->
      // just brute force it by checking all the possible report sequences
      // a cleaner way might be to use `fold` with a little accumulator data structure tracking the error count
      // and previous value, and if it ends with error count > 1 then it fails
      val allReports = sequence {
        yield(reports)
        repeat(reports.size) { index -> yield(reports.toMutableList().apply { removeAt(index) }) }
      }
      allReports.any(::passPredicate)
    }
  }

  private fun parseInput(input: Lines) = input
    .map { line ->
      line.split(" ").map { it.toInt() }
    }

  private fun passPredicate(reports: List<Int>): Boolean {
    val adjacents = reports.zipWithNext()
    val directionPredicate: (Pair<Int, Int>) -> Boolean =
      if (reports[0] > reports[1]) {
        { it.first > it.second }
      } else {
        { it.first < it.second }
      }
    return adjacents.all(directionPredicate) && adjacents.all { abs(it.first - it.second) in 1..3 }
  }
}
