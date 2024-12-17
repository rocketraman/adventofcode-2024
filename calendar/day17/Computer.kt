package day17

import kotlin.math.pow

/**
 * This is a bit more complicated that necessary because I modified the IntCodeComputer from AOC 2019,
 * which supports variable opcode parameter inputs and so on. Could just have implemented it as a loop
 * with a `when` on the opcode and some vars, but that's ok.
 */
class Computer(val program: IntArray, var a: Long, var b: Long, var c: Long, val ioOutput: (Int) -> Unit) {
  enum class Instruction(
    val opCode: Int,
    val paramCount: Int,
    val fn: (c: Computer, operands: IntArray) -> Boolean
  ) {
    ADV(0, 1,
      { c, operands -> c.a = c.a / (2.toDouble().pow(c.comboOperand(operands[0]).toDouble())).toLong(); false }
    ),
    BXL(1, 1,
      { c, operands -> c.b = c.b.xor(operands[0].toLong()); false }
    ),
    BST(2, 1,
      { c, operands -> c.b = c.comboOperand(operands[0]) % 8; false }
    ),
    JNZ(3, 1,
      { c, operands -> if (c.a != 0L) { c.instructionPointer = operands[0]; true } else false }
    ),
    BXC(4, 1,
      { c, operands -> c.b = c.b.xor(c.c); false }
    ),
    OUT(5, 1,
      { c, operands -> c.ioOutput((c.comboOperand(operands[0]) % 8).toInt()); false }
    ),
    BDV(6, 1,
      { c, operands -> c.b = c.a / (2.toDouble().pow(c.comboOperand(operands[0]).toDouble())).toLong(); false }
    ),
    CDV(7, 1,
      { c, operands -> c.c = c.a / (2.toDouble().pow(c.comboOperand(operands[0]).toDouble())).toLong(); false }
    ),
    ;
  }

  private var instructionPointer = 0
  private var operationCount = 0

  private fun comboOperand(value: Int): Long = when (value) {
    0, 1, 2, 3 -> value.toLong()
    4 -> a
    5 -> b
    6 -> c
    else -> error("Reserved combo operand $value")
  }

  private fun instruction(opCode: Int) = Instruction.entries.find { it.opCode == opCode }
    ?: error("Invalid opcode $opCode at instruction pointer $instructionPointer, instruction=${program[instructionPointer]}")

  fun tick(): Boolean {
    if (instructionPointer >= program.size) return false

    val opCode = program[instructionPointer]
    val instruction = instruction(opCode)

    val params = IntArray(instruction.paramCount) {
      program[instructionPointer + it + 1]
    }

    //println("Instruction $instruction (${instruction.opCode}) with params=[${params.joinToString()}] registers=$registers")

    val jump = instruction.fn(this, params)
    if(!jump) instructionPointer += params.size + 1

    if (++operationCount >= 100_000) {
      error("$operationCount operations exceeds the limit")
    }

    return true
  }

  fun runUntilHalt() {
    while(tick()) Unit
  }
}
