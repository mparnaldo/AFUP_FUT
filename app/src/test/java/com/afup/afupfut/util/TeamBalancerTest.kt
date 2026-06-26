package com.afup.afupfut.util

import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.data.model.PresencePlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamBalancerTest {
    @Test
    fun testBalanceTeams() {
        // Criando 2 goleiros com ratings diferentes
        val gk1 = Athlete(id = "gk1", name = "Goleiro 1", nickname = "G1", positions = listOf("Goleiro"), rating = 8)
        val gk2 = Athlete(id = "gk2", name = "Goleiro 2", nickname = "G2", positions = listOf("Goleiro"), rating = 6)

        // Criando 12 jogadores de linha com níveis mistos (ratings 5 e 9)
        val linePlayers = (1..12).map { i ->
            Athlete(
                id = "p$i",
                name = "Jogador $i",
                nickname = "J$i",
                positions = listOf("Meia"),
                rating = if (i % 2 == 0) 9 else 5,
                birthDate = "10/10/1990"
            )
        }

        val allAthletes = listOf(gk1, gk2) + linePlayers
        val athletesMap = allAthletes.associateBy { it.id }

        val presenceList = allAthletes.map {
            PresencePlayer(athleteId = it.id, name = it.name, nickname = it.nickname)
        }

        // Executando o balanceamento em 2 times
        val teams = TeamBalancer.balanceTeams(presenceList, athletesMap, numTeams = 2)

        // Asserções
        assertEquals(2, teams.size)
        // Cada time deve ter exatamente 7 jogadores (1 goleiro + 6 de linha)
        assertEquals(7, teams[0].players.size)
        assertEquals(7, teams[1].players.size)

        // Verifica se cada time recebeu exatamente 1 goleiro
        val gkInTeamA = teams[0].players.count { it.positions.contains("Goleiro") }
        val gkInTeamB = teams[1].players.count { it.positions.contains("Goleiro") }
        assertEquals(1, gkInTeamA)
        assertEquals(1, gkInTeamB)

        // Verifica se a diferença de força média é menor que 1 estrela
        val diff = Math.abs(teams[0].averageRating - teams[1].averageRating)
        assertTrue("Diferença de média técnica ($diff) excedeu o limite tolerado", diff < 1.0)
        
        println("--- TESTE DE DISTRIBUIÇÃO AFUP FUT ---")
        println("Time A (Média: ${teams[0].averageRating}): " + teams[0].players.map { "${it.nickname} (${it.rating}★)" })
        println("Time B (Média: ${teams[1].averageRating}): " + teams[1].players.map { "${it.nickname} (${it.rating}★)" })
        println("Diferença técnica: $diff")
    }
}
