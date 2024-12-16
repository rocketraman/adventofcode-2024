package day15

import Day
import Example
import Lines
import day15.Day15.Direction.EAST
import day15.Day15.Direction.NORTH
import day15.Day15.Direction.SOUTH
import day15.Day15.Direction.WEST
import org.junit.jupiter.api.Test

class Day15 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      ########
      #..O.O.#
      ##@.O..#
      #...O..#
      #.#.O..#
      #...O..#
      #......#
      ########
      
      <^^>>>vv<v>>v<<
      """ to 2028,

      """
      ##########
      #..O..O.O#
      #......O.#
      #.OO..O.O#
      #..O@..O.#
      #O#..O...#
      #O..O..O.#
      #.OO.O.OO#
      #....O...#
      ##########
      
      <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
      vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
      ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
      <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
      ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
      ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
      >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
      <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
      ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
      v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
      """ to 10092,
    )

    override val part2Examples: List<Example?> = listOf(
      """
      #######
      #...#.#
      #.....#
      #..OO@#
      #..O..#
      #.....#
      #######
      
      <vv<<^^<<^^
      """ to 618,

      """
      ##########
      #..O..O.O#
      #......O.#
      #.OO..O.O#
      #..O@..O.#
      #O#..O...#
      #O..O..O.#
      #.OO.O.OO#
      #....O...#
      ##########
      
      <vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
      vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
      ><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
      <<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
      ^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
      ^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
      >^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
      <><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
      ^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
      v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
      """ to 9021,

      """
      ############
      #          #
      #    O     #
      #    O     #
      #   #O     #
      #    O@    #
      #    O     #
      #          #
      ############
      
      <>^^<>vvvv<<^^^^^
      """ to 2048
    )
  }

  enum class Direction {
    NORTH, SOUTH, WEST, EAST
  }

  data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction): Point = when (direction) {
      NORTH -> copy(y = y - 1)
      SOUTH -> copy(y = y + 1)
      WEST -> copy(x = x - 1)
      EAST -> copy(x = x + 1)
    }
  }

  interface Positioned<out T : Positioned<T>> {
    val position: Point

    fun withMove(move: Direction): T
  }

  data class Robot(override val position: Point) : Positioned<Robot> {
    override fun withMove(move: Direction) = copy(position.move(move))
  }

  data class Box(override val position: Point, val width: Int = 1) : Positioned<Box> {
    override fun withMove(move: Direction) = copy(position.move(move))

    fun positions(): List<Point> =
      (0..<width).map { this.position.copy(x = this.position.x + it) }

    fun overlapsPosition(position: Point): Boolean =
      positions().any { it == position }

    fun overlapsPositions(positions: Set<Point>): Boolean =
      positions().any { it in positions }
  }

  data class Warehouse(val robot: Robot, val boxes: Set<Box>)

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val walls: Set<Point>
  )

  @Test
  fun boxOverlapsPosition() {
    Box(Point(5, 5)).let { box ->
      assert(box.overlapsPosition(Point(5, 5)))
      assert(!box.overlapsPosition(Point(4, 5)))
      assert(!box.overlapsPosition(Point(6, 5)))
    }
    Box(Point(5, 5), 2).let { box ->
      assert(box.overlapsPosition(Point(5, 5)))
      assert(box.overlapsPosition(Point(6, 5)))
      assert(!box.overlapsPosition(Point(4, 5)))
      assert(!box.overlapsPosition(Point(7, 5)))
    }
  }

  override fun part1(input: Lines): Any {
    val (chart, moves) = parseInput(input)

    val walls = pointsOfChars(chart, '#')
    val grid = Grid(chart.first().length, chart.size, walls)
    val boxes = pointsOfChars(chart, 'O').map { Box(it) }.toSet()
    val robot = Robot(pointsOfChars(chart, '@').first())

    val warehouse = moves.fold(Warehouse(robot, boxes)) { warehouse, move ->
      val (updatedRobot, updatedBoxes) = warehouse.robot.move(move, grid, warehouse.boxes)
      warehouse.copy(robot = updatedRobot, boxes = updatedBoxes)
    }

    return warehouse.boxes.sumOf { it.gps() }
  }

  override fun part2(input: Lines): Any {
    val (chart, moves) = parseInput(input)
    val width = 2

    val walls = pointsOfChars(chart, '#').flatMap { p ->
      (0..<width).map { p.copy(x = p.x * width + it, p.y) }
    }.toSet()
    val grid = Grid(chart.first().length * width, chart.size, walls)
    val boxes = pointsOfChars(chart, 'O').map { Box(it.copy(it.x * width), width) }.toSet()
    val robot = Robot(pointsOfChars(chart, '@').map { it.copy(x = it.x * width) }.first())
    val initial = Warehouse(robot, boxes)
    //initial.print("Initial", grid)

    val warehouse = moves.foldIndexed(initial) { idx, warehouse, move ->
      val (updatedRobot, updatedBoxes) = warehouse.robot.move(move, grid, warehouse.boxes)
      warehouse.copy(robot = updatedRobot, boxes = updatedBoxes).also {
        //it.print(move, grid)
      }
    }

    return warehouse.boxes.sumOf { it.gps() }
  }

  private fun parseInput(input: Lines): Pair<List<String>, List<Direction>> {
    val chart = input.takeWhile { !it.isEmpty() }
    val moves = input.dropWhile { !it.isEmpty() }.drop(1).joinToString("")
      .map {
        when (it) {
          '^' -> NORTH
          '>' -> EAST
          'v' -> SOUTH
          '<' -> WEST
          else -> error("Unexpected character: $it")
        }
      }
    return Pair(chart, moves)
  }

  private fun pointsOfChars(chart: List<String>, c: Char): Set<Point> = buildSet {
    chart.forEachIndexed { y, line ->
      line.forEachIndexed { x, cell ->
        if (cell == c) {
          add(Point(x, y))
        }
      }
    }
  }

  private fun Robot.move(direction: Direction, grid: Grid, boxes: Set<Box>): Pair<Robot, Set<Box>> {
    val updatedRobot = withMove(direction)
    val newUpdatedBoxes = mutableSetOf<Box>()
    val oldUpdatedBoxes = mutableSetOf<Box>()
    var forcesOnPositions = setOf(updatedRobot.position)
    while (boxes.any { it.overlapsPositions(forcesOnPositions) }) {
      val pushedBoxes = boxes.filter { it.overlapsPositions(forcesOnPositions) }.toSet()
      oldUpdatedBoxes.addAll(pushedBoxes)
      newUpdatedBoxes.addAll(pushedBoxes.map { it.withMove(direction) })
      forcesOnPositions = when (direction) {
        NORTH, SOUTH -> pushedBoxes.flatMap { it.positions().map { it.move(direction) } }.toSet()
        WEST -> pushedBoxes.map { it.position.move(direction) }.toSet()
        EAST -> pushedBoxes.map { it.positions().last().move(direction) }.toSet()
      }
    }
    return if (forcesOnPositions.any { it in grid.walls } || newUpdatedBoxes.any { it.overlapsPositions(grid.walls) }) this to boxes
    else updatedRobot to (boxes - oldUpdatedBoxes + newUpdatedBoxes)
  }

  private fun Box.gps() = 100 * position.y + position.x

  private fun Warehouse.print(label: Any, grid: Grid) {
    println("$label ${boxes.sumOf { it.gps() }}")
    (0..<grid.sizeY).forEach { y ->
      (0..<grid.sizeX).forEach { x ->
        val gridPosition = Point(x, y)
        val box = boxes.singleOrNull { it.overlapsPosition(gridPosition) }
        when {
          robot.position.let { it.x == x && it.y == y } -> print('@')
          box != null -> if (box.width == 1) print('O') else if (box.position == gridPosition) print('[') else print(']')
          grid.walls.any { it.x == x && it.y == y } -> print('#')
          else -> print('.')
        }
      }
      println()
    }
  }
}
