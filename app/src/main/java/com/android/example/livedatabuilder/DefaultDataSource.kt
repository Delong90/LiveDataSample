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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


/**
 * A source of data for [LiveDataViewModel], showcasing different LiveData + coroutines patterns.
 */
/**
* Источник данных для [LiveDataViewModel], демонстрирующий различные шаблоны сопрограмм LiveData +.
*/
class DefaultDataSource(private val ioDispatcher: CoroutineDispatcher) : DataSource {

    /**
     * LiveData builder generating a value that will be transformed.
     */
    /**
    * Конструктор LiveData, генерирующий значение, которое будет преобразовано.
    */
    override fun getCurrentTime(): LiveData<Long> =
        liveData {
            while (true) {
                emit(System.currentTimeMillis())
                delay(1000)
            }
        }

    /**
     * emit + emitSource pattern (see ViewModel).
     */
    /**
    * шаблон emit + emitSource (см. ViewModel).
    */

    // Exposes a LiveData of changing weather conditions, every 2 seconds.
    // Предоставляет LiveData об изменении погодных условий каждые 2 секунды.
    private val weatherConditions = listOf("Sunny", "Cloudy", "Rainy", "Stormy", "Snowy")

    override fun fetchWeather(): LiveData<String> = liveData {
        var counter = 0
        while (true) {
            counter++
            delay(2000)

            emit(weatherConditions[counter % weatherConditions.size])
        }
    }

    /**
     * Cache + Remote data source.
     */
    /**
    * Кэш + удаленный источник данных.
    */
    // Cache of a data point that is exposed to VM
    // Кэш точки данных, доступной виртуальной машине
    private val _cachedData = MutableLiveData("This is old data")
    override val cachedData: LiveData<String> = _cachedData

    // Called when the cache needs to be refreshed. Must be called from coroutine.
    // Вызывается, когда необходимо обновить кеш. Должен вызываться из сопрограммы.
    override suspend fun fetchNewData() {
        // Force Main thread
        withContext(Dispatchers.Main) {
            _cachedData.value = "Fetching new data..."
            _cachedData.value = simulateNetworkDataFetch()
        }
    }

    // Fetches new data in the background. Must be called from coroutine so it's scoped correctly.
    // Извлекает новые данные в фоновом режиме. Должен вызываться из сопрограммы, чтобы ее область видимости была правильной.
    private var counter = 0
    // Using ioDispatcher because the function simulates a long and expensive operation.
    // Использование ioDispatcher, потому что функция имитирует долгую и дорогостоящую операцию.
    private suspend fun simulateNetworkDataFetch(): String = withContext(ioDispatcher) {
        delay(3000)
        counter++
        "New data from request #$counter"
    }
}


