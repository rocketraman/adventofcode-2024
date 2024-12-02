package day01

import Day
import Lines
import kotlin.math.abs

class Day1 : Day() {
  override fun part1(input: Lines): Any {
    val lists = input.map { it.split(" ") }
    val first = lists.map { it.first().toInt() }.sorted()
    val second = lists.map { it.last().toInt() }.sorted()
    return first.zip(second).sumOf { (first, second) -> abs(first - second) }
  }

  override fun part2(input: Lines): Any {
    val lists = input.map { it.split(" ") }
    val first = lists.map { it.first().toInt() }
    val secondCounts = lists.map { it.last().toInt() }.fold(mapOf<Int, Int>()) { map, element ->
      val elementCount = if (map.containsKey(element)) map[element]!! + 1 else 1
      map + (element to elementCount)
    }
    return first.fold(0) { count, element ->
      count + (element * (secondCounts[element] ?: 0))
    }
  }
}
