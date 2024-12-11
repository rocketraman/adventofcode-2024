package day11

import Day
import Lines

class Day11 : Day() {
  val stoneIterationCache = mutableMapOf<Pair<Long, Int>, Long>()

  override fun part1(input: Lines): Any {
    val stones = input[0].split(" ").map { it.toLong() }
    val blinks = 25

    val result = (0..<blinks).fold(stones) { stones, _ -> stones.flatMap { next(it) } }
    return result.size
  }

  override fun part2(input: Lines): Any {
    val stones = input[0].split(" ").map { it.toLong() }
    val blinks = 75

    return countOf(stones, blinks)
  }

  private fun next(stone: Long): List<Long> = when {
    stone == 0L -> listOf(1L)
    stone.toString().length % 2 == 0 -> {
      val s = stone.toString()
      listOf(
        s.take(s.length / 2).toLong(),
        s.drop(s.length / 2).toLong(),
      )
    }
    else -> listOf(stone * 2024)
  }

  private fun countOf(stones: List<Long>, blinks: Int): Long {
    return if (blinks == 0) stones.size.toLong()
    else stones.sumOf { stone ->
      stoneIterationCache.getOrElse(stone to blinks) {
        countOf(next(stone), blinks - 1).also { stoneIterationCache.put(stone to blinks, it) }
      }
    }
  }
}
