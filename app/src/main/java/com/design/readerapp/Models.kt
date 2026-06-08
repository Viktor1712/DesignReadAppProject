package com.design.readerapp

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("id", alternate = ["_id", "ID", "bookId", "book_id"]) val id: String? = null,
    @SerializedName("user_id", alternate = ["userId", "ownerId", "owner_id"]) val userId: String? = null,
    val title: String = "",
    val author: String = "",
    val description: String? = null,
    val category: String = "",
    val language: String? = "es",
    @SerializedName("currentStatus", alternate = ["current_status", "status"]) val currentStatus: String? = "activo",
    @SerializedName("isPublic", alternate = ["is_public", "public"]) val isPublic: Boolean = true,
    @SerializedName("pdf_blob_name", alternate = ["pdfBlobName", "pdf_blob"]) val pdfBlobName: String? = null,
    @SerializedName("cover_blob_name", alternate = ["coverBlobName", "cover_blob"]) val coverBlobName: String? = null,
    @SerializedName("created_at", alternate = ["createdAt"]) val createdAt: String? = null,
    
    // Virtual fields for UI (or provided by backend in some endpoints)
    @SerializedName("cover_url", alternate = ["coverUrl", "image", "thumbnail", "cover", "imageUrl", "image_url", "portrait"]) var coverUrl: String? = null,
    @SerializedName("file_url", alternate = ["fileUrl", "pdf_url", "url", "pdfUrl"]) var fileUrl: String? = null
)

data class User(
    @SerializedName("id", alternate = ["_id", "uid", "userId"]) val id: String? = null,
    val username: String = "",
    @SerializedName("name", alternate = ["full_name", "fullName", "displayName"]) val name: String = "",
    val email: String = "",
    val initials: String? = null,
    @SerializedName("firebaseUid", alternate = ["firebase_uid", "firebase_id"]) val firebaseUid: String? = null
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
    @SerializedName("id", alternate = ["_id"]) val id: String? = null,
    @SerializedName("userId", alternate = ["user_id"]) val userId: String = "",
    @SerializedName("bookId", alternate = ["book_id"]) val bookId: String = "",
    @SerializedName("currentPage", alternate = ["current_page"]) val currentPage: Int = 0,
    @SerializedName("totalPages", alternate = ["total_pages"]) val totalPages: Int = 0,
    val percentage: Int = 0,
    @SerializedName("updatedAt", alternate = ["updated_at"]) val updatedAt: String? = null
)

data class Favorite(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    @SerializedName("user_id", alternate = ["userId"]) val userId: String = "",
    @SerializedName("book_id", alternate = ["bookId"]) val bookId: String = ""
)

data class SasUrlResponse(
    @SerializedName("url", alternate = ["uploadUrl", "sasUrl", "sas_url", "pdfUrl", "fileUrl", "coverUrl", "cover_url"]) val url: String,
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
    val id: String? = null,
    val recipientUserId: String = "",
    val actorUserId: String = "",
    val actorName: String = "",
    val bookId: String? = null,
    val type: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
)
