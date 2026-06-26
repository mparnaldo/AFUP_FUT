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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.exceptions.GetCredentialException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MatchViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegisterProfile: () -> Unit
) {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()

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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Divisor "OU"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceLightDark)
                        Text(
                            text = "OU",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceLightDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botão Google
                    OutlinedButton(
                        onClick = {
                            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                            val webClientId = if (resId != 0) context.getString(resId) else null
                            if (webClientId.isNullOrBlank()) {
                                loginError = "ID do cliente web não configurado."
                                return@OutlinedButton
                            }

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(webClientId)
                                .setAutoSelectEnabled(true)
                                .build()

                            val getCredRequest = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            coroutineScope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = getCredRequest
                                    )
                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val idToken = googleIdTokenCredential.idToken
                                        viewModel.signInWithGoogle(
                                            idToken = idToken,
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
                                    } else {
                                        loginError = "Credencial do Google inválida."
                                    }
                                } catch (e: GetCredentialException) {
                                    loginError = "Erro no Google Sign-In: ${e.localizedMessage}"
                                } catch (e: Exception) {
                                    loginError = "Erro inesperado: ${e.localizedMessage}"
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary,
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.dp, SurfaceLightDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogo(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Entrar com o Google",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
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

@Composable
fun GoogleLogo(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val sizeMin = minOf(width, height)
        val strokeWidth = sizeMin * 0.18f
        val radius = (sizeMin - strokeWidth) / 2
        val cx = width / 2
        val cy = height / 2

        // Draw the circular arc
        val path = Path().apply {
            addArc(
                oval = Rect(cx - radius, cy - radius, cx + radius, cy + radius),
                startAngleDegrees = 45f,
                sweepAngleDegrees = 270f
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw horizontal line
        drawLine(
            color = color,
            start = Offset(cx, cy),
            end = Offset(cx + radius, cy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
