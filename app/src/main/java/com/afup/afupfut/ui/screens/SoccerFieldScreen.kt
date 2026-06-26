package com.afup.afupfut.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.ui.theme.*
import com.afup.afupfut.ui.viewmodel.MatchViewModel
import com.afup.afupfut.util.Team

@Composable
fun SoccerFieldScreen(
    viewModel: MatchViewModel,
    onNavigateBack: () -> Unit
) {
    val teams = viewModel.teams

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Cabeçalho superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        text = "Times Escalados",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Visualização tática dos times formados",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (teams.size < 2) {
                // Estado vazio se os times não foram gerados
                Box(
                    modifier = Modifier
                        .fillPage()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Times ainda não gerados.\nVolte ao Painel Admin para gerar os times balanceados.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            } else {
                val teamA = teams[0]
                val teamB = teams[1]

                // Campo de Futebol de Grama Customizado
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SoccerPitchBackground()

                    // Camada de Jogadores por Cima
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val pitchWidth = maxWidth
                        val pitchHeight = maxHeight

                        // --- TIME A (METADE SUPERIOR, Y: 0% A 45%) ---
                        RenderTeamOnField(
                            team = teamA,
                            isTopHalf = true,
                            width = pitchWidth.value,
                            height = pitchHeight.value
                        )

                        // --- TIME B (METADE INFERIOR, Y: 55% A 100%) ---
                        RenderTeamOnField(
                            team = teamB,
                            isTopHalf = false,
                            width = pitchWidth.value,
                            height = pitchHeight.value
                        )
                    }
                }

                // Legendas de Força / Média dos Times
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TeamScoreBadge(team = teamA, colorAccent = MemberGold)
                    TeamScoreBadge(team = teamB, colorAccent = ElectricCyan)
                }
            }
        }
    }
}

