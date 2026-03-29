package com.example.projectandroid.telas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaVerDenuncias(listaDeDenuncias: List<Denuncia>, paddingBarra: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(modifier = Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Minhas Denúncias", fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Surface(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp), color = Color.White) {
            if (listaDeDenuncias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Nenhuma denúncia registrada ainda.", color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Seja o primeiro a ajudar o bairro!", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp, bottom = paddingBarra.calculateBottomPadding() + 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(listaDeDenuncias.reversed()) { denuncia ->
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FFF5)), border = BorderStroke(1.dp, Color(0xFF13C69D)), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)) {
                                        if (denuncia.imagem != null) Image(bitmap = denuncia.imagem.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(denuncia.tipo, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF13C69D), modifier = Modifier.size(16.dp))
                                            Text(denuncia.endereco, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(denuncia.descricao, color = Color.DarkGray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}