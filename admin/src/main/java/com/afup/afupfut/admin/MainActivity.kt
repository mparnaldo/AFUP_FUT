package com.afup.afupfut.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afup.afupfut.admin.ui.screens.AdminPanelScreen
import com.afup.afupfut.ui.screens.LoginScreen
import com.afup.afupfut.ui.screens.SoccerFieldScreen
import com.afup.afupfut.ui.theme.AFUP_FUTTheme
import com.afup.afupfut.ui.theme.BackgroundDark
import com.afup.afupfut.ui.theme.RedError
import com.afup.afupfut.ui.viewmodel.MatchViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AFUP_FUTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val isSuperuser = viewModel.currentUserEmail?.equals("mpires.arnaldo@gmail.com", ignoreCase = true) == true || viewModel.currentUserProfile?.isAdmin == true
                    val isAuthorized = isSuperuser || viewModel.currentUserProfile?.isManager == true

                    val startDestination = when {
                        viewModel.isCheckingAuth -> "splash"
                        viewModel.currentUserProfile == null -> "login"
                        !isAuthorized -> "unauthorized"
                        else -> "admin_panel"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("splash") {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    if (isAuthorized) {
                                        navController.navigate("admin_panel") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("unauthorized") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToRegisterProfile = {
                                    // Desativado no Admin App
                                }
                            )
                        }
                        composable("unauthorized") {
                            UnauthorizedScreen(
                                onSignOut = {
                                    viewModel.signOut(onSuccess = {
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    })
                                }
                            )
                        }
                        composable("admin_panel") {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    // Tela principal, não volta
                                },
                                onNavigateToField = {
                                    navController.navigate("soccer_field")
                                }
                            )
                        }
                        composable("soccer_field") {
                            SoccerFieldScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    // Redirecionamento em caso de alteração no estado de autenticação
                    LaunchedEffect(viewModel.currentUserProfile, viewModel.isCheckingAuth) {
                        if (!viewModel.isCheckingAuth) {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            if (viewModel.currentUserProfile == null) {
                                if (currentRoute != "login" && currentRoute != "splash") {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else if (!isAuthorized) {
                                if (currentRoute != "unauthorized" && currentRoute != "splash") {
                                    navController.navigate("unauthorized") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnauthorizedScreen(onSignOut: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Acesso Restrito",
                color = RedError,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Esta área é reservada para administradores do clube AFUP. Sua conta não possui as permissões necessárias para acessar este aplicativo.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = RedError),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Sair e Usar Outra Conta", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
