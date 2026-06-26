# AFUP FUT - Plataforma de Gestão de Peladas do Clube AFUP

O **AFUP FUT** é um aplicativo Android nativo desenvolvido em **Kotlin** e **Jetpack Compose** integrado ao **Firebase**. Projetado para o clube AFUP, ele simplifica o cadastro de atletas, a confirmação de presença em partidas, a avaliação técnica dos jogadores e a divisão automatizada e equilibrada de times, exibindo-os de forma tática em um campinho de futebol.

---

## 🚀 Principais Funcionalidades

1. **Cadastro Obrigatório de Atletas**:
   - Captura de foto do perfil (armazenada no Firebase Storage).
   - Coleta de dados: Nome, Apelido (usado na lista), Altura (m), Peso (kg), Pé Dominante (Direito/Esquerdo) e Posições (Goleiro, Zagueiro, Lateral, Volante, Meia, Atacante).
   - Cálculo automático e exibição em tempo real da idade com base na data de nascimento.

2. **Lista de Presença Inteligente e em Tempo Real**:
   - Controle administrativo para abrir e fechar as inscrições de uma partida específica.
   - Combobox para indicar se a inscrição é como **Associado** ou **Convidado**.
   - **Prioridade de Associados**: A lista exibe automaticamente os sócios no topo, seguidos pelos convidados (organizados por horário de inscrição).
   - **Notificações em Tempo Real**: Todos os usuários com o aplicativo aberto recebem um banner animado instantâneo no topo da tela a cada nova inscrição.

3. **Painel do Administrador**:
   - Controle liga/desliga para abrir/fechar a lista informando a data do jogo.
   - Alertas destacados de novos atletas cadastrados que ainda não foram avaliados pelo administrador.
   - Sistema de classificação por estrelas (de 1 a 10) para definir o nível técnico de cada jogador (salvo no Firestore).

4. **Algoritmo Inteligente de Geração de Times**:
   - Separa goalkeepers automaticamente e os divide igualmente entre os times.
   - Ordena e distribui os jogadores de linha usando a técnica *Snake Draft* (alternada: 1, 2, 2, 1) para equiparar a média técnica.
   - Executa buscas locais (swaps) para refinar o balanceamento técnico (média de estrelas) e físico (média de idade, altura e peso).

5. **Campinho de Futebol Tático**:
   - Renderização visual premium de um gramado de futebol com as marcações oficiais (linhas, círculo central, grandes áreas).
   - Distribuição espacial dos jogadores nos lados do campo de acordo com suas posições de jogo (Goleiros atrás, Zagueiros/Laterais na defesa, Meias no meio, Atacantes na frente).
   - Exibição de cada jogador como um círculo com sua foto, estrelas de classificação e apelido.
   - Exibição do selo do time com a média geral de estrelas (força do time), mostrando o equilíbrio técnico exato.

---

## 🛠️ Stack Tecnológica

- **Linguagem**: Kotlin (JVM 11)
- **Interface Gráfica**: Jetpack Compose (Material Design 3)
- **Banco de Dados**: Cloud Firestore (Real-Time NoSQL)
- **Armazenamento de Mídia**: Firebase Storage (Upload de fotos de perfil)
- **Autenticação**: Firebase Authentication (E-mail e senha)
- **Notificações**: Firebase Cloud Messaging (FCM)
- **Carregamento de Imagens**: Coil (com OkHttp)

---

## 📂 Arquitetura do Projeto

Abaixo estão localizados os principais arquivos criados para esta solução:

- [settings.gradle.kts](settings.gradle.kts): Repositórios e configurações de módulos.
- [build.gradle.kts](build.gradle.kts): Plugins raiz do projeto.
- [gradle/libs.versions.toml](gradle/libs.versions.toml): Catálogo de versões e bibliotecas de terceiros.
- [AndroidManifest.xml](app/src/main/AndroidManifest.xml): Permissões do Android, atividades e serviços.
- [app/build.gradle.kts](app/build.gradle.kts): Dependências de compilação Compose/Firebase e tarefas automáticas do APK.
- **Camada de Dados (Modelos & Repositórios)**:
  - [Athlete.kt](app/src/main/java/com/afup/afupfut/data/model/Athlete.kt)
  - [MatchState.kt](app/src/main/java/com/afup/afupfut/data/model/MatchState.kt)
  - [FirebaseRepository.kt](app/src/main/java/com/afup/afupfut/data/repository/FirebaseRepository.kt)
  - [FCMService.kt](app/src/main/java/com/afup/afupfut/data/repository/FCMService.kt)
- **Lógica e UI**:
  - [TeamBalancer.kt](app/src/main/java/com/afup/afupfut/util/TeamBalancer.kt): Algoritmo de distribuição.
  - [MatchViewModel.kt](app/src/main/java/com/afup/afupfut/ui/viewmodel/MatchViewModel.kt): Gerenciador de estados reativos.
  - [Theme.kt / Color.kt](app/src/main/java/com/afup/afupfut/ui/theme/): Paleta Futuristic Stadium Dark.
  - [LoginScreen.kt](app/src/main/java/com/afup/afupfut/ui/screens/LoginScreen.kt)
  - [RegisterAthleteScreen.kt](app/src/main/java/com/afup/afupfut/ui/screens/RegisterAthleteScreen.kt)
  - [MatchPresenceScreen.kt](app/src/main/java/com/afup/afupfut/ui/screens/MatchPresenceScreen.kt)
  - [AdminPanelScreen.kt](app/src/main/java/com/afup/afupfut/ui/screens/AdminPanelScreen.kt)
  - [SoccerFieldScreen.kt](app/src/main/java/com/afup/afupfut/ui/screens/SoccerFieldScreen.kt)
  - [MainActivity.kt](app/src/main/java/com/afup/afupfut/MainActivity.kt): Navegação geral.

---

## 🛠️ Como Compilar e Gerar o APK

Para compilar o aplicativo localmente e gerar o instalador do Android, siga os passos abaixo:

1. Certifique-se de configurar o Firebase baixando seu arquivo `google-services.json` e inserindo-o na pasta `app/` do projeto (siga o guia detalhado em [FIREBASE_SETUP.md](FIREBASE_SETUP.md)).
2. No diretório raiz do projeto, execute o comando de build Gradle no terminal:
   ```bash
   export JAVA_HOME=/home/arnaldo/.android_build_env/jdk-17
   export ANDROID_HOME=/home/arnaldo/.android_build_env/android-sdk
   export PATH=$JAVA_HOME/bin:$PATH
   ./gradlew assembleDebug
   ```
3. O script Gradle foi personalizado para copiar e renomear o arquivo resultante automaticamente. Ao concluir a execução, o APK instalado estará pronto na pasta do projeto em:
   ```text
   /home/arnaldo/Documentos/Android-PRJ/AFUP_FUT/APKs Gerados/AFUP_FUT-debug.apk
   ```

---

## 🧪 Rodando Testes Unitários do Balanceador

Para testar e validar o algoritmo de balanceamento com 14 jogadores simulados e conferir o equilíbrio de forças das médias de estrelas geradas:
```bash
./gradlew :app:testDebugUnitTest
```
Os resultados e o log detalhado dos times gerados aparecerão diretamente no console do Gradle.
