package com.example.basardemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.basardemo.Analytics.AnalyticsActivity

class MainActivity : AppCompatActivity() {
    var tvToken: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvToken = findViewById(R.id.tvToken)

        val receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction("com.example.basardemo.ON_NEW_TOKEN")
        this@MainActivity.registerReceiver(receiver, filter)
    }

    fun accountActivityClick(view: View) {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }

    fun mapActivityClick(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    fun locationActivityClick(view: View) {
        val intent = Intent(this, LocationActivity::class.java)
        startActivity(intent)
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if ("com.example.basardemo.ON_NEW_TOKEN" == intent.action) {
                val token = intent.getStringExtra("token")
                tvToken!!.append(token + "\n")
            }
        }
    }

    fun driveActivityClick(view: View) {
        val intent = Intent(this, DriveActivity::class.java)
        startActivity(intent)
    }

    fun analyticsActivityClick(view: View) {
        val intent = Intent(this, AnalyticsActivity::class.java)
        startActivity(intent)
    }

    fun scanActivityClick(view: View) {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }

    fun gameActivityClick(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    fun crashActivityclick(view: View) {
        val intent = Intent(this, CrashActivity::class.java)
        startActivity(intent)
    }

}