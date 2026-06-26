package com.afup.afupfut.util

import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.data.model.PresencePlayer

data class Team(
    val name: String,
    val players: List<Athlete>,
    val averageRating: Double = if (players.isEmpty()) 0.0 else players.map { it.rating ?: 5 }.average(),
    val averageAge: Double = if (players.isEmpty()) 0.0 else players.map { it.getAge() }.average(),
    val averageHeight: Double = if (players.isEmpty()) 0.0 else players.map { it.height }.average(),
    val averageWeight: Double = if (players.isEmpty()) 0.0 else players.map { it.weight }.average()
)

object TeamBalancer {
    fun balanceTeams(
        presencePlayers: List<PresencePlayer>,
        athletesMap: Map<String, Athlete>,
        numTeams: Int = 2
    ): List<Team> {
        if (presencePlayers.isEmpty()) return emptyList()

        // 1. Obter os objetos Athlete detalhados cadastrados
        val activeAthletes = presencePlayers.mapNotNull { presence ->
            athletesMap[presence.athleteId]?.copy(
                // Preservar a classificação ou preencher padrão (5) se ainda não avaliado pelo admin
                rating = athletesMap[presence.athleteId]?.rating ?: 5
            )
        }

        if (activeAthletes.isEmpty()) return emptyList()

        // 2. Separar Goleiros e Jogadores de Linha
        val goalkeepers = activeAthletes.filter { it.positions.contains("Goleiro") }
            .sortedByDescending { it.rating ?: 5 }
        val outfieldPlayers = activeAthletes.filter { !it.positions.contains("Goleiro") }
            .sortedWith(
                compareByDescending<Athlete> { it.rating ?: 5 }
                    .thenByDescending { it.height }
                    .thenBy { it.getAge() }
            )

        // Inicializa as listas de jogadores por time
        val teamsPlayers = List(numTeams) { mutableListOf<Athlete>() }

        // 3. Distribuição de Goleiros
        for (i in goalkeepers.indices) {
            val teamIndex = i % numTeams
            teamsPlayers[teamIndex].add(goalkeepers[i])
        }

        // 4. Distribuição de Jogadores de Linha via Snake Draft (1, 2, 2, 1...)
        var forward = true
        var teamIdx = 0
        for (player in outfieldPlayers) {
            teamsPlayers[teamIdx].add(player)
            if (forward) {
                if (teamIdx == numTeams - 1) {
                    forward = false
                } else {
                    teamIdx++
                }
            } else {
                if (teamIdx == 0) {
                    forward = true
                } else {
                    teamIdx--
                }
            }
        }

        // 5. Ajuste Fino via Otimização Local (Swaps)
        // Executa trocas iterativas de jogadores de linha entre o time mais forte e o mais fraco
        // para minimizar a diferença na classificação média
        var improved = true
        var iterations = 0
        while (improved && iterations < 150) {
            improved = false
            iterations++

            var strongestIdx = 0
            var weakestIdx = 0
            var maxRating = -1.0
            var minRating = 999.0

            for (t in 0 until numTeams) {
                val currentRating = if (teamsPlayers[t].isEmpty()) 0.0 else teamsPlayers[t].map { it.rating ?: 5 }.average()
                if (currentRating > maxRating) {
                    maxRating = currentRating
                    strongestIdx = t
                }
                if (currentRating < minRating) {
                    minRating = currentRating
                    weakestIdx = t
                }
            }

            val ratingDiffBefore = maxRating - minRating
            if (ratingDiffBefore <= 0.1) {
                // Nível técnico perfeitamente equilibrado
                break
            }

            val strongestTeam = teamsPlayers[strongestIdx]
            val weakestTeam = teamsPlayers[weakestIdx]

            var bestSwapPair: Pair<Athlete, Athlete>? = null
            var bestRatingDiffAfter = ratingDiffBefore

            // Busca par de jogadores de linha para realizar a troca
            for (pStrong in strongestTeam) {
                if (pStrong.positions.contains("Goleiro")) continue
                for (pWeak in weakestTeam) {
                    if (pWeak.positions.contains("Goleiro")) continue

                    val strongRating = pStrong.rating ?: 5
                    val weakRating = pWeak.rating ?: 5
                    if (strongRating == weakRating) continue

                    val newStrongestRating = (strongestTeam.map { it.rating ?: 5 }.sum() - strongRating + weakRating) / strongestTeam.size.toDouble()
                    val newWeakestRating = (weakestTeam.map { it.rating ?: 5 }.sum() - weakRating + strongRating) / weakestTeam.size.toDouble()
                    val newDiff = Math.abs(newStrongestRating - newWeakestRating)

                    if (newDiff < bestRatingDiffAfter) {
                        bestRatingDiffAfter = newDiff
                        bestSwapPair = Pair(pStrong, pWeak)
                    }
                }
            }

            if (bestSwapPair != null && bestRatingDiffAfter < ratingDiffBefore) {
                val (pStrong, pWeak) = bestSwapPair
                strongestTeam.remove(pStrong)
                strongestTeam.add(pWeak)
                weakestTeam.remove(pWeak)
                weakestTeam.add(pStrong)
                improved = true
            }
        }

        // Retorna a lista dos times estruturados
        return teamsPlayers.mapIndexed { index, players ->
            val letter = ('A'.code + index).toChar()
            Team(name = "Time $letter", players = players.sortedBy { getPositionPriority(it.positions) })
        }
    }

    // Função auxiliar para ordenar jogadores em campo de trás para frente (Goleiro -> Zagueiro -> Meia -> Atacante)
    private fun getPositionPriority(positions: List<String>): Int {
        return when {
            positions.contains("Goleiro") -> 0
            positions.contains("Zagueiro") || positions.contains("Lateral") -> 1
            positions.contains("Volante") || positions.contains("Meia") -> 2
            else -> 3
        }
    }
}
