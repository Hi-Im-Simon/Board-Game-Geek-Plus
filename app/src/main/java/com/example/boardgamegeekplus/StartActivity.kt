package com.example.boardgamegeekplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class StartActivity : AppCompatActivity() {
    lateinit var userName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        userName = findViewById(R.id.userName)
    }

    fun switchToMain(v: View) {
        SharedData.userName = userName.text.toString()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

object SharedData {
    var userName = ""
    var lastSyncDate = ""
    var sync = false
}