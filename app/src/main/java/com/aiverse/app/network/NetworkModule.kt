package com.aiverse.app.network

import com.aiverse.app.BuildConfig
import com.aiverse.app.network.GeminiService
import com.aiverse.app.network.OpenAiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val OPENAI_BASE_URL = "https://api.openai.com/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"


    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @Named("OpenAiClient")
    fun provideOpenAiClient(): OkHttpClient {
        val apiKey = BuildConfig.OPENAI_API_KEY

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("GeminiClient")
    fun provideGeminiClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAiService(
        @Named("OpenAiClient") client: OkHttpClient,
        gson: Gson
    ): OpenAiService {
        return Retrofit.Builder()
            .baseUrl(OPENAI_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OpenAiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiService(
        @Named("GeminiClient") client: OkHttpClient,
        gson: Gson
    ): GeminiService {
        return Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GeminiService::class.java)
    }
}
