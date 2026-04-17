package com.design.readerapp

import com.google.gson.annotations.SerializedName

data class Book(
    val id: String? = null,
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("autor") val autor: String = "",
    @SerializedName("categoria") val categoria: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("estado") val estado: String = "Publicado",
    @SerializedName("paginas") val paginas: Int = 0,
    @SerializedName("idioma") val idioma: String = "Español",
    @SerializedName("portada") val portada: String = "",
    @SerializedName("pdfUrl") val pdfUrl: String = ""
)