package day08

import Day
import Lines

class Day8 : Day() {
  data class Point(val x: Int, val y: Int) {
    fun antinode(otherPoint: Point): Point = Point(x + x - otherPoint.x, y + y - otherPoint.y)

    fun lineAntinodes(otherPoint: Point): Sequence<Point> {
      val deltaX = x - otherPoint.x
      val deltaY = y - otherPoint.y
      return generateSequence(this) { p ->
        Point(p.x + deltaX, p.y + deltaY)
      }
    }
  }

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val antennas: Map<Char, Set<Point>>,
  )

  override fun part1(input: Lines): Any {
    val grid = grid(input)

    val antinodeMap = grid.antennas.mapValues { (_, points) ->
      points.cartesianProduct().flatMap { (a1, a2) ->
        listOf(a2.antinode(a1), a1.antinode(a2))
      }.toSet()
    }

    //grid.print(antinodeMap)

    val antinodes = antinodeMap.values.flatten()
    return antinodes.toSet().count { it.inGrid(grid) }
  }

  override fun part2(input: Lines): Any {
    val grid = grid(input)

    val antinodeMap = grid.antennas.mapValues { (_, points) ->
      points.cartesianProduct().flatMap { (a1, a2) ->
        a2.lineAntinodes(a1).takeWhile { it.inGrid(grid) }.toList() +
          a1.lineAntinodes(a2).takeWhile { it.inGrid(grid) }.toList()
      }.toSet()
    }

    //grid.print(antinodeMap)

    val antinodes = antinodeMap.values.flatten()
    return antinodes.toSet().count { it.inGrid(grid) }
  }

  private fun Grid.print(antinodes: Map<Char, Set<Point>> = emptyMap()) {
    fun antinodeCharMap(c: Char) = when (c) {
      '0' -> '#'
      'A' -> '#'
      else -> "#"
    }

    (0..<sizeX).forEach { x ->
      print(if (x == 0) "   0" else if(x < 10) "  $x" else " $x")
    }
    println()
    (0..<sizeY).forEach { y ->
      print(if (y < 10) " $y " else "$y ")
      (0..<sizeX).forEach { x ->
        val point = Point(x, y)
        val antenna = antennas.entries.firstOrNull { entry -> entry.value.contains(point) }?.key
        val antinode = antinodes.entries.firstOrNull { point in it.value }?.key?.let { antinodeCharMap(it) }
        when {
          antinode != null && antenna != null -> print("$antenna$antinode ")
          antinode != null -> print("$antinode  ")
          antenna != null -> print("$antenna  ")
          else -> print(".  ")
        }
      }
      println()
    }
  }

  private fun Point.inGrid(grid: Grid) =
    x in 0..<grid.sizeX && y in 0..<grid.sizeY

  private fun grid(input: Lines): Grid {
    val antennas = input.flatMapIndexed { y, line ->
      line.mapIndexedNotNull { x, c ->
        if (c != '.') c to Point(x, y) else null
      }
    }
      .groupBy { it.first }
      .mapValues { it.value.map { it.second }.toSet() }
      .toMap()

    // we mark visited the guard's starting position!
    return Grid(
      sizeX = input[0].length,
      sizeY = input.size,
      antennas = antennas,
    )
  }

  private fun <T> Set<T>.cartesianProduct(): Set<Pair<T, T>> =
    flatMap { elem -> (this - elem).map { elem to it } }.toSet()
}
