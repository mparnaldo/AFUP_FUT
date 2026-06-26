package com.afup.afupfut.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.Image
import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.ui.theme.*
import com.afup.afupfut.ui.viewmodel.MatchViewModel
import com.afup.afupfut.util.ImageUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MatchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToField: () -> Unit
) {
    val matchState = viewModel.matchState
    val allAthletes = viewModel.allAthletes

    // Alertas de novos atletas sem nível avaliado (rating == null)
    val unratedAthletes = remember(allAthletes) {
        allAthletes.filter { it.rating == null }
    }

    val currentUserProfile = viewModel.currentUserProfile
    val isSuperuser = viewModel.currentUserEmail?.equals("mpires.arnaldo@gmail.com", ignoreCase = true) == true || currentUserProfile?.isAdmin == true

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var matchDateInput by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
    }
    
    // Abas de Controle
    var selectedTabState by remember { mutableIntStateOf(0) }
    val tabs = listOf("Partida Atual", "Atletas & Níveis")

    // Controle da janela de avaliação por estrelas
    var selectedAthleteForRating by remember { mutableStateOf<Athlete?>(null) }
    var ratingStarsSelected by remember { mutableIntStateOf(5) }

    // Controle do diálogo de promoção de gestor
    var athleteForRoleChange by remember { mutableStateOf<Athlete?>(null) }

    // Controle do diálogo de adicionar atleta manualmente
    var showAddAthleteDialog by remember { mutableStateOf(false) }
    var newNickname by remember { mutableStateOf("") }
    var newFullName by remember { mutableStateOf("") }
    var newAgeStr by remember { mutableStateOf("") }
    var newHeightStr by remember { mutableStateOf("1.75") }
    var newWeightStr by remember { mutableStateOf("75") }
    var newRating by remember { mutableIntStateOf(5) }
    var newAthleteType by remember { mutableStateOf("Associado") }
    var newSelectedPositions by remember { mutableStateOf(listOf("Meia")) }
    var isNewTypeDropdownExpanded by remember { mutableStateOf(false) }

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

            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(SurfaceDark, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Painel do Admin",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Gerenciamento de nível e partidas",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Abas
            TabRow(
                selectedTabIndex = selectedTabState,
                containerColor = SurfaceDark,
                contentColor = NeonGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabState == index,
                        onClick = { selectedTabState = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTabState == index) NeonGreen else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTabState == 0) {
                // TAB 0: Partida Atual
                
                // Card de Controle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceLightDark, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Lista de Presença",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (matchState?.isOpen == true) "Inscrições Abertas" else "Inscrições Fechadas",
                                    color = if (matchState?.isOpen == true) NeonGreen else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (matchState?.isOpen == true) "Data: ${matchState.matchDate}" else "Status: Inativo",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Switch(
                                checked = matchState?.isOpen == true,
                                onCheckedChange = { open ->
                                    if (open) {
                                        matchDateInput = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        showDatePickerDialog = true
                                    } else {
                                        viewModel.toggleMatchState(isOpen = false, date = matchState?.matchDate ?: "")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BackgroundDark,
                                    checkedTrackColor = NeonGreen,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = SurfaceLightDark
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botão Gerar Times (Apenas com confirmados)
                val confirmedPlayersCount = remember(matchState?.playersList) {
                    matchState?.playersList?.count { it.isConfirmed } ?: 0
                }

                Button(
                    onClick = {
                        viewModel.generateTeams()
                        onNavigateToField()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = confirmedPlayersCount > 0
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GERAR TIMES ($confirmedPlayersCount CONFIRMADOS)",
                            color = BackgroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Confirmar Presença In Loco",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Listagem de presença interativa
                val currentPlayers = matchState?.playersList ?: emptyList()
                if (currentPlayers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nenhum atleta inscrito na lista.", color = TextMuted)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentPlayers) { player ->
                            val isMember = player.type == "Associado"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark, RoundedCornerShape(14.dp))
                                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceLightDark)
                                ) {
                                    val base64Image = ImageUtils.rememberBase64Image(player.photoUrl)
                                    if (base64Image != null) {
                                        Image(
                                            bitmap = base64Image,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (player.photoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = player.photoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.align(Alignment.Center))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Apelido e Nome
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = player.nickname, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = player.name, color = TextSecondary, fontSize = 11.sp)
                                }

                                // Badge interativa (clicável para alternar)
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
                                        .clickable {
                                            val nextType = if (isMember) "Convidado" else "Associado"
                                            viewModel.updatePlayerPresenceType(player.athleteId, nextType)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = player.type,
                                        color = if (isMember) MemberGold else GuestCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Checkbox de Confirmação
                                Checkbox(
                                    checked = player.isConfirmed,
                                    onCheckedChange = { confirmed ->
                                        viewModel.togglePlayerPresenceConfirmation(player.athleteId, confirmed)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = NeonGreen,
                                        uncheckedColor = TextSecondary,
                                        checkmarkColor = BackgroundDark
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Atletas & Níveis
                if (unratedAthletes.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RedError.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RedError)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Novos Atletas Pendentes (${unratedAthletes.size})",
                                    color = RedError,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Toque em um atleta abaixo para classificar e integrá-lo ao algoritmo.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Lista rápida horizontal de pendentes
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                unratedAthletes.forEach { athlete ->
                                    Box(
                                        modifier = Modifier
                                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                                            .border(1.dp, RedError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedAthleteForRating = athlete
                                                ratingStarsSelected = 5
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.StarBorder, contentDescription = null, tint = RedError, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = athlete.nickname, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { showAddAthleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADICIONAR ATLETA MANUALMENTE",
                            color = BackgroundDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Classificar Nível dos Atletas",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allAthletes) { athlete ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(14.dp))
                                .border(1.dp, SurfaceLightDark, RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedAthleteForRating = athlete
                                    ratingStarsSelected = athlete.rating ?: 5
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Foto
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceLightDark)
                            ) {
                                val base64Image = ImageUtils.rememberBase64Image(athlete.photoUrl)
                                if (base64Image != null) {
                                    Image(
                                        bitmap = base64Image,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (athlete.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = athlete.photoUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.align(Alignment.Center))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Nome
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = athlete.nickname, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (athlete.isManager) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "GESTOR", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                                Text(
                                    text = athlete.positions.joinToString(", ") + " • ${athlete.getAge()} anos",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            val isThisAthleteSuperuser = athlete.isAdmin

                            if (isSuperuser && !isThisAthleteSuperuser) {
                                IconButton(
                                    onClick = { athleteForRoleChange = athlete },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        tint = if (athlete.isManager) NeonGreen else TextMuted,
                                        contentDescription = "Gerenciar Permissão"
                                    )
                                }
                            }

                            // Rating Atual (Estrelas)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (athlete.rating != null) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${athlete.rating}/10",
                                        color = GoldStar,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(RedError.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Pendente", color = RedError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIALOG ABERTURA DE PARTIDA ---
        if (showDatePickerDialog) {
            AlertDialog(
                onDismissRequest = { showDatePickerDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Abrir Lista de Presença", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Informe a data da partida para abrir as inscrições:",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = matchDateInput,
                            onValueChange = { matchDateInput = it },
                            placeholder = { Text("Ex: 28/06/2026") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = SurfaceLightDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (matchDateInput.isNotBlank()) {
                                viewModel.toggleMatchState(isOpen = true, date = matchDateInput)
                                showDatePickerDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Abrir Lista", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePickerDialog = false }) {
                        Text("Cancelar", color = ElectricCyan)
                    }
                }
            )
        }

        // --- DIALOG AVALIAÇÃO DE JOGADOR (1 a 10 ESTRELAS) ---
        if (selectedAthleteForRating != null) {
            val athlete = selectedAthleteForRating!!
            AlertDialog(
                onDismissRequest = { selectedAthleteForRating = null },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "Classificar ${athlete.nickname}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Foto de Perfil no Diálogo
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SurfaceLightDark)
                                .border(1.dp, NeonGreen, CircleShape)
                        ) {
                            val base64Image = ImageUtils.rememberBase64Image(athlete.photoUrl)
                            if (base64Image != null) {
                                Image(
                                    bitmap = base64Image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (athlete.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = athlete.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp).align(Alignment.Center))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Informações Básicas no Diálogo
                        Text(
                            text = athlete.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Text(
                            text = "${athlete.getAge()} anos • ${athlete.height}m • ${athlete.weight}kg",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Text(
                            text = "Vínculo: ${athlete.athleteType} • Pé: ${athlete.dominantFoot}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        
                        Text(
                            text = "Posições: ${athlete.positions.joinToString(", ")}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Divider(color = SurfaceLightDark, modifier = Modifier.padding(bottom = 12.dp))

                        Text(
                            text = "Atribua uma classificação de nível de 1 a 10 estrelas para este jogador:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Mostrador numérico grande
                        Text(
                            text = "$ratingStarsSelected / 10",
                            color = GoldStar,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Slider de Estrelas (1 a 10)
                        Slider(
                            value = ratingStarsSelected.toFloat(),
                            onValueChange = { ratingStarsSelected = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldStar,
                                activeTrackColor = GoldStar,
                                inactiveTrackColor = SurfaceLightDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 (Perna de Pau)", color = TextMuted, fontSize = 11.sp)
                            Text("10 (Craque)", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updatePlayerRating(athlete.id, ratingStarsSelected)
                            selectedAthleteForRating = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Salvar Nível", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAthleteForRating = null }) {
                        Text("Cancelar", color = ElectricCyan)
                    }
                }
            )
        }

        // --- DIALOG PROMOÇÃO DE GESTOR ---
        if (athleteForRoleChange != null) {
            val athlete = athleteForRoleChange!!
            AlertDialog(
                onDismissRequest = { athleteForRoleChange = null },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = if (athlete.isManager) "Remover Gestor" else "Tornar Gestor",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (athlete.isManager) 
                            "Deseja remover as permissões de Gestor de ${athlete.nickname}? Ele não poderá mais acessar o painel de administrador."
                            else "Deseja tornar ${athlete.nickname} um Gestor? Ele terá acesso ao app do admin para classificar atletas, abrir lista de partidas e balancear times.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleManagerRole(athlete.id, !athlete.isManager)
                            athleteForRoleChange = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (athlete.isManager) RedError else NeonGreen)
                    ) {
                        Text(
                            text = if (athlete.isManager) "Remover" else "Tornar Gestor",
                            color = BackgroundDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { athleteForRoleChange = null }) {
                        Text("Cancelar", color = ElectricCyan)
                    }
                }
            )
        }

        // --- DIALOG REGISTRAR NOVO ATLETA MANUALMENTE ---
        if (showAddAthleteDialog) {
            var validationError by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { showAddAthleteDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Registrar Novo Atleta", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = newNickname,
                            onValueChange = { newNickname = it; validationError = null },
                            label = { Text("Apelido * (Nome na Lista)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = SurfaceLightDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = newFullName,
                            onValueChange = { newFullName = it },
                            label = { Text("Nome Completo (Opcional)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = SurfaceLightDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newAgeStr,
                                onValueChange = { newAgeStr = it; validationError = null },
                                label = { Text("Idade *", color = TextSecondary) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = SurfaceLightDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            OutlinedTextField(
                                value = newHeightStr,
                                onValueChange = { newHeightStr = it; validationError = null },
                                label = { Text("Altura (m) *", color = TextSecondary) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = SurfaceLightDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newWeightStr,
                                onValueChange = { newWeightStr = it; validationError = null },
                                label = { Text("Peso (kg) *", color = TextSecondary) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = SurfaceLightDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Vínculo Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .border(1.dp, SurfaceLightDark, RoundedCornerShape(10.dp))
                                        .clickable { isNewTypeDropdownExpanded = true }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = newAthleteType, color = TextPrimary, fontSize = 14.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonGreen)
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = isNewTypeDropdownExpanded,
                                    onDismissRequest = { isNewTypeDropdownExpanded = false },
                                    modifier = Modifier.background(SurfaceDark)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Associado", color = TextPrimary) },
                                        onClick = { newAthleteType = "Associado"; isNewTypeDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Convidado", color = TextPrimary) },
                                        onClick = { newAthleteType = "Convidado"; isNewTypeDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Rating/Level
                        Text(
                            text = "Nível Técnico: $newRating / 10",
                            color = GoldStar,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Slider(
                            value = newRating.toFloat(),
                            onValueChange = { newRating = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldStar,
                                activeTrackColor = GoldStar,
                                inactiveTrackColor = SurfaceLightDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Posições
                        Text(
                            text = "Posições (Selecione pelo menos uma):",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        val positionsList = listOf("Goleiro", "Zagueiro", "Lateral", "Volante", "Meia", "Atacante")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            positionsList.forEach { pos ->
                                val isSelected = newSelectedPositions.contains(pos)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.Transparent,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonGreen else SurfaceLightDark,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            newSelectedPositions = if (isSelected) {
                                                newSelectedPositions.filter { it != pos }
                                            } else {
                                                newSelectedPositions + pos
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = pos,
                                        color = if (isSelected) NeonGreen else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        
                        validationError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = err, color = RedError, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val age = newAgeStr.toIntOrNull()
                            val height = newHeightStr.replace(",", ".").toDoubleOrNull()
                            val weight = newWeightStr.replace(",", ".").toDoubleOrNull()
                            
                            if (newNickname.isBlank()) {
                                validationError = "Apelido é obrigatório."
                                return@Button
                            }
                            if (age == null || age <= 0) {
                                validationError = "Insira uma idade válida."
                                return@Button
                            }
                            if (height == null || height <= 0.0) {
                                validationError = "Insira uma altura válida."
                                return@Button
                            }
                            if (weight == null || weight <= 0.0) {
                                validationError = "Insira um peso válido."
                                return@Button
                            }
                            if (newSelectedPositions.isEmpty()) {
                                validationError = "Selecione pelo menos uma posição."
                                return@Button
                            }
                            
                            val athleteId = java.util.UUID.randomUUID().toString()
                            val athlete = Athlete(
                                id = athleteId,
                                name = newFullName.ifBlank { newNickname },
                                nickname = newNickname,
                                height = height,
                                weight = weight,
                                dominantFoot = "Direito",
                                positions = newSelectedPositions,
                                birthDate = "01/01/${LocalDate.now().year - age}",
                                rating = newRating,
                                isAdmin = false,
                                athleteType = newAthleteType,
                                isManager = false
                            )
                            viewModel.registerAthleteManually(athlete, addToMatch = matchState?.isOpen == true)
                            
                            // Reset states
                            showAddAthleteDialog = false
                            newNickname = ""
                            newFullName = ""
                            newAgeStr = ""
                            newHeightStr = "1.75"
                            newWeightStr = "75"
                            newRating = 5
                            newAthleteType = "Associado"
                            newSelectedPositions = listOf("Meia")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Registrar", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAthleteDialog = false }) {
                        Text("Cancelar", color = ElectricCyan)
                    }
                }
            )
        }
    }
}
