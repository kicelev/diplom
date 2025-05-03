package com.example.librarykotlin
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.libraryndarray.NDArray
import  com.example.libraryndarray.NDArrayFactory
import com.example.libraryndarray.NDArrayMath
import com.example.libraryndarray.Order
import com.example.libraryndarray.Slice

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test() // Вызываем ваш метод
    }

    fun test() {
//Примеры с методами create в фабрике
//        val intData1 = arrayOf(1, 2, 3, 4, 5, 6)
//        val intShape1 = intArrayOf(2, 3)
//        val ndArray1 = NDArrayFactory.create(intData1, intShape1, Int::class)
//        println("Создан NDArray с явным указанием класса: $ndArray1")
//        println("----------------------")
//
//        val doubleData = arrayOf(1.0, 2.0, 3.0, 4.0)
//        val doubleShape = intArrayOf(2, 2)
//        val ndArray2 = NDArrayFactory.create(doubleData, doubleShape)
//        println("Создан NDArray с reified типом: $ndArray2")
//        println("----------------------")
//
//        val stringData = arrayOf("a", "b", "c")
//        val ndArray3 = NDArrayFactory.create(stringData)
//        println("Создан одномерный NDArray: $ndArray3")
//        println("----------------------")

//Примеры с методоми setElement и getElement
//        val data = arrayOf(10, 20, 30, 40, 50, 60)
//        val ndArray = NDArrayFactory.create(data, intArrayOf(2, 3))
//
//        println("Исходный массив:")
//        println(ndArray)
//
//        println("\nПытаемся установить элемент на позиции (1, 2) в значение 99:")
//        val setSuccess = ndArray.setElement(intArrayOf(1, 2), 99)
//        println("Успешно? $setSuccess")  // true
//        println("Массив после изменения:")
//        println(ndArray)
//
//        println("\nПолучаем элемент на позиции (0, 1):")
//        val element = ndArray.getElement(intArrayOf(0, 1))
//        println("Элемент: $element")

//Пример метода reshape
//        val data = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
//
//        val ndArray = NDArrayFactory.create(data, intArrayOf(2, 2, 3))
//        println("Исходный 3D массив:")
//        println(ndArray)
//        println("Элемент [1, 1, 2]: ${ndArray.getElement(intArrayOf(1, 1, 2))}")
//
//        println("\n--- Reshape 2x2x3 -> 3x2x2 (ROW_MAJOR) ---")
//        ndArray.reshapeInPlace(intArrayOf(3, 2, 2))
//        println(ndArray)
//        println("Элемент [2, 1, 1]: ${ndArray.getElement(intArrayOf(2, 1, 1))}")
//
//        println("\n--- Reshape 3x2x2 -> 2x3x2 (COLUMN_MAJOR) ---")
//        ndArray.reshapeInPlace(intArrayOf(2, 3, 2), Order.COLUMN_MAJOR)
//        println(ndArray)
//        println("Элемент [1, 2, 1]: ${ndArray.getElement(intArrayOf(1, 2, 1))}")

//Пример метода broadcast
//        // Пример 1: Базовый broadcasting (расширение массива 3x1 до 3x4)
//        val data1 = arrayOf(1, 2, 3) // Исходная форма [3,1]
//        val ndArray1 = NDArrayFactory.create(data1, intArrayOf(3, 1))
//
//        println("Исходный массив 3x1:")
//        println(ndArray1) // [[1], [2], [3]]
//
//        val broadcasted1 = ndArray1.broadcast(intArrayOf(3, 4))
//        println("\nПосле broadcast в 3x4:")
//        println(broadcasted1)
//        /* Ожидаемый вывод:
//           [[1, 1, 1, 1],
//            [2, 2, 2, 2],
//            [3, 3, 3, 3]]
//        */
//
//        // Пример 2: Broadcasting с несколькими измерениями (1x3x1 -> 2x3x4)
//        val data2 = arrayOf(10, 20, 30) // Форма [1,3,1]
//        val ndArray2 = NDArrayFactory.create(data2, intArrayOf(1, 3, 1))
//
//        println("\nИсходный массив 1x3x1:")
//        println(ndArray2) // [[[10], [20], [30]]]
//
//        val broadcasted2 = ndArray2.broadcast(intArrayOf(2, 3, 4))
//        println("\nПосле broadcast в 2x3x4:")
//        println(broadcasted2)
//        /* Ожидаемый вывод:
//           [
//             [[10,10,10,10], [20,20,20,20], [30,30,30,30]],
//             [[10,10,10,10], [20,20,20,20], [30,30,30,30]]
//           ]
//        */

//        //Пример метода broadcast
//        val matrixData = arrayOf(1, 2, 3, 4, 5, 6)
//        val matrix = NDArrayFactory.create(matrixData, intArrayOf(2, 3))
//        println("Исходная матрица 2x3:")
//        println(matrix) // [[1, 2, 3], [4, 5, 6]]
//        // Транспонируем матрицу
//        val transposedMatrix = matrix.transpose()
//        println("\nТранспонированная матрица 3x2:")
//        println(transposedMatrix) // [[1, 4], [2, 5], [3, 6]]
//        // Проверяем элементы
//        println("\nПроверка элементов:")
//        println("transposed[0,1] = ${transposedMatrix.getElement(intArrayOf(0, 1))}") // 4
//        println("transposed[2,0] = ${transposedMatrix.getElement(intArrayOf(2, 0))}") // 3

//Пример методов median, mean, std
//        val tensorData = arrayOf(
//            1, 2, 3,
//            4, 5, 6,
//            7, 8, 9,
//            10, 11, 12
//        )
//        val tensor = NDArrayFactory.create(tensorData, intArrayOf(2, 2, 3))
//        println("\nGlobal Statistics:")
//        println("Median: ${tensor.median()}")  // 6.5
//        println("Mean: ${tensor.mean()}")      // 6.5
//        println("Std: ${tensor.std()}")        // ~3.452
//
//// По глубине (axis=0)
//        println("\nDepth-wise (axis=0):")
//        println("Medians: ${tensor.median(axis = 0)}") // [[4.0, 5.0, 6.0], [7.0, 8.0, 9.0]]
//        println("Means: ${tensor.mean(axis = 0)}")     // [[4.0, 5.0, 6.0], [7.0, 8.0, 9.0]]
//        println("Std: ${tensor.std(axis = 0)}")        // [[3.0, 3.0, 3.0], [3.0, 3.0, 3.0]]
//
//// По строкам (axis=1)
//        println("\nRow-wise (axis=1):")
//        println("Medians: ${tensor.median(axis = 1)}") // [[2.5, 3.5, 4.5], [8.5, 9.5, 10.5]]
//        println("Means: ${tensor.mean(axis = 1)}")     // [[2.5, 3.5, 4.5], [8.5, 9.5, 10.5]]
//        println("Std: ${tensor.std(axis = 1)}")        // [[1.5, 1.5, 1.5], [1.5, 1.5, 1.5]]

// Пример методов map, reduce, unaryOp, binaryOp
//        val tensorData = arrayOf(
//            1, 2, 3,
//            4, 5, 6,
//            7, 8, 9,
//            10, 11, 12
//        )
//        val tensor = NDArrayFactory.create(tensorData, intArrayOf(2, 2, 3))
//// Увеличим каждый элемент на 10
//        val mapped = tensor.map { it + 10 }
//        println(mapped)
//        /*
//        NDArray(shape=[2,2,3], data=[
//          [[11, 12, 13],
//           [14, 15, 16]],
//          [[17, 18, 19],
//           [20, 21, 22]]
//        ])
//        */
//
//// Преобразуем в строки
//        val stringTensor = tensor.map { "val-$it" }
//        println(stringTensor)
//
//// Сумма всех элементов
//        val sum = tensor.reduce({ acc, x -> acc + x }, 0)
//        println("Total sum: $sum") // 78
//
//// Максимальное значение
//        val max = tensor.reduce({ acc, x -> maxOf(acc, x) }, Int.MIN_VALUE)
//        println("Max value: $max")
//
//// Возведение в квадрат
//        val squared = tensor.unaryOp { it * it }
//        println(squared)
//        /*
//        NDArray(shape=[2,2,3], data=[
//          [[1, 4, 9],
//           [16, 25, 36]],
//          [[49, 64, 81],
//           [100, 121, 144]]
//        ])
//        */
//
//        val otherData = arrayOf(
//            2, 2, 2,
//            2, 2, 2,
//            2, 2, 2,
//            2, 2, 2
//        )
//        val otherTensor = NDArrayFactory.create(otherData, intArrayOf(2, 2, 3))
//
//// Поэлементное умножение
//        val multiplied = tensor.binaryOp(otherTensor) { a, b -> a * b }
//        println(multiplied)
//        /*
//        NDArray(shape=[2,2,3], data=[
//          [[2, 4, 6],
//           [8, 10, 12]],
//          [[14, 16, 18],
//           [20, 22, 24]]
//        ])
//        */

// Пример метода slice
//// Исходный тензор 2x2x3
//        val tensor = NDArrayFactory.create(
//            arrayOf(1,2,3,4,5,6,7,8,9,10,11,12),
//            intArrayOf(2,2,3)
//        )
//
//        val (dim1, dim2, dim3) = tensor.getShape()
//
//// 1. Все матрицы, первая строка в каждой
//        val slice3 = tensor.slice(
//            Slice[0 until tensor.getShape()[0]],  // все по первой оси
//            Slice[0]                             // первая строка
//        )
//        println(slice3)
//        /*
//        NDArray(shape=[2,3], data=[
//          [1, 2, 3],
//          [7, 8, 9]
//        ])
//        */
//
//// 2. Первая матрица, все элементы
//        val slice4 = tensor.slice(
//            Slice[0],                            // первая матрица
//            Slice[0 until tensor.getShape()[1]], // все строки
//            Slice[0 until tensor.getShape()[2]]  // все столбцы
//        )
//        println(slice4)
//        /*
//        NDArray(shape=[2,3], data=[
//          [1, 2, 3],
//          [4, 5, 6]
//        ])
//        */

        val vector1 = NDArrayFactory.create(arrayOf(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0), intArrayOf(2, 2, 2))
        val vector2 = NDArrayFactory.create(arrayOf(1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0), intArrayOf(2, 2, 2))

        val math1 = NDArrayMath(vector1)
        val math2 = NDArrayMath(vector2)

        println("Vector addition:")
        println((math1 + math2).getData().contentToString()) // [5.0, 7.0, 9.0]

        println("\nVector multiplication:")
        println((math1 * math2).getData().contentToString()) // [4.0, 10.0, 18.0]
    }
}