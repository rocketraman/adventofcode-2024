package day13

import Day
import Example
import Lines
import kotlin.test.Test

class Day13 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      Button A: X+94, Y+34
      Button B: X+22, Y+67
      Prize: X=8400, Y=5400
      """ to 280L,

      """
      Button A: X+26, Y+66
      Button B: X+67, Y+21
      Prize: X=12748, Y=12176
      """ to 0L,

      """
      Button A: X+17, Y+86
      Button B: X+84, Y+37
      Prize: X=7870, Y=6450
      """ to 200L,

      """
      Button A: X+69, Y+23
      Button B: X+27, Y+71
      Prize: X=18641, Y=10279
      """ to 0L,

      """
      Button A: X+94, Y+34
      Button B: X+22, Y+67
      Prize: X=8400, Y=5400
      
      Button A: X+26, Y+66
      Button B: X+67, Y+21
      Prize: X=12748, Y=12176
      
      Button A: X+17, Y+86
      Button B: X+84, Y+37
      Prize: X=7870, Y=6450
      
      Button A: X+69, Y+23
      Button B: X+27, Y+71
      Prize: X=18641, Y=10279
      """ to 480L,
    )

    override val part2Examples: List<Example?> = EMPTY

    private const val COST_A = 3L
    private const val COST_B = 1L
  }

  data class Delta(
    val x: Long,
    val y: Long,
  )

  data class Machine(
    val dA: Delta,
    val dB: Delta,
    val prize: Delta,
  )

  data class Pushes(val a: Long, val b: Long)

  private val buttonRegex = """Button [AB]: X\+(\d*), Y\+(\d*)""".toRegex()
  private val prizeRegex = """Prize: X=(\d*), Y=(\d*)""".toRegex()

  @Test
  fun part2Example() {
    val input = """
      Button A: X+94, Y+34
      Button B: X+22, Y+67
      Prize: X=10000000008400, Y=10000000005400
      
      Button A: X+26, Y+66
      Button B: X+67, Y+21
      Prize: X=10000000012748, Y=10000000012176
      
      Button A: X+17, Y+86
      Button B: X+84, Y+37
      Prize: X=10000000007870, Y=10000000006450
      
      Button A: X+69, Y+23
      Button B: X+27, Y+71
      Prize: X=10000000018641, Y=10000000010279
      """.trimIndent()

    val solution = parseMachines(input.lines())
      .map { solveMachine(it)?.cost() }

    assert(solution[0] == null)
    assert(solution[1].let { it != null && it > 0L })
    assert(solution[2] == null)
    assert(solution[3].let { it != null && it > 0L })
  }

  override fun part1(input: Lines): Any {
    val machines = parseMachines(input)
    return machines
      .mapNotNull { machine -> solveMachine(machine) }
      .sumOf { it.cost() }
  }

  override fun part2(input: Lines): Any {
    val conversion = 10_000_000_000_000
    val machines = parseMachines(input)
    return machines
      .map { it.copy(prize = Delta(it.prize.x + conversion, it.prize.y + conversion)) }
      .mapNotNull { solveMachine(it) }
      .sumOf { it.cost() }
  }

  private fun parseMachines(input: Lines): List<Machine> = input.chunked(4).map { chunk ->
    val buttonAMatch = buttonRegex.matchEntire(chunk[0]) ?: error("Invalid button A: '${chunk[0]}'")
    val buttonBMatch = buttonRegex.matchEntire(chunk[1]) ?: error("Invalid button B: '${chunk[1]}'")
    val prizeMatch = prizeRegex.matchEntire(chunk[2]) ?: error("Invalid prize: '${chunk[2]}'")
    Machine(
      dA = Delta(buttonAMatch.groupValues[1].toLong(), buttonAMatch.groupValues[2].toLong()),
      dB = Delta(buttonBMatch.groupValues[1].toLong(), buttonBMatch.groupValues[2].toLong()),
      prize = Delta(prizeMatch.groupValues[1].toLong(), prizeMatch.groupValues[2].toLong()),
    )
  }

  private fun solveMachine(machine: Machine): Pushes? = with(machine) {
    // system of two equations with two unknowns
    val b = (dA.x * prize.y - dA.y * prize.x) / (dA.x * dB.y - dA.y * dB.x)
    val a = (prize.y - b * dB.y) / dA.y
    val valid = a * dA.x + b * dB.x == prize.x && a * dA.y + b * dB.y == prize.y
    if (valid) Pushes(a, b) else null
  }

  private fun Pushes.cost() = a * COST_A + b * COST_B
}
