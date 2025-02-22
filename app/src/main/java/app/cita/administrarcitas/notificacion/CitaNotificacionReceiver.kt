package app.cita.administrarcitas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat


class CitaNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val estado = intent?.getStringExtra("estado") ?: return
        val citaId = intent.getStringExtra("citaId") ?: return
        val mensaje = if (estado == "ACEPTADO") "Tu cita ha sido aceptada" else "Tu cita ha sido cancelada"

        // Crear la notificación
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = citaId.hashCode()

        // Crear el canal de notificación (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "cita_channel_id",
                "Citas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificación de citas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Crear la notificación
        val notification = NotificationCompat.Builder(context, "cita_channel_id")
            .setSmallIcon(R.drawable.ic_add)
            .setContentTitle("Estado de tu cita")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        notificationManager.notify(notificationId, notification)
    }
}
