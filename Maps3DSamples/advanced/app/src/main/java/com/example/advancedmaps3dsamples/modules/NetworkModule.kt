// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * A Dependency Injection (DI) module using Hilt. 
 * 
 * WHY DI?
 * As an application grows, managing dependencies (like network clients or databases) manually 
 * becomes complex and fragile. Hilt automates this by constructing and wiring objects for us.
 * 
 * The @Module annotation tells Hilt "This is a blueprint for how to create certain objects."
 * The @InstallIn(SingletonComponent::class) tells Hilt to keep these objects alive for the 
 * entire lifecycle of the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a singleton instance of the [HttpClient] configured with JSON serialization.
     * 
     * WHY SINGLETON?
     * Network clients (like Ktor's HttpClient or Retrofit) are heavy objects. They manage connection pools 
     * and threads. We only ever want one instance of this client shared across the whole app to save memory 
     * and avoid connection overhead. The @Singleton annotation enforces this.
     * 
     * WHY CONTENT NEGOTIATION?
     * APIs talk in text (JSON), but our app talks in Kotlin objects. The ContentNegotiation plugin acts 
     * as an automatic translator, deserializing the JSON response directly into our data classes (like RoutesResponse).
     */
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }
}
