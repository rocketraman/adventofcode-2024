package day05

import Day
import Lines

class Day5 : Day() {
  override fun part1(input: Lines): Any {
    val orderingRules = getOrderingRules(input)
    val updates = getUpdates(input)
    return updates.filter { it.isValid(orderingRules) }
      .sumOfMiddle()
  }

  override fun part2(input: Lines): Any {
    val orderingRules = getOrderingRules(input)
    val updates = getUpdates(input)
    return updates.filterNot { it.isValid(orderingRules) }
      .map { it.reorder(orderingRules) }
      .sumOfMiddle()
  }

  private fun getUpdates(input: Lines) =
    input.dropWhile { it.isNotBlank() }.drop(1).fold(emptyList<List<Int>>()) { acc, line ->
      acc.plusElement(line.split(",").map(String::toInt))
    }

  private fun getOrderingRules(input: Lines) = input.takeWhile { it.isNotBlank() }.fold(emptyMap<Int, Set<Int>>()) { acc, line ->
    val (p1, p2) = line.split("|").map(String::toInt)
    val p1Rules = acc.getOrElse(p1) { emptySet() }
    acc + (p1 to (p1Rules + p2))
  }

  private fun List<Int>.isValid(orderingRules: Map<Int, Set<Int>>): Boolean {
    forEachIndexed { i, p ->
      val rule = orderingRules[p] ?: emptySet()
      drop(i + 1).forEach { p2 ->
        if (!rule.contains(p2)) {
          return false
        }
      }
    }
    return true
  }

  private fun List<Int>.reorder(orderingRules: Map<Int, Set<Int>>): List<Int> {
    forEachIndexed { i, p ->
      val rule = orderingRules[p] ?: emptySet()
      drop(i + 1).forEachIndexed { j, p2 ->
        if (!rule.contains(p2)) {
          val updated = toMutableList().apply {
            // remove the p2 page that has to be before the p page
            removeAt(i + j + 1)
            // add it back at the current position of p
            add(i, p2)
          }
          // could refactor to do it in one pass, but for simplicity
          // recursively call this if we're still not valid, we'll fix one element at a time until valid
          return if (updated.isValid(orderingRules)) updated else updated.reorder(orderingRules)
        }
      }
    }
    return this
  }

  private fun List<List<Int>>.sumOfMiddle() = sumOf { it[(it.size + 1) / 2 - 1] }
}
