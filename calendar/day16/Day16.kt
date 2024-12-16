package day16

import Day
import Example
import Lines
import day16.Day16.Direction.*
import day16.Day16.Turn.*
import java.util.PriorityQueue

class Day16 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      ###############
      #.......#....E#
      #.#.###.#.###.#
      #.....#.#...#.#
      #.###.#####.#.#
      #.#.#.......#.#
      #.#.#####.###.#
      #...........#.#
      ###.#.#####.#.#
      #...#.....#.#.#
      #.#.#.###.#.#.#
      #.....#...#.#.#
      #.###.#.#.#.#.#
      #S..#.....#...#
      ###############
      """ to 7036,

      """
      #################
      #...#...#...#..E#
      #.#.#.#.#.#.#.#.#
      #.#.#.#...#...#.#
      #.#.#.#.###.#.#.#
      #...#.#.#.....#.#
      #.#.#.#.#.#####.#
      #.#...#.#.#.....#
      #.#.#####.#.###.#
      #.#.#.......#...#
      #.#.###.#####.###
      #.#.#...#.....#.#
      #.#.#.#####.###.#
      #.#.#.........#.#
      #.#.#.#########.#
      #S#.............#
      #################
      """ to 11048
    )

    override val part2Examples: List<Example> = listOf(
      """
      ###############
      #.......#....E#
      #.#.###.#.###.#
      #.....#.#...#.#
      #.###.#####.#.#
      #.#.#.......#.#
      #.#.#####.###.#
      #...........#.#
      ###.#.#####.#.#
      #...#.....#.#.#
      #.#.#.###.#.#.#
      #.....#...#.#.#
      #.###.#.#.#.#.#
      #S..#.....#...#
      ###############
      """ to 45,

      """
      #################
      #...#...#...#..E#
      #.#.#.#.#.#.#.#.#
      #.#.#.#...#...#.#
      #.#.#.#.###.#.#.#
      #...#.#.#.....#.#
      #.#.#.#.#.#####.#
      #.#...#.#.#.....#
      #.#.#####.#.###.#
      #.#.#.......#...#
      #.#.###.#####.###
      #.#.#...#.....#.#
      #.#.#.#####.###.#
      #.#.#.........#.#
      #.#.#.#########.#
      #S#.............#
      #################
      """ to 64
    )
  }

  enum class Direction {
    NORTH, SOUTH, WEST, EAST
  }

  enum class Turn(val cost: Int) {
    LEFT(1000), RIGHT(1000), NONE(1)
  }

  data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction): Point = when (direction) {
      NORTH -> copy(y = y - 1)
      SOUTH -> copy(y = y + 1)
      WEST -> copy(x = x - 1)
      EAST -> copy(x = x + 1)
    }
  }

  private fun Direction.turn(turn: Turn): Direction = when (this) {
    NORTH -> when (turn) {
      LEFT -> WEST
      RIGHT -> EAST
      NONE -> NORTH
    }
    SOUTH -> when (turn) {
      LEFT -> EAST
      RIGHT -> WEST
      NONE -> SOUTH
    }
    WEST -> when (turn) {
      LEFT -> SOUTH
      RIGHT -> NORTH
      NONE -> WEST
    }
    EAST -> when (turn) {
      LEFT -> NORTH
      RIGHT -> SOUTH
      NONE -> EAST
    }
  }

  data class Position(val point: Point, val direction: Direction)

  data class PathNode(val children: List<PathNode>, val value: Position)

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val walls: Set<Point>,
    val start: Position,
    val end: Point,
  )

  override fun part1(input: Lines): Any {
    val grid = grid(input)
    val shortestPaths = shortestPathDjikstra(grid)
    return shortestPaths
      .filterKeys { it.point == grid.end }
      .toList()
      .minBy { it.second }
      .second
  }

  override fun part2(input: Lines): Any {
    val grid = grid(input)
    val shortestPaths = shortestPathDjikstra(grid)
    val endPosition = shortestPaths
      .filter { it.key.point == grid.end }
      .minBy { it.value }
      .key

    val bestPaths = buildBestReversePaths(endPosition, grid, shortestPaths)
    return bestPaths.allPoints().count()
  }

  private fun grid(input: Lines): Grid {
    val cells = input.flatMapIndexed { y, line ->
      line.mapIndexedNotNull { x, c ->
        Point(x, y) to c
      }
    }

    val cellValues = cells.groupBy { it.second }.mapValues { it.value.map { it.first }.toSet() }

    return Grid(
      sizeX = input[0].length,
      sizeY = input.size,
      walls = cellValues['#']!!,
      start = Position(cellValues['S']!!.single(), EAST),
      end = cellValues['E']!!.single(),
    )
  }

  /**
   * Given a position, determine the next possible positions and their costs. A neighbor could be the same point
   * with a turn, or the next point straight ahead. Basically, one move, not a turn + move.
   */
  private fun Position.next(grid: Grid): Set<Pair<Position, Int>> {
    return listOf(
      copy(direction = direction.turn(LEFT)) to LEFT.cost,
      copy(direction = direction.turn(RIGHT)) to RIGHT.cost,
      copy(point = point.move(direction)) to NONE.cost,
    ).filterNot { it.first.point in grid.walls }.toSet()
  }

  private fun buildBestReversePaths(
    position: Position,
    grid: Grid,
    costs: Map<Position, Int>,
    visited: Set<Position> = emptySet(),
  ): PathNode {
    // we can't build all possible paths, but we can limit it to only the *best* paths to the end
    // backwards because a point later in the path could have unequal intermediate costs e.g. two different turns
    // getting to the same place
    val possiblePredecessors = if (position == grid.start) emptySet() else position.reverse().next(grid)
      .map { it.first.reverse() to it.second }
      // cost to get here is the cost to get to the predecessor plus the cost from the predecessor to here
      .map { it.first to costs[it.first]!! + it.second }
      .filterNot { it.first in visited }
      .sortedBy { it.second }

    val childrenSameCost = possiblePredecessors
      .takeWhile { it.second == possiblePredecessors.first().second }
      .map { (p, _) -> buildBestReversePaths(p, grid, costs, visited + p) }

    return PathNode(childrenSameCost, position)
  }

  private fun PathNode.allPoints(): Set<Point> =
    children.map { it.allPoints() }.flatten().toSet() + value.point

  private fun shortestPathDjikstra(grid: Grid): Map<Position, Int> {
    val distances = mutableMapOf<Position, Int>().withDefault { Int.MAX_VALUE }.apply { this[grid.start] = 0 }
    val priorityQueue = PriorityQueue<Pair<Position, Int>>(compareBy { it.second }).apply { add(grid.start to 0) }

    while (priorityQueue.isNotEmpty()) {
      val (node, currentDist) = priorityQueue.poll()
      node.next(grid).forEach { (adjacent, weight) ->
        val totalDist = currentDist + weight
        if (totalDist < distances.getValue(adjacent)) {
          distances[adjacent] = totalDist
          priorityQueue.add(adjacent to totalDist)
        }
      }
    }
    return distances
  }

  private fun Position.reverse() = copy(direction = direction.turn(RIGHT).turn(RIGHT))

  private fun Grid.toString(label: Any, other: (Point) -> Char? = { null }): String = buildString {
    appendLine(label)
    (0..<sizeY).forEach { y ->
      (0..<sizeX).forEach { x ->
        val gridPoint = Point(x, y)
        when (gridPoint) {
          in walls -> append('#')
          start.point -> append(other(gridPoint) ?: 'S')
          end -> append(other(gridPoint) ?: 'E')
          else -> append(other(gridPoint) ?: '.')
        }
      }
      appendLine()
    }
  }
}
