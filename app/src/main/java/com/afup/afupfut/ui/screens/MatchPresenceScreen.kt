package com.afup.afupfut.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.afup.afupfut.data.model.PresencePlayer
import com.afup.afupfut.ui.theme.*
import com.afup.afupfut.ui.viewmodel.MatchViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchPresenceScreen(
    viewModel: MatchViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToField: () -> Unit
) {
    val profile = viewModel.currentUserProfile
    val matchState = viewModel.matchState

    var showJoinDialog by remember { mutableStateOf(false) }
    var joinType by remember { mutableStateOf("Associado") }
    var joinDropdownExpanded by remember { mutableStateOf(false) }

    // Fechar a notificação interna após 4 segundos automaticamente
    LaunchedEffect(viewModel.inAppNotification) {
        if (viewModel.inAppNotification != null) {
            delay(4000)
            viewModel.inAppNotification = null
        }
    }

    // Ordenação prioritária: Associados no topo, Convidados abaixo, ambos por ordem cronológica (timestamp)
    val sortedPlayers = remember(matchState?.playersList) {
        matchState?.playersList?.sortedWith(
            compareBy<PresencePlayer> { if (it.type == "Associado") 0 else 1 }
                .thenBy { it.timestamp }
        ) ?: emptyList()
    }

    val isUserRegistered = remember(matchState?.playersList, profile?.id) {
        matchState?.playersList?.any { it.athleteId == profile?.id } ?: false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho Principal do Painel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AFUP FUT",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.horizontalGradient(listOf(NeonGreen, ElectricCyan))
                        )
                    )
                    Text(
                        text = "Lista de Presença",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botão Campinho (Visualização)
                    IconButton(
                        onClick = {
                            viewModel.generateTeams() // Carrega os times se houver
                            onNavigateToField()
                        },
                        modifier = Modifier
                            .background(SurfaceDark, CircleShape)
                            .border(1.dp, SurfaceLightDark, CircleShape)
                    ) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = "Campinho", tint = NeonGreen)
                    }

                    if (profile?.isAdmin == true) {
                        Spacer(modifier = Modifier.width(10.dp))
                        // Botão Painel do Administrador
                        IconButton(
                            onClick = onNavigateToAdmin,
                            modifier = Modifier
                                .background(SurfaceDark, CircleShape)
                                .border(1.dp, SurfaceLightDark, CircleShape)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = ElectricCyan)
                        }
                    }
                }
            }

            // --- ESTADO DA PARTIDA (CARD) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (matchState?.isOpen == true) "Partida Aberta" else "Lista Fechada",
                            color = if (matchState?.isOpen == true) NeonGreen else RedError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (matchState?.matchDate.isNullOrBlank()) "Aguardando próxima data" else "Data: ${matchState?.matchDate}",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    // Contador de inscritos
                    Box(
                        modifier = Modifier
                            .background(SurfaceLightDark, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sortedPlayers.size} Confirmados",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- BOTOES DE AÇÃO PARTICIPAR/SAIR ---
            if (matchState?.isOpen == true) {
                if (isUserRegistered) {
                    Button(
                        onClick = { viewModel.leaveMatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, RedError, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = RedError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REMOVER MEU NOME DA LISTA", color = RedError, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { showJoinDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = BackgroundDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADICIONAR MEU NOME NA LISTA", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedError.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, RedError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A lista de presença está fechada para esta partida.",
                        color = RedError,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LISTAGEM DE JOGADORES CONFIRMADOS ---
            Text(
                text = "Lista de Confirmados",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (sortedPlayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum atleta na lista ainda.", color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedPlayers) { player ->
                        val isMember = player.type == "Associado"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(14.dp))
                                .border(1.dp, if (isMember) MemberGold.copy(alpha = 0.3f) else SurfaceLightDark, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceLightDark)
                            ) {
                                if (player.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = player.photoUrl,
                                        contentDescription = "Foto do jogador",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Dados do Jogador
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = player.nickname,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = player.name,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            // Badge de Tipo (Associado vs Convidado)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isMember) MemberGold.copy(alpha = 0.15f) else GuestCyan.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isMember) MemberGold else GuestCyan,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = player.type,
                                    color = if (isMember) MemberGold else GuestCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- POPUP NOTIFICAÇÃO REAL-TIME (TOAST BANNER SUPERIOR) ---
        AnimatedVisibility(
            visible = viewModel.inAppNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            viewModel.inAppNotification?.let { notificationText ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonGreen, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NeonGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = notificationText,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { viewModel.inAppNotification = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // --- DIALOG PARA ESCOLHER TIPO AO ADICIONAR NOME ---
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Confirmar Presença", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Selecione o seu tipo de inscrição para esta partida:",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Dropdown Combobox estilizada
                        Box {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(10.dp))
                                    .clickable { joinDropdownExpanded = true }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = joinType, color = TextPrimary)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonGreen)
                                }
                            }

                            DropdownMenu(
                                expanded = joinDropdownExpanded,
                                onDismissRequest = { joinDropdownExpanded = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Associado (Prioritário)", color = TextPrimary) },
                                    onClick = { joinType = "Associado"; joinDropdownExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Convidado", color = TextPrimary) },
                                    onClick = { joinType = "Convidado"; joinDropdownExpanded = false }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showJoinDialog = false
                            viewModel.joinMatch(joinType, onError = { /* Tratar erro */ })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Confirmar", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("Cancelar", color = ElectricCyan)
                    }
                }
            )
        }
    }
}
