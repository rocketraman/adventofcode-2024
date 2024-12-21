package day19

import Day
import Example
import Lines

class Day19 : Day() {
  companion object: Examples {
    override val part1Examples: List<Example> = listOf(
      """
      r, wr, b, g, bwu, rb, gb, br
      
      brwrr
      bggr
      gbbr
      rrbgbr
      ubwu
      bwurrg
      brgr
      bbrgwb
      """ to 6,
    )

    override val part2Examples: List<Example> = listOf(
      """
      r, wr, b, g, bwu, rb, gb, br
      
      brwrr
      """ to 2L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      bggr
      """ to 1L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      gbbr
      """ to 4L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      br
      """ to 2L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      rrbgbr
      """ to 6L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      ubwu
      """ to 0L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      bwurrg
      """ to 1L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      brgr
      """ to 2L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      bbrgwb
      """ to 0L,

      """
      r, wr, b, g, bwu, rb, gb, br
      
      brwrr
      bggr
      gbbr
      rrbgbr
      ubwu
      bwurrg
      brgr
      bbrgwb
      """ to 16L,
    )
  }

  private val memoizedPatternCombinations = mutableMapOf<String, Long>()

  override fun part1(input: Lines): Any {
    val (patterns, designs) = parse(input)
    val matcher = matchRegex(patterns)
    return designs.count { matcher.matches(it) }
  }

  override fun part2(input: Lines): Any {
    val (patterns, designs) = parse(input)
    val matcher = matchRegex(patterns)
    return designs.filter { matcher.matches(it) }.sumOf { d ->
      recursiveMatch(d, patterns, matcher)
    }
  }

  private fun parse(input: Lines): Pair<List<String>, List<String>> {
    val patterns = input.first().split(", ")
    val designs = input.drop(2)
    return Pair(patterns, designs)
  }

  private fun matchRegex(patterns: List<String>): Regex = patterns.joinToString("|", prefix = "(", postfix = ")*").toRegex()

  private fun recursiveMatch(s: String, patterns: List<String>, matcher: Regex, count: Long = 0L): Long {
    val matchingPatterns = patterns.filter { s.startsWith(it) }
    val remaining = matchingPatterns.map { s.substringAfter(it) }
    return memoizedPatternCombinations.getOrPut(s) {
      if (matchingPatterns.isEmpty()) {
        0
      } else if (remaining.isEmpty()) {
        count + 1
      } else {
        remaining
          .count { it.isEmpty() } +
          remaining
            .filter { matcher.matches(it) }
            .sumOf { recursiveMatch(it, patterns, matcher, count) }
      }
    }
  }
}
