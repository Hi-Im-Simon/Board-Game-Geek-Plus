package com.example.boardgamegeekplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlin.math.min

class GamesActivity : AppCompatActivity() {
    lateinit var itemIDs: Array<TextView>
    lateinit var itemNames: Array<TextView>
    lateinit var itemImages: Array<ImageView>
    lateinit var itemRanks: Array<TextView>
    lateinit var titleEmpty: TextView

    var games: MutableList<Game> = emptyArray<Game>().toMutableList()

    var pageID = 0
    var gameIDLocal = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("games")

        itemIDs = arrayOf(findViewById(R.id.itemID1), findViewById(R.id.itemID2), findViewById(R.id.itemID3), findViewById(R.id.itemID4))
        itemNames = arrayOf(findViewById(R.id.itemName1), findViewById(R.id.itemName2), findViewById(R.id.itemName3), findViewById(R.id.itemName4))
        itemImages = arrayOf(findViewById(R.id.itemImage1), findViewById(R.id.itemImage2), findViewById(R.id.itemImage3), findViewById(R.id.itemImage4))
        itemRanks = arrayOf(findViewById(R.id.itemRank1), findViewById(R.id.itemRank2), findViewById(R.id.itemRank3), findViewById(R.id.itemRank4))
        titleEmpty = findViewById(R.id.titleEmpty)

        loadData()
    }

    fun sortDefault(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("games")
        loadData()
    }

    fun sortName(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("games", "name")
        loadData()
    }

    fun sortRank(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        games = dbHandler.listItems("games", "rank")
        loadData()
    }

    private fun loadData() {
        if (games.size == 0) {
            titleEmpty.text = "Nie posiadasz żadnych gier!"
            return
        }
        for (i in pageID * 4 until min(pageID * 4 + 4, games.size)) {
            itemIDs[i % 4].text = (pageID * 4 + (i % 4) + 1).toString()
            itemNames[i % 4].text = games[i].name
            Picasso.get().load(games[i].image).into(itemImages[i % 4])
            itemRanks[i % 4].text = "Ranking: " + games[i].rank.toString()
        }
    }

    fun switchBack(v: View) {
        if (pageID > 0) {
            pageID--
            gameIDLocal -= 4
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