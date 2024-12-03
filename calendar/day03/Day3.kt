package day03

import Day
import Lines

class Day3 : Day() {
  override fun part1(input: Lines): Any {
    val regex = """mul\((\d*),(\d*)\)""".toRegex(RegexOption.MULTILINE)
    val matches = regex.findAll(input.joinToString("\n"))
    return matches.map { it.groupValues[1].toInt() * it.groupValues[2].toInt() }.sum()
  }

  override fun part2(input: Lines): Any {
    val regex = """mul\((\d*),(\d*)\)|do\(\)|don't\(\)""".toRegex(RegexOption.MULTILINE)
    val matches = regex.findAll(input.joinToString("\n"))

    data class MemState(val enabled: Boolean, val value: Int)

    return matches.fold(MemState(true, 0)) { state, match ->
      when {
        match.groupValues[0].startsWith("mul(") -> {
          if (state.enabled) {
            state.copy(value = state.value + (match.groupValues[1].toInt() * match.groupValues[2].toInt()))
          } else state
        }
        match.groupValues[0].startsWith("do(") -> state.copy(enabled = true)
        match.groupValues[0].startsWith("don't(") -> state.copy(enabled = false)
        else -> error("Invalid match $match on state $state")
      }
    }.value
  }
}
