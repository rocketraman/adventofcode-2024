package day06

import Day
import Lines
import day06.Day6.Direction.*

class Day6 : Day() {
  enum class Direction {
    NORTH, SOUTH, WEST, EAST
  }

  enum class Turn {
    LEFT, RIGHT
  }

  data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction): Point = when (direction) {
      NORTH -> copy(y = y - 1)
      SOUTH -> copy(y = y + 1)
      WEST -> copy(x = x - 1)
      EAST -> copy(x = x + 1)
    }
  }

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val obstacles: Set<Point>,
  )

  private fun Direction.turn(turn: Turn): Direction = when (this) {
    NORTH -> when (turn) {
      Turn.LEFT -> WEST
      Turn.RIGHT -> EAST
    }
    SOUTH -> when (turn) {
      Turn.LEFT -> EAST
      Turn.RIGHT -> WEST
    }
    WEST -> when (turn) {
      Turn.LEFT -> SOUTH
      Turn.RIGHT -> NORTH
    }
    EAST -> when (turn) {
      Turn.LEFT -> NORTH
      Turn.RIGHT -> SOUTH
    }
  }

  data class Position(val point: Point, val direction: Direction)

  private fun Grid.print(visited: Set<Point>, loop: Set<Point> = emptySet()) {
    (0..<sizeY).forEach { y ->
      (0..<sizeX).forEach { x ->
        val point = Point(x, y)
        print(
          when (point) {
            in loop -> "0"
            in visited -> "X"
            in obstacles -> "#"
            else -> "."
          }
        )
      }
      println()
    }
  }

  private fun Position.inGrid(grid: Grid) =
    point.x in 0..<grid.sizeX && point.y in 0..<grid.sizeY

  override fun part1(input: Lines): Any {
    val initialPosition = Position(initialPoint(input), NORTH)
    val grid = grid(input)
    val visited = traverseGrid(grid, initialPosition).map { it.point }.toSet()
    //grid.print(visited)
    return visited.size
  }

  override fun part2(input: Lines): Any {
    val initialPosition = Position(initialPoint(input), NORTH)
    val grid = grid(input)

    val startingPath = traverseGrid(grid, initialPosition)

    val loopPoints = startingPath.filter { pos ->
      val visitedPositions = mutableSetOf<Position>()
      var loop = false
      traverseGrid(grid.copy(obstacles = grid.obstacles + pos.point), initialPosition)
        .onEach { loop = if (it in visitedPositions) true else false }
        .onEach { visitedPositions.add(it) }
        .takeWhile { !loop }
        .last()
      pos != initialPosition && loop
    }.map { it.point }.toSet()

    //grid.print(startingPath.map { it.point }.toSet(), loopPoints)
    return loopPoints.size
  }

  private fun initialPoint(input: Lines): Point {
    return input.mapIndexedNotNull { index: Int, line: String ->
      line.indexOf('^').takeIf { it != -1 }?.let { Point(it, index) }
    }.single()
  }

  private fun grid(input: Lines): Grid {
    val obstacles = input.flatMapIndexed { index, line ->
      line.positionsOf('#').map { Point(it, index) }
    }.toSet()

    // we mark visited the guard's starting position!
    return Grid(
      sizeX = input[0].length,
      sizeY = input.size,
      obstacles = obstacles,
    )
  }

  private fun traverseGrid(
    grid: Grid,
    initialPosition: Position,
    breakOn: (Position) -> Boolean = { !it.inGrid(grid) },
  ) =
    generateSequence(initialPosition) { pos ->
      val nextPoint = pos.point.move(pos.direction)
      val nextPosition = if (nextPoint in grid.obstacles) {
        pos.copy(direction = pos.direction.turn(Turn.RIGHT))
      } else {
        pos.copy(point = nextPoint)
      }
      if (breakOn(nextPosition)) null else nextPosition
    }

  private fun String.positionsOf(s: Char): List<Int> =
    mapIndexedNotNull { index, c -> if (c == s) index else null }
}
