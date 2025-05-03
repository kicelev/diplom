package com.example.libraryndarray

class NDArrayMath<T : Number>(
    private val ndArray: NDArray<T>
) {
    init {
        when (ndArray.getClazz()) {
            Double::class, Float::class, Int::class, Long::class -> {}
            else -> throw IllegalArgumentException("Only Double, Float, Int and Long types are supported")
        }
    }

    operator fun plus(other: NDArrayMath<T>): NDArray<T> = elementWiseOperation(other) { a, b -> a + b }
    operator fun minus(other: NDArrayMath<T>): NDArray<T> = elementWiseOperation(other) { a, b -> a - b }
    operator fun times(other: NDArrayMath<T>): NDArray<T> = elementWiseOperation(other) { a, b -> a * b }
    operator fun div(other: NDArrayMath<T>): NDArray<T> = elementWiseOperation(other) { a, b ->
        require(b != 0.0) { "Division by zero" }
        a / b
    }

    private inline fun elementWiseOperation(
        other: NDArrayMath<T>,
        noinline operation: (Double, Double) -> Double
    ): NDArray<T> {
        require(ndArray.getShape().contentEquals(other.ndArray.getShape())) {
            "Shape mismatch: ${ndArray.getShape().contentToString()} != ${other.ndArray.getShape().contentToString()}"
        }

        return when (val clazz = ndArray.getClazz()) {
            Double::class -> createDoubleResult(other, operation)
            Float::class -> createFloatResult(other, operation)
            Int::class -> createIntResult(other, operation)
            Long::class -> createLongResult(other, operation)
            else -> throw UnsupportedOperationException()
        }
    }

    private fun createDoubleResult(
        other: NDArrayMath<T>,
        operation: (Double, Double) -> Double
    ): NDArray<T> {
        val result = Array(ndArray.getData().size) { i ->
            operation(
                ndArray.getData()[i].toDouble(),
                other.ndArray.getData()[i].toDouble()
            )
        }
        @Suppress("UNCHECKED_CAST")
        return NDArray.create(result as Array<T>, ndArray.getShape(), ndArray.getClazz())
    }

    private fun createFloatResult(
        other: NDArrayMath<T>,
        operation: (Double, Double) -> Double
    ): NDArray<T> {
        val result = Array(ndArray.getData().size) { i ->
            operation(
                ndArray.getData()[i].toDouble(),
                other.ndArray.getData()[i].toDouble()
            ).toFloat()
        }
        @Suppress("UNCHECKED_CAST")
        return NDArray.create(result as Array<T>, ndArray.getShape(), ndArray.getClazz())
    }

    private fun createIntResult(
        other: NDArrayMath<T>,
        operation: (Double, Double) -> Double
    ): NDArray<T> {
        val result = Array(ndArray.getData().size) { i ->
            operation(
                ndArray.getData()[i].toDouble(),
                other.ndArray.getData()[i].toDouble()
            ).toInt()
        }
        @Suppress("UNCHECKED_CAST")
        return NDArray.create(result as Array<T>, ndArray.getShape(), ndArray.getClazz())
    }

    private fun createLongResult(
        other: NDArrayMath<T>,
        operation: (Double, Double) -> Double
    ): NDArray<T> {
        val result = Array(ndArray.getData().size) { i ->
            operation(
                ndArray.getData()[i].toDouble(),
                other.ndArray.getData()[i].toDouble()
            ).toLong()
        }
        @Suppress("UNCHECKED_CAST")
        return NDArray.create(result as Array<T>, ndArray.getShape(), ndArray.getClazz())
    }
}