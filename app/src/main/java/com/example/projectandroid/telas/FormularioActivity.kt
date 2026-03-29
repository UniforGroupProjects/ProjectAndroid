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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import java.util.Locale

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
            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val enderecos = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!enderecos.isNullOrEmpty()) {
                        val rua = enderecos[0].thoroughfare ?: "Rua não identificada"
                        val numero = enderecos[0].subThoroughfare ?: ""
                        tituloRua = if (numero.isNotEmpty()) "$rua, $numero" else rua
                        val cidade = enderecos[0].locality ?: "Cidade desconhecida"
                        enderecoDetectado = "$rua - $cidade"
                    } else {
                        enderecoDetectado = "Endereço não encontrado"
                        tituloRua = "Rua não identificada"
                    }
                } else {
                    enderecoDetectado = "Localização não encontrada"
                    tituloRua = "Nenhuma Rua"
                }
            }.addOnFailureListener {
                enderecoDetectado = "Erro ao obter localização"
                tituloRua = "Erro GPS"
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
                                    if (index == 0) {
                                        telaAtual = 0
                                        Toast.makeText(context, "Denúncia cancelada.", Toast.LENGTH_SHORT).show()
                                    } else if (index != abaAtiva) {
                                        Toast.makeText(context, "Termine ou cancele a denúncia primeiro!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    if (index == 1) {
                                        permissaoCameraLauncher.launch(Manifest.permission.CAMERA)
                                    } else if (index == 2) {
                                        Toast.makeText(context, "Para iniciar, toque no '+' e tire uma foto primeiro!", Toast.LENGTH_LONG).show()
                                    } else {
                                        telaAtual = index
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
                        Toast.makeText(context, "Denúncia salva com sucesso!!!", Toast.LENGTH_LONG).show()
                        telaAtual = 0
                    },
                    paddingBarra = innerPadding
                )
                else -> TelaEmConstrucao()
            }
        }
    }
}