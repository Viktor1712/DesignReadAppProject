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
    @GET("books-id") // Based on OpenAPI spec provided
    suspend fun getBookById(@Query("id") id: Int): Book

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @POST("books")
    suspend fun createBook(@Body book: Book): ApiResponse<Int>

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @PUT("books-id")
    suspend fun updateBook(@Query("id") id: Int, @Body book: Book): Book

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @DELETE("books-id")
    suspend fun deleteBook(@Query("id") id: Int): Response<Unit>

    // Users
    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @GET("users")
    suspend fun getUsers(): List<User>

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @POST("users")
    suspend fun createUser(@Body user: User): User

    // Categories
    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @GET("categories")
    suspend fun getCategories(): List<Category>

    // Reading Progress
    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @GET("reading-progress")
    suspend fun getReadingProgress(): List<ReadingProgress>

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @POST("reading-progress")
    suspend fun updateReadingProgress(@Body progress: ReadingProgress): ReadingProgress

    // Favorites
    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @GET("favorites")
    suspend fun getFavorites(): List<Favorite>

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @POST("favorites")
    suspend fun addFavorite(@Body favorite: Favorite): Favorite

    @Headers("Ocp-Apim-Subscription-Key: b23b54a59f4f449eb64d507b55ea93e3")
    @DELETE("favorites-id")
    suspend fun removeFavorite(@Query("id") id: Int): Response<Unit>
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
    suspend fun getBookById(id: Int) = api.getBookById(id)
    suspend fun createBook(book: Book) = api.createBook(book)
    suspend fun updateBook(id: Int, book: Book) = api.updateBook(id, book)
    suspend fun deleteBook(id: Int) = api.deleteBook(id)
    
    suspend fun getUsers() = api.getUsers()
    suspend fun createUser(user: User) = api.createUser(user)
    
    suspend fun getCategories() = api.getCategories()
    
    suspend fun getReadingProgress() = api.getReadingProgress()
    suspend fun updateReadingProgress(progress: ReadingProgress) = api.updateReadingProgress(progress)
    
    suspend fun getFavorites() = api.getFavorites()
    suspend fun addFavorite(favorite: Favorite) = api.addFavorite(favorite)
    suspend fun removeFavorite(id: Int) = api.removeFavorite(id)
}
