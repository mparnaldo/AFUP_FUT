package com.afup.afupfut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afup.afupfut.ui.screens.*
import com.afup.afupfut.ui.theme.AFUP_FUTTheme
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

                    // Determina a rota inicial com base no login e se o cadastro do perfil foi feito
                    val startDestination = when {
                        viewModel.isCheckingAuth -> "splash"
                        viewModel.currentUserProfile == null -> "login"
                        viewModel.currentUserProfile?.name.isNullOrBlank() -> "register_profile"
                        else -> "match_presence"
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
                                    navController.navigate("match_presence") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegisterProfile = {
                                    navController.navigate("register_profile")
                                }
                            )
                        }
                        composable("register_profile") {
                            RegisterAthleteScreen(
                                viewModel = viewModel,
                                onRegistrationSuccess = {
                                    navController.navigate("match_presence") {
                                        popUpTo("register_profile") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("match_presence") {
                            MatchPresenceScreen(
                                viewModel = viewModel,
                                onNavigateToAdmin = {
                                    navController.navigate("admin_panel")
                                },
                                onNavigateToField = {
                                    navController.navigate("soccer_field")
                                }
                            )
                        }
                        composable("admin_panel") {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
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

                    // Efeito reativo para redirecionar se o estado de login for invalidado
                    LaunchedEffect(viewModel.currentUserProfile, viewModel.isCheckingAuth) {
                        if (!viewModel.isCheckingAuth) {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            if (viewModel.currentUserProfile == null) {
                                if (currentRoute != "login" && currentRoute != "splash") {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            } else if (viewModel.currentUserProfile?.name.isNullOrBlank()) {
                                if (currentRoute != "register_profile" && currentRoute != "splash") {
                                    navController.navigate("register_profile") {
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
