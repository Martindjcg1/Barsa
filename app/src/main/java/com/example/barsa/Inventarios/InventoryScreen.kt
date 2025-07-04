package com.example.barsa.Body.Inventory

import androidx.compose.animation.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.InventoryCategory

import com.example.barsa.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.barsa.Inventarios.InventoryChangesScreen
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel

@Composable
fun InventoryScreen(
    onNavigate: (String) -> Unit,
    inventoryViewModel: InventoryViewModel,
    userViewModel: UserViewModel // NUEVO: Agregar UserViewModel para obtener rol del usuario
) {
    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var adminAction by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showTransactionOptions by remember { mutableStateOf(false) }
    var transactionAction by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Obtener el estado del ViewModel
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val createMaterialState by inventoryViewModel.createMaterialState.collectAsState()
    val updateMaterialState by inventoryViewModel.updateMaterialState.collectAsState()

    // NUEVO: Observar información del usuario para verificar rol
    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()

    // NUEVO: Cargar información del usuario al inicializar
    LaunchedEffect(Unit) {
        userViewModel.obtenerInfoUsuarioPersonal()
    }

    // NUEVO: Verificar si el usuario tiene rol de inventarios
    val hasInventoryRole = remember(infoUsuarioResult) {
        infoUsuarioResult?.getOrNull()?.rol?.lowercase()?.contains("inventarios") == true
    }

    // Lista de todas las categorías posibles
    val categories = remember {
        listOf(
            InventoryCategory(1, "Cubetas", "Catálogo de cubetas", 0),
            InventoryCategory(2, "Telas", "Catálogo de telas", 0),
            InventoryCategory(3, "Cascos", "Catálogo de cascos", 0),
            InventoryCategory(4, "Herramientas", "Catálogo de herramientas", 0),
            InventoryCategory(5, "Bisagras y Herrajes", "Catálogo de bisagras y herrajes", 0),
            InventoryCategory(6, "Pernos y Sujetadores", "Catálogo de pernos y sujetadores", 0),
            InventoryCategory(7, "Cintas y Adhesivos", "Catálogo de cintas y adhesivos", 0),
            InventoryCategory(8, "Separadores y Accesorios de Cristal", "Catálogo de separadores", 0),
            InventoryCategory(9, "Cubrecantos y Acabados", "Catálogo de cubrecantos", 0),
            InventoryCategory(10, "Otros Materiales de Construcción", "Otros materiales de construcción", 0),
            InventoryCategory(11, "Todo", "Catálogo completo", 0)
        )
    }

    // Colores de la mueblería
    val primaryBrown = MaterialTheme.colorScheme.primary
    val accentBrown = MaterialTheme.colorScheme.secondary

    // Manejar el resultado de crear material
    LaunchedEffect(createMaterialState) {
        when (createMaterialState) {
            is InventoryViewModel.CreateMaterialState.Success -> {
                successMessage = "Material creado exitosamente: ${(createMaterialState as InventoryViewModel.CreateMaterialState.Success).response.message}"
                showSuccessDialog = true
                adminAction = null
                // Recargar datos si hay una categoría seleccionada
                selectedCategory?.let {
                    val categoryFilter = when (it.name) {
                        "Todo" -> null
                        "Cubetas" -> "cubeta"
                        "Telas" -> "tela"
                        "Cascos" -> "casco"
                        "Herramientas" -> "herramienta"
                        "Bisagras y Herrajes" -> "bisagra"
                        "Pernos y Sujetadores" -> "perno"
                        "Cintas y Adhesivos" -> "cinta"
                        "Separadores y Accesorios de Cristal" -> "cristal"
                        "Cubrecantos y Acabados" -> "cubrecanto"
                        else -> null
                    }
                    inventoryViewModel.getInventoryItems(page = 1, descripcion = categoryFilter)
                }
                inventoryViewModel.resetCreateMaterialState()
            }
            is InventoryViewModel.CreateMaterialState.Error -> {
                // El error se maneja en AddInventoryScreen
            }
            else -> { /* No hacer nada */ }
        }
    }

    // Manejar el resultado de actualizar material
    LaunchedEffect(updateMaterialState) {
        when (updateMaterialState) {
            is InventoryViewModel.UpdateMaterialState.Success -> {
                successMessage = "Material actualizado exitosamente: ${(updateMaterialState as InventoryViewModel.UpdateMaterialState.Success).response.message}"
                showSuccessDialog = true
                selectedItem = null
                adminAction = "edit" // Volver a la lista de edición
                // Recargar datos si hay una categoría seleccionada
                selectedCategory?.let {
                    val categoryFilter = when (it.name) {
                        "Todo" -> null
                        "Cubetas" -> "cubeta"
                        "Telas" -> "tela"
                        "Cascos" -> "casco"
                        "Herramientas" -> "herramienta"
                        "Bisagras y Herrajes" -> "bisagra"
                        "Pernos y Sujetadores" -> "perno"
                        "Cintas y Adhesivos" -> "cinta"
                        "Separadores y Accesorios de Cristal" -> "cristal"
                        "Cubrecantos y Acabados" -> "cubrecanto"
                        else -> null
                    }
                    inventoryViewModel.getInventoryItems(page = 1, descripcion = categoryFilter)
                }
                inventoryViewModel.resetUpdateMaterialState()
            }
            is InventoryViewModel.UpdateMaterialState.Error -> {
                // El error se maneja en EditItemScreen
            }
            else -> { /* No hacer nada */ }
        }
    }

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

            // MODIFICADO: Solo mostrar botones si tiene rol de inventarios
            if (hasInventoryRole) {
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
                            contentDescription = "Panel de Inventarios", // MODIFICADO: Cambiar descripción
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // MODIFICADO: Panel de transacciones (visible solo cuando showTransactionOptions es true Y tiene rol de inventarios)
        if (hasInventoryRole) {
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
        }

        // MODIFICADO: Panel de administrador (visible solo cuando showAdminPanel es true Y tiene rol de inventarios)
        if (hasInventoryRole) {
            AnimatedVisibility(
                visible = showAdminPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InventoryPanel( // MODIFICADO: Cambiar nombre del componente
                    onAddInventory = { adminAction = "add" },
                    onEditInventory = {
                        if (selectedCategory == null) {
                            adminAction = "select_category_first"
                        } else {
                            adminAction = "edit"
                        }
                    },
                    onDeleteInventory = {
                        if (selectedCategory == null) {
                            adminAction = "select_category_first"
                        } else {
                            adminAction = "delete"
                        }
                    }
                )
            }
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

        // Diálogo de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color.Green,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (successMessage.contains("creado")) "Material Creado"
                            else "Material Actualizado"
                        )
                    }
                },
                text = { Text(successMessage) },
                confirmButton = {
                    Button(onClick = { showSuccessDialog = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }

        // Contenido principal
        when {
            // Acciones de transacción
            transactionAction == "entry" -> {
                // Comentado por ahora
            }
            transactionAction == "exit" -> {
                // Comentado por ahora
            }
            transactionAction == "history" -> {
                // Comentado por ahora
            }

            // Acciones de administrador
            adminAction == "add" -> {
                AddInventoryScreen(
                    categories = categories,
                    onCancel = {
                        adminAction = null
                        inventoryViewModel.resetCreateMaterialState()
                    },
                    onSave = { newItem ->
                        // El manejo del éxito se hace en el LaunchedEffect arriba
                        // No necesitamos hacer nada aquí porque el estado se maneja automáticamente
                    },
                    inventoryViewModel = inventoryViewModel // Pasar el ViewModel
                )
            }
            selectedItem != null -> {
                EditItemScreen(
                    item = selectedItem!!,
                    onCancel = {
                        selectedItem = null
                        if (adminAction == "edit") {
                            adminAction = "edit"
                        }
                        inventoryViewModel.resetUpdateMaterialState()
                    },
                    onSave = { updatedItem ->
                        // El manejo del éxito se hace en el LaunchedEffect arriba
                        // selectedItem se resetea automáticamente en el LaunchedEffect
                    },
                    inventoryViewModel = inventoryViewModel // Pasar el ViewModel
                )
            }
            adminAction == "edit" && selectedCategory != null -> {
                EditInventoryScreen(
                    category = selectedCategory!!,
                    onCancel = { adminAction = null },
                    onItemSelected = { item ->
                        selectedItem = item
                    },
                    inventoryViewModel = inventoryViewModel
                )
            }
            adminAction == "delete" && selectedCategory != null -> {
                DeleteInventoryScreen(
                    category = selectedCategory!!,
                    onCancel = { adminAction = null },
                    inventoryViewModel = inventoryViewModel // Pasar el ViewModel
                )
            }

            // Vista normal de inventario
            selectedCategory == null -> {
                // Mostrar solo las categorías, sin cargar datos aún
                CategoryList(
                    categories = categories,
                    onCategorySelected = { selectedCategory = it },
                    inventoryState = InventoryViewModel.InventoryState.Initial // Estado inicial
                )
            }
            else -> {
                // Mostrar lista de items - el LaunchedEffect está ahora solo en InventoryItemsList
                InventoryItemsList(
                    category = selectedCategory!!,
                    onBackClick = {
                        selectedCategory = null
                        // Limpiar el estado cuando se regresa
                        inventoryViewModel.resetInventoryState()
                        inventoryViewModel.resetSearchState()
                    },
                    onItemClick = { item ->
                        if (adminAction == "edit") {
                            selectedItem = item
                        }
                    },
                    inventoryViewModel = inventoryViewModel
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

// MODIFICADO: Cambiar nombre de AdminPanel a InventoryPanel
@Composable
fun InventoryPanel(
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
                text = "Panel de Inventarios", // MODIFICADO: Cambiar título
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InventoryButton( // MODIFICADO: Cambiar nombre de AdminButton a InventoryButton
                    icon = Icons.Default.Add,
                    text = "Agregar",
                    onClick = onAddInventory
                )

                InventoryButton( // MODIFICADO: Cambiar nombre de AdminButton a InventoryButton
                    icon = Icons.Default.Edit,
                    text = "Editar",
                    onClick = onEditInventory
                )

                InventoryButton( // MODIFICADO: Cambiar nombre de AdminButton a InventoryButton
                    icon = Icons.Default.Delete,
                    text = "Eliminar",
                    onClick = onDeleteInventory
                )
            }
        }
    }
}

// MODIFICADO: Cambiar nombre de AdminButton a InventoryButton
@Composable
fun InventoryButton(
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
