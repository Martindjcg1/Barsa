package com.example.barsa.Login

import android.content.Context
import android.graphics.Canvas

import android.graphics.Paint
import androidx.compose.ui.graphics.Path as ComposePath

import android.util.AttributeSet
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lint.kotlin.metadata.Visibility
import com.example.barsa.R
import com.example.barsa.data.retrofit.ui.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    onLoginClick: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val primaryBrown = Color(0xFF8B4513) // Marrón oscuro
    val lightBrown = Color(0xFFDEB887)   // Marrón claro
    val accentBrown = Color(0xFF654321)  // Marrón medio

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(lightBrown, primaryBrown),
                    tileMode = TileMode.Clamp
                )
            )
    ) {
        // Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawDecorations(accentBrown)
        }

        // Contenedor blanco para los campos de entrada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isLandscape) 16.dp else 32.dp)
                .align(Alignment.Center)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = true
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(if (isLandscape) 16.dp else 24.dp)
        ) {
            if (isLandscape) {
                // Diseño horizontal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Barsa Muebles Logo",
                        modifier = Modifier
                            .weight(1f)
                            .size(150.dp)
                            .padding(end = 16.dp)
                    )

                    // Campos de entrada
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Username TextField
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuario") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentBrown,
                                unfocusedBorderColor = lightBrown
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password TextField
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentBrown,
                                unfocusedBorderColor = lightBrown
                            ),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (passwordVisible) {
                                                R.drawable.ic_visibility
                                            } else {
                                                R.drawable.ic_visibility_off
                                            }
                                        ),
                                        contentDescription = if (passwordVisible) {
                                            "Ocultar contraseña"
                                        } else {
                                            "Mostrar contraseña"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Login Button
                        Button(
                            onClick = { onLoginClick(username, password) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentBrown
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                "Entrar",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                // Diseño vertical
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Barsa Muebles Logo",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Username TextField
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBrown,
                            unfocusedBorderColor = lightBrown
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password TextField
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBrown,
                            unfocusedBorderColor = lightBrown
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) {
                                            R.drawable.ic_visibility
                                        } else {
                                            R.drawable.ic_visibility_off
                                        }
                                    ),
                                    contentDescription = if (passwordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Login Button
                    Button(
                        onClick = { onLoginClick(username, password) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentBrown
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            "Entrar",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDecorations(accentBrown: Color) {
    val path = ComposePath().apply {
        moveTo(0f, size.height * 0.25f)
        lineTo(size.width * 0.25f, 0f)
        lineTo(size.width * 0.75f, 0f)
        lineTo(size.width, size.height * 0.25f)
        close()
    }
    drawPath(
        path = path,
        color = accentBrown.copy(alpha = 0.1f)
    )
}