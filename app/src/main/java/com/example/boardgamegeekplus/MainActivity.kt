package com.example.boardgamegeekplus

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.net.URL

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.net.MalformedURLException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

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
        if (SharedData.sync || !dbHandler.checkUser(SharedData.userName)) {
            downloadData()
            SharedData.sync = false
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

    @SuppressLint("StaticFieldLeak")
    @Suppress("DEPRECATION")
    private inner class DataDownloader : AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            loadData()
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                val gameUrl = URL("https://boardgamegeek.com/xmlapi2/collection?username=" + SharedData.userName + "&type=user&stats=1")
                val connection = gameUrl.openConnection()
                connection.connect()

                val lengthOfFile = connection.contentLength
                val isStream = gameUrl.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/data.xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100 / lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                return "Wrong URL Error"
            } catch (e: FileNotFoundException) {
                return "No file Error"
            } catch (e: IOException) {
                return "IO Error"
            }
            return "success"
        }
    }

    fun downloadData() {
        val dd = DataDownloader()
        dd.execute()
    }

    fun syncData(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        SharedData.lastSyncDate = dbHandler.getLastSyncDate()

        val intent = Intent(this, SyncActivity::class.java)
        startActivity(intent)
//        downloadData()
    }

    fun loadData() {
        val fileName = "data.xml"
        val path = filesDir
        val inDir = File(path, "XML")

        if (inDir.exists()) {
            val dbHandler = MyDBHandler(this, null, null, 1)
            dbHandler.clearUserData(SharedData.userName)

            val currentTime = SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.GERMANY).format(Date()).toString()

            val file = File(inDir, fileName)
            if (file.exists()) {
                val xml: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xml.documentElement.normalize()

                var ranks_i = 0

                var count_games = 0
                var count_addons = 0

                val items: NodeList = xml.getElementsByTagName("item")
                val ranks: NodeList = xml.getElementsByTagName("rank")

                for (i in 0 until items.length) {
                    var itemNode: Node = items.item(i)

                    var itemIsgame = 0

                    if (itemNode.getAttributes().getNamedItem("subtype").getNodeValue() == "boardgame") {
                        count_games++
                        itemIsgame = 1
                    }
                    else count_addons++

                    var itemName = ""
                    var itemYear = 0
                    var itemImage = ""
                    var itemRank = 0

                    if (itemNode.nodeType == Node.ELEMENT_NODE) {
                        itemNode = itemNode as Element
                        val childrenNodes = itemNode.childNodes

                        for (j in 0 until childrenNodes.length) {
                            val childNode = childrenNodes.item(j)
                            if (childNode is Element) {
                                when (childNode.nodeName) {
                                    "name" -> {
                                        itemName = childNode.textContent
                                    }
                                    "yearpublished" -> {
                                        itemYear = childNode.textContent.toInt()
                                    }
                                    "thumbnail" -> {
                                        itemImage = childNode.textContent
                                    }
                                }
                            }
                        }

                        while (true) {
                            val rankNode: Node = ranks.item(ranks_i)
                            ranks_i++
                            if (rankNode.getAttributes().getNamedItem("name").getNodeValue() == "boardgame") {
                                val itemRankTemp = rankNode.getAttributes().getNamedItem("value").getNodeValue().toIntOrNull()
                                if (itemRankTemp != null) itemRank = itemRankTemp
                                else itemRank = 0
                                break
                            }
                        }
                    }

                    val game = Game(itemName, itemYear, itemImage, itemRank, itemIsgame)

                    dbHandler.addGame(game, SharedData.userName, currentTime)
                }
            }
        }
        showData()
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

    fun clearUserData(userName: String) {
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

    fun listItems(type: String): MutableList<Game> {
        val games: MutableList<Game> = emptyArray<Game>().toMutableList()

        var query = ""

        if (type == "games") {
            query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ISGAME = 1"
        }
        else if (type == "addons") {
            query = "SELECT * FROM $TABLE_GAMES WHERE $COLUMN_ISGAME = 0"
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
        return "false"
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
