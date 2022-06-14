package com.example.boardgamegeekplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlin.math.min

class AddonsActivity : AppCompatActivity() {
    lateinit var itemIDs: Array<TextView>
    lateinit var itemNames: Array<TextView>
    lateinit var itemImages: Array<ImageView>
    lateinit var titleEmpty: TextView

    var games: MutableList<Game> = emptyArray<Game>().toMutableList()

    var pageID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addons)

        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("addons")

        itemIDs = arrayOf(findViewById(R.id.itemID1), findViewById(R.id.itemID2), findViewById(R.id.itemID3), findViewById(R.id.itemID4))
        itemNames = arrayOf(findViewById(R.id.itemName1), findViewById(R.id.itemName2), findViewById(R.id.itemName3), findViewById(R.id.itemName4))
        itemImages = arrayOf(findViewById(R.id.itemImage1), findViewById(R.id.itemImage2), findViewById(R.id.itemImage3), findViewById(R.id.itemImage4))
        titleEmpty = findViewById(R.id.titleEmpty)

        loadData()
    }

    fun sortDefault(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("addons")
        loadData()
    }

    fun sortName(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("addons", "name")
        loadData()
    }

    private fun loadData() {
        if (games.size == 0) {
            titleEmpty.text = "Nie posiadasz żadnych dodatków!"
            return
        }
        for (i in pageID * 4 until min(pageID * 4 + 4, games.size)) {
            itemIDs[i % 4].text = (pageID * 4 + (i % 4) + 1).toString()
            itemNames[i % 4].text = games[i].name
            Picasso.get().load(games[i].image).into(itemImages[i % 4])
        }
    }

    fun switchBack(v: View) {
        if (pageID > 0) {
            pageID--
            loadData()
        }
    }

    fun switchNext(v: View) {
        if (pageID < games.size / 4) {
            pageID++
            loadData()
        }
    }

    fun switchToMain(v: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}