package com.ema.trafficlass.capturePeatonal.presentation.view
import com.ema.trafficlass.core.permissions.rememberPermissionRequester
import android.widget.Toast
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ema.trafficlass.R
import com.ema.trafficlass.capturePeatonal.presentation.viewModel.Clasificacion
import com.ema.trafficlass.capturePeatonal.presentation.viewModel.traffitViewModel
import com.ema.trafficlass.domain.TrafficClassifier

val señalSignificados = mapOf(
    "Alto" to "Indica que debes detenerte completamente.",
    "Cinturon" to "Obligatorio el uso del cinturón de seguridad.",
    "Cruce Peatonal" to "Zona destinada al paso de peatones.",
    "Cruce de Ferrocarril" to "Precaución, cruce de trenes cercano.",
    "Discapacidad" to "Zona reservada para personas con discapacidad.",
    "Doble Sentido" to "Vía con circulación en ambos sentidos.",
    "Limite de velocidad" to "Indica la velocidad máxima permitida.",
    "No Estacionarse" to "Prohibido estacionar en esta área.",
    "No cruzar" to "Zona prohibida para el paso de peatones.",
    "Parada de autobus" to "Área designada para paradas de autobuses.",
    "Paso de Ciclistas" to "Cruce o circulación frecuente de bicicletas.",
    "Prohibido arrojar basura" to "Mantén limpio, no arrojar desechos.",
    "Prohibido celulares" to "Prohibido el uso de celulares.",
    "Prohibido motos" to "Zona no permitida para motocicletas.",
    "Prohibido seguir" to "No está permitido seguir derecho.",
    "Tope" to "Reductor de velocidad en el camino.",
    "Uno x uno" to "Avance alternado entre vehículos.",
    "Vuelta Prohibida" to "Giro no permitido en esta dirección.",
    "WC" to "Baños públicos disponibles.",
    "Zona Escolar" to "Precaución, tránsito de estudiantes."
)

@Composable
fun TraffitScreen(onLogout: () -> Unit, homeViewModel: traffitViewModel) {
    val lightBlue = Color(0xFFF0F8FF)
    val primaryBlue = Color(0xFF0078D7)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val classifier = remember { TrafficClassifier(context) }
    val resultado by homeViewModel.resultado.collectAsState()
    val topResultados by homeViewModel.topResultados.collectAsState()
    val launcher = rememberImageCaptureLauncher { bitmap ->
        val resultados = classifier.classifyTop3(bitmap)
        homeViewModel.setResultadoPrincipal(resultados.firstOrNull()?.etiqueta ?: "Sin resultado")
        homeViewModel.setTopResultados(resultados)
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
            AppHeader(onLogout, primaryBlue)
            ScreenTitle()
            CameraSection(launcher)
            ResultadoAnalisisCard(resultado)
            TopResultadosCard(resultados = topResultados)
            AdvertenciaBanner()
        }
    }
}

@Composable
fun AppHeader(onLogout: () -> Unit, primaryBlue: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onLogout) {
            Icon(Icons.Default.ArrowCircleLeft, contentDescription = "Volver", tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("TraffiClass", color = primaryBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Icon(Icons.Filled.StackedBarChart, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(20.dp))
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
fun CameraSection(launcher: ActivityResultLauncher<Void?>) {
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
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { requestPermission() }, // 👈 Primero pedimos permiso
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Capturar", tint = Color.White, modifier = Modifier.size(32.dp))
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
