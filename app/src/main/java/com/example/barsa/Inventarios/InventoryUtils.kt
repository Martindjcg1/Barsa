package com.example.barsa.Body.Inventory


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barsa.Models.InventoryCategory // Asegúrate que esta ruta sea correcta
import com.example.barsa.R // Asegúrate de que R esté importado correctamente
import com.example.barsa.data.retrofit.ui.InventoryViewModel // Asegúrate que esta ruta sea correcta

@Composable
fun CategoryList(
    categories: List<InventoryCategory>,
    onCategorySelected: (InventoryCategory) -> Unit,
    inventoryState: InventoryViewModel.InventoryState
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: InventoryCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fondo con gradiente sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                getCategoryColor(category.name).copy(alpha = 0.1f),
                                getCategoryColor(category.name).copy(alpha = 0.05f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contenedor del icono con fondo circular
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = getCategoryColor(category.name).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // ¡Usando category.iconResId directamente!
                    Icon(
                        painter = painterResource(id = category.iconResId),
                        contentDescription = category.name,
                        modifier = Modifier.size(28.dp),
                        tint = getCategoryColor(category.name)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Nombre de la categoría
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// Función para obtener el color de cada categoría
@Composable
fun getCategoryColor(categoryName: String): Color {
    return when (categoryName) {
        "Todo" -> Color(0xFF6366F1) // Indigo
        "Cubetas" -> Color(0xFF3B82F6) // Blue
        "Telas" -> Color(0xFF8B5CF6) // Purple
        "Cascos" -> Color(0xFF10B981) // Emerald
        "Herramientas" -> Color(0xFFF59E0B) // Amber
        "Bisagras y Herrajes" -> Color(0xFFEF4444) // Red
        "Pernos y Sujetadores" -> Color(0xFF6B7280) // Gray
        "Cintas y Adhesivos" -> Color(0xFFEC4899) // Pink
        "Separadores y Accesorios de Cristal" -> Color(0xFF06B6D4) // Cyan
        "Cubrecantos y Acabados" -> Color(0xFF84CC16) // Lime
        else -> Color(0xFF64748B) // Slate
    }
}

// Función helper para categorizar materiales (se mantiene igual)
fun categorizarMaterial(descripcion: String): String {
    return when {
        descripcion.contains("cubeta", ignoreCase = true) -> "Cubetas"
        descripcion.contains("tela", ignoreCase = true) -> "Telas"
        descripcion.contains("casco", ignoreCase = true) -> "Cascos"
        descripcion.contains("Llave", ignoreCase = true) -> "Llave"
        descripcion.contains("bisagra", ignoreCase = true) ||
                descripcion.contains("herraje", ignoreCase = true) -> "Bisagras y Herrajes"
        descripcion.contains("perno", ignoreCase = true) ||
                descripcion.contains("tornillo", ignoreCase = true) -> "Pernos y Sujetadores"
        descripcion.contains("cinta", ignoreCase = true) ||
                descripcion.contains("adhesivo", ignoreCase = true) -> "Cintas y Adhesivos"
        descripcion.contains("cristal", ignoreCase = true) ||
                descripcion.contains("vidrio", ignoreCase = true) -> "Separadores y Accesorios de Cristal"
        descripcion.contains("cubrecanto", ignoreCase = true) -> "Cubrecantos y Acabados"
        else -> "Otros Materiales de Construcción"
    }
}
