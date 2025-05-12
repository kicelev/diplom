package com.example.libraryndarray

class ArithmeticNDArray<T : Number>(
    private val ndArray: NDArray<T>
): Operable<T>{
    init {
        when (ndArray.getClazz()) {
            Double::class, Float::class, Int::class, Long::class -> {}
            else -> throw IllegalArgumentException("Only Double, Float, Int and Long types are supported")
        }
    }

    override operator fun plus(other: ArithmeticNDArray<T>): NDArray<T> = elementWiseOperation(other) { a, b -> a + b }
    override operator fun minus(other: ArithmeticNDArray<T>): NDArray<T> = elementWiseOperation(other) { a, b -> a - b }
    override operator fun times(other: ArithmeticNDArray<T>): NDArray<T> = elementWiseOperation(other) { a, b -> a * b }
    override operator fun div(other: ArithmeticNDArray<T>): NDArray<T> = elementWiseOperation(other) { a, b ->
        require(b != 0.0) { "Division by zero" }
        a / b
    }

    private inline fun elementWiseOperation(
        other: ArithmeticNDArray<T>,
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
        other: ArithmeticNDArray<T>,
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
        other: ArithmeticNDArray<T>,
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
        other: ArithmeticNDArray<T>,
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
        other: ArithmeticNDArray<T>,
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