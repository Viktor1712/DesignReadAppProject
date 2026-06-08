package com.design.readerapp

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("id", alternate = ["_id"]) val id: String? = null,
    @SerializedName("user_id", alternate = ["userId"]) val userId: String? = null,
    val title: String = "",
    val author: String = "",
    val description: String? = null,
    val category: String = "",
    val language: String? = "es",
    @SerializedName("currentStatus", alternate = ["current_status"]) val currentStatus: String? = "activo",
    @SerializedName("isPublic", alternate = ["is_public"]) val isPublic: Boolean = true,
    @SerializedName("pdf_blob_name", alternate = ["pdfBlobName"]) val pdfBlobName: String? = null,
    @SerializedName("cover_blob_name", alternate = ["coverBlobName"]) val coverBlobName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    
    // Virtual fields for UI
    var coverUrl: String? = null,
    var fileUrl: String? = null
)

data class User(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    val username: String = "",
    @SerializedName("name", alternate = ["full_name"]) val name: String = "",
    val email: String = "",
    val initials: String? = null,
    @SerializedName("firebaseUid", alternate = ["firebase_uid"]) val firebaseUid: String? = null
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class TokenRequest(
    val idToken: String
)

data class Category(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    val name: String = "",
    val label: String = ""
)

data class ReadingProgress(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    @SerializedName("user_id", alternate = ["userId"]) val userId: String = "",
    @SerializedName("book_id", alternate = ["bookId"]) val bookId: String = "",
    @SerializedName("current_page", alternate = ["currentPage"]) val currentPage: Int = 0,
    @SerializedName("total_pages", alternate = ["totalPages"]) val totalPages: Int = 0,
    val percentage: Int = 0,
    @SerializedName("updated_at", alternate = ["updatedAt"]) val updatedAt: String? = null
)

data class Favorite(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    @SerializedName("user_id", alternate = ["userId"]) val userId: String = "",
    @SerializedName("book_id", alternate = ["bookId"]) val bookId: String = ""
)

data class SasUrlResponse(
    @SerializedName("url", alternate = ["uploadUrl", "sasUrl", "sas_url", "pdfUrl", "fileUrl"]) val url: String,
    val blobName: String? = null
)

data class UploadUrlRequest(
    val fileName: String,
    val contentType: String
)

data class LikeStatusResponse(
    val liked: Boolean,
    val likesCount: Int
)

data class SignalRConnection(
    val url: String,
    val accessToken: String,
    val userId: String,
    val hub: String
)

data class Notification(
    val id: Int,
    val recipientUserId: String,
    val actorUserId: String,
    val actorName: String,
    val bookId: String?,
    val type: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String
)
