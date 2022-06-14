package com.example.boardgamegeekplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class StartActivity : AppCompatActivity() {
    lateinit var userName: TextView
    lateinit var buttonConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        userName = findViewById(R.id.userName)
        buttonConfirm = findViewById(R.id.buttonConfirm)
    }

    fun switchToMain(v: View) {
        SharedData.userName = userName.text.toString()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

object SharedData {
    var userName = ""
    var dataDownloaded = false
    var userUpToDate = true
}