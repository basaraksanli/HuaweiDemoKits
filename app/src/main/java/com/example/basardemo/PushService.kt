package com.example.basardemo

import android.content.Intent
import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class PushService : HmsMessageService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.i(TAG, "receive token:$token")

        sendTokenToDisplay(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            Log.i(
                TAG,
                "Message data payload: " + remoteMessage.data
            )

        }
        if (remoteMessage.notification != null) {
            Log.i(
                TAG,
                "Message Notification Body: " + remoteMessage.notification.body
            )
        }
    }

    override fun onMessageSent(s: String) {}
    override fun onSendError(s: String, e: Exception) {}
    private fun sendTokenToDisplay(token: String) {
        val intent = Intent("com.example.basardemo.ON_NEW_TOKEN")
        intent.putExtra("token", token)
        sendBroadcast(intent)
    }
    companion object {
        private const val TAG = "PushDemoLog"
    }
}