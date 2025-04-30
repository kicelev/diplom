package com.example.libraryndarray

interface Sliceable<T : Any> {

    fun slice(vararg slices: Slice): NDArray<T>

}