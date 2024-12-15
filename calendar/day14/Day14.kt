package day14

import Day
import Example
import Lines
import org.junit.jupiter.api.Test

class Day14 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      11,7
      p=2,4 v=2,-3
      """ to 1,

      """
      11,7
      p=0,4 v=3,-3
      p=6,3 v=-1,-3
      p=10,3 v=-1,2
      p=2,0 v=2,-1
      p=0,0 v=1,3
      p=3,0 v=-2,-2
      p=7,6 v=-1,-3
      p=3,0 v=-1,-2
      p=9,3 v=2,3
      p=7,3 v=-1,2
      p=2,4 v=2,-3
      p=9,5 v=-3,-3
      """ to 12,
    )

    override val part2Examples: List<Example?> = EMPTY
  }

  data class Velocity(val x: Int, val y: Int)

  data class Point(val x: Int, val y: Int)

  data class Robot(val id: Int, val position: Point, val velocity: Velocity)

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
  )

  @Test
  fun testWrap() {
    val robot = Robot(0, Point(8, 2), Velocity(2, -3))
    val grid = Grid(11, 7)
    robot.move().wrap(grid).also { assert(it.position == Point(10, 6)) }
      .move().wrap(grid).also { assert(it.position == Point(1, 3)) }
      .move().wrap(grid).also { assert(it.position == Point(3, 0)) }
      .move().wrap(grid).also { assert(it.position == Point(5, 4)) }
  }

  override fun part1(input: Lines): Any {
    val (grid, robots) = inputs(input)

    val finalRobots = (0..<100).fold(robots) { acc, tick -> acc.map { it.move().wrap(grid) } }

    val middleX = grid.sizeX / 2
    val middleY = grid.sizeY / 2

    return finalRobots
      .filter { it.position.x != middleX && it.position.y != middleY }
      .groupBy { (it.position.x < middleX) to (it.position.y < middleY) }
      .mapValues { it.value.size }
      .values
      .reduce { c1, c2 -> c1 * c2 }
  }

  override fun part2(input: Lines): Any {
    val (grid, robots) = inputs(input)

    val sequence = generateSequence(robots) { robots -> robots.map { it.move().wrap(grid) } }

    val robotTree = sequence.withIndex().dropWhile { (_, robots) ->
      // thanks to Paul Woitaschek for the idea in the Kotlin Slack to look for a filled 3x3 square
      val robotPositions = robots.map { it.position }.toSet()
      robotPositions.none { robotPositions.containsAll(it.adjacent()) }
    }.first()
    robotTree.value.print(grid)
    return robotTree.index
  }

  private fun inputs(input: Lines): Pair<Grid, List<Robot>> {
    val regex = """p=(\d*),(\d*) v=(-?\d*),(-?\d*)""".toRegex()
    // we add the size of the grid into the first line of the output to make testing easier
    val grid = input.first().split(",").let { Grid(it[0].toInt(), it[1].toInt()) }

    val robots = input.drop(1).mapIndexed { id, line ->
      val match = regex.matchEntire(line) ?: error("Invalid input: $line")
      Robot(
        id,
        Point(match.groupValues[1].toInt(), match.groupValues[2].toInt()),
        Velocity(match.groupValues[3].toInt(), match.groupValues[4].toInt())
      )
    }
    return grid to robots
  }

  private fun Robot.move(): Robot = copy(position = position.copy(x = position.x + velocity.x, y = position.y + velocity.y))

  private fun Robot.wrap(grid: Grid): Robot {
    if (position.x in 0..<grid.sizeX && position.y in 0..<grid.sizeY) return this

    tailrec fun wrap(value: Int, size: Int): Int {
      val newValue = when {
        value < 0 -> size + value
        value >= size -> value - size
        else -> value
      }
      return if (newValue in 0..<size) newValue else wrap(newValue, size)
    }

    return copy(position = position.copy(x = wrap(position.x, grid.sizeX), y = wrap(position.y, grid.sizeY)))
  }

  private fun List<Robot>.print(grid: Grid) {
    (0..grid.sizeY).forEach { y ->
      (0..grid.sizeX).forEach { x ->
        if (any { it.position.x == x && it.position.y == y }) { print("█") } else print(" ")
      }
      println()
    }
  }

  private fun Point.adjacent(): Set<Point> = buildSet {
    add(copy(x = x + 1))
    add(copy(x = x - 1))
    add(copy(y = y + 1))
    add(copy(y = y - 1))
    add(copy(x = x + 1, y = y + 1))
    add(copy(x = x - 1, y = y - 1))
    add(copy(x = x + 1, y = y - 1))
    add(copy(x = x - 1, y = y + 1))
  }
}
