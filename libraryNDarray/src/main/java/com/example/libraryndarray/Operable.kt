package com.example.libraryndarray

interface Operable<T : Number> {
    operator fun plus(other: ArithmeticNDArray<T>): NDArray<T>
    operator fun minus(other: ArithmeticNDArray<T>): NDArray<T>
    operator fun times(other: ArithmeticNDArray<T>): NDArray<T>
    operator fun div(other: ArithmeticNDArray<T>): NDArray<T>
}