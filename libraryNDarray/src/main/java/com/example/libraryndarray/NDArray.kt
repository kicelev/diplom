package com.example.libraryndarray

import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.reflect.KClass

class NDArray<T : Any> internal constructor(
    private var array: Array<T>,
    private var shape: IntArray,
    private val clazz: KClass<T>
) : Sliceable<T> {
    companion object {
        private fun IntArray.product(): Int = this.fold(1, Int::times)

        fun <T : Any> create(array: Array<T>, shape: IntArray, clazz: KClass<T>): NDArray<T> {
            require(shape.product() == array.size) {
                "Shape product ${shape.product()} doesn't match array size ${array.size}"
            }
            return NDArray(array, shape, clazz)
        }
    }

    var storageOrder: Order = Order.ROW_MAJOR
        private set

    fun setElement(indexes: IntArray, value: T): Boolean {
        if(indexes.size!=shape.size) return false

        indexes.forEachIndexed {i, index ->
            if (index !in 0 until shape[i]) return false
        }

        array[transformationIndex(indexes)] = value
        return true
    }

    fun getElement(indexes: IntArray): T{
        require(indexes.size == shape.size) {"Invalid indices size"}
        indexes.forEachIndexed{i, index ->
            require(index in 0 until shape[i]) {"Index $index out of bounds for dimension $i"}
        }

        return  array[transformationIndex(indexes)]
    }

    private fun transformationIndex(indexes: IntArray): Int {
        return when (storageOrder) {
            Order.ROW_MAJOR -> {
                var index = 0
                var step = 1
                for (i in indexes.indices.reversed()) {
                    index += indexes[i] * step
                    step *= shape[i]
                }
                index
            }
            Order.COLUMN_MAJOR -> {
                var index = 0
                var step = 1
                for (i in indexes.indices) {
                    index += indexes[i] * step
                    step *= shape[i]
                }
                index
            }
        }
    }

    fun reshapeInPlace(newShape: IntArray, newStorageOrder: Order? = null) {
        require(shape.product() == newShape.product()) {
            "Несовместимые формы: ${shape.joinToString()} -> ${newShape.joinToString()}"
        }

        if (newStorageOrder != null && newStorageOrder != storageOrder) {
            reorderData(newShape, newStorageOrder)
        }

        shape = newShape.copyOf()
        newStorageOrder?.let { storageOrder = it }
    }

    private fun linearToMultiIndex(linearIndex: Int, shape: IntArray, order: Order): IntArray {
        val indices = IntArray(shape.size)
        var remaining = linearIndex

        when (order) {
            Order.ROW_MAJOR -> {
                for (i in shape.indices.reversed()) {
                    indices[i] = remaining % shape[i]
                    remaining /= shape[i]
                }
            }
            Order.COLUMN_MAJOR -> {
                for (i in shape.indices) {
                    indices[i] = remaining % shape[i]
                    remaining /= shape[i]
                }
            }
        }

        return indices
    }

    private fun multiToLinearIndex(indices: IntArray, shape: IntArray, order: Order): Int {
        return when (order) {
            Order.ROW_MAJOR -> {
                var index = 0
                var stride = 1
                for (i in indices.indices.reversed()) {
                    index += indices[i] * stride
                    stride *= shape[i]
                }
                index
            }
            Order.COLUMN_MAJOR -> {
                var index = 0
                var stride = 1
                for (i in 0 until indices.size) {
                    index += indices[i] * stride
                    stride *= shape[i]
                }
                index
            }
        }
    }

    private fun reorderData(newShape: IntArray, newOrder: Order) {
        require(shape.size == newShape.size) {
            "Размерности старой и новой формы должны совпадать"
        }

        val oldOrder = storageOrder
        val oldShape = shape.copyOf()

        @Suppress("UNCHECKED_CAST")
        val newArray = java.lang.reflect.Array.newInstance(
            array.javaClass.componentType,
            array.size
        ) as Array<T>

        // Автоматически определяем перестановку измерений
        val permutation = findPermutation(oldShape, newShape)

        for (i in array.indices) {
            val multiIndexOld = linearToMultiIndex(i, oldShape, oldOrder)
            val permutedIndex = permuteIndices(multiIndexOld, permutation)
            val newLinearIndex = multiToLinearIndex(permutedIndex, newShape, newOrder)
            newArray[newLinearIndex] = array[i]
        }

        array = newArray
    }

    private fun findPermutation(oldShape: IntArray, newShape: IntArray): IntArray {
        val permutation = IntArray(oldShape.size)
        val used = BooleanArray(newShape.size)

        for (i in oldShape.indices) {
            for (j in newShape.indices) {
                if (!used[j] && oldShape[i] == newShape[j]) {
                    permutation[i] = j
                    used[j] = true
                    break
                }
            }
        }

        return permutation
    }


    private fun permuteIndices(indices: IntArray, permutation: IntArray): IntArray {
        require(indices.size == permutation.size) {
            "Размеры массивов indices (${indices.size}) и permutation (${permutation.size}) должны совпадать"
        }

        val sortedPermutation = permutation.sorted()
        require(sortedPermutation == IntArray(indices.size) { it }.toList()) {
            "Перестановка должна содержать все индексы от 0 до ${indices.size - 1} без повторений"
        }

        return IntArray(indices.size) { i ->
            val newPos = permutation[i]
            require(newPos in indices.indices) {
                "Индекс $newPos выходит за границы массива indices"
            }
            indices[newPos]
        }
    }



    fun broadcast(newShape: IntArray): NDArray<T> {
        if (!canBroadcast(newShape)) {
            throw IllegalArgumentException("Cannot broadcast shape ${shape.joinToString()} to ${newShape.joinToString()}")
        }

        val newArray = java.lang.reflect.Array.newInstance(clazz.java, newShape.product()) as Array<T>

        for (i in newArray.indices) {
            val originalIndices = computeIndicesForBroadcast(newShape, i)
            newArray[i] = getElement(originalIndices)
        }

        return NDArray(newArray, newShape, clazz)
    }


    private fun canBroadcast(newShape: IntArray): Boolean {
        val paddedShape = padShape(shape, newShape.size)
        val paddedNewShape = padShape(newShape, newShape.size)

        return paddedShape.zip(paddedNewShape).all { (oldDim, newDim) ->
            oldDim == 1 || oldDim == newDim || newDim == 1
        }
    }

    private fun padShape(shape: IntArray, targetRank: Int): IntArray {
        if (shape.size >= targetRank) return shape
        return IntArray(targetRank - shape.size) { 1 } + shape
    }

    private fun computeIndicesForBroadcast(newShape: IntArray, linearIndex: Int): IntArray {
        val originalRank = shape.size
        val newRank = newShape.size
        val paddedShape = padShape(shape, newRank)

        val newIndices = IntArray(newRank)
        var remaining = linearIndex

        for (i in newRank - 1 downTo 0) {
            newIndices[i] = remaining % newShape[i]
            remaining /= newShape[i]
        }

        return IntArray(originalRank) { i ->
            val dim = newRank - originalRank + i
            if (paddedShape[dim] == 1) 0 else newIndices[dim]
        }
    }

    fun transpose(): NDArray<T> {
        if (shape.size < 2) {
            return NDArray.create(array.copyOf(), shape.copyOf(), clazz).apply {
                storageOrder = this.storageOrder
            }
        }

        val newShape = shape.reversedArray()
        val newArray = java.lang.reflect.Array.newInstance(clazz.java, array.size) as Array<T>

        for (i in array.indices) {
            val oldIndices = linearToMultiIndex(i, shape, storageOrder)
            val newIndices = oldIndices.reversedArray()
            val newLinearIndex = multiToLinearIndex(newIndices, newShape, storageOrder) // Не меняем порядок хранения!
            newArray[newLinearIndex] = array[i]
        }

        return NDArray.create(newArray, newShape, clazz).apply {
            storageOrder = this.storageOrder
        }
    }

    fun median(axis: Int? = null): Any {
        return if (axis == null) {
            if (array.isEmpty()) Double.NaN else {
                val sorted = array.mapToDoubleArray().sortedArray()
                getMedianFromSorted(sorted)
            }
        } else {
            require(axis in shape.indices) { "Axis $axis is out of bounds" }
            val result = computeAlongAxisForMedian(axis) { values ->
                values.sort()
                getMedianFromSorted(values)
            }

            // Возвращаем плоский массив значений вместо NDArray
            result.array.contentToString()
        }
    }

    private inline fun <T> Array<T>.mapToDoubleArray(transform: (T) -> Double = { (it as Number).toDouble() }): DoubleArray {
        return DoubleArray(size) { transform(this[it]) }
    }

    private fun getMedianFromSorted(sorted: DoubleArray): Double {
        return when {
            sorted.isEmpty() -> Double.NaN
            sorted.size % 2 == 1 -> sorted[sorted.size / 2]
            else -> (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        }
    }

    private fun computeAlongAxisForMedian(axis: Int, block: (DoubleArray) -> Double): NDArray<Double> {
        val newShape = shape.removeAxis(axis)
        val resultSize = newShape.product()
        val result = DoubleArray(resultSize)
        val step = shape.drop(axis + 1).fold(1) { acc, i -> acc * i }
        val steps = shape[axis]

        for (i in 0 until resultSize) {
            val values = DoubleArray(steps) { j ->
                val index = calculateIndex(i, j, steps, step, axis)
                if (index >= array.size) {
                    throw IllegalStateException("Index $index out of bounds for array size ${array.size}")
                }
                (array[index] as? Number)?.toDouble() ?: throw IllegalArgumentException(
                    "Element ${array[index]} (${array[index]::class}) cannot be converted to Double"
                )
            }
            result[i] = block(values)
        }

        return NDArrayFactory.create(result.toTypedArray(), newShape)
    }

    private fun calculateIndex(i: Int, j: Int, steps: Int, step: Int, axis: Int): Int {
        val remainingDimProduct = shape.drop(axis + 1).fold(1) { acc, dim -> acc * dim }
        return (i / remainingDimProduct) * step * steps +
                j * step +
                (i % remainingDimProduct)
    }

    fun mean(axis: Int? = null): Any {
        return if (axis == null) {
            if (array.isEmpty()) Double.NaN
            else array.sumOf { it.toDouble() } / array.size
        } else {
            require(axis in shape.indices) { "Axis $axis is out of bounds" }
            val result = computeAlongAxis(axis) { values ->
                if (values.isEmpty()) Double.NaN else values.average()
            }

            result.contentToString()
        }
    }

    fun std(axis: Int? = null): Any {
        return if (axis == null) {
            if (array.isEmpty()) Double.NaN else {
                val m = mean() as Double
                sqrt(array.sumOf { (it.toDouble() - m).pow(2) } / array.size)
            }
        } else {
            require(axis in shape.indices) { "Axis $axis is out of bounds" }
            val result = computeAlongAxis(axis) { values ->
                if (values.isEmpty()) Double.NaN else {
                    val m = values.average()
                    sqrt(values.sumOf { (it - m).pow(2) } / values.size)
                }
            }
            result.contentToString()
        }
    }

    private inline fun computeAlongAxis(axis: Int, block: (DoubleArray) -> Double): DoubleArray {
        val newShape = shape.removeAxis(axis)
        val resultSize = newShape.product()
        val result = DoubleArray(resultSize)

        val outerDims = shape.take(axis).product()
        val innerDims = shape.drop(axis + 1).product()
        val axisSize = shape[axis]

        for (outer in 0 until outerDims) {
            for (inner in 0 until innerDims) {
                val values = DoubleArray(axisSize) { k ->
                    val index = outer * axisSize * innerDims + k * innerDims + inner
                    if (index >= array.size) {
                        throw IllegalStateException(
                            "Index calculation error: index=$index, size=${array.size}, " +
                                    "shape=${shape.contentToString()}, axis=$axis"
                        )
                    }
                    array[index].toDouble()
                }
                result[outer * innerDims + inner] = block(values)
            }
        }

        return result
    }

    private fun IntArray.take(n: Int): List<Int> = this.slice(0 until n)
    private fun IntArray.drop(n: Int): List<Int> = this.slice(n until size)
    private fun List<Int>.product(): Int = this.fold(1) { acc, i -> acc * i }

    private fun IntArray.removeAxis(axis: Int): IntArray {
        return IntArray(size - 1) { i ->
            if (i < axis) this[i] else this[i + 1]
        }
    }

    private fun Any.toDouble(): Double = when (this) {
        is Double -> this
        is Int -> this.toDouble()
        is Float -> this.toDouble()
        else -> throw IllegalArgumentException("Unsupported number type: ${this::class}")
    }

    override fun slice(vararg slices: Slice): NDArray<T> {
        require(slices.size <= shape.size) {
            "Number of slices (${slices.size}) exceeds array dimensions (${shape.size})"
        }

        val (sliceIndices, newShape) = processSlices(slices)
        val newSize = newShape.product()

        // Создаем новый массив правильного типа
        @Suppress("UNCHECKED_CAST")
        val newData = java.lang.reflect.Array.newInstance(
            array.javaClass.componentType,
            newSize
        ) as Array<T>

        // Заполняем массив данными
        fillSlicedData(sliceIndices, newData, newShape)

        return NDArray.create(newData, newShape, clazz)
    }

    private fun fillSlicedData(
        sliceIndices: Array<IntArray>,
        newData: Array<T>,
        newShape: IntArray
    ) {
        val originalStrides = calculateStrides(shape)
        val newStrides = calculateStrides(newShape)

        val indices = IntArray(shape.size)
        var newIndex = 0

        fun traverse(dim: Int, offset: Int) {
            if (dim == shape.size) {
                newData[newIndex++] = array[offset]
                return
            }

            val currentIndices = if (dim < sliceIndices.size) sliceIndices[dim] else (0 until shape[dim]).toList().toIntArray()

            for (i in currentIndices) {
                indices[dim] = i
                traverse(
                    dim + 1,
                    offset + i * originalStrides[dim]
                )
            }
        }

        traverse(0, 0)
    }

    private fun calculateStrides(shape: IntArray): IntArray {
        val strides = IntArray(shape.size)
        var stride = 1
        for (i in shape.size - 1 downTo 0) {
            strides[i] = stride
            stride *= shape[i]
        }
        return strides
    }

    private fun processSlices(slices: Array<out Slice>): Pair<Array<IntArray>, IntArray> {
        val newShape = mutableListOf<Int>()
        val sliceArrays = Array(slices.size) { i ->
            val resolved = slices[i].resolveIndices(shape[i])
            if (resolved.size > 1 || slices[i].indices.size > 1) {
                newShape.add(resolved.size)
            }
            resolved
        }

        // Добавляем оставшиеся размерности
        for (i in slices.size until shape.size) {
            newShape.add(shape[i])
        }

        return Pair(sliceArrays, newShape.toIntArray())
    }

    inline fun <reified R : Any> map(transform: (T) -> R): NDArray<R> {
        val data = getData()
        val newArray = Array(data.size) { i -> transform(data[i]) }
        return NDArray.create(newArray, getShape(), R::class)
    }

    fun reduce(operation: (T, T) -> T, initial: T): T {
        var accumulator = initial
        for (element in array) {
            accumulator = operation(accumulator, element)
        }
        return accumulator
    }

    fun unaryOp(op: (T) -> T): NDArray<T> {
        val data = getData()
        val arrayType = data.javaClass.componentType
        val newArray = java.lang.reflect.Array.newInstance(arrayType, data.size) as Array<T>

        for (i in data.indices) {
            newArray[i] = op(data[i])
        }

        return NDArray.create(newArray, getShape().copyOf(), getClazz())
    }

    fun binaryOp(other: NDArray<T>, op: (T, T) -> T): NDArray<T> {
        require(getShape().contentEquals(other.getShape())) {
            "Shape mismatch: ${getShape().contentToString()} != ${other.getShape().contentToString()}"
        }

        val thisData = getData()
        val arrayType = thisData.javaClass.componentType
        val newArray = java.lang.reflect.Array.newInstance(arrayType, thisData.size) as Array<T>

        for (i in thisData.indices) {
            newArray[i] = op(thisData[i], other.getData()[i])
        }

        return NDArray.create(newArray, getShape().copyOf(), getClazz())
    }

    override fun toString(): String {
        return buildString {
            append("NDArray(")
            append("shape=${shape.contentToString()}, ")
            append("order=$storageOrder, ")
            append("data=")
            append(formatArray(array, shape))
            append(")")
        }
    }

    private fun formatArray(array: Array<*>, shape: IntArray, offset: Int = 0, dimension: Int = 0): String {
        if (array.isEmpty()) return "[]"
        if (shape.isEmpty()) return array.contentToString()

        // Проверка на выход за границы массива
        if (offset >= array.size) return "[]"

        return buildString {
            if (dimension == shape.size - 1) {
                // Последняя размерность - выводим элементы
                val end = (offset + shape[dimension]).coerceAtMost(array.size)
                val elements = array.sliceArray(offset until end)
                append(elements.contentToString())
            } else {
                // Рекурсивная обработка подмассивов
                val subArraySize = shape.copyOfRange(dimension + 1, shape.size).product()
                append("[")

                for (i in 0 until shape[dimension]) {
                    if (i != 0) append(", ")

                    val elementOffset = when (storageOrder) {
                        Order.ROW_MAJOR -> offset + i * subArraySize
                        Order.COLUMN_MAJOR -> {
                            var stride = 1
                            for (j in dimension + 1 until shape.size) {
                                stride *= shape[j]
                            }
                            offset + i * stride
                        }
                    }

                    // Проверка на валидность offset перед рекурсивным вызовом
                    if (elementOffset >= array.size) {
                        append("[]")
                    } else {
                        append(formatArray(array, shape, elementOffset, dimension + 1))
                    }
                }
                append("]")
            }
        }
    }

    fun getShape(): IntArray = shape.copyOf()
    fun getData(): Array<T> = array.copyOf()
    fun getClazz(): KClass<T> = clazz
}