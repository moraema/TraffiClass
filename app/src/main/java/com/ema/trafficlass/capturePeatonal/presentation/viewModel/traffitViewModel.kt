package com.ema.trafficlass.capturePeatonal.presentation.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Clasificacion(
    val etiqueta: String,
    val probabilidad: Float,
    val significado: String
)

class traffitViewModel : ViewModel() {

    private val _resultado = MutableStateFlow<String?>(null)
    val resultado: StateFlow<String?> = _resultado

    private val _topResultados = MutableStateFlow<List<Clasificacion>>(emptyList())
    val topResultados: StateFlow<List<Clasificacion>> = _topResultados

    fun setResultadoPrincipal(resultado: String) {
        _resultado.value = resultado
    }

    fun setTopResultados(resultados: List<Clasificacion>) {
        _topResultados.value = resultados
    }

    fun limpiarResultados() {
        _resultado.value = null
        _topResultados.value = emptyList()
    }
}