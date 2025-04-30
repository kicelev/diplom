package com.example.libraryndarray

class Slice private constructor(
    val indices: IntArray
) {
    companion object {
        operator fun get(vararg indices: Int): Slice {
            require(indices.all { it >= 0 }) { "Negative indices are not allowed" }
            return Slice(indices)
        }

        operator fun get(range: IntRange): Slice {
            require(range.first >= 0 && range.last >= 0) { "Negative indices are not allowed" }
            return Slice(intArrayOf(range.first, range.last))
        }
    }

    fun resolveIndices(dimSize: Int): IntArray {
        return when (indices.size) {
            1 -> {
                val index = indices[0].coerceAtMost(dimSize - 1)
                intArrayOf(index)
            }
            else -> {
                val start = indices[0].coerceAtLeast(0)
                val end = indices[1].coerceAtMost(dimSize - 1)
                (start..end).toList().toIntArray()
            }
        }.also {
            require(it.isNotEmpty()) { "Empty slice is not allowed" }
        }
    }
}