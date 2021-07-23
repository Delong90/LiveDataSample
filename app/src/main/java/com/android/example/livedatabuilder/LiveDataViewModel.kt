/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.livedatabuilder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Showcases different patterns using the liveData coroutines builder.
 */
/**
* Демонстрирует различные шаблоны с помощью конструктора сопрограмм liveData.
*/
class LiveDataViewModel(
    private val dataSource: DataSource
) : ViewModel() {

    // Exposed LiveData from a function that returns a LiveData generated with a liveData builder
    // Отображение LiveData из функции, которая возвращает LiveData, сгенерированную с помощью построителя liveData
    val currentTime = dataSource.getCurrentTime()

    // Coroutines inside a transformation
    // Сопрограммы внутри преобразования
    val currentTimeTransformed = currentTime.switchMap {
        // timeStampToTime is a suspend function so we need to call it from a coroutine.
        // timeStampToTime - это функция приостановки, поэтому нам нужно вызвать ее из сопрограммы.
        liveData { emit(timeStampToTime(it)) }
    }

    // Exposed liveData that emits and single value and subsequent values from another source.
    // Открытые liveData, которые генерируют одно значение и последующие значения из другого источника.
    val currentWeather: LiveData<String> = liveData {
        emit(LOADING_STRING)
        emitSource(dataSource.fetchWeather())
    }

    // Exposed cached value in the data source that can be updated later on
    // Открытое кэшированное значение в источнике данных, которое может быть обновлено позже
    val cachedValue = dataSource.cachedData

    // Called when the user clicks on the "FETCH NEW DATA" button. Updates value in data source.
    // Вызывается, когда пользователь нажимает кнопку «ПОЛУЧИТЬ НОВЫЕ ДАННЫЕ». Обновляет значение в источнике данных.
    fun onRefresh() {
        // Launch a coroutine that reads from a remote data source and updates cache
        // Запускаем сопрограмму, которая читает из удаленного источника данных и обновляет кеш
        viewModelScope.launch {
            dataSource.fetchNewData()
        }
    }

    // Simulates a long-running computation in a background thread
    // Имитирует длительное вычисление в фоновом потоке
    private suspend fun timeStampToTime(timestamp: Long): String {
        delay(500)  // Simulate long operation
        val date = Date(timestamp)
        return date.toString()
    }

    companion object {
        // Real apps would use a wrapper on the result type to handle this.
        // Настоящие приложения будут использовать оболочку для типа результата, чтобы справиться с этим.
        const val LOADING_STRING = "Loading..."
    }
}


/**
 * Factory for [LiveDataViewModel].
 */
/**
* Заводская для [LiveDataViewModel].
*/
object LiveDataVMFactory : ViewModelProvider.Factory {

    private val dataSource = DefaultDataSource(Dispatchers.IO)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LiveDataViewModel(dataSource) as T
    }
}
