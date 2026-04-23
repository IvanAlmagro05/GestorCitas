package com.example.gestorcitas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("citaId", 0)
        val nombre = intent.getStringExtra("nombre") ?: "Cliente"
        val fecha = intent.getStringExtra("fecha") ?: ""
        val hora = intent.getStringExtra("hora") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""
        val tipo = intent.getStringExtra("tipo") ?: "Cita"

        if (telefono.isEmpty()) {
            mostrarNotificacionError(context, id, "Número de teléfono no encontrado.")
            return
        }

        val message = "Hola, $nombre recordatorio de tu cita de $tipo para mañana $fecha a las $hora."

        try {
            // Usamos el metodo más compatible para obtener SmsManager
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            // Enviar el SMS
            smsManager.sendTextMessage(telefono, null, message, null, null)
            
            Log.d("SMS_SENT", "Intentando enviar SMS a $telefono")
            mostrarNotificacionExito(context, id, tipo, nombre)
            
        } catch (e: Exception) {
            Log.e("SMS_ERROR", "Error al enviar: ${e.message}")
            mostrarNotificacionError(context, id, "Error técnico al enviar el SMS.")
        }
    }

    private fun mostrarNotificacionExito(context: Context, id: Int, tipo: String, nombre: String) {
        val channelId = "cita_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Recordatorios", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_speakerphone)
            .setContentTitle("Recordatorio Automático")
            .setContentText("SMS enviado con éxito a $nombre.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun mostrarNotificacionError(context: Context, id: Int, errorMsg: String) {
        val channelId = "cita_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Fallo en el Recordatorio")
            .setContentText(errorMsg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
