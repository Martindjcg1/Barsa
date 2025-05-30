package com.example.barsa.Body.Inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem
import androidx.compose.ui.res.painterResource
import com.example.barsa.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.barsa.Inventarios.InventoryChangesScreen


@Composable
fun InventoryScreen(onNavigate: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var adminAction by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showTransactionOptions by remember { mutableStateOf(false) }
    var transactionAction by remember { mutableStateOf<String?>(null) }

    // Lista de todas las categorías posibles
    val categories = remember {
        listOf(
            InventoryCategory(1, "Cubetas", "Catálogo de cubetas", R.drawable.ic_pintura),
            InventoryCategory(2, "Telas", "Catálogo de telas", R.drawable.ic_fabric),
            InventoryCategory(3, "Cascos", "Catálogo de cascos", R.drawable.ic_helmet),
            InventoryCategory(4, "Herramientas", "Catálogo de herramientas", R.drawable.ic_herramientas),
            InventoryCategory(5, "Bisagras y Herrajes", "Catálogo de bisagras y herrajes", R.drawable.ic_hinge),
            InventoryCategory(6, "Pernos y Sujetadores", "Catálogo de pernos y sujetadores", R.drawable.ic_bolt),
            InventoryCategory(7, "Cintas y Adhesivos", "Catálogo de cintas y adhesivos", R.drawable.ic_tape),
            InventoryCategory(8, "Separadores y Accesorios de Cristal", "Catálogo de separadores", R.drawable.ic_glass),
            InventoryCategory(9, "Cubrecantos y Acabados", "Catálogo de cubrecantos", R.drawable.ic_edge),
            InventoryCategory(10, "Otros Materiales de Construcción", "Otros materiales de construcción", R.drawable.ic_materiales),
            InventoryCategory(11, "Todo", "Catálogo completo", R.drawable.ic_all)
        )
    }

    // Colores de la mueblería
    val primaryBrown = MaterialTheme.colorScheme.primary
    val accentBrown = MaterialTheme.colorScheme.secondary

    // Nombre del usuario actual (en una implementación real, esto vendría de un sistema de autenticación)
    val currentUser = remember { "Martin Castañeda" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado con título, botón de transacciones y botón de administrador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de transacciones (entradas/salidas)
                IconButton(
                    onClick = { showTransactionOptions = !showTransactionOptions },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (showTransactionOptions) MaterialTheme.colorScheme.tertiary else primaryBrown,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Entradas/Salidas",
                        tint = Color.White
                    )
                }

                // Botón de administrador
                IconButton(
                    onClick = { showAdminPanel = !showAdminPanel },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (showAdminPanel) accentBrown else primaryBrown,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Panel de Administrador",
                        tint = Color.White
                    )
                }
            }
        }

        // Panel de transacciones (visible solo cuando showTransactionOptions es true)
        AnimatedVisibility(
            visible = showTransactionOptions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TransactionPanel(
                onRegisterEntry = { transactionAction = "entry" },
                onRegisterExit = { transactionAction = "exit" },
                onViewHistory = { transactionAction = "history" }
            )
        }

        // Panel de administrador (visible solo cuando showAdminPanel es true)
        AnimatedVisibility(
            visible = showAdminPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            AdminPanel(
                onAddInventory = { adminAction = "add" },
                onEditInventory = {
                    if (selectedCategory == null) {
                        // Si no hay categoría seleccionada, mostrar mensaje
                        adminAction = "select_category_first"
                    } else {
                        adminAction = "edit"
                    }
                },
                onDeleteInventory = {
                    if (selectedCategory == null) {
                        // Si no hay categoría seleccionada, mostrar mensaje
                        adminAction = "select_category_first"
                    } else {
                        adminAction = "delete"
                    }
                }
            )
        }

        // Mensaje para seleccionar categoría primero
        if (adminAction == "select_category_first") {
            AlertDialog(
                onDismissRequest = { adminAction = null },
                title = { Text("Seleccione una categoría") },
                text = { Text("Por favor, seleccione una categoría primero para poder editar o eliminar inventario.") },
                confirmButton = {
                    Button(onClick = { adminAction = null }) {
                        Text("Entendido")
                    }
                }
            )
        }

        // Contenido principal
        when {
            // Acciones de transacción
            transactionAction == "entry" -> {
                InventoryEntriesScreen(
                    onBackClick = { transactionAction = null }
                )
            }
            transactionAction == "exit" -> {
                InventoryExitsScreen(
                    onBackClick = { transactionAction = null }
                )
            }
            transactionAction == "history" -> {
                InventoryChangesScreen(
                    onBackClick = { transactionAction = null }
                )
            }

            // Acciones de administrador
            adminAction == "add" -> {
                AddInventoryScreen(
                    categories = categories,
                    onCancel = { adminAction = null },
                    onSave = { newItem ->
                        // Aquí se guardaría el nuevo item en la base de datos
                        // Por ahora solo cerramos la pantalla
                        adminAction = null
                    }
                )
            }
            selectedItem != null -> {
                // Pantalla de edición de item específico
                EditItemScreen(
                    item = selectedItem!!,
                    onCancel = {
                        selectedItem = null
                        // Si estábamos en modo edición, volvemos a la lista de edición
                        if (adminAction == "edit") {
                            adminAction = "edit"
                        }
                    },
                    onSave = { updatedItem ->
                        // Aquí se guardaría el item actualizado en la base de datos
                        selectedItem = null
                        // Si estábamos en modo edición, volvemos a la lista de edición
                        if (adminAction == "edit") {
                            adminAction = "edit"
                        }
                    }
                )
            }
            adminAction == "edit" && selectedCategory != null -> {
                EditInventoryScreen(
                    category = selectedCategory!!,
                    onCancel = { adminAction = null },
                    onItemSelected = { item ->
                        selectedItem = item
                        // No cambiamos adminAction para mantener el contexto de edición
                    }
                )
            }
            adminAction == "delete" && selectedCategory != null -> {
                DeleteInventoryScreen(
                    category = selectedCategory!!,
                    onCancel = { adminAction = null }
                )
            }

            // Vista normal de inventario
            selectedCategory == null -> {
                CategoryList(
                    categories = categories,
                    onCategorySelected = { selectedCategory = it }
                )
            }
            else -> {
                InventoryItemsList(
                    category = selectedCategory!!,
                    onBackClick = { selectedCategory = null },
                    onItemClick = { item ->
                        if (adminAction == "edit") {
                            selectedItem = item
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionPanel(
    onRegisterEntry: () -> Unit,
    onRegisterExit: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Movimientos de Inventario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TransactionButton(
                    icon = Icons.Default.AddCircle,
                    text = "Entrada",
                    onClick = onRegisterEntry
                )

                TransactionButton(
                    icon = Icons.Default.ExitToApp,
                    text = "Salida",
                    onClick = onRegisterExit
                )

                TransactionButton(
                    icon = Icons.Default.Email,
                    text = "Historial",
                    onClick = onViewHistory
                )
            }
        }
    }
}

@Composable
fun TransactionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(6.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun AdminPanel(
    onAddInventory: () -> Unit,
    onEditInventory: () -> Unit,
    onDeleteInventory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Panel de Administrador",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdminButton(
                    icon = Icons.Default.Add,
                    text = "Agregar",
                    onClick = onAddInventory
                )

                AdminButton(
                    icon = Icons.Default.Edit,
                    text = "Editar",
                    onClick = onEditInventory
                )

                AdminButton(
                    icon = Icons.Default.Delete,
                    text = "Eliminar",
                    onClick = onDeleteInventory
                )
            }
        }
    }
}

@Composable
fun AdminButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .padding(6.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
