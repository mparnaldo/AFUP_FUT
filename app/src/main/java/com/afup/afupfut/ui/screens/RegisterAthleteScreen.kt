package com.afup.afupfut.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.afup.afupfut.ui.theme.*
import com.afup.afupfut.ui.viewmodel.MatchViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterAthleteScreen(
    viewModel: MatchViewModel,
    onRegistrationSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Estados do Formulário
    var name by remember { mutableStateOf(viewModel.currentUserProfile?.name ?: "") }
    var nickname by remember { mutableStateOf(viewModel.currentUserProfile?.nickname ?: "") }
    var heightStr by remember { mutableStateOf(viewModel.currentUserProfile?.height?.toString() ?: "") }
    var weightStr by remember { mutableStateOf(viewModel.currentUserProfile?.weight?.toString() ?: "") }
    var dominantFoot by remember { mutableStateOf(viewModel.currentUserProfile?.dominantFoot ?: "Direito") }
    var birthDate by remember { mutableStateOf(viewModel.currentUserProfile?.birthDate ?: "") }
    var selectedPositions by remember { mutableStateOf(viewModel.currentUserProfile?.positions ?: emptyList<String>()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf(viewModel.currentUserProfile?.photoUrl ?: "") }

    var formError by remember { mutableStateOf<String?>(null) }

    // Calcula idade dinamicamente para mostrar em tempo real
    val calculatedAge = remember(birthDate) {
        if (birthDate.isNotBlank()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dob = LocalDate.parse(birthDate, formatter)
                val now = LocalDate.now()
                ChronoUnit.YEARS.between(dob, now).toInt()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Seletor de Foto do Sistema
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Configurando DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            birthDate = formattedDate
        },
        calendar.get(Calendar.YEAR) - 20, // Padrão: 20 anos atrás
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Lista de posições de futebol
    val availablePositions = listOf("Goleiro", "Zagueiro", "Lateral", "Volante", "Meia", "Atacante")

    // Controle do Dropdown de pé dominante
    var isFootDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Perfil do Atleta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.horizontalGradient(listOf(NeonGreen, ElectricCyan))
                )
            )

            Text(
                text = "Preencha todos os dados obrigatórios para ingressar",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // --- FOTO DE PERFIL ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(2.dp, NeonGreen, CircleShape)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Foto selecionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (existingPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = existingPhotoUrl,
                        contentDescription = "Foto existente",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Adicionar Foto", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- NOME ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; formError = null },
                label = { Text("Nome Completo *", color = TextSecondary) },
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

            // --- APELIDO ---
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it; formError = null },
                label = { Text("Apelido * (Nome na Lista)", color = TextSecondary) },
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

            // --- ALTURA E PESO (LADO A LADO) ---
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it; formError = null },
                    label = { Text("Altura * (m)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Ex: 1.78") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SurfaceLightDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it; formError = null },
                    label = { Text("Peso * (kg)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Ex: 75.0") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SurfaceLightDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- DATA DE NASCIMENTO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(12.dp))
                    .clickable { datePickerDialog.show() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (birthDate.isBlank()) "Data de Nascimento * (dd/MM/yyyy)" else "Nascimento: $birthDate",
                        color = if (birthDate.isBlank()) TextSecondary else TextPrimary,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        calculatedAge?.let { age ->
                            Text(
                                text = "($age anos)",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = NeonGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PÉ DOMINANTE (DROPDOWN) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(1.dp, SurfaceLightDark, RoundedCornerShape(12.dp))
                        .clickable { isFootDropdownExpanded = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pé Dominante: $dominantFoot",
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonGreen)
                    }
                }

                DropdownMenu(
                    expanded = isFootDropdownExpanded,
                    onDismissRequest = { isFootDropdownExpanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Direito (Destro)", color = TextPrimary) },
                        onClick = { dominantFoot = "Direito"; isFootDropdownExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Esquerdo (Canhoto)", color = TextPrimary) },
                        onClick = { dominantFoot = "Esquerdo"; isFootDropdownExpanded = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- POSIÇÕES EM QUE JOGA (SELEÇÃO MÚLTIPLA) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(16.dp))
                    .background(SurfaceDark.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Posições em que Joga * (Selecione pelo menos uma)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Grid simples de posições
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 3
                ) {
                    availablePositions.forEach { pos ->
                        val isSelected = selectedPositions.contains(pos)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonGreen else SurfaceLightDark,
                                    RoundedCornerShape(20.dp)
                                )
                                .background(
                                    if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    selectedPositions = if (isSelected) {
                                        selectedPositions.filter { it != pos }
                                    } else {
                                        selectedPositions + pos
                                    }
                                    formError = null
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pos,
                                color = if (isSelected) NeonGreen else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ERRO DO FORMULÁRIO ---
            formError?.let { err ->
                Text(
                    text = err,
                    color = RedError,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÃO SALVAR ---
            Button(
                onClick = {
                    val height = heightStr.replace(",", ".").toDoubleOrNull()
                    val weight = weightStr.replace(",", ".").toDoubleOrNull()

                    // Validação de todos os dados obrigatórios
                    if (selectedImageUri == null && existingPhotoUrl.isBlank()) {
                        formError = "A foto de perfil é obrigatória."
                        return@Button
                    }
                    if (name.isBlank()) {
                        formError = "O nome é obrigatório."
                        return@Button
                    }
                    if (nickname.isBlank()) {
                        formError = "O apelido é obrigatório."
                        return@Button
                    }
                    if (height == null || height <= 0.0) {
                        formError = "Insira uma altura válida."
                        return@Button
                    }
                    if (weight == null || weight <= 0.0) {
                        formError = "Insira um peso válido."
                        return@Button
                    }
                    if (birthDate.isBlank() || calculatedAge == null || calculatedAge <= 0) {
                        formError = "Selecione uma data de nascimento válida."
                        return@Button
                    }
                    if (selectedPositions.isEmpty()) {
                        formError = "Selecione pelo menos uma posição."
                        return@Button
                    }

                    viewModel.registerAthlete(
                        contentResolver = context.contentResolver,
                        name = name,
                        nickname = nickname,
                        height = height,
                        weight = weight,
                        dominantFoot = dominantFoot,
                        positions = selectedPositions,
                        birthDate = birthDate,
                        photoUri = selectedImageUri,
                        onSuccess = {
                            onRegistrationSuccess()
                        },
                        onError = { formError = it }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SALVAR PERFIL E ENTRAR",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
