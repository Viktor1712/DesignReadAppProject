package com.design.readerapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface BooksApiService {
    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @GET("books")
    suspend fun getBooks(): List<Book>

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: String): Book

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @POST("books")
    suspend fun createBook(@Body book: Book): Book

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") id: String, @Body book: Book): Book

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: String): Response<Unit>
}

object BooksService {
    private const val BASE_URL = "https://librosapi.azure-api.net/v1/"

    private val api: BooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BooksApiService::class.java)
    }

    suspend fun getBooks() = api.getBooks()
    suspend fun getBookById(id: String) = api.getBookById(id)
    suspend fun createBook(book: Book) = api.createBook(book)
    suspend fun updateBook(id: String, book: Book) = api.updateBook(id, book)
    suspend fun deleteBook(id: String) = api.deleteBook(id)
}