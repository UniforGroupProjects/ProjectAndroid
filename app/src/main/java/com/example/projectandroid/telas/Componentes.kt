package com.example.projectandroid.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TelaEmConstrucao() {
    Surface(shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp), color = Color.White, modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Em Construção \uD83D\uDEA7", color = Color.Gray, textAlign = TextAlign.Center) }
    }
}

@Composable
fun BotaoZelus(texto: String, icone: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFFA7EAB0)).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.padding(start = 4.dp).size(48.dp).clip(CircleShape).background(Color(0xFF13C69D)), contentAlignment = Alignment.Center) { Icon(icone, contentDescription = null, tint = Color.Black) }
        Text(text = texto, modifier = Modifier.weight(1f).padding(end = 52.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}