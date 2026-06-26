package com.afup.afupfut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afup.afupfut.ui.theme.*
import com.afup.afupfut.ui.viewmodel.MatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MatchViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegisterProfile: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Detalhe de fundo: Brilho neon sutil
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonGreen.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone temático de Futebol
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = "Logo",
                tint = NeonGreen,
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AFUP FUT",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.horizontalGradient(listOf(NeonGreen, ElectricCyan))
                )
            )

            Text(
                text = "Gestão de Peladas do Clube AFUP",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Card principal de login (Glassmorphism sutil)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Criar Conta" else "Iniciar Sessão",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo de E-mail
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; loginError = null },
                        label = { Text("E-mail", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SurfaceLightDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Senha
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; loginError = null },
                        label = { Text("Senha", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SurfaceLightDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Erro
                    loginError?.let { err ->
                        Text(
                            text = err,
                            color = RedError,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Ação Principal
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                loginError = "Preencha todos os campos."
                                return@Button
                            }
                            if (isRegisterMode) {
                                viewModel.signUp(email, password,
                                    onSuccess = {
                                        onNavigateToRegisterProfile()
                                    },
                                    onError = { loginError = it }
                                )
                            } else {
                                viewModel.signIn(email, password,
                                    onSuccess = {
                                        viewModel.currentUserProfile?.let { profile ->
                                            if (profile.name.isBlank()) {
                                                onNavigateToRegisterProfile()
                                            } else {
                                                onLoginSuccess()
                                            }
                                        } ?: onNavigateToRegisterProfile()
                                    },
                                    onError = { loginError = it }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isRegisterMode) "CADASTRAR" else "ENTRAR",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = BackgroundDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alternar Modo (Cadastro / Login)
                    TextButton(
                        onClick = { isRegisterMode = !isRegisterMode; loginError = null }
                    ) {
                        Text(
                            text = if (isRegisterMode) "Já tem conta? Faça login" else "Não tem conta? Cadastre-se",
                            color = ElectricCyan,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
