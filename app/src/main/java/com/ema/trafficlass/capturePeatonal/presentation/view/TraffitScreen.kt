package com.ema.trafficlass.capturePeatonal.presentation.view
import com.ema.trafficlass.core.permissions.rememberPermissionRequester
import android.widget.Toast
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ema.trafficlass.R
import com.ema.trafficlass.capturePeatonal.presentation.viewModel.Clasificacion
import com.ema.trafficlass.capturePeatonal.presentation.viewModel.traffitViewModel
import com.ema.trafficlass.domain.TrafficClassifier
import kotlinx.coroutines.launch

val señalSignificados = mapOf(
    "Alto" to "Debes detenerte por completo antes de continuar.",
    "Cinturon" to "Recuerda usar el cinturón de seguridad siempre que viajes.",
    "Cruce Peatonal" to "Estás en una zona de cruce. Cruza con precaución.",
    "Cruce de Ferrocarril" to "Ten cuidado, puede pasar un tren. Detente y observa.",
    "Discapacidad" to "Área reservada para personas con discapacidad. Respeta el espacio.",
    "Doble Sentido" to "La vía tiene tráfico en ambas direcciones. Mira a ambos lados.",
    "Limite de velocidad" to "Hay un límite de velocidad en esta zona. Modera tu paso.",
    "No Estacionarse" to "No se permite estacionar aquí. Mantén libre el paso.",
    "No cruzar" to "Está prohibido el paso a cualquier persona no autorizada.",
    "Parada de autobus" to "Zona de parada de autobuses. Atención a los vehículos.",
    "Paso de Ciclistas" to "Cruce frecuente de ciclistas. Avanza con cuidado.",
    "Prohibido arrojar basura" to "Prohibido tirar basura. Mantén limpio el lugar.",
    "Prohibido celulares" to "Evita usar el celular en esta zona por tu seguridad.",
    "Prohibido motos" to "No se permite el paso de motocicletas por aquí.",
    "Prohibido seguir" to "Debes detenerte, no está permitido continuar en esta dirección.",
    "Tope" to "Hay un tope en el camino. Reduce la velocidad.",
    "Uno x uno" to "Pasa de forma alternada con otros vehículos. Turno por turno.",
    "Vuelta Prohibida" to "No está permitido girar en esta dirección.",
    "WC" to "Baños públicos disponibles cerca de esta zona.",
    "Zona Escolar" to "Zona escolar. Reduce la velocidad y pon atención a los niños."
)

val señalImagenes = mapOf(
    "Alto" to R.drawable.uno_uno,
    "Cinturon" to R.drawable.cinturon,
    "Cruce Peatonal" to R.drawable.cruce_peatonal,
    "Cruce de Ferrocarril" to R.drawable.cruce_ferrocarril,
    "Discapacidad" to R.drawable.discapacidad,
    "Doble Sentido" to R.drawable.doble_sentido,
    "Limite de velocidad" to R.drawable.limite_velocidad,
    "No Estacionarse" to R.drawable.no_estacionarse,
    "No cruzar" to R.drawable.no_cruzar,
    "Parada de autobus" to R.drawable.parada_autobus,
    "Paso de Ciclistas" to R.drawable.paso_ciclistas,
    "Prohibido arrojar basura" to R.drawable.prohibido_basura,
    "Prohibido celulares" to R.drawable.prohibido_celulares,
    "Prohibido motos" to R.drawable.prohibido_motos,
    "Prohibido seguir" to R.drawable.prohibido_seguir,
    "Tope" to R.drawable.tope,
    "Uno x uno" to R.drawable.uno_uno,
    "Vuelta Prohibida" to R.drawable.vuelta_prohibida,
    "WC" to R.drawable.wc,
    "Zona Escolar" to R.drawable.zona_escolar
)


@Composable
fun TraffitScreen(onLogout: () -> Unit, homeViewModel: traffitViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lightBlue = Color(0xFFF0F8FF)
    val primaryBlue = Color(0xFF0078D7)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val mostrarDialogo = remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context, null).apply {
            language = Locale("es", "MX")
        }
    }

    val classifier = remember { TrafficClassifier(context) }
    val resultado by homeViewModel.resultado.collectAsState()
    val topResultados by homeViewModel.topResultados.collectAsState()
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberImageCaptureLauncher { bitmap ->
        capturedBitmap = bitmap
        val resultados = classifier.classifyTop3(bitmap)
        val principal = resultados.firstOrNull()
        homeViewModel.setResultadoPrincipal(principal?.etiqueta ?: "Sin resultado")
        homeViewModel.setTopResultados(resultados)

        // 🗣️ Hablar resultado
        principal?.significado?.let { significado ->
            tts.speak(significado, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeader(onLogout, primaryBlue, mostrarDialogo)
            ScreenTitle()
            CameraSection(launcher,capturedBitmap)
            ResultadoAnalisisCard(resultado)
            if (capturedBitmap != null || resultado != null) {
                Button(
                    onClick = {
                        homeViewModel.limpiarResultados()
                        capturedBitmap = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Resultados limpiados")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar Resultado", color = Color.White)
                }
            }
            TopResultadosCard(resultados = topResultados)
            AdvertenciaBanner()
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
        if (mostrarDialogo.value) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo.value = false },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogo.value = false }) {
                        Text("Cerrar")
                    }
                },
                title = { Text("Clases detectadas", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        señalSignificados.forEach { (nombre, significado) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                señalImagenes[nombre]?.let { resId ->
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = nombre,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(end = 12.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = significado,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

    }
}

@Composable
fun AppHeader(onLogout: () -> Unit, primaryBlue: Color, mostrarDialogo: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ArrowCircleLeft, contentDescription = "Volver", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("TraffiClass", color = primaryBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.StackedBarChart, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = { mostrarDialogo.value = true }) {
            Icon(Icons.Default.Help, contentDescription = "Ayuda", tint = Color.Black)
        }
    }
}


@Composable
fun ScreenTitle() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Análisis de Señales", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Captura y analiza señales peatonales para entender su significado", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CameraSection(launcher: ActivityResultLauncher<Void?>, capturedBitmap: Bitmap?) {
    val context = LocalContext.current
    val primaryBlue = Color(0xFF0078D7)

    // 🟡 Permiso CAMERA
    val requestPermission = rememberPermissionRequester { granted ->
        if (granted) {
            launcher.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Cámara", tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enfoque la señal peatonal", color = Color.Gray, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, primaryBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (capturedBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = capturedBitmap.asImageBitmap(),
                    contentDescription = "Foto capturada",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            } else {
                Button(
                    onClick = { requestPermission() },
                    modifier = Modifier.size(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Capturar", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

    }
}



@Composable
fun ResultadoAnalisisCard(result: String?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.logo), contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resultado del Análisis", color = Color(0xFF2196F3), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result ?: "Tome una foto de una señal peatonal para analizar.",
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AdvertenciaBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFF9C4)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capture una imagen de una señal peatonal para comenzar el análisis", color = Color(0xFFF57C00), fontSize = 14.sp)
        }
    }
}

@Composable
fun rememberImageCaptureLauncher(onImageCaptured: (Bitmap) -> Unit): ActivityResultLauncher<Void?> {
    return rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { onImageCaptured(it) }
    }
}

@Composable
fun TopResultadosCard(resultados: List<Clasificacion>) {
    if (resultados.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resultados principales:", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            resultados.forEach { resultado ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("${resultado.etiqueta} - ${(resultado.probabilidad * 100).toInt()}%", fontSize = 16.sp)
                    LinearProgressIndicator(
                        progress = resultado.probabilidad,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50)
                    )
                    Text(resultado.significado, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}
