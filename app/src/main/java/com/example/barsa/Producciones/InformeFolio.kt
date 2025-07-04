package com.example.barsa.Producciones

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.barsa.R
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shape.toVicoShape
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeFolio(
    TipoId: String,
    Folio: Int,
    Fecha: String,
    Status: String,
    onNavigate: (String) -> Unit,
    papeletaViewModel: PapeletaViewModel
) {
    val context = LocalContext.current
    val tiemposState by papeletaViewModel.tiemposFolioState.collectAsState()
    val detencionesState by papeletaViewModel.detencionesFolioState.collectAsState()
    val detalle = papeletaViewModel.detalleActual.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }
    var etapaDialog by remember { mutableStateOf("") }

    LaunchedEffect(Folio) {
        papeletaViewModel.resetTiemposFolioState()
        papeletaViewModel.resetDetencionesFolioState()
        papeletaViewModel.cargarTiemposPorFolio(Folio)
        papeletaViewModel.cargarDetencionesPorFolio(Folio)
    }

    LaunchedEffect(tiemposState, detencionesState) {
        (tiemposState as? PapeletaViewModel.TiemposFolioState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            papeletaViewModel.resetTiemposFolioState()
        }
        (detencionesState as? PapeletaViewModel.DetencionesFolioState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            papeletaViewModel.resetDetencionesFolioState()
        }
    }

    val tiempos = (tiemposState as? PapeletaViewModel.TiemposFolioState.Success)?.lista.orEmpty()
    val detenciones = (detencionesState as? PapeletaViewModel.DetencionesFolioState.Success)?.lista.orEmpty()

    if (tiempos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin tiempos capturados o en proceso")
        }
    }

    TopAppBar(
        title = { Text("Folio: $Folio", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            Row {
                IconButton(onClick = {
                    onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                }
                IconButton(
                    onClick = {
                        generarPDF(context, Folio, tiempos, detenciones)
                    },
                    colors = IconButtonDefaults.iconButtonColors(Color.Black)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_aranceles),
                        contentDescription = "Exportar PDF"
                    )
                }
            }
        },
        actions = {
            if (detalle.isNotEmpty()) {
                IconButton(
                    onClick = { showDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(Color.Black)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.detalles),
                        contentDescription = "Ver detalles"
                    )
                }
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (tiemposState is PapeletaViewModel.TiemposFolioState.Loading ||
        detencionesState is PapeletaViewModel.DetencionesFolioState.Loading
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(tiempos) {
        modelProducer.runTransaction {
            columnSeries {
                series(tiempos.map { it.tiempo.toFloat() })
            }
            extras { it[labelKey] = tiempos.map { it.etapa } }
        }
    }

    val axisFormatter = CartesianValueFormatter { context, x, _ ->
        context.model.extraStore[labelKey].getOrNull(x.toInt()) ?: ""
    }

    // Tooltip / Marker personalizado
    val tiempoSmartFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        val column = (targets.first() as ColumnCartesianLayerMarkerTarget).columns.first()
        val tiempoSegundos = column.entry.y.toInt()
        val texto = formatTiempoSmart(tiempoSegundos)

        SpannableStringBuilder().apply {
            append(
                texto,
                ForegroundColorSpan(0xFF4CAF50.toInt()), // Verde
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            Text("Informe por etapas", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            if (tiempos.isNotEmpty()) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(
                                    fill = fill(Color(0xFF4CAF50)), // Verde
                                    thickness = 16.dp,
                                    shape = RoundedCornerShape(6.dp).toVicoShape()
                                )
                            )
                        ),
                        startAxis = VerticalAxis.rememberStart(
                            valueFormatter = { _, y, _ -> formatTiempoSmart(y.toInt()) }
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
                            valueFormatter = axisFormatter
                        ),
                        marker = rememberDefaultCartesianMarker(
                            label = rememberTextComponent(), // Puedes personalizar el label si lo deseas
                            valueFormatter = tiempoSmartFormatter
                        ),
                        layerPadding = {
                            CartesianLayerPadding(
                                scalableStartDp = 8f,
                                scalableEndDp = 8f
                            )
                        }
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    scrollState = rememberVicoScrollState(scrollEnabled = false)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        tiempos.forEach { tiempo ->
            val etapaDetenciones = detenciones.filter { it.etapa == tiempo.etapa }
            item {
                val fechafin = tiempo.fechaFin ?: ""
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(tiempo.etapa, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoText("Inicio", formatearFechaHoraLinda(tiempo.fechaInicio ?: ""))
                        if (fechafin.isNotEmpty()) {
                            InfoText("Finalización", formatearFechaHoraLinda(fechafin))
                        } else {
                            InfoText("Sin Finalizar", "En curso")
                        }
                        InfoText("Tiempo total", formatTiempoSmart(tiempo.tiempo))

                        if (etapaDetenciones.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Button(onClick = { etapaDialog = tiempo.etapa }) {
                                    Text("Ver detenciones")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        DetallePapeletaDialog(detalles = detalle, onDismiss = { showDialog = false })
    }

    if (etapaDialog.isNotEmpty()) {
        Dialog(onDismissRequest = { etapaDialog = "" }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                val lista = detenciones.filter { it.etapa == etapaDialog }
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Detenciones - $etapaDialog", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { etapaDialog = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    lista.forEach {
                        InfoText("Motivo", it.motivo)
                        InfoText("Fecha", formatearFechaHoraLinda(it.fecha))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Función para mostrar tiempos adaptativos
fun formatTiempoSmart(segundos: Int): String {
    return when {
        segundos >= 3600 -> {
            val h = segundos / 3600
            val m = (segundos % 3600) / 60
            val s = segundos % 60
            "${h}h ${m}m ${s}s"
        }
        segundos >= 60 -> {
            val m = segundos / 60
            val s = segundos % 60
            "${m}m ${s}s"
        }
        else -> "${segundos}s"
    }
}



