package com.example.projectandroid.telas

import android.graphics.Bitmap

data class Denuncia(
    val tipo: String,
    val descricao: String,
    val endereco: String,
    val imagem: Bitmap?
)