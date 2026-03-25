package com.example.projectandroid.telas

import android.Manifest
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectandroid.R
import com.google.android.gms.location.LocationServices
import java.util.Locale

data class Denuncia(
    val tipo: String,
    val descricao: String,
    val endereco: String,
    val imagem: Bitmap?
)

class FormularioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFA7EAB0)) {
                    AplicativoZelus()
                }
            }
        }
    }
}

@Composable
fun AplicativoZelus() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // ESTADOS: 0=Home, 3=Lista, 4=Sobre, 10=Foto, 11=Mapa, 12=Formulário Texto
    var telaAtual by remember { mutableIntStateOf(0) }
    var imagemCapturada by remember { mutableStateOf<Bitmap?>(null) }
    var enderecoDetectado by remember { mutableStateOf("Buscando sua rua...") }
    var tituloRua by remember { mutableStateOf("Buscando...") }

    var bancoDeDados by remember { mutableStateOf(listOf<Denuncia>()) }

    val abaAtiva = when (telaAtual) {
        10 -> 1
        11 -> 2
        12 -> 3
        4 -> 4
        else -> telaAtual
    }

    fun buscarLocalizacao() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val enderecos = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!enderecos.isNullOrEmpty()) {
                        val rua = enderecos[0].thoroughfare ?: "Rua não identificada"
                        val numero = enderecos[0].subThoroughfare ?: ""
                        tituloRua = if (numero.isNotEmpty()) "$rua, $numero" else rua
                        val cidade = enderecos[0].locality ?: "Cidade desconhecida"
                        enderecoDetectado = "$rua - $cidade"
                    }
                } else {
                    enderecoDetectado = "Localização não encontrada"
                    tituloRua = "Nenhuma Rua"
                }
            }
        } catch (e: SecurityException) {
            enderecoDetectado = "Sem permissão de GPS"
            tituloRua = "Sem GPS"
        }
    }

    val launcherGps = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            buscarLocalizacao()
            telaAtual = 11
        } else {
            Toast.makeText(context, "GPS necessário para a denúncia!", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imagemCapturada = bitmap
            telaAtual = 10
        }
    }

    val permissaoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Surface(shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp), color = Color(0xFFEFFFF1), modifier = Modifier.fillMaxWidth()) {
                NavigationBar(containerColor = Color.Transparent) {
                    val icons = listOf(Icons.Default.Home, Icons.Default.AddCircle, Icons.Default.LocationOn, Icons.AutoMirrored.Filled.List, Icons.Default.Person)
                    icons.forEachIndexed { index, icon ->
                        NavigationBarItem(
                            icon = {
                                if (abaAtiva == index) {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF13C69D)), contentAlignment = Alignment.Center)
                                    { Icon(icon, contentDescription = null, tint = Color.Black) }
                                } else { Icon(icon, contentDescription = null, tint = Color.Black) }
                            },
                            selected = abaAtiva == index,
                            onClick = {
                                if (telaAtual in 10..12) {
                                    // Bloqueio se estiver no meio da denúncia
                                    if (index == 0) {
                                        telaAtual = 0
                                        Toast.makeText(context, "Denúncia cancelada.", Toast.LENGTH_SHORT).show()
                                    } else if (index != abaAtiva) {
                                        Toast.makeText(context, "Termine ou cancele a denúncia primeiro!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    
                                    if (index == 1) {
                                        permissaoCameraLauncher.launch(Manifest.permission.CAMERA) // Abre a Câmera
                                    } else if (index == 2) {
                                        // Clicou no mapa fora de hora! Avisa o usuário:
                                        Toast.makeText(context, "Para iniciar, toque no '+' e tire uma foto primeiro!", Toast.LENGTH_LONG).show()
                                    } else {
                                        telaAtual = index // Navega normalmente para Home, Lista ou Sobre
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (telaAtual) {
                0 -> TelaHome(
                    onNovaDenunciaClick = { permissaoCameraLauncher.launch(Manifest.permission.CAMERA) },
                    onVerDenunciasClick = { telaAtual = 3 },
                    onSobreClick = { telaAtual = 4 },
                    paddingBarra = innerPadding
                )
                3 -> TelaVerDenuncias(listaDeDenuncias = bancoDeDados, paddingBarra = innerPadding)
                4 -> TelaSobreApp(onVoltarClick = { telaAtual = 0 }, paddingBarra = innerPadding)

                10 -> TelaNovaDenuncia(imagem = imagemCapturada, onConfirmarClick = { launcherGps.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, onRefazerFotoClick = { permissaoCameraLauncher.launch(Manifest.permission.CAMERA) }, paddingBarra = innerPadding)
                11 -> TelaLocalizacao(endereco = enderecoDetectado, ruaTitle = tituloRua, onProsseguirClick = { telaAtual = 12 }, paddingBarra = innerPadding)
                12 -> TelaDescricaoDenuncia(
                    onEnviarClick = { tipoEscolhido, descricaoDigitada ->
                        val novaDenuncia = Denuncia(tipo = tipoEscolhido, descricao = descricaoDigitada, endereco = enderecoDetectado, imagem = imagemCapturada)
                        bancoDeDados = bancoDeDados + novaDenuncia
                        Toast.makeText(context, "Denúncia salva com sucesso!", Toast.LENGTH_LONG).show()
                        telaAtual = 0
                    },
                    paddingBarra = innerPadding
                )
                else -> TelaEmConstrucao()
            }
        }
    }
}

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
            Column(modifier = Modifier.fillMaxSize().padding(start = 32.dp, top = 40.dp, end = 32.dp, bottom = paddingBarra.calculateBottomPadding() + 16.dp), verticalArrangement = Arrangement.spacedBy(45.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                Text(text = "O Zelus é um aplicativo criado para ajudar cidadãos a denunciar problemas urbanos de forma rápida e simples:", fontSize = 15.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))

                val topicos = listOf(
                    "Facilitar o registro de problemas urbanos na cidade;",
                    "Permitir que cidadãos enviem denúncias com foto e localização;",
                    "Ajudar na identificação rápida de problemas nas ruas;",
                    "Melhorar a comunicação entre moradores e órgãos responsáveis;",
                    "Contribuir para uma cidade mais organizada e segura."
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

@Composable
fun TelaNovaDenuncia(imagem: Bitmap?, onConfirmarClick: () -> Unit, onRefazerFotoClick: () -> Unit, paddingBarra: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(shape = RoundedCornerShape(topStart = 80.dp, topEnd = 80.dp), color = Color(0xFFF3FFF5), modifier = Modifier.fillMaxSize().padding(top = 100.dp)) {}
        Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingBarra.calculateBottomPadding()).padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nova Denúncia", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(30.dp))
            Box(modifier = Modifier.fillMaxWidth(0.75f).aspectRatio(9f/16f).clip(RoundedCornerShape(24.dp)).background(Color(0xFF3B3253)), contentAlignment = Alignment.Center) {
                if (imagem != null) Image(bitmap = imagem.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Text("Câmera Pronta", color = Color.White)
            }
            Spacer(modifier = Modifier.height(35.dp))
            Button(onClick = onConfirmarClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA7EAB0)), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)) {
                Text("Confirmar Localização", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRefazerFotoClick, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(0.6f).height(50.dp), border = BorderStroke(2.dp, Color(0xFF13C69D))) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refazer Foto", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TelaLocalizacao(endereco: String, ruaTitle: String, onProsseguirClick: () -> Unit, paddingBarra: PaddingValues) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(modifier = Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = ruaTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Surface(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = paddingBarra.calculateBottomPadding() + 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(20.dp), color = Color(0xFFF3FFF5), border = BorderStroke(2.dp, Color(0xFF13C69D))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFEFFFF1)), contentAlignment = Alignment.Center)
                        { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.Black) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(endereco, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(30.dp)).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(id = R.drawable.mapa_), contentDescription = "Mapa Detectado", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = onProsseguirClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13C69D)), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("Prosseguir para Denúncia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TelaDescricaoDenuncia(onEnviarClick: (String, String) -> Unit, paddingBarra: PaddingValues) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    var tipoProblemaSelecionado by remember { mutableStateOf("SELECIONE O TIPO...") }
    var menuExpandido by remember { mutableStateOf(false) }
    val opcoesDeProblema = listOf("BURACO NA RUA", "POSTE QUEBRADO", "LIXO NA RUA", "VAZAMENTO DE ÁGUA", "OUTROS")

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = paddingBarra.calculateBottomPadding()).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Descrição Da Denúncia", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp)).background(Color(0xFF13C69D)).padding(12.dp)) {
                        Text("DADOS PESSOAIS", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("NOME:") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.LightGray))
                OutlinedTextField(value = telefone, onValueChange = { telefone = it }, label = { Text("TELEFONE:") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.LightGray))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("EMAIL:") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.LightGray))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.clickable { menuExpandido = true }.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TIPO:", fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tipoProblemaSelecionado, color = Color(0xFF13C69D), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Escolher Problema", tint = Color.Black)
                    }
                }
                DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }, modifier = Modifier.background(Color.White)) {
                    opcoesDeProblema.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                            onClick = { tipoProblemaSelecionado = opcao; menuExpandido = false }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                placeholder = { Text("Descreva o problema em detalhes...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent),
                maxLines = 8
            )
        }
        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { onEnviarClick(tipoProblemaSelecionado, descricao) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13C69D)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Enviar Denúncia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TelaEmConstrucao() {
    Surface(shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp), color = Color.White, modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Em Construção 🚧", color = Color.Gray, textAlign = TextAlign.Center) }
    }
}

@Composable
fun BotaoZelus(texto: String, icone: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(32.dp)).background(Color(0xFFA7EAB0)).clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.padding(start = 4.dp).size(48.dp).clip(CircleShape).background(Color(0xFF13C69D)), contentAlignment = Alignment.Center) { Icon(icone, contentDescription = null, tint = Color.Black) }
        Text(text = texto, modifier = Modifier.weight(1f).padding(end = 52.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
