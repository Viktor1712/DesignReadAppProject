package com.design.readerapp

import com.google.gson.annotations.SerializedName

data class Book(
    val id: Int? = null,
    val userId: Int? = null,
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val description: String = "",
    val language: String = "",
    val coverUrl: String = "",
    val pdfUrl: String = "",
    val pdfFileName: String? = null,
    val pdfFileSize: Long? = null,
    val totalPages: Int = 0,
    val currentStatus: String = "",
    val isPublic: Boolean = true,
    val uploadedAt: String? = null,
    val updatedAt: String? = null,
    
    // Support for legacy/mixed naming if necessary based on POST examples
    @SerializedName("titulo") val titulo: String? = null,
    @SerializedName("autor") val autor: String? = null,
    @SerializedName("categoria") val categoria: String? = null
)

data class User(
    val id: Int? = null,
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val password: String? = null,
    val profileImageUrl: String? = null,
    val initials: String? = null
)

data class Category(
    val id: Int = 0,
    val name: String = "",
    val label: String = ""
)

data class ReadingProgress(
    val id: Int? = null,
    val userId: Int = 0,
    val bookId: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val percentage: Int = 0,
    val lastReadAt: String? = null
)

data class Favorite(
    val id: Int? = null,
    val userId: Int = 0,
    val bookId: Int = 0,
    val addedAt: String? = null
)

data class ApiResponse<T>(
    val message: String? = null,
    val data: T? = null,
    val id: Int? = null
)
