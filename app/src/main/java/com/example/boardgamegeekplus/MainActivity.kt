package com.example.boardgamegeekplus

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var userNameMain: TextView
    lateinit var countGames: TextView
    lateinit var countAddons: TextView
    lateinit var lastSyncDate: TextView
    lateinit var buttonSyncData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userNameMain = findViewById(R.id.userNameMain)
        countGames = findViewById(R.id.countGames)
        countAddons = findViewById(R.id.countAddons)
        lastSyncDate = findViewById(R.id.lastSyncDate)
        buttonSyncData = findViewById(R.id.buttonSyncData)

        userNameMain.text = "Użytkownik " + SharedData.userName + " "

        val dbHandler = MyDBHandler(this, null, null, 1)
        if (!dbHandler.checkUser(SharedData.userName)) {
            SharedData.userUpToDate = false
            syncData()
        }
        showData()
    }

    fun switchToGames(v: View) {
        val intent = Intent(this, GamesActivity::class.java)
        startActivity(intent)
    }

    fun switchToAddons(v: View) {
        val intent = Intent(this, AddonsActivity::class.java)
        startActivity(intent)
    }

    fun syncData(v: View) {
        val intent = Intent(this, SyncActivity::class.java)
        startActivity(intent)
    }

    fun syncData() {
        val intent = Intent(this, SyncActivity::class.java)
        startActivity(intent)
    }

    fun clearData(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        dbHandler.clearUserData()
        moveTaskToBack(true)
        exitProcess(-1)
    }

    fun showData() {
        val dbHandler = MyDBHandler(this, null, null, 1)

        countGames.text = "Liczba gier: " + dbHandler.listItems("games").size
        countAddons.text = "Liczba dodatków: " + dbHandler.listItems("addons").size
        lastSyncDate.text = "Data ostatniej synchronizacji: " + dbHandler.getLastSyncDate()
    }
}

class MyDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "gamesDB.db"
        val TABLE_GAMES = "games"
        val COLUMN_ID = "_id"
        val COLUMN_NAME = "name"
        val COLUMN_YEAR = "year"
        val COLUMN_IMAGE = "image"
        val COLUMN_RANK = "rank"
        val COLUMN_ISGAME = "isgame"
        val COLUMN_USER = "user"
        val COLUMN_SYNCTIME = "synctime"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAMES_TABLE = (
            "CREATE TABLE " + TABLE_GAMES
            + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_YEAR + " INTEGER,"
            + COLUMN_IMAGE + " TEXT,"
            + COLUMN_RANK + " INTEGER,"
            + COLUMN_ISGAME + " INTEGER,"
            + COLUMN_USER + " TEXT,"
            + COLUMN_SYNCTIME + " TEXT"
            + ")"
        )
        db.execSQL(CREATE_GAMES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        onCreate(db)
    }

    fun clearUserData() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        onCreate(db)
    }

    fun addGame(game: Game, userName: String, syncTime: String) {
        val values = ContentValues()
        values.put(COLUMN_NAME, game.name)
        values.put(COLUMN_YEAR, game.year)
        values.put(COLUMN_IMAGE, game.image)
        values.put(COLUMN_RANK, game.rank)
        values.put(COLUMN_ISGAME, game.isgame)
        values.put(COLUMN_USER, userName)
        values.put(COLUMN_SYNCTIME, syncTime)

        val db = this.writableDatabase
        db.insert(TABLE_GAMES, null, values)
        db.close()
    }

    fun listItems(type: String, listBy: String = "_id"): MutableList<Game> {
        val games: MutableList<Game> = emptyArray<Game>().toMutableList()

        var query = ""

        if (type == "games") {
            query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ISGAME = 1 ORDER BY $listBy"
        }
        else if (type == "addons") {
            query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ISGAME = 0 ORDER BY $listBy"
        }

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val game = Game()
                game.id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                game.name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                game.year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
                game.image = cursor.getString(cursor.getColumnIndexOrThrow("image"))
                game.rank = cursor.getInt(cursor.getColumnIndexOrThrow("rank"))
                games.add(game)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return games
    }

    fun checkUser(newUserName: String) : Boolean {
        val query = "SELECT $COLUMN_USER FROM $TABLE_GAMES WHERE $COLUMN_ID = 1"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow("user")) == newUserName
        }
        return false
    }

    fun getLastSyncDate() : String {
        val query = "SELECT $COLUMN_SYNCTIME FROM $TABLE_GAMES WHERE $COLUMN_ID = 1"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndexOrThrow("synctime"))
        }
        return "nigdy"
    }
}


class Game {
    var id: Int = 0
    var name: String? = null
    var year: Int = 0
    var image: String? = null
    var rank: Int = 0
    var isgame: Int = 0

    constructor(id: Int, name: String, year: Int, image: String, rank: Int, isgame: Int) {
        this.id = id
        this.name = name
        this.year = year
        this.image = image
        this.rank = rank
        this.isgame = isgame
    }

    constructor(name: String, year: Int, image: String, rank: Int, isgame: Int) {
        this.name = name
        this.year = year
        this.image = image
        this.rank = rank
        this.isgame = isgame
    }

    constructor() {
        this.name = ""
        this.year = 0
        this.image = ""
        this.rank = 0
    }
}
