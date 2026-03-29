package com.example.projectandroid.telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectandroid.R

@Composable
fun TelaHome(onNovaDenunciaClick: () -> Unit, onVerDenunciasClick: () -> Unit, onSobreClick: () -> Unit, paddingBarra: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.5f)) {
                    Text("Olá, Bem Vindo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Denuncie problemas da sua cidade\ne ajude a melhorar o bairro.", fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                Image(painter = painterResource(id = R.drawable.paisagem_cidade), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Surface(shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp), color = Color.White, modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 32.dp, end = 32.dp, top = 40.dp, bottom = paddingBarra.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BotaoZelus(texto = "Nova Denúncia", icone = Icons.Default.Notifications, onClick = onNovaDenunciaClick)
                BotaoZelus(texto = "Ver Denúncias", icone = Icons.AutoMirrored.Filled.List, onClick = onVerDenunciasClick)
                BotaoZelus(texto = "Sobre O Aplicativo", icone = Icons.Default.Info, onClick = onSobreClick)
            }
        }
    }
}

@Composable
fun TelaSobreApp(onVoltarClick: () -> Unit, paddingBarra: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = paddingBarra.calculateBottomPadding() + 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.Black, modifier = Modifier.clickable { onVoltarClick() })
                Spacer(modifier = Modifier.width(16.dp))
                Text("Sobre O Aplicativo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFFD6F5DA), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "O Zelus é um aplicativo criado para ajudar cidadãos a denunciar problemas urbanos de forma rápida e simples.", fontSize = 15.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))

                val topicos = listOf(
                    "Facilitar o registro de problemas urbanos na cidade",
                    "Permitir que cidadãos enviem denúncias com foto e localização",
                    "Ajudar na identificação rápida de problemas nas ruas",
                    "Melhorar a comunicação entre moradores e órgãos responsáveis",
                    "Contribuir para uma cidade mais organizada e segura"
                )

                topicos.forEach { topico ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text(topico, fontSize = 15.sp, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(id = R.drawable.zelus_app), contentDescription = "Logo Zelus", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}