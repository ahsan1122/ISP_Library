package com.esp.library.exceedersesp.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.esp.library.R
import com.esp.library.exceedersesp.controllers.applications.ApplicationDetailScreenActivity
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer
import com.esp.library.utilities.common.CustomLogs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class MyFirebaseInstanceIDService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: " + remoteMessage.from!!)
        Log.d(TAG, "Fromnotification: " + remoteMessage.notification.toString())

        // Check if message contains a data payload.
        /* if (remoteMessage.data.size > 0) {
             Log.d(TAG, "Message data payload: " + remoteMessage.data)

             CustomLogs.displayLogs(TAG+" Message data payload: " + remoteMessage.data)

             val dataJson = JSONObject(remoteMessage.data as Map<*, *>)
             Log.e("JSON OBJECT", dataJson.toString())
             val title = dataJson.getString("title")
             val body = dataJson.getString("body")
             val applicationId = dataJson.getInt("applicationId")
             localNotification(title,body,applicationId)

         }*/


        //  val dataJson = JSONObject(remoteMessage.notification.toString())
        Log.e("JSON OBJECT", remoteMessage.notification?.body)
        Log.e("JSON OBJECT", remoteMessage.notification?.title)

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        localNotification(title, body, 0)
        /*
         val applicationId = dataJson.getInt("applicationId")
         */


        // Check if message contains a notification payload.
        /*if (remoteMessage.notification != null) {

            Log.e("JSON OBJECT", remoteMessage.notification?.body)
            val dataJson = JSONObject(remoteMessage.notification?.body)
            val title = dataJson.getString("title")
            val message = dataJson.getString("message")
            val applicationId = dataJson.getInt("applicationId")
            localNotification(title,message,applicationId)


        }*/
    }

    private fun sendRegistrationToServer(token: String?) {
        // sending gcm token to server
        Log.e(TAG, "sendRegistrationToServer: " + token!!)
    }

    companion object {
        private val TAG = MyFirebaseInstanceIDService::class.java.simpleName
    }

    fun localNotification(title: String?, messageBody: String?, applicationId: Int) {
        val intent = Intent(applicationContext, ApplicationsActivityDrawer::class.java)
        //    intent.putExtra("applicationId",applicationId)
        //you can use your launcher Activity insted of SplashActivity, But if the Activity you used here is not launcher Activty than its not work when App is in background.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "ESP"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "ESP", NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.description = "Description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            channel.canBypassDnd()
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        notificationBuilder.setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.green))
                .setContentTitle(title)
                .setContentText(messageBody)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setSmallIcon(R.drawable.ic_launch_logo)
                .setContentIntent(pendingIntent)
                // .setNumber(4)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setAutoCancel(true)


        notificationManager.notify(1000, notificationBuilder.build())
    }

}
