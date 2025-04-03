package com.ema.trafficlass.domain

import android.content.Context
import android.graphics.Bitmap
import com.ema.trafficlass.capturePeatonal.presentation.viewModel.Clasificacion
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import androidx.core.graphics.scale
import androidx.core.graphics.get

class TrafficClassifier(context: Context) {

    private var interpreter: Interpreter
    private val inputSize = 128
    private val modelPath = "modelo_senales.tflite"
    private val labels = listOf(
        "Alto", "Cinturon", "Cruce Peatonal", "Cruce de Ferrocarril", "Discapacidad",
        "Doble Sentido", "Limite de velocidad", "No Estacionarse", "No cruzar",
        "Parada de autobus", "Paso de Ciclistas", "Prohibido arrojar basura",
        "Prohibido celulares", "Prohibido motos", "Prohibido seguir", "Tope",
        "Uno x uno", "Vuelta Prohibida", "WC", "Zona Escolar"
    )
    private val numClasses = labels.size

    init {
        val model = loadModelFile(context)
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(context: Context): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = bitmap.scale(inputSize, inputSize)
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resized[x, y]
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }

        return byteBuffer
    }

    fun classifyTop3(bitmap: Bitmap): List<Clasificacion> {
        val inputBuffer = preprocess(bitmap)
        val output = Array(1) { FloatArray(numClasses) }
        interpreter.run(inputBuffer, output)

        val confidences = output[0]
        return confidences
            .mapIndexed { index, score -> Clasificacion(labels[index], score, obtenerSignificado(labels[index])) }
            .sortedByDescending { it.probabilidad }
            .take(3)
    }

    private fun obtenerSignificado(etiqueta: String): String {
        return when (etiqueta) {
            "Alto" -> "Detente completamente antes de continuar."
            "Cinturon" -> "Usa cinturón de seguridad obligatorio."
            "Cruce Peatonal" -> "Zona segura para cruzar peatones."
            "Cruce de Ferrocarril" -> "Atención: tren puede pasar."
            "Discapacidad" -> "Área reservada para personas con discapacidad."
            "Doble Sentido" -> "La vía tiene circulación en ambos sentidos."
            "Limite de velocidad" -> "No excedas el límite permitido."
            "No cruzar" -> "Está prohibido cruzar en este punto."
            "No Estacionarse" -> "Prohibido estacionar aquí."
            "Parada de autobus" -> "Zona de espera para transporte público."
            "Paso de Ciclistas" -> "Respeta a los ciclistas que cruzan."
            "Prohibido arrojar basura" -> "Mantén el área limpia."
            "Prohibido celulares" -> "Evita el uso del móvil en esta zona."
            "Prohibido motos" -> "No se permite circulación de motocicletas."
            "Prohibido seguir" -> "Detente, no avances tras el vehículo."
            "Tope" -> "Reduce velocidad: tope en camino."
            "Uno x uno" -> "Cede el paso de forma alternada."
            "Vuelta Prohibida" -> "Giro no permitido en esta dirección."
            "WC" -> "Baño público disponible aquí."
            "Zona Escolar" -> "Reduce velocidad y precaución con escolares."
            else -> "Sin descripción disponible."
        }
    }

}
