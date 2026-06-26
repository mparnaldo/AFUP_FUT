package com.afup.afupfut.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afup.afupfut.data.model.Athlete
import com.afup.afupfut.data.model.MatchState
import com.afup.afupfut.data.model.PresencePlayer
import com.afup.afupfut.data.repository.FirebaseRepository
import com.afup.afupfut.util.Team
import com.afup.afupfut.util.TeamBalancer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Estados de UI
    var currentUserProfile by mutableStateOf<Athlete?>(null)
        private set

    var isCheckingAuth by mutableStateOf(true)
        private set

    var matchState by mutableStateOf<MatchState?>(null)
        private set

    var allAthletes by mutableStateOf<List<Athlete>>(emptyList())
        private set

    var teams by mutableStateOf<List<Team>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    // Alertas de notificações em tempo real dentro do app
    var inAppNotification by mutableStateOf<String?>(null)

    private var previousPlayersList = emptyList<PresencePlayer>()
    private var matchListenerJob: Job? = null
    private var athletesListenerJob: Job? = null

    init {
        checkUserAuth()
    }

    private fun checkUserAuth() {
        isCheckingAuth = true
        viewModelScope.launch {
            val loggedIn = repository.isUserLoggedIn()
            if (loggedIn) {
                repository.currentUserId?.let { uid ->
                    val profile = repository.getAthleteProfile(uid)
                    currentUserProfile = profile
                    startRealtimeListeners()
                }
            }
            isCheckingAuth = false
        }
    }

    fun startRealtimeListeners() {
        // 1. Escuta o estado da partida e a lista de presença
        matchListenerJob?.cancel()
        matchListenerJob = viewModelScope.launch {
            repository.listenToMatchState()
                .catch { e -> errorMessage = e.message }
                .collect { state ->
                    matchState = state
                    
                    // Identifica se um novo jogador se inscreveu para disparar notificação
                    if (previousPlayersList.isNotEmpty() && state.playersList.size > previousPlayersList.size) {
                        val newPlayers = state.playersList.filter { newP -> 
                            previousPlayersList.none { oldP -> oldP.athleteId == newP.athleteId }
                        }
                        if (newPlayers.isNotEmpty()) {
                            val p = newPlayers.first()
                            inAppNotification = "⚽ Nova Inscrição: ${p.nickname} entrou como ${p.type}!"
                        }
                    }
                    previousPlayersList = state.playersList
                }
        }

        // 2. Escuta a lista de atletas cadastrados
        athletesListenerJob?.cancel()
        athletesListenerJob = viewModelScope.launch {
            repository.listenToAllAthletes()
                .catch { e -> errorMessage = e.message }
                .collect { list ->
                    allAthletes = list.sortedBy { it.nickname }
                    
                    // Atualiza o perfil do usuário logado se houver mudanças (como rating do admin)
                    val myUid = repository.currentUserId
                    if (myUid != null) {
                        list.find { it.id == myUid }?.let { updatedProfile ->
                            currentUserProfile = updatedProfile
                        }
                    }
                }
        }
    }

    fun stopListeners() {
        matchListenerJob?.cancel()
        athletesListenerJob?.cancel()
    }

    // --- AUTENTICAÇÃO ---

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true
        errorMessage = null
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                viewModelScope.launch {
                    val uid = repository.currentUserId ?: ""
                    val profile = repository.getAthleteProfile(uid)
                    currentUserProfile = profile
                    startRealtimeListeners()
                    isLoading = false
                    onSuccess()
                }
            }
            .addOnFailureListener {
                isLoading = false
                val msg = it.localizedMessage ?: "Erro de autenticação"
                errorMessage = msg
                onError(msg)
            }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true
        errorMessage = null
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                currentUserProfile = Athlete(id = uid) // Cria perfil limpo para preencher formulário
                isLoading = false
                onSuccess()
            }
            .addOnFailureListener {
                isLoading = false
                val msg = it.localizedMessage ?: "Erro ao criar conta"
                errorMessage = msg
                onError(msg)
            }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true
        errorMessage = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                viewModelScope.launch {
                    val uid = repository.currentUserId ?: ""
                    val profile = repository.getAthleteProfile(uid)
                    currentUserProfile = profile ?: Athlete(id = uid)
                    startRealtimeListeners()
                    isLoading = false
                    onSuccess()
                }
            }
            .addOnFailureListener {
                isLoading = false
                val msg = it.localizedMessage ?: "Erro ao entrar com o Google"
                errorMessage = msg
                onError(msg)
            }
    }

    fun signOut(onSuccess: () -> Unit) {
        stopListeners()
        repository.signOut()
        currentUserProfile = null
        onSuccess()
    }

    // --- PERFIL DO ATLETA ---

    fun registerAthlete(
        name: String,
        nickname: String,
        height: Double,
        weight: Double,
        dominantFoot: String,
        positions: List<String>,
        birthDate: String,
        photoUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val uid = repository.currentUserId ?: return@launch
            var finalPhotoUrl = currentUserProfile?.photoUrl ?: ""

            if (photoUri != null) {
                val uploadUrl = repository.uploadAthletePhoto(uid, photoUri)
                if (uploadUrl != null) {
                    finalPhotoUrl = uploadUrl
                } else {
                    isLoading = false
                    onError("Falha ao enviar a foto de perfil.")
                    return@launch
                }
            }

            // O primeiro usuário criado assume papel administrativo para facilitar os testes
            val isFirstUser = allAthletes.isEmpty()
            val isAdmin = currentUserProfile?.isAdmin ?: isFirstUser

            val newAthlete = Athlete(
                id = uid,
                photoUrl = finalPhotoUrl,
                name = name,
                nickname = nickname,
                height = height,
                weight = weight,
                dominantFoot = dominantFoot,
                positions = positions,
                birthDate = birthDate,
                rating = currentUserProfile?.rating, // Preserva classificação se houver
                isAdmin = isAdmin
            )

            val success = repository.saveAthleteProfile(newAthlete)
            isLoading = false
            if (success) {
                currentUserProfile = newAthlete
                startRealtimeListeners()
                onSuccess()
            } else {
                onError("Erro ao salvar dados cadastrais no Firestore.")
            }
        }
    }

    // --- CONTROLE DE PRESENÇA E PARTIDAS ---

    fun joinMatch(type: String, onError: (String) -> Unit) {
        val uid = repository.currentUserId ?: return
        val profile = currentUserProfile ?: return
        val presence = PresencePlayer(
            athleteId = uid,
            name = profile.name,
            nickname = profile.nickname,
            photoUrl = profile.photoUrl,
            type = type
        )

        viewModelScope.launch {
            val success = repository.addPlayerToPresenceList(presence)
            if (!success) {
                onError("Lista de presença fechada ou erro ao registrar.")
            }
        }
    }

    fun leaveMatch() {
        val uid = repository.currentUserId ?: return
        viewModelScope.launch {
            repository.removePlayerFromPresenceList(uid)
        }
    }

    fun toggleMatchState(isOpen: Boolean, date: String) {
        viewModelScope.launch {
            repository.setMatchState(isOpen, date)
        }
    }

    // --- FUNÇÕES EXCLUSIVAS ADMIN ---

    fun updatePlayerRating(athleteId: String, stars: Int) {
        viewModelScope.launch {
            repository.updateAthleteRating(athleteId, stars)
        }
    }

    fun generateTeams(numTeams: Int = 2) {
        val currentPlayers = matchState?.playersList ?: return
        val athletesMap = allAthletes.associateBy { it.id }
        teams = TeamBalancer.balanceTeams(currentPlayers, athletesMap, numTeams)
    }

    override fun onCleared() {
        super.onCleared()
        stopListeners()
    }
}
