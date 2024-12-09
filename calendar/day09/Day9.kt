package day09

import Day
import Lines
import java.math.BigInteger

class Day9 : Day() {
  sealed class Block {
    abstract val length: Int
    data class File(val id: Int, override val length: Int): Block()
    data class Free(override val length: Int): Block()
  }

  data class Disk(
    val blocks: List<Block> = emptyList(),
    val size: Int,
  ) {
    data class DiskSegments(
      val compacted: List<Block.File>,
      val free: Block.Free,
      val fragmented: List<Block>,
      val freeEnd: Block.Free,
    )

    data class DiskSegments2(
      val start: List<Block>,
      val free: Block.Free,
      val endBeforeBlock: List<Block>,
      val endAfterBlock: List<Block>,
    )

    fun lastFileOrNull(): Block.File? =
      blocks.filterIsInstance<Block.File>().lastOrNull()

    fun lastFileId(): Int =
      lastFileOrNull()?.id ?: -1

    fun withFile(length: Int): Disk =
      copy(blocks = blocks + Block.File(lastFileId() + 1, length))

    fun withFreeSpace(length: Int): Disk =
      copy(blocks = blocks + Block.Free(length))

    fun checksum(): BigInteger = blocks.fold(0L to BigInteger.ZERO) { (position, sum), block ->
      (position + block.length) to when (block) {
        is Block.Free -> sum
        is Block.File -> {
          val fileChecksum = (position..<(position + block.length)).sumOf { it * block.id }
          sum + fileChecksum.toBigInteger()
        }
      }
    }.second

    fun segmentsOnFirstFree(): DiskSegments {
      val indexOfFirstFree = blocks.indexOfFirst { it is Block.Free }
      val compacted = blocks.filterIsInstance<Block.File>().take(indexOfFirstFree)
      val free = blocks[indexOfFirstFree] as Block.Free
      val fragmented = blocks.drop(indexOfFirstFree + 1).dropLastWhile { it is Block.Free }
      val freeEnd = Block.Free(size - (compacted + free + fragmented).size())
      return DiskSegments(
        compacted = compacted,
        free = free,
        fragmented = fragmented,
        freeEnd = freeEnd
      )
    }

    fun segmentsOnFirstFreeOfBlock(block: Block.File): DiskSegments2? {
      val indexOfFirstFree = blocks.indexOfFirst { it is Block.Free && it.length >= block.length }
      if (indexOfFirstFree == -1) return null

      val start = blocks.take(indexOfFirstFree)
      val free = blocks[indexOfFirstFree] as Block.Free
      val end = blocks.drop(indexOfFirstFree + 1)

      val indexOfBlock = end.indexOf(block)
      if (indexOfBlock == -1) return null

      val endBeforeBlock = end.take(indexOfBlock)
      val endAfterBlock = end.drop(indexOfBlock + 1)

      return DiskSegments2(
        start = start,
        free = free,
        endBeforeBlock = endBeforeBlock,
        endAfterBlock = endAfterBlock
      )
    }

    fun List<Block>.size() = sumOf { block -> block.length }

    fun simplify(): Disk = copy(blocks = blocks.simplify())

    fun List<Block>.simplify(): List<Block> = filterNot { it is Block.Free && it.length == 0 }
      .fold(emptyList()) { acc, b ->
        val l = acc.lastOrNull()
        when {
          l is Block.Free && b is Block.Free -> acc.dropLast(1) + Block.Free(l.length + b.length)
          l is Block.File && b is Block.File && l.id == b.id -> acc.dropLast(1) + Block.File(l.id, l.length + b.length)
          else -> acc + b
        }
      }

    fun print(): String = buildString {
      blocks.map {
        when (it) {
          is Block.File -> appendLine("File ${it.id} (${it.length})")
          is Block.Free -> appendLine("Free (${it.length})")
        }
      }
    }

    fun simplePrint(): String = buildString {
      blocks.map { b ->
        repeat(b.length) {
          when (b) {
            is Block.File -> print(b.id)
            is Block.Free -> print(".")
          }
        }
      }
    }
  }

  private fun Disk.compact(): Disk {
    var disk = this
    while (true) {
      val (compacted, free, fragmented, freeEnd) = disk.segmentsOnFirstFree()
      if (fragmented.isEmpty()) return disk
      val lastFragmentedFile = fragmented.filterIsInstance<Block.File>().lastOrNull() ?: return disk

      val updatedBlocks = if (fragmented.none { it is Block.Free }) {
        compacted +
          fragmented +
          freeEnd.copy(length = freeEnd.length + free.length)
      } else when {
        lastFragmentedFile.length == free.length -> {
          compacted +
            lastFragmentedFile +
            fragmented.dropLast(1) +
            freeEnd
        }

        lastFragmentedFile.length < free.length -> {
          compacted +
            lastFragmentedFile +
            Block.Free(free.length - lastFragmentedFile.length) +
            fragmented.dropLast(1) +
            freeEnd
        }

        else -> {
          compacted +
            lastFragmentedFile.copy(length = free.length) +
            fragmented.dropLast(1) +
            lastFragmentedFile.copy(length = lastFragmentedFile.length - free.length) +
            freeEnd
        }
      }
      disk = disk.copy(blocks = updatedBlocks)
    }
  }

  private fun Disk.compactContiguousFiles(): Disk {
    var disk = this
    disk.blocks.filterIsInstance<Block.File>().asReversed().forEach { block ->
      val segments = disk.segmentsOnFirstFreeOfBlock(block) ?: return@forEach

      val (start, free, endBeforeBlock, endAfterBlock) = segments

      val updatedEnd = (
        endBeforeBlock.takeLastWhile { it is Block.Free } +
          Block.Free(block.length) +
          endAfterBlock.takeWhile { it is Block.Free }
        ).simplify()

      val updatedBlocks = start +
        block +
        Block.Free(free.length - block.length) +
        endBeforeBlock.dropLastWhile { it is Block.Free } +
        updatedEnd +
        endAfterBlock.dropWhile { it is Block.Free }

      disk = disk.copy(blocks = updatedBlocks)
    }
    return disk
  }

  private fun parseDisk(input: Lines): Disk {
    val size = input[0].map { it.digitToInt() }.sum()
    val disk = input[0].fold(Disk(emptyList(), size)) { d, c ->
      when (d.blocks.lastOrNull()) {
        null, is Block.Free -> d.withFile(c.digitToInt())
        is Block.File -> d.withFreeSpace(c.digitToInt())
      }
    }.simplify()
    return disk
  }

  override fun part1(input: Lines): Any = parseDisk(input).compact().checksum()

  override fun part2(input: Lines): Any = parseDisk(input).compactContiguousFiles().checksum()
}
