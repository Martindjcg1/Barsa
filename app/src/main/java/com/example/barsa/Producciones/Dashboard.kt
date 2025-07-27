package com.example.barsa.Producciones

import android.text.Layout
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.barsa.data.retrofit.models.EtapasFinalizadasInfo
import com.example.barsa.data.retrofit.models.TiemposPeriodo
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.LocalDate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.sp
import com.example.barsa.R
import com.patrykandpatrick.vico.core.common.component.TextComponent
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformePeriodo(fechaInicio: String, fechaFin: String, onNavigate: (String) -> Unit, papeletaViewModel: PapeletaViewModel) {

    val informeState by papeletaViewModel.informePeriodoState.collectAsState()
    val context = LocalContext.current
    var error = ""

    TopAppBar(
        title = { Text("Información $fechaInicio - $fechaFin", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = { onNavigate("producciones") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
        }
    )

    LaunchedEffect(Unit) {
        papeletaViewModel.resetInformePeriodoState()
            papeletaViewModel.cargarInformePorPeriodo(fechaInicio, fechaFin)
    }

    when (informeState) {
        is PapeletaViewModel.InformePeriodoState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PapeletaViewModel.InformePeriodoState.Error -> {
            (informeState as? PapeletaViewModel.InformePeriodoState.Error)?.let {
                error = it.message
                if(error == "Fallo de conexión. Verifica tu red") {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
            }
            if(error == "Fallo de conexión. Verifica tu red") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick =
                    {
                        papeletaViewModel.resetInformePeriodoState()
                        papeletaViewModel.cargarInformePorPeriodo(fechaInicio, fechaFin)
                    })
                    {
                        Text("Reintentar")
                    }
                }
            }
            else{
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(error, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        is PapeletaViewModel.InformePeriodoState.Success -> {
            val informe = (informeState as PapeletaViewModel.InformePeriodoState.Success).informe

            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    IconButton(
                        onClick = { generarPDFInformePeriodo(context, fechaInicio, fechaFin, informe.tiemposRaw) },
                        modifier = Modifier
                            .border(.6.dp, Color(0x11000000), shape = CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Icon(painterResource(id = R.drawable.ic_aranceles), contentDescription = "Exportar PDF", tint = Color.Black, modifier = Modifier.background(Color.White).size(32.dp))
                    }

                    // 📄 Total de papeletas
                    Text("Total de papeletas:", style = MaterialTheme.typography.titleMedium)
                    Text("${informe.totalPapeletas}", style = MaterialTheme.typography.displayMedium)

                    Spacer(modifier = Modifier.height(24.dp))

                    // 📊 Información de etapas
                    Text("Etapas", style = MaterialTheme.typography.titleMedium)
                    EtapasFinalizadasPieChart(informe.etapasFinalizadas)

                    Spacer(modifier = Modifier.height(24.dp))

                    // ⏱️ Promedio por etapa — Gráfico con Vico
                    Text("Tiempo promedio por etapa", style = MaterialTheme.typography.titleMedium)
                    PromedioPorEtapaBarChart(informe.tiempoPromedioPorEtapa)

                    Spacer(modifier = Modifier.height(24.dp))

                    // ⏸️ Total detenciones — PieChart
                    Text("Detenciones", style = MaterialTheme.typography.titleMedium)
                    PieChartDetenciones(
                        activas = informe.detencionesInfo.activas,
                        inactivas = informe.detencionesInfo.inactivas
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 📊 Evolución de tiempos — Gráfica de línea con Vico
                    Text("Capturas de tiempos", style = MaterialTheme.typography.titleMedium)
                    LineChartTiemposRaw(informe.tiemposRaw)
                }
            }
        }
    }
}

@Composable
fun EtapasFinalizadasPieChart(
    etapasFinalizadas: EtapasFinalizadasInfo
) {
    val finalizadas = etapasFinalizadas.totalFinalizadas
    val sinFinalizar = etapasFinalizadas.totalSinFinalizar
    val total = finalizadas + sinFinalizar

    if (total == 0) {
        Text("Sin etapas registradas", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val colorFinalizadas = Color(0xFF4CAF50)   // Verde
    val colorSinFinalizar = Color(0xFFF44336) // Rojo

    Spacer(modifier = Modifier.height(12.dp))

    Text("$total etapas", style = MaterialTheme.typography.titleSmall)

    Spacer(modifier = Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (finalizadas > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorIndicator(color = colorFinalizadas)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$finalizadas finalizadas", style = MaterialTheme.typography.titleSmall)
            }
        }
        if (sinFinalizar > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorIndicator(color = colorSinFinalizar)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$sinFinalizar sin finalizar", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val pieData = listOf(
        Pie(
            data = finalizadas.toFloat().toDouble(),
            color = colorFinalizadas,
            label = finalizadas.toString()
        ),
        Pie(
            data = sinFinalizar.toFloat().toDouble(),
            color = colorSinFinalizar,
            label = sinFinalizar.toString()
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp)
    ) {
        PieChart(
            data = pieData,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun LineChartTiemposRaw(tiemposRaw: List<TiemposPeriodo>) {
    val scrollState = rememberVicoScrollState(scrollEnabled = true)

    if (tiemposRaw.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    // X: índices (0, 1, 2...) — Y: tiempos
    val x = tiemposRaw.indices.map { it }
    val y = tiemposRaw.map { it.tiempo.toFloat() }

    // 🏷️ Etiquetas X: Usamos la fecha (o puedes usar it.etapa si prefieres)
    val etiquetasX = tiemposRaw.map { tiempo ->
        tiempo.fechaInicio?.let { fechaStr ->
            try {
                val fecha = LocalDate.parse(fechaStr.take(10)) // yyyy-MM-dd
                "${fecha.monthValue}/${fecha.dayOfMonth}"
            } catch (e: Exception) {
                "s/f"
            }
        } ?: "s/f"
    }

    val axisTextComponent = TextComponent(
        color = android.graphics.Color.BLACK,
        typeface = android.graphics.Typeface.DEFAULT,
        textSizeSp = 10f,
        textAlignment = Layout.Alignment.ALIGN_CENTER,
    )


    LaunchedEffect(tiemposRaw) {
        modelProducer.runTransaction {
            lineSeries { series(x, y) }
        }
    }

    val lineColor = Color(0xFF4CAF50) // Verde

    val axisYFormatter = CartesianValueFormatter { _, yVal, _ ->
        formatTiempoSmart(yVal.toInt())
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            fill(
                                ShaderProvider.verticalGradient(
                                    arrayOf(lineColor.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                        ),
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = axisYFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(
                itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    etiquetasX.getOrNull(value.toInt()) ?: ""
                },
                label = axisTextComponent // 🎯 Aquí aplicas tu estilo
            )

        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        scrollState = scrollState
    )
}

@Composable
fun PieChartDetenciones(
    activas: Int,
    inactivas: Int
) {
    val total = activas + inactivas

    if (total == 0) {
        Text("Sin detenciones registradas", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val colorActivas = Color(0xFFF44336)   // Rojo
    val colorInactivas = Color(0xFF4CAF50) // Verde

    Spacer(modifier = Modifier.height(12.dp))

    Text("$total detenciones", style = MaterialTheme.typography.titleSmall)

    Spacer(modifier = Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (activas > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorIndicator(color = colorActivas)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$activas activas", style = MaterialTheme.typography.titleSmall)
            }
        }
        if (inactivas > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorIndicator(color = colorInactivas)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$inactivas inactivas", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val pieData = listOf(
        Pie(
            data = activas.toFloat().toDouble(),
            color = colorActivas,
            label = activas.toString() // Solo el número
        ),
        Pie(
            data = inactivas.toFloat().toDouble(),
            color = colorInactivas,
            label = inactivas.toString() // Solo el número
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp)
    ) {
        PieChart(
            data = pieData,
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
fun ColorIndicator(
    color: Color,
    isCircle: Boolean = true // Cambia a false si prefieres cuadrado
) {
    Canvas(
        modifier = Modifier
            .size(10.dp) // Tamaño pequeño como pediste
    ) {
        if (isCircle) {
            drawCircle(color = color)
        } else {
            drawRect(color = color)
        }
    }
}


@Composable
fun PromedioPorEtapaBarChart(tiempoPromedioPorEtapa: Map<String, Float>) {
    val scrollState = rememberVicoScrollState(scrollEnabled = true)

    if (tiempoPromedioPorEtapa.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val labelKey = remember { ExtraStore.Key<List<String>>() }

    // Preparar los datos para la gráfica
    val valores = tiempoPromedioPorEtapa.values.toList()
    val etiquetas = tiempoPromedioPorEtapa.keys.toList()

    LaunchedEffect(tiempoPromedioPorEtapa) {
        modelProducer.runTransaction {
            columnSeries {
                series(valores)
            }
            extras { it[labelKey] = etiquetas }
        }
    }

    val axisFormatter = CartesianValueFormatter { _, x, _ ->
        etiquetas.getOrNull(x.toInt()) ?: ""
    }

    val axisTextComponent = TextComponent(
        color = android.graphics.Color.BLACK,
        typeface = android.graphics.Typeface.DEFAULT,
        textSizeSp = 10f,
        textAlignment = Layout.Alignment.ALIGN_CENTER,
    )

    val tiempoSmartFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        val column = (targets.firstOrNull() as? ColumnCartesianLayerMarkerTarget)?.columns?.firstOrNull()
        val tiempoSegundos = column?.entry?.y?.toInt() ?: 0
        val texto = formatTiempoSmart(tiempoSegundos)

        SpannableStringBuilder().apply {
            append(
                texto,
                ForegroundColorSpan(0xFF4CAF50.toInt()), // Verde
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(Color(0xFF4CAF50)),
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
                valueFormatter = axisFormatter,
                label = axisTextComponent
            ),
            marker = rememberDefaultCartesianMarker(
                label = rememberTextComponent(),
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
        scrollState = scrollState
    )
}