@Composable
fun SoccerPitchBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, SurfaceLightDark, RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F3A1A), // Verde gramado escuro
                        Color(0xFF1E5C32)  // Verde gramado mais claro
                    )
                )
            )
    ) {
        val width = size.width
        val height = size.height
        val lineColor = Color.White.copy(alpha = 0.5f)
        val strokeWidth = 4f

        // Linha Lateral do Campo (Margem interna)
        val marginX = 16f
        val marginY = 16f
        drawRect(
            color = lineColor,
            topLeft = Offset(marginX, marginY),
            size = Size(width - 2 * marginX, height - 2 * marginY),
            style = Stroke(width = strokeWidth)
        )

        // Linha de Meio de Campo
        val midY = height / 2
        drawLine(
            color = lineColor,
            start = Offset(marginX, midY),
            end = Offset(width - marginX, midY),
            strokeWidth = strokeWidth
        )

        // Círculo Central
        drawCircle(
            color = lineColor,
            radius = 100f,
            center = Offset(width / 2, midY),
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = lineColor,
            radius = 8f,
            center = Offset(width / 2, midY)
        )

        // Pequenas e Grandes Áreas (Metade Superior)
        // Grande Área
        val penaltyAreaWidth = width * 0.6f
        val penaltyAreaHeight = height * 0.15f
        drawRect(
            color = lineColor,
            topLeft = Offset((width - penaltyAreaWidth) / 2, marginY),
            size = Size(penaltyAreaWidth, penaltyAreaHeight),
            style = Stroke(width = strokeWidth)
        )
        // Pequena Área
        val smallAreaWidth = width * 0.3f
        val smallAreaHeight = height * 0.05f
        drawRect(
            color = lineColor,
            topLeft = Offset((width - smallAreaWidth) / 2, marginY),
            size = Size(smallAreaWidth, smallAreaHeight),
            style = Stroke(width = strokeWidth)
        )

        // Pequenas e Grandes Áreas (Metade Inferior)
        // Grande Área
        drawRect(
            color = lineColor,
            topLeft = Offset((width - penaltyAreaWidth) / 2, height - marginY - penaltyAreaHeight),
            size = Size(penaltyAreaWidth, penaltyAreaHeight),
            style = Stroke(width = strokeWidth)
        )
        // Pequena Área
        drawRect(
            color = lineColor,
            topLeft = Offset((width - smallAreaWidth) / 2, height - marginY - smallAreaHeight),
            size = Size(smallAreaWidth, smallAreaHeight),
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun BoxWithConstraintsScope.RenderTeamOnField(
    team: Team,
    isTopHalf: Boolean,
    width: Float,
    height: Float
) {
    // Agrupa e separa os jogadores para organizar espacialmente por posições
    val goalkeepers = team.players.filter { it.positions.contains("Goleiro") }
    val defenders = team.players.filter { it.positions.contains("Zagueiro") || it.positions.contains("Lateral") }
    val midfielders = team.players.filter { it.positions.contains("Volante") || it.positions.contains("Meia") }
    val forwards = team.players.filter { !it.positions.contains("Goleiro") && !it.positions.contains("Zagueiro") && !it.positions.contains("Lateral") && !it.positions.contains("Volante") && !it.positions.contains("Meia") }

    // Goalkeeper
    goalkeepers.forEachIndexed { _, player ->
        val yPercent = if (isTopHalf) 0.08f else 0.92f
        PlayerOnFieldItem(player = player, xPercent = 0.50f, yPercent = yPercent, width = width, height = height)
    }

    // Defensores
    defenders.forEachIndexed { index, player ->
        val xPercent = getHorizontalDistribution(index, defenders.size)
        val yPercent = if (isTopHalf) 0.22f else 0.78f
        PlayerOnFieldItem(player = player, xPercent = xPercent, yPercent = yPercent, width = width, height = height)
    }

    // Meio-campistas
    midfielders.forEachIndexed { index, player ->
        val xPercent = getHorizontalDistribution(index, midfielders.size)
        val yPercent = if (isTopHalf) 0.33f else 0.67f
        PlayerOnFieldItem(player = player, xPercent = xPercent, yPercent = yPercent, width = width, height = height)
    }

    // Atacantes
    forwards.forEachIndexed { index, player ->
        val xPercent = getHorizontalDistribution(index, forwards.size)
        val yPercent = if (isTopHalf) 0.43f else 0.57f
        PlayerOnFieldItem(player = player, xPercent = xPercent, yPercent = yPercent, width = width, height = height)
    }
}

// Retorna o espaçamento horizontal das posições para organizar jogadores na linha
fun getHorizontalDistribution(index: Int, total: Int): Float {
    if (total <= 1) return 0.50f
    val gap = 0.70f / (total - 1)
    return 0.15f + (index * gap)
}

@Composable
fun PlayerOnFieldItem(
    player: Athlete,
    xPercent: Float,
    yPercent: Float,
    width: Float,
    height: Float
) {
    val sizePx = 45.dp
    // Calcula coordenadas absolutas
    val left = (xPercent * width).dp - (sizePx / 2)
    val top = (yPercent * height).dp - (sizePx / 2)

    Box(
        modifier = Modifier
            .offset(x = left, y = top)
            .width(60.dp), // Espaço de toque / centralizado
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Círculo com foto do Jogador
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.5.dp, NeonGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (player.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = player.photoUrl,
                        contentDescription = player.nickname,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Nome abreviado / Apelido
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = player.nickname,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Nota (Estrelas)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(10.dp))
                Text(
                    text = (player.rating ?: 5).toString(),
                    color = GoldStar,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun TeamScoreBadge(team: Team, colorAccent: Color) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .border(1.dp, SurfaceLightDark, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = team.name,
                color = colorAccent,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldStar, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f Estrelas", team.averageRating),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${team.players.size} jogadores",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Extensão rápida para manter consistência nos nomes
fun Modifier.fillPage(): Modifier = this.fillMaxSize()
