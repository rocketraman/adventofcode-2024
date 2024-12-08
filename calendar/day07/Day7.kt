package day07

import Day
import Lines
import kotlin.math.pow

class Day7 : Day() {
  data class Equation(val result: Long, val inputs: List<Long>)

  enum class Op(val fn: (Long, Long) -> Long) {
    ADD({ a, b -> a + b }),
    MUL({ a, b -> a * b }),
    CAT({ a, b -> (a.toString() + b.toString()).toLong() }),
  }

  override fun part1(input: Lines): Any = solve(input, 2)

  override fun part2(input: Lines): Any = solve(input, 3)

  private fun solve(input: Lines, opCount: Int): Long {
    val equations = input.map { line ->
      val result = line.split(": ")[0].trim().toLong()
      val inputs = line.split(": ")[1].split(" ").map { it.toLong() }
      Equation(result, inputs)
    }

    return equations.filter { e ->
      val size = e.inputs.size
      val operationsCount = opCount.toDouble().pow(size - 1).toLong()
      // determine all the possible combinations of operators
      // this is brute force, but the runtime isn't bad even with 3 operators
      val opCombos = (0..<operationsCount).map { possibility ->
        val operatorsAsBinary = possibility.toULong().toString(opCount).padStart(size - 1, '0')
        val operatorFns: List<Op> = operatorsAsBinary.map { digit -> Op.entries[digit.digitToInt()] }
        operatorFns
      }
      val results = opCombos.map { op ->
        e.inputs.reduceIndexed { index, result, operand ->
          op[index - 1].fn(result, operand)
        } to op
      }
      results.any { it.first == e.result }
    }.sumOf { it.result }
  }
}
