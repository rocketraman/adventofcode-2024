package day04

import Day
import Lines

private typealias Matrix<T> = List<List<T>>

class Day4 : Day() {
  override fun part1(input: Lines): Any {
    val matrix = inputAsMatrix(input)

    // now get all possible strings in all directions
    val horizontals = matrix.map { it.asString() }
    val verticals = matrix.transpose().map { it.asString() }
    val slashDiagonals = matrix.diagonals().filter { it.size > 3 }.map { it.asString() }
    val backslashDiagonals = matrix.diagonals(mirrored = true).filter { it.size > 3 }.map { it.asString() }

    return (horizontals + verticals + slashDiagonals + backslashDiagonals).fold(0) { count, s ->
      count + s.windowed(4).count { it == "XMAS" || it == "SAMX" }
    }
  }

  override fun part2(input: Lines): Any {
    val matrix = inputAsMatrix(input)

    fun List<List<Char>>.matchesXmas() =
      single { it.size == 3 }.asString().let { it == "MAS" || it == "SAM" }

    // window our matrix into all possible 3x3 sub-matrices, and check if they match the pattern
    return matrix
      .submatrices(3)
      .count { it.diagonals().matchesXmas() && it.diagonals(mirrored = true).matchesXmas() }
  }

  private fun inputAsMatrix(input: Lines) =
    input.map { line -> line.toCharArray().toList().filterNot { it == ' ' } }.toList()

  private fun <T> Matrix<T>.print() {
    (this[0].indices).map { x ->
      (this[x].indices).map { y ->
        print(this[x][y])
        print(" ")
      }
      println()
    }
  }

  private fun <T> Matrix<T>.transpose(): Matrix<T> =
    (this[0].indices).map { x ->
      (this[x].indices).map { y ->
        this[y][x]
      }
    }

  private fun <T> Matrix<T>.diagonals(mirrored: Boolean = false): List<List<T>> = buildList {
    val matrix = this@diagonals
    val size = matrix.size
    for (j in 0..size + size - 2) {
      add(buildList {
        for (y in 0..j) {
          val x = if (mirrored) size - j + y else j - y
          if (x in 0..<size && y < size) {
            add(matrix[x][y])
          }
        }
      })
    }
  }

  private fun <T> Matrix<T>.submatrices(size: Int): List<Matrix<T>> = buildList {
    (0..(this@submatrices.size - size)).forEach { x ->
      (0..(this@submatrices[x].size - size)).forEach { y ->
        add(
          (0..<size).map { tx ->
            (0..<size).map { ty ->
              this@submatrices[x + tx][y + ty]
            }
          }
        )
      }
    }
  }

  private fun List<Char>.asString() = joinToString("")
}
