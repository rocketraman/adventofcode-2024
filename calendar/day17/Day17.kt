package day17

import Day
import Example
import Lines

class Day17 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      Register A: 729
      Register B: 0
      Register C: 0
      
      Program: 0,1,5,4,3,0
      """ to "4,6,3,5,6,3,5,2,1,0",
    )

    override val part2Examples: List<Example> = listOf(
      """
      Register A: 2024
      Register B: 0
      Register C: 0
      
      Program: 0,3,5,4,3,0
      """ to 117440
    )
  }

  override fun part1(input: Lines): Any {
    val (registers, program) = parseInput(input)

    val output = buildList {
      Computer(program, registers['A']!!, registers['B']!!, registers['C']!!) { add(it) }.runUntilHalt()
    }
    return output.joinToString(",")
  }

  override fun part2(input: Lines): Any {
    val (registers, program) = parseInput(input)

    fun solve(a: Long): List<Int> = buildList {
      Computer(program, a, registers['B']!!, registers['C']!!) { add(it) }.runUntilHalt()
    }

    // thanks to Michael de Kaste from Slack for this solution, TLDFO
    fun recurseExpected(expected: List<Int>): Long {
      var a = when(expected.size) {
        1 -> 0
        else -> recurseExpected(expected.drop(1)) shl 3
      }
      while(solve(a) != expected) {
        a++
      }
      return a
    }

    return recurseExpected(program.toList())
  }

  private fun parseInput(input: Lines): Pair<Map<Char, Long>, IntArray> {
    val registerRegex = """Register (.): (\d*)""".toRegex()
    val registers = input.take(3)
      .associate { registerRegex.matchEntire(it)!!.groupValues.let { s -> s[1].toCharArray()[0] to s[2].toLong() } }
    val program = input.drop(4).first().split(": ").last().split(",").map { it.toInt() }.toIntArray()
    return Pair(registers, program)
  }
}
