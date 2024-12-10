package day10

import Day
import Lines
import day10.Day10.Direction.*

class Day10 : Day() {
  enum class Direction {
    NORTH, SOUTH, WEST, EAST
  }

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val heights: Map<Point, Int>,
  )

  data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction): Point = when (direction) {
      NORTH -> copy(y = y - 1)
      SOUTH -> copy(y = y + 1)
      WEST -> copy(x = x - 1)
      EAST -> copy(x = x + 1)
    }
  }

  data class PathNode(val children: List<PathNode>, val value: Point)

  override fun part1(input: Lines): Any {
    val grid = grid(input)
    val trailheads = trailheads(grid)
    return trailheads.sumOf { buildPath(it, grid).reachableNines(grid).size }
  }

  override fun part2(input: Lines): Any {
    val grid = grid(input)
    val trailheads = trailheads(grid)
    return trailheads.sumOf { buildPath(it, grid).rating(grid) }
  }

  private fun grid(input: Lines): Grid {
    val heights = input.flatMapIndexed { y, line ->
      line.mapIndexedNotNull { x, c ->
        Point(x, y) to c.digitToInt()
      }
    }.toMap()

    return Grid(
      sizeX = input[0].length,
      sizeY = input.size,
      heights = heights,
    )
  }

  private fun Grid.allPoints() =
    (0..<sizeX).flatMap { x ->
      (0..<sizeY).map { y ->
        Point(x, y)
      }
    }

  private fun trailheads(grid: Grid): Set<Point> =
    grid.allPoints().filter { grid.heights[it] == 0 }.toSet()

  private fun Point.inGrid(grid: Grid) =
    x in 0..<grid.sizeX && y in 0..<grid.sizeY

  private fun Point.reachable(grid: Grid): Set<Point> {
    val desiredHeight = grid.heights[this]!! + 1
    return Direction.entries.map { move(it) }
      .filter { it.inGrid(grid) }
      .filter { grid.heights[it] == desiredHeight }
      .toSet()
  }

  private fun buildPath(point: Point, grid: Grid): PathNode {
    val children = point.reachable(grid).map { buildPath(it, grid) }
    return PathNode(children, point)
  }

  private fun PathNode.print(grid: Grid, indentLevel: Int = 0): String = buildString {
    repeat(indentLevel * 2) { append(" ") }
    appendLine("$value (${grid.heights[value]})")
    children.forEach { child -> append(child.print(grid, indentLevel + 1)) }
  }

  private fun PathNode.reachableNines(grid: Grid): Set<Point> = buildSet {
    if (grid.heights[value] == 9) add(value)
    children.forEach { child -> addAll(child.reachableNines(grid)) }
  }

  private fun PathNode.rating(grid: Grid): Int {
    // count all paths that end in a 9
    return if (grid.heights[value] == 9) 1
    else children.sumOf { it.rating(grid) }
  }
}
