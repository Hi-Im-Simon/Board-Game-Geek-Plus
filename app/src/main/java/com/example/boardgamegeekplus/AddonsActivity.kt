package com.example.boardgamegeekplus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AddonsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addons)

        loadData()
    }

    private fun loadData() {
        val dbHandler = MyDBHandler(this, null, null, 1)
//        val addons = dbHandler.listItems("addons")

//        for (i in 0 until addons.size) {
//            println(addons[i].name)
//        }
    }
}