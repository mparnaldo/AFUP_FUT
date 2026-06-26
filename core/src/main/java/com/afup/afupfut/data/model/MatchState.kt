package com.afup.afupfut.data.model

import com.google.firebase.firestore.PropertyName

data class PresencePlayer(
    val athleteId: String = "",
    val name: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val type: String = "", // "Associado" ou "Convidado"
    @get:PropertyName("isConfirmed") @field:PropertyName("isConfirmed") val isConfirmed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class MatchState(
    val isOpen: Boolean = true,
    val matchDate: String = "",
    val playersList: List<PresencePlayer> = emptyList()
)
