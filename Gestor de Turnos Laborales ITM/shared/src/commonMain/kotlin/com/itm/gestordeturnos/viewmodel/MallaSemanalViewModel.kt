package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.DiaColumnaMalla
import com.itm.gestordeturnos.domain.model.FilaMalla
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class MallaSemanalState(
    val titulo: String = "Malla semanal",
    val subtitulo: String = "Verifica que la jornada de 06:00 a 22:00 queda cubierta antes de publicar. Los vacíos de cobertura se resaltan.",
    val fechaInicioSemana: LocalDate = LocalDate(2026, 9, 14),
    val rangoSemana: String = "14 – 20 sep 2026",
    val sucursal: String = "Centro",
    val opcionesSucursal: List<String> = listOf("Centro", "Norte", "Sur"),
    val estadoMalla: String = "BORRADOR - NO VISIBLE PARA LOS EMPLEADOS",
    val diasSemana: List<DiaColumnaMalla> = listOf(
        DiaColumnaMalla("LUN", "14", cubierto = true),
        DiaColumnaMalla("MAR", "15", cubierto = true),
        DiaColumnaMalla("MIÉ", "16", cubierto = true),
        DiaColumnaMalla("JUE", "17", cubierto = true),
        DiaColumnaMalla("VIE", "18", cubierto = true),
        DiaColumnaMalla("SÁB", "19", cubierto = false), // SIN CUBRIR
        DiaColumnaMalla("DOM", "20", cubierto = true)
    ),
    val filas: List<FilaMalla> = emptyList(),
    val publicado: Boolean = false
)

class MallaSemanalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MallaSemanalState())
    val uiState: StateFlow<MallaSemanalState> = _uiState.asStateFlow()

    init {
        observarFilasMalla()
    }

    private fun observarFilasMalla() {
        viewModelScope.launch {
            TurnoRepository.filasMalla.collect { filas ->
                _uiState.update { it.copy(filas = filas) }
            }
        }
    }

    fun onSucursalCambio(nuevaSucursal: String) {
        _uiState.update { it.copy(sucursal = nuevaSucursal) }
    }

    fun publicarSemana() {
        _uiState.update {
            it.copy(
                publicado = true,
                estadoMalla = "PUBLICADA - VISIBLE PARA LOS EMPLEADOS"
            )
        }
    }

    fun seleccionarFechaSemana(fecha: LocalDate) {
        val diasDesdeLunes = fecha.dayOfWeek.ordinal
        val lunes = fecha.minus(DatePeriod(days = diasDesdeLunes))
        actualizarSemana(lunes)
    }

    fun cambiarSemana(deltaSemanas: Int) {
        val actual = _uiState.value.fechaInicioSemana
        val nueva = actual.plus(DatePeriod(days = deltaSemanas * 7))
        actualizarSemana(nueva)
    }

    private fun actualizarSemana(lunes: LocalDate) {
        val domingo = lunes.plus(DatePeriod(days = 6))
        val nombresDias = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

        val nuevosDias = (0..6).map { i ->
            val diaFecha = lunes.plus(DatePeriod(days = i))
            val esSabado = i == 5
            DiaColumnaMalla(
                diaSemana = nombresDias[i],
                numeroDia = "${diaFecha.dayOfMonth}",
                cubierto = if (_uiState.value.publicado) true else !esSabado
            )
        }

        val rangoTexto = if (lunes.monthNumber == domingo.monthNumber) {
            "${lunes.dayOfMonth} – ${domingo.dayOfMonth} ${obtenerMesCorto(domingo.monthNumber)} ${domingo.year}"
        } else {
            "${lunes.dayOfMonth} ${obtenerMesCorto(lunes.monthNumber)} – ${domingo.dayOfMonth} ${obtenerMesCorto(domingo.monthNumber)} ${domingo.year}"
        }

        _uiState.update {
            it.copy(
                fechaInicioSemana = lunes,
                rangoSemana = rangoTexto,
                diasSemana = nuevosDias
            )
        }
    }

    private fun obtenerMesCorto(mes: Int): String = when (mes) {
        1 -> "ene"
        2 -> "feb"
        3 -> "mar"
        4 -> "abr"
        5 -> "may"
        6 -> "jun"
        7 -> "jul"
        8 -> "ago"
        9 -> "sep"
        10 -> "oct"
        11 -> "nov"
        12 -> "dic"
        else -> ""
    }
}
