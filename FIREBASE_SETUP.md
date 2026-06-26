# Guia Passo a Passo: Configuração do Firebase no AFUP FUT

Este documento orienta detalhadamente como criar, configurar e conectar o Firebase ao aplicativo **AFUP FUT**.

---

## Passo 1: Criar o Projeto no Console do Firebase

1. Acesse o [Console do Firebase](https://console.firebase.google.com/).
2. Clique em **Adicionar projeto** (ou **Criar um projeto**).
3. Insira o nome do projeto: `AFUP FUT` e clique em **Continuar**.
4. (Opcional) Escolha se deseja ativar o Google Analytics para o projeto e clique em **Continuar** (ou desative se preferir um setup mais rápido).
5. Clique em **Criar projeto** e aguarde a inicialização. Quando estiver pronto, clique em **Continuar**.

---

## Passo 2: Registrar o Aplicativo Android

1. Na tela inicial do seu projeto no console do Firebase, clique no ícone do **Android** (a figura do robozinho) para adicionar um aplicativo.
2. No campo **Nome do pacote do Android**, insira exatamente o ID do pacote configurado no aplicativo:
   ```text
   com.afup.afupfut
   ```
3. No campo **Apelido do app (opcional)**, insira `AFUP FUT Android`.
4. No campo **Certificado de assinatura de depuração SHA-1 (opcional)**, você pode inserir sua chave de assinatura (útil se for usar login do Google futuramente).
   - Para obter essa chave no seu terminal local, execute:
     ```bash
     ./gradlew signingReport
     ```
     Copie o valor `SHA1` listado na tarefa de debug e cole no campo correspondente.
5. Clique em **Registrar app**.

---

## Passo 3: Baixar e Adicionar o Arquivo de Configuração

1. O console irá gerar o arquivo de configuração **`google-services.json`**.
2. Faça o download desse arquivo.
3. Copie o arquivo `google-services.json` baixado e coloque-o na pasta:
   ```text
   /home/arnaldo/Documentos/Android-PRJ/AFUP_FUT/app/
   ```
   > [!IMPORTANT]
   > O build do aplicativo falhará se o arquivo `google-services.json` não estiver presente dentro da pasta `app/`.

4. Clique em **Próximo** nas etapas restantes no console do Firebase até concluir.

---

## Passo 4: Ativar o Firebase Authentication

1. No menu lateral esquerdo do Console do Firebase, expanda **Compilação** (Build) e clique em **Authentication**.
2. Clique em **Começar** (Get Started).
3. Na aba **Método de login** (Sign-in method), selecione **E-mail/senha**.
4. Ative a primeira chave de opção (**E-mail/senha**) e clique em **Salvar**.
   - Isso permitirá que atletas criem contas e façam login usando e-mail e senha.

---

## Passo 5: Configurar o Cloud Firestore (Banco de Dados)

1. No menu lateral, clique em **Firestore Database**.
2. Clique em **Criar banco de dados**.
3. Selecione o local do servidor (recomenda-se `southamerica-east1` para menor latência no Brasil, ou o padrão dos EUA) e clique em **Próximo**.
4. Selecione **Iniciar no modo de teste** (para desenvolvimento rápido) ou **Modo de produção** e clique em **Criar**.
5. Vá na aba **Regras** (Rules) do Firestore e substitua o conteúdo pelas seguintes regras de segurança, garantindo proteção e acesso apropriado:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Permite leitura e escrita a usuários autenticados para perfis de atletas
    match /athletes/{athleteId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == athleteId;
      // Administradores podem ler e atualizar as notas de todos os atletas
      allow update: if request.auth != null && (request.auth.uid == athleteId || get(/databases/$(database)/documents/athletes/$(request.auth.uid)).data.isAdmin == true);
    }
    
    // Regras para a lista de presença e partidas
    match /matches/{matchId} {
      allow read: if request.auth != null;
      // Qualquer atleta pode se adicionar/remover da lista se estiver aberta, mas apenas admins podem abrir/fechar a lista
      allow write: if request.auth != null;
    }
  }
}
```
6. Clique em **Publicar**.

---

## Passo 6: Configurar o Firebase Storage (Fotos)

1. No menu lateral, clique em **Storage**.
2. Clique em **Começar** (Get Started).
3. Clique em **Avançar** e depois em **Concluir**.
4. Na aba **Regras** (Rules) do Storage, defina as regras para permitir o upload de fotos de perfil por usuários autenticados:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /avatars/{userId}.jpg {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
5. Clique em **Publicar**.

---

## Passo 7: Testar Notificações em Tempo Real (FCM)

Como o aplicativo possui o `FCMService` registrado e integrado, você pode enviar notificações em segundo plano para todos os dispositivos de teste:

1. No menu lateral, sob **Compilação**, clique em **Messaging**.
2. Clique em **Criar sua primeira campanha**.
3. Selecione **Mensagens do Firebase Notification** e clique em **Criar**.
4. Insira um **Título da notificação** (ex: *AFUP FUT*) e o **Texto da notificação** (ex: *A lista está aberta, confirme sua presença!*).
5. Clique em **Próximo**. Em **Segmentação**, escolha o aplicativo `com.afup.afupfut`.
6. Clique em **Próximo** nas demais opções e clique em **Revisar** -> **Publicar**.
7. Qualquer celular rodando o aplicativo receberá a notificação instantaneamente no sistema operacional!
