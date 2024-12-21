package day18

import Day
import Example
import Lines
import java.util.*

class Day18 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      7,7,12
      5,4
      4,2
      4,5
      3,0
      2,1
      6,3
      2,4
      1,5
      0,6
      3,3
      2,6
      5,1
      1,2
      5,5
      2,5
      6,5
      1,4
      0,4
      6,4
      1,1
      6,1
      1,0
      0,5
      1,6
      2,0
      """ to 22,
    )

    override val part2Examples: List<Example> = listOf(
      """
      7,7
      5,4
      4,2
      4,5
      3,0
      2,1
      6,3
      2,4
      1,5
      0,6
      3,3
      2,6
      5,1
      1,2
      5,5
      2,5
      6,5
      1,4
      0,4
      6,4
      1,1
      6,1
      1,0
      0,5
      1,6
      2,0
      """ to "6,1"
    )
  }

  enum class Direction(val diagonal: Boolean = false) {
    NORTH, NORTH_EAST(true), EAST, SOUTH_EAST(true), SOUTH, SOUTH_WEST(true), WEST, NORTH_WEST(true)
  }

  data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction): Point = when (direction) {
      Direction.NORTH -> copy(y = y - 1)
      Direction.NORTH_EAST -> copy(x = x + 1, y = y - 1)
      Direction.EAST -> copy(x = x + 1)
      Direction.SOUTH_EAST -> copy(x = x + 1, y = y + 1)
      Direction.SOUTH -> copy(y = y + 1)
      Direction.SOUTH_WEST -> copy(x = x - 1, y = y + 1)
      Direction.WEST -> copy(x = x - 1)
      Direction.NORTH_WEST -> copy(x = x - 1, y = y - 1)
    }
  }

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val corrupted: Set<Point>,
  )

  override fun part1(input: Lines): Any {
    val setup = input.first().split(",")
    val sizeX = setup[0].toInt()
    val sizeY = setup[1].toInt()
    val take = setup[2].toInt()

    val corruptedCoordinates = input.drop(1).take(take)
      .map { it.split(",").let { Point(it[0].toInt(), it[1].toInt()) } }.toSet()
    val grid = Grid(sizeX, sizeY, corruptedCoordinates)
    val shortestPaths = shortestPathDjikstra(Point(0,0)) {
      it.neighbors(grid, diagonal = false) { if (it in grid.corrupted) null else 1 }
    }
    return shortestPaths
      .toMap()[Point(sizeX - 1, sizeY - 1)]!!
  }

  override fun part2(input: Lines): Any {
    val setup = input.first().split(",")
    val sizeX = setup[0].toInt()
    val sizeY = setup[1].toInt()

    // as the bytes fall, we need to check if we have a path of corrupted bytes from anywhere on the
    // bottom/left to the top/right
    val corruptedCoordinates = input.drop(1)
      .map { it.split(",").let { Point(it[0].toInt(), it[1].toInt()) } }

    val blockedAt = (0..<corruptedCoordinates.size).dropWhile { index ->
      val grid = Grid(sizeX, sizeY, corruptedCoordinates.take(index + 1).toSet())

//      println()
//      println("Grid at index $index (point = ${corruptedCoordinates[index]})")
//      grid.print()

      val pointsBl = ((0..<grid.sizeY).map { Point(0, it) } + (0..<grid.sizeX).map { Point(it, grid.sizeY - 1) })
        .filter { it in grid.corrupted }.toSet()
      val pointsTr = ((0..<grid.sizeX).map { Point(it, 0) } + (0..<grid.sizeY).map { Point(sizeX - 1, it) })
        .filter { it in grid.corrupted }.toSet()

      pointsBl.isEmpty() || pointsTr.isEmpty() || pointsBl.firstOrNull { bl ->
        val shortestPaths = shortestPathDjikstra(bl) {
          it.neighbors(grid, diagonal = true) { if (it in grid.corrupted) 1 else null }
        }
        pointsTr.any { tr -> tr in shortestPaths.keys }
      } == null
    }.first()

    return corruptedCoordinates[blockedAt].let { "${it.x},${it.y}" }
  }

  /**
   * Given a point, determine its valid neighbors.
   */
  private fun Point.neighbors(grid: Grid, diagonal: Boolean, weightFn: (point: Point) -> Int?): Set<Pair<Point, Int?>> {
    return Direction.entries
      .filter { if (diagonal) true else !it.diagonal }
      .map { this.move(it) }
      .filter { it.inGrid(grid) }
      .map { it to weightFn(it) }
      .toSet()
  }

  private fun shortestPathDjikstra(start: Point, neighborsFn: (Point) -> Set<Pair<Point, Int?>>): Map<Point, Int> {
    val distances = mutableMapOf<Point, Int>().withDefault { Int.MAX_VALUE }.apply { this[start] = 0 }
    val priorityQueue = PriorityQueue<Pair<Point, Int>>(compareBy { it.second }).apply { add(start to 0) }

    while (priorityQueue.isNotEmpty()) {
      val (node, currentDist) = priorityQueue.poll()
      neighborsFn(node).forEach { (adjacent, weight) ->
        val totalDist = if (weight == null) Int.MAX_VALUE else currentDist + weight
        if (totalDist < distances.getValue(adjacent)) {
          distances[adjacent] = totalDist
          priorityQueue.add(adjacent to totalDist)
        }
      }
    }
    return distances
  }

  private fun Grid.print() {
    (0..<sizeY).forEach { y ->
      (0..<sizeX).forEach { x ->
        val point = Point(x, y)
        print(
          when (point) {
            in corrupted -> "#"
            else -> "."
          }
        )
      }
      println()
    }
  }

  private fun Point.inGrid(grid: Grid) =
    x in 0..<grid.sizeX && y in 0..<grid.sizeY
}
