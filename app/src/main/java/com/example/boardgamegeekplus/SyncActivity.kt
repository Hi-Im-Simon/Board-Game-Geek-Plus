package com.example.boardgamegeekplus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class SyncActivity : AppCompatActivity() {
    lateinit var lastSyncDate: TextView
    lateinit var buttonSyncData: Button

    var syncClicks = 0
    var lastSyncDateVal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)

        lastSyncDate = findViewById(R.id.lastSyncDate)
        buttonSyncData = findViewById(R.id.buttonSyncData)

        val dbHandler = MyDBHandler(this, null, null, 1)
        lastSyncDateVal = dbHandler.getLastSyncDate()

        if (SharedData.userUpToDate) {
            lastSyncDate.text = "Data ostatniej synchronizacji: " + lastSyncDateVal
        }
        else {
            lastSyncDate.text = "Data ostatniej synchronizacji: nigdy"
        }

    }

    fun switchToMain(v: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun syncData(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        if (lastSyncDateVal.take(9) == SimpleDateFormat("dd/M/yyyy", Locale.GERMANY).format(Date()).toString() && syncClicks == 0) {
            buttonSyncData.setText("Na pewno?")
            syncClicks++
        }
        else {
            downloadData()
            SharedData.dataDownloaded = false
            buttonSyncData.setText("...")

            Handler().postDelayed({
                if (dbHandler.listItems("games").size + dbHandler.listItems("addons").size > 0) {
                    val newLastSyncDateVal = SimpleDateFormat("dd/M/yyyy HH:mm:ss", Locale.GERMANY).format(Date()).toString()
                    lastSyncDate.text = "Data ostatniej synchronizacji: " + newLastSyncDateVal
                    lastSyncDateVal = newLastSyncDateVal
                    buttonSyncData.setText("Synchronizuj")
                    syncClicks = 0
                }
                else {
                    buttonSyncData.setText("Użytkownik nie posiada żadnych gier!")
                }
            }, 1000)
        }
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

    fun loadData() {
        SharedData.dataDownloaded = false
        val fileName = "data.xml"
        val path = filesDir
        val inDir = File(path, "XML")

        if (inDir.exists()) {
            val dbHandler = MyDBHandler(this, null, null, 1)
            dbHandler.clearUserData()

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
                SharedData.dataDownloaded = true
            }
        }
        SharedData.userUpToDate = true
    }
}