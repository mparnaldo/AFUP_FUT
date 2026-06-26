package com.afup.afupfut.data.repository

import android.net.Uri
import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.data.model.MatchState
import com.afup.afupfut.data.model.PresencePlayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // ID do usuário logado
    val currentUserId: String?
        get() = auth.currentUser?.uid

    // E-mail do usuário logado
    val currentUserEmail: String?
        get() = auth.currentUser?.email

    // Verifica se está logado
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Sair da conta
    fun signOut() {
        auth.signOut()
    }

    // Carrega o perfil detalhado do atleta
    suspend fun getAthleteProfile(uid: String): Athlete? {
        return try {
            val document = firestore.collection("athletes").document(uid).get().await()
            if (document.exists()) {
                document.toObject(Athlete::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Salva ou atualiza o perfil do atleta (primeiro cadastro ou edição)
    suspend fun saveAthleteProfile(athlete: Athlete): Boolean {
        return try {
            firestore.collection("athletes").document(athlete.id).set(athlete).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Redimensiona, compacta e converte a imagem para Base64 para evitar custos com Storage
    suspend fun uploadAthletePhoto(contentResolver: android.content.ContentResolver, fileUri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(fileUri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Redimensiona mantendo proporção (max 200px)
            val maxDim = 200
            val width = originalBitmap.width
            val height = originalBitmap.height
            val (newWidth, newHeight) = if (width > height) {
                val ratio = width.toFloat() / height.toFloat()
                Pair(maxDim, (maxDim / ratio).toInt())
            } else {
                val ratio = height.toFloat() / width.toFloat()
                Pair((maxDim / ratio).toInt(), maxDim)
            }

            val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

            val outputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            val bytes = outputStream.toByteArray()
            
            "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Busca todos os atletas cadastrados (para o painel de classificação do admin)
    suspend fun getAllAthletes(): List<Athlete> {
        return try {
            val result = firestore.collection("athletes")
                .get()
                .await()
            result.toObjects(Athlete::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Escuta em tempo real a lista de atletas cadastrados
    fun listenToAllAthletes(): Flow<List<Athlete>> = callbackFlow {
        val listener = firestore.collection("athletes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Athlete::class.java)
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    // Atualiza a nota/estrelas (1 a 10) de um atleta
    suspend fun updateAthleteRating(athleteId: String, rating: Int): Boolean {
        return try {
            firestore.collection("athletes").document(athleteId)
                .update("rating", rating)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- CONTROLE DE PARTIDA E PRESENÇA ---

    // Obtém o estado atual da partida (se a lista está aberta, data e jogadores)
    suspend fun getCurrentMatchState(): MatchState {
        return try {
            val document = firestore.collection("matches").document("current_match").get().await()
            if (document.exists()) {
                document.toObject(MatchState::class.java) ?: MatchState()
            } else {
                // Se não existir, inicializa fechado por padrão
                val defaultState = MatchState(isOpen = false, matchDate = "")
                firestore.collection("matches").document("current_match").set(defaultState).await()
                defaultState
            }
        } catch (e: Exception) {
            MatchState(isOpen = false)
        }
    }

    // Escuta em tempo real o estado da partida e a lista de presença
    fun listenToMatchState(): Flow<MatchState> = callbackFlow {
        val docRef = firestore.collection("matches").document("current_match")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val state = snapshot.toObject(MatchState::class.java) ?: MatchState()
                trySend(state)
            } else {
                trySend(MatchState(isOpen = false))
            }
        }
        awaitClose { listener.remove() }
    }

    // Abre ou fecha a lista de presença (apenas administrador)
    suspend fun setMatchState(isOpen: Boolean, matchDate: String): Boolean {
        return try {
            val updates = mapOf(
                "isOpen" to isOpen,
                "matchDate" to matchDate
            )
            // Se fechar, podemos optar por manter ou limpar a lista anterior. Vamos mantê-la ou limpá-la conforme necessidade.
            // Para abrir uma nova, normalmente limpamos a lista anterior de jogadores confirmados.
            if (isOpen) {
                val newState = MatchState(isOpen = true, matchDate = matchDate, playersList = emptyList())
                firestore.collection("matches").document("current_match").set(newState).await()
            } else {
                firestore.collection("matches").document("current_match").update(updates).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Adiciona o nome do atleta logado à lista de presença
    suspend fun addPlayerToPresenceList(presencePlayer: PresencePlayer): Boolean {
        return try {
            val docRef = firestore.collection("matches").document("current_match")
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val state = snapshot.toObject(MatchState::class.java) ?: MatchState()
                if (!state.isOpen) return false // Não adiciona se a lista estiver fechada

                // Evitar duplicações
                val updatedList = state.playersList.filter { it.athleteId != presencePlayer.athleteId }.toMutableList()
                updatedList.add(presencePlayer)

                docRef.update("playersList", updatedList).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Remove o nome da lista de presença (cancelar ida)
    suspend fun removePlayerFromPresenceList(athleteId: String): Boolean {
        return try {
            val docRef = firestore.collection("matches").document("current_match")
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val state = snapshot.toObject(MatchState::class.java) ?: MatchState()
                val updatedList = state.playersList.filter { it.athleteId != athleteId }
                docRef.update("playersList", updatedList).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Promove ou rebaixa um atleta ao papel de Gestor no Firestore
    suspend fun toggleManagerRole(athleteId: String, makeManager: Boolean): Boolean {
        return try {
            firestore.collection("athletes").document(athleteId).update("isManager", makeManager).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
