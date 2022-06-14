package com.example.boardgamegeekplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class SyncActivity : AppCompatActivity() {
    lateinit var lastSyncDate: TextView
    lateinit var buttonSyncData: Button

    var syncClicks = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)

        lastSyncDate = findViewById(R.id.lastSyncDate)
        buttonSyncData = findViewById(R.id.buttonSyncData)

        lastSyncDate.text = "Data ostatniej synchronizacji: " + SharedData.lastSyncDate
    }

    fun switchToMain(v: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun syncData(v: View) {
        if (SharedData.lastSyncDate.take(9) == SimpleDateFormat("dd/M/yyyy", Locale.GERMANY).format(Date()).toString() && syncClicks == 0) {
            buttonSyncData.setText("Na pewno?")
            syncClicks++
        }
        else {
            SharedData.sync = true
            lastSyncDate.text = ("Data ostatniej synchronizacji: " + SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.GERMANY).format(Date()).toString())
            buttonSyncData.setText("Synchronizuj")
        }
    }
}