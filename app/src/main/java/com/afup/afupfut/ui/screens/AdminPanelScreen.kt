package com.afup.afupfut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.ui.theme.*
import com.afup.afupfut.ui.viewmodel.MatchViewModel

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

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var matchDateInput by remember { mutableStateOf("") }
    
    // Controle da janela de avaliação por estrelas
    var selectedAthleteForRating by remember { mutableStateOf<Athlete?>(null) }
    var ratingStarsSelected by remember { mutableIntStateOf(5) }

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

            Spacer(modifier = Modifier.height(12.dp))

            // --- CONTROLE DE ABERTURA DE LISTA ---
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

            // --- SEÇÃO DE ALERTAS (ATLETAS NÃO CLASSIFICADOS) ---
            if (unratedAthletes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTAO GERAR TIMES ---
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
                enabled = (matchState?.playersList?.isNotEmpty() == true)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GERAR TIMES BALANCEADOS (${matchState?.playersList?.size ?: 0} jog.)",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- LISTA DE JOGADORES DO CLUBE PARA AVALIAÇÃO ---
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
                            if (athlete.photoUrl.isNotBlank()) {
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
                            Text(text = athlete.nickname, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = athlete.positions.joinToString(", ") + " • ${athlete.getAge()} anos",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
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
                                matchDateInput = ""
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
                        Text(
                            text = "Atribua uma classificação de nível de 1 a 10 estrelas para este jogador:",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
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
    }
}
