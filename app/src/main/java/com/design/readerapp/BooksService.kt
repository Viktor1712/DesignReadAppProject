package com.design.readerapp

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface BooksApiService {
    // Auth
    @POST("auth/token")
    suspend fun getAuthToken(@Body request: TokenRequest): AuthResponse

    // Books (MS-1)
    @GET("books")
    suspend fun getBooks(): List<Book>

    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: String): Book

    @POST("books")
    suspend fun createBook(@Body book: Book): Book

    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") id: String, @Body book: Book): Book

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: String): Response<Unit>

    @GET("books/{id}/file-url")
    suspend fun getBookFileUrl(@Path("id") id: String, @Query("userId") userId: String? = null): SasUrlResponse

    @GET("books/{id}/cover-url")
    suspend fun getBookCoverUrl(@Path("id") id: String, @Query("userId") userId: String? = null): SasUrlResponse

    @POST("generate-upload-url")
    suspend fun generateUploadUrl(@Body request: UploadUrlRequest): SasUrlResponse

    // Users (MS-2)
    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): User

    @POST("users")
    suspend fun createUser(@Body user: User): User

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): User

    // Reading Progress (MS-2)
    @GET("reading-progress")
    suspend fun getReadingProgress(): List<ReadingProgress>

    @POST("reading-progress")
    suspend fun updateReadingProgress(@Body progress: ReadingProgress): ReadingProgress

    // Favorites (MS-2)
    @GET("favorites")
    suspend fun getFavorites(): List<Favorite>

    @POST("favorites")
    suspend fun addFavorite(@Body favorite: Favorite): Favorite

    @DELETE("favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: String): Response<Unit>

    // Likes (MS-3)
    @POST("books/{id}/like")
    suspend fun toggleLike(@Path("id") id: String): Response<Unit>

    @GET("books/{id}/like-status")
    suspend fun getLikeStatus(@Path("id") id: String): LikeStatusResponse

    // Notifications (MS-3)
    @GET("notifications")
    suspend fun getNotifications(): List<Notification>

    @POST("notifications/negotiate")
    suspend fun negotiateSignalR(): SignalRConnection
}

object BooksService {
    private const val BASE_URL = "https://librosapi.azure-api.net/v1/"
    private const val SUBSCRIPTION_KEY = "b23b54a59f4f449eb64d507b55ea93e3"
    
    var authToken: String? = null
    var currentUser: User? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        
        // Evitar enviar cabeceras de API a otros dominios (como Azure Blob Storage)
        // Las SAS URLs de Azure fallan si se les envía un Bearer token o el sub-key
        if (!original.url.toString().startsWith(BASE_URL)) {
            return@Interceptor chain.proceed(original)
        }

        val requestBuilder = original.newBuilder()
            .header("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
        
        authToken?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun getOkHttpClient() = okHttpClient

    private val api: BooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BooksApiService::class.java)
    }

    suspend fun getAuthToken(idToken: String) = api.getAuthToken(TokenRequest(idToken))

    suspend fun getBooks() = api.getBooks()
    
    suspend fun getBookById(id: String): Book {
        return try {
            api.getBookById(id)
        } catch (e: Exception) {
            // Temporal: fallback buscando en la lista general (igual que la web)
            val books = getBooks()
            books.find { it.id == id } ?: throw e
        }
    }

    suspend fun createBook(book: Book) = api.createBook(book)
    suspend fun updateBook(id: String, book: Book) = api.updateBook(id, book)
    suspend fun deleteBook(id: String) = api.deleteBook(id)
    
    suspend fun getBookFileUrl(id: String, userId: String? = null) = api.getBookFileUrl(id, userId)
    suspend fun getBookCoverUrl(id: String, userId: String? = null) = api.getBookCoverUrl(id, userId)
    
    suspend fun getUsers() = api.getUsers()
    suspend fun getUserById(id: String) = api.getUserById(id)
    suspend fun createUser(user: User) = api.createUser(user)
    suspend fun updateUser(id: String, user: User) = api.updateUser(id, user)
    
    suspend fun getCategories(): List<Category> {
        // Sincronizado con la web: Datos hardcoded para evitar 404 del backend
        return listOf(
            Category(id = "1", name = "clásicos universales", label = "Clásicos universales"),
            Category(id = "2", name = "literatura latinoamericana", label = "Literatura latinoamericana"),
            Category(id = "3", name = "realismo mágico", label = "Realismo mágico"),
            Category(id = "4", name = "novela psicológica", label = "Novela psicológica"),
            Category(id = "5", name = "ciencia ficción", label = "Ciencia ficción"),
            Category(id = "6", name = "distopía", label = "Distopía"),
            Category(id = "7", name = "crítica social y política", label = "Crítica social y política"),
            Category(id = "8", name = "teatro", label = "Teatro"),
            Category(id = "9", name = "tragedia", label = "Tragedia"),
            Category(id = "10", name = "épica", label = "Épica"),
            Category(id = "11", name = "mitología", label = "Mitología"),
            Category(id = "12", name = "fantasía filosófica", label = "Fantasía filosófica"),
            Category(id = "13", name = "terror gótico", label = "Terror gótico"),
            Category(id = "14", name = "romance", label = "Romance"),
            Category(id = "15", name = "novela experimental", label = "Novela experimental"),
            Category(id = "16", name = "existencialismo", label = "Existencialismo"),
            Category(id = "17", name = "novela histórica", label = "Novela histórica"),
            Category(id = "18", name = "contexto social", label = "Contexto social")
        )
    }
    
    suspend fun getReadingProgress() = api.getReadingProgress()
    suspend fun updateReadingProgress(progress: ReadingProgress) = api.updateReadingProgress(progress)
    
    suspend fun getFavorites() = api.getFavorites()
    suspend fun addFavorite(bookId: String, userId: String) = api.addFavorite(Favorite(bookId = bookId, userId = userId))
    suspend fun removeFavorite(id: String) = api.removeFavorite(id)

    suspend fun toggleLike(id: String) = api.toggleLike(id)
    suspend fun getLikeStatus(id: String) = api.getLikeStatus(id)

    suspend fun getNotifications() = api.getNotifications()
    suspend fun negotiateSignalR() = api.negotiateSignalR()

    suspend fun generateUploadUrl(fileName: String, contentType: String) = 
        api.generateUploadUrl(UploadUrlRequest(fileName, contentType))
}
