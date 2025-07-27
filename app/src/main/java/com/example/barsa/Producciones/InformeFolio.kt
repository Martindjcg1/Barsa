package com.example.barsa.Producciones

import android.text.Layout
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
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.barsa.data.retrofit.models.DetallePapeleta
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.TiempoRemoto
import com.patrykandpatrick.vico.core.common.component.TextComponent

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
    val detalle by papeletaViewModel.detalleActual.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var etapaDialog by remember { mutableStateOf("") }

    // 🔄 Cargar al inicio
    LaunchedEffect(Folio) {
        papeletaViewModel.cargarTiemposPorFolio(Folio)
        papeletaViewModel.cargarDetencionesPorFolio(Folio)
    }

    // 🔔 Mostrar toasts de error
    LaunchedEffect(tiemposState, detencionesState) {
        (tiemposState as? PapeletaViewModel.TiemposFolioState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
        (detencionesState as? PapeletaViewModel.DetencionesFolioState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }

    // 🚦 Manejo de estados combinados
    val isLoading = tiemposState is PapeletaViewModel.TiemposFolioState.Loading ||
            detencionesState is PapeletaViewModel.DetencionesFolioState.Loading

    val isError = tiemposState is PapeletaViewModel.TiemposFolioState.Error ||
            detencionesState is PapeletaViewModel.DetencionesFolioState.Error

    val tiempos = (tiemposState as? PapeletaViewModel.TiemposFolioState.Success)?.lista.orEmpty()
    val detenciones = (detencionesState as? PapeletaViewModel.DetencionesFolioState.Success)?.lista.orEmpty()

    // 🔄 AppBar común
    TopAppBar(
        title = { Text("Folio: $Folio", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            Row {
                IconButton(onClick = { onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                if (tiempos.isNotEmpty()) {
                    IconButton(
                        onClick = { generarPDF(context, Folio, tiempos, detenciones) },
                        modifier = Modifier
                            .border(.6.dp, Color(0x11000000), shape = CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Icon(painterResource(id = R.drawable.ic_aranceles), contentDescription = "Exportar PDF", tint = Color.Black, modifier = Modifier.background(Color.White).size(32.dp))
                    }
                }
            }
        },
        actions = {
            if (detalle.isNotEmpty() && tiempos.isNotEmpty()) {
                IconButton(onClick = { showDialog = true }, modifier = Modifier
                    .border(.6.dp, Color(0x11000000), shape = CircleShape), colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White, contentColor = Color.Black)) {
                    Icon(painterResource(id = R.drawable.detalles), contentDescription = "Ver detalles", tint = Color.Black, modifier = Modifier.background(Color.White).size(32.dp))
                }
            }
        }
    )

    // 🚥 Loading
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // ❌ Error
    if (isError) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    papeletaViewModel.resetTiemposFolioState()
                    papeletaViewModel.resetDetencionesFolioState()
                    papeletaViewModel.cargarTiemposPorFolio(Folio)
                    papeletaViewModel.cargarDetencionesPorFolio(Folio)
                }) {
                    Text("Reintentar")
                }
            }
        }
        return
    }

    // 📭 Sin tiempos
    if (tiempos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin tiempos capturados o en proceso")
        }
        return
    }

    // ✅ Contenido principal
    InformeFolioContenido(
        tiempos = tiempos,
        detenciones = detenciones,
        detalle = detalle,
        showDialog = showDialog,
        onShowDialogChange = { showDialog = it },
        etapaDialog = etapaDialog,
        onEtapaDialogChange = { etapaDialog = it }
    )
}

@Composable
fun InformeFolioContenido(
    tiempos: List<TiempoRemoto>,
    detenciones: List<DetencionRemota>,
    detalle: List<DetallePapeleta>,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    etapaDialog: String,
    onEtapaDialogChange: (String) -> Unit
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    LaunchedEffect(tiempos) {
        modelProducer.runTransaction {
            columnSeries { series(tiempos.map { it.tiempo.toFloat() }) }
            extras { it[labelKey] = tiempos.map { it.etapa } }
        }
    }

    val axisFormatter = CartesianValueFormatter { context, x, _ ->
        context.model.extraStore[labelKey].getOrNull(x.toInt()) ?: ""
    }

    val axisTextComponent = TextComponent(
        color = android.graphics.Color.BLACK,
        typeface = android.graphics.Typeface.DEFAULT,
        textSizeSp = 10f,
        textAlignment = Layout.Alignment.ALIGN_CENTER,
    )

    val tiempoSmartFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        val column = (targets.first() as ColumnCartesianLayerMarkerTarget).columns.first()
        val tiempoSegundos = column.entry.y.toInt()
        SpannableStringBuilder().apply {
            append(formatTiempoSmart(tiempoSegundos), ForegroundColorSpan(0xFF4CAF50.toInt()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    LazyColumn(Modifier.padding(16.dp)) {
        item {
            Text("Informe por etapas", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(fill = fill(Color(0xFF4CAF50)), thickness = 16.dp, shape = RoundedCornerShape(6.dp).toVicoShape())
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(valueFormatter = { _, y, _ -> formatTiempoSmart(y.toInt()) }),
                    bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = axisFormatter, itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() }, label = axisTextComponent),
                    marker = rememberDefaultCartesianMarker(
                        label = rememberTextComponent(),
                        valueFormatter = tiempoSmartFormatter
                    ),
                    layerPadding = { CartesianLayerPadding(scalableStartDp = 8f, scalableEndDp = 8f) }
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                scrollState = rememberVicoScrollState(scrollEnabled = true)
            )

            Spacer(Modifier.height(16.dp))
        }

        tiempos.forEach { tiempo ->
            val etapaDetenciones = detenciones.filter { it.etapa == tiempo.etapa }
            item {
                InformeFolioCard(
                    tiempo = tiempo,
                    etapaDetenciones = etapaDetenciones,
                    onVerDetenciones = { onEtapaDialogChange(tiempo.etapa) }
                )
            }
        }
    }

    if (showDialog) {
        DetallePapeletaDialog(detalles = detalle, onDismiss = { onShowDialogChange(false) })
    }

    if (etapaDialog.isNotEmpty()) {
        InformeFolioDetencionesDialog(etapa = etapaDialog, detenciones = detenciones, onClose = { onEtapaDialogChange("") })
    }
}

@Composable
fun InformeFolioCard(
    tiempo: TiempoRemoto,
    etapaDetenciones: List<DetencionRemota>,
    onVerDetenciones: () -> Unit
) {
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
                    Button(onClick = onVerDetenciones) {
                        Text("Ver detenciones")
                    }
                }
            }
        }
    }
}

@Composable
fun InformeFolioDetencionesDialog(
    etapa: String,
    detenciones: List<DetencionRemota>,
    onClose: () -> Unit
) {
    val lista = detenciones.filter { it.etapa == etapa }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detenciones - $etapa", fontWeight = FontWeight.Bold)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (lista.isEmpty()) {
                    Text("No hay detenciones registradas.", style = MaterialTheme.typography.bodyMedium)
                } else {
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



