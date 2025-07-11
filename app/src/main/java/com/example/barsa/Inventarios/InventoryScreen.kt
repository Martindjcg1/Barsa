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
    userViewModel: UserViewModel
) {
    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var adminAction by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showTransactionOptions by remember { mutableStateOf(false) }
    var transactionAction by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val createMaterialState by inventoryViewModel.createMaterialState.collectAsState()
    val updateMaterialState by inventoryViewModel.updateMaterialState.collectAsState()

    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.obtenerInfoUsuarioPersonal()
    }

    val hasInventoryRole = remember(infoUsuarioResult) {
        infoUsuarioResult?.getOrNull()?.rol?.lowercase()?.contains("inventarios") == true
    }

    // Lista de todas las categorías posibles con sus iconResId
    // ¡Aquí es donde se pasa el iconResId a cada InventoryCategory!
    val categories = remember {
        listOf(
            InventoryCategory(1, "Cubetas", "Catálogo de cubetas", R.drawable.ic_pintura),
            InventoryCategory(2, "Telas", "Catálogo de telas", R.drawable.ic_fabric),
            InventoryCategory(3, "Cascos", "Catálogo de cascos", R.drawable.ic_edge),
            InventoryCategory(4, "Herramientas", "Catálogo de herramientas", R.drawable.ic_herramientas),
            InventoryCategory(5, "Bisagras y Herrajes", "Catálogo de bisagras y herrajes", R.drawable.ic_bolt),
            InventoryCategory(6, "Pernos y Sujetadores", "Catálogo de pernos y sujetadores", R.drawable.ic_materiales),
            InventoryCategory(7, "Cintas y Adhesivos", "Catálogo de cintas y adhesivos", R.drawable.ic_tape),
            InventoryCategory(8, "Separadores y Accesorios de Cristal", "Catálogo de separadores", R.drawable.ic_glass),
            InventoryCategory(9, "Cubrecantos y Acabados", "Catálogo de cubrecantos", R.drawable.ic_tape), // Puedes usar otro icono si tienes
            InventoryCategory(10, "Otros Materiales de Construcción", "Otros materiales de construcción", R.drawable.ic_all),
            InventoryCategory(11, "Todo", "Catálogo completo", R.drawable.ic_all) // Icono para "Todo"
        )
    }

    val primaryBrown = MaterialTheme.colorScheme.primary
    val accentBrown = MaterialTheme.colorScheme.secondary

    LaunchedEffect(createMaterialState) {
        when (createMaterialState) {
            is InventoryViewModel.CreateMaterialState.Success -> {
                successMessage = "Material creado exitosamente: ${(createMaterialState as InventoryViewModel.CreateMaterialState.Success).response.message}"
                showSuccessDialog = true
                adminAction = null
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

    LaunchedEffect(updateMaterialState) {
        when (updateMaterialState) {
            is InventoryViewModel.UpdateMaterialState.Success -> {
                successMessage = "Material actualizado exitosamente: ${(updateMaterialState as InventoryViewModel.UpdateMaterialState.Success).response.message}"
                showSuccessDialog = true
                selectedItem = null
                adminAction = "edit"
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
            if (hasInventoryRole) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
                            contentDescription = "Panel de Inventarios",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

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

        if (hasInventoryRole) {
            AnimatedVisibility(
                visible = showAdminPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InventoryPanel(
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
                            tint = MaterialTheme.colorScheme.primary,
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

        when {
            transactionAction == "entry" -> {
                // Comentado por ahora
            }
            transactionAction == "exit" -> {
                // Comentado por ahora
            }
            transactionAction == "history" -> {
                // Comentado por ahora
            }
            adminAction == "add" -> {
                AddInventoryScreen(
                    categories = categories,
                    onCancel = {
                        adminAction = null
                        inventoryViewModel.resetCreateMaterialState()
                    },
                    onSave = { newItem ->
                        // El manejo del éxito se hace en el LaunchedEffect arriba
                    },
                    inventoryViewModel = inventoryViewModel
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
                    },
                    inventoryViewModel = inventoryViewModel
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
                    inventoryViewModel = inventoryViewModel
                )
            }
            selectedCategory == null -> {
                CategoryList(
                    categories = categories,
                    onCategorySelected = { selectedCategory = it },
                    inventoryState = inventoryState // Se pasa inventoryState, aunque CategoryList ya no lo use para itemCount
                )
            }
            else -> {
                InventoryItemsList(
                    category = selectedCategory!!,
                    onBackClick = {
                        selectedCategory = null
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
                text = "Panel de Inventarios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InventoryButton(
                    icon = Icons.Default.Add,
                    text = "Agregar",
                    onClick = onAddInventory
                )
                InventoryButton(
                    icon = Icons.Default.Edit,
                    text = "Editar",
                    onClick = onEditInventory
                )
                InventoryButton(
                    icon = Icons.Default.Delete,
                    text = "Eliminar",
                    onClick = onDeleteInventory
                )
            }
        }
    }
}

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
