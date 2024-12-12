package day12

import Day
import Example
import Lines
import day12.Day12.Direction.*

class Day12 : Day() {
  companion object : Examples {
    override val part1Examples: List<Example> = listOf(
      """
      AAAA
      BBCD
      BBCC
      EEEC
      """ to 140,

      """
      OOOOO
      OXOXO
      OOOOO
      OXOXO
      OOOOO
      """ to 772,

      """
      RRRRIICCFF
      RRRRIICCCF
      VVRRRCCFFF
      VVRCCCJFFF
      VVVVCJJCFE
      VVIVCCJJEE
      VVIIICJJEE
      MIIIIIJJEE
      MIIISIJEEE
      MMMISSJEEE
      """ to 1930,
    )

    override val part2Examples: List<Example> = listOf(
      """
      AAAA
      BBCD
      BBCC
      EEEC
      """ to 80,

      """
      OOOOO
      OXOXO
      OOOOO
      OXOXO
      OOOOO
      """ to 436,

      """
      EEEEE
      EXXXX
      EEEEE
      EXXXX
      EEEEE
      """ to 236,

      """
      RRRRIICCFF
      RRRRIICCCF
      VVRRRCCFFF
      VVRCCCJFFF
      VVVVCJJCFE
      VVIVCCJJEE
      VVIIICJJEE
      MIIIIIJJEE
      MIIISIJEEE
      MMMISSJEEE
      """ to 1206,
    )
  }

  enum class Direction {
    NORTH, SOUTH, WEST, EAST
  }

  data class Grid(
    val sizeX: Int,
    val sizeY: Int,
    val plants: Map<Plot, Char>,
  )

  data class Plot(val x: Int, val y: Int) {
    fun move(direction: Direction): Plot = when (direction) {
      NORTH -> copy(y = y - 1)
      SOUTH -> copy(y = y + 1)
      WEST -> copy(x = x - 1)
      EAST -> copy(x = x + 1)
    }
  }

  data class Region(
    val plant: Char,
    val plots: Set<Plot>,
  )

  data class Side(
    val dimension: Int, // either x or y, x must have side WEST/EAST and y must have NORTH/SOUTH
    val otherDimension: Int,
    val side: Direction,
  )

  override fun part1(input: Lines): Any {
    val grid = grid(input)
    val regions = regions(grid)
    return regions.sumOf { it.area() * it.perimeter(grid) }
  }

  override fun part2(input: Lines): Any {
    val grid = grid(input)
    val regions = regions(grid)
    return regions.sumOf { it.area() * it.sides(grid) }
  }

  private fun regions(grid: Grid): List<Region> {
    val allPlots = grid.allPlots()
    return allPlots
      .map { plot -> plot to grid.plants[plot]!! }
      .fold(emptyMap<Char, List<Region>>()) { map, (plot, plant) ->
        map + (plant to map[plant].orEmpty().addPlot(plot, grid))
      }
      .values
      .flatten()
  }

  private fun grid(input: Lines): Grid {
    val plants = input.flatMapIndexed { y, line ->
      line.mapIndexedNotNull { x, c ->
        Plot(x, y) to c
      }
    }.toMap()

    return Grid(
      sizeX = input[0].length,
      sizeY = input.size,
      plants = plants,
    )
  }

  private fun Grid.allPlots() =
    (0..<sizeX).flatMap { x ->
      (0..<sizeY).map { y ->
        Plot(x, y)
      }
    }

  private fun Plot.inGrid(grid: Grid) =
    x in 0..<grid.sizeX && y in 0..<grid.sizeY

  private fun Plot.reachable(grid: Grid): Set<Plot> {
    return Direction.entries.map { move(it) }
      .filter { it.inGrid(grid) }
      .toSet()
  }

  private fun Plot.sides(grid: Grid): Set<Side> {
    val plant = grid.plants[this]!!
    return Direction.entries
      .mapNotNull { direction ->
        val plot = move(direction)
        val dimension = if (direction == EAST || direction == WEST) x else y
        val otherDimension = if (direction == EAST || direction == WEST) y else x
        if (!plot.inGrid(grid) || grid.plants[plot] != plant) {
          Side(dimension, otherDimension, direction)
        } else null
      }
      .toSet()
  }

  private fun Plot.gridEdges(grid: Grid): Int {
    return Direction.entries.map { move(it) }
      .filterNot { it.inGrid(grid) }
      .size
  }

  private fun Region.tryAddPlot(plot: Plot, grid: Grid): Pair<Boolean, Region> {
    return if (plots.any { plot in it.reachable(grid) }) {
      true to copy(plots = plots + plot)
    } else false to this
  }

  private fun List<Region>.addPlot(plot: Plot, grid: Grid): List<Region> {
    val tryRegions = map { it.tryAddPlot(plot, grid) }
    return when (tryRegions.count { it.first }) {
      0 -> tryRegions.map { it.second } + Region(grid.plants[plot]!!, setOf(plot))
      1 -> tryRegions.map { it.second }
      else -> {
        // plot was added to more than one region, combine the adjacent ones
        tryRegions.filterNot { it.first }.map { it.second } +
          tryRegions.filter { it.first }.map { it.second }.combine()
      }
    }
  }

  private fun List<Region>.combine(): Region = reduce { a, b -> a + b }

  private operator fun Region.plus(region: Region): Region {
    require(this.plant == region.plant)
    return Region(this.plant, this.plots + region.plots)
  }

  private fun Region.area() = plots.size

  private fun Region.perimeter(grid: Grid) = plots.sumOf { p ->
    val plant = grid.plants[p]!!
    p.reachable(grid).count { grid.plants[it] != plant } + p.gridEdges(grid)
  }

  private fun Region.sides(grid: Grid) =
    plots.fold(emptySet<Side>()) { acc, p -> acc + p.sides(grid) }.countDiscontinuous()

  private fun Set<Side>.countDiscontinuous(): Int {
    val groupedSides = groupBy { it.side to it.dimension }
    return groupedSides.mapValues { (_, sides) ->
      // count the jumps where there are gaps in the other dimension of each side
      // probably an easier way to do this
      val continuousRanges = sides
        .map { side -> side.otherDimension }
        .sorted()
        .fold(emptyList<IntRange>()) { dims, dim ->
          var lastDims = dims.lastOrNull()
          if (lastDims?.endInclusive == dim - 1) {
            dims.dropLast(1).plusElement(lastDims.start..dim)
          } else dims.plusElement(dim..dim)
        }
      continuousRanges.count()
    }.values.sum()
  }
}
