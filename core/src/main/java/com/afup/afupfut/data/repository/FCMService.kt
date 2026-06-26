package com.afup.afupfut.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aqui o token FCM pode ser enviado para o banco de dados se necessário.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extrai título e corpo do payload da notificação
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "AFUP FUT"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Atualização da partida!"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "afup_fut_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação para Android 8.0 (Oreo) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações AFUP FUT",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal de alertas de presença e times para o AFUP FUT"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Configura a intent de clique para abrir a atividade launcher do aplicativo de forma dinâmica
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        } ?: Intent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reconstrói o ícone usando o ícone launcher padrão
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces) // Ícone temporário do sistema ou launcher
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
