/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.placesuikit3d.di

import android.content.Context
import com.example.placesuikit3d.data.repository.PlacesRepository
import com.example.placesuikit3d.data.repository.PlacesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module binding [PlacesRepository] interface to [PlacesRepositoryImpl].
 */
@Module
@InstallIn(SingletonComponent::class)
object PlacesRepositoryModule {

    @Provides
    @Singleton
    fun providePlacesRepository(
        @ApplicationContext context: Context,
    ): PlacesRepository = PlacesRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideStringProvider(
        @ApplicationContext context: Context,
    ): com.example.placesuikit3d.utils.StringProvider = com.example.placesuikit3d.utils.StringProvider { resId, formatArgs ->
        if (formatArgs.isEmpty()) context.getString(resId) else context.getString(resId, *formatArgs)
    }
}
