package com.example.libraryndarray

import kotlin.reflect.KClass

class NDArrayFactory {
    companion object {
        fun <T : Any> create(
            data: Array<T>,
            shape: IntArray,
            clazz: KClass<T>
        ): NDArray<T> {
            return NDArray.create(data, shape, clazz)
        }

        inline fun <reified T : Any> create(
            data: Array<T>,
            shape: IntArray
        ): NDArray<T> {
            return create(data, shape, T::class)
        }

        inline fun <reified T : Any> create(data: Array<T>): NDArray<T> {
            return create(data, intArrayOf(data.size))
        }

        fun validateShape(shape: IntArray, dataSize: Int) {
            require(shape.product() == dataSize) {
                "Shape ${shape.joinToString()} is incompatible with data size $dataSize"
            }
        }

        private fun IntArray.product(): Int = fold(1, Int::times)
    }
}