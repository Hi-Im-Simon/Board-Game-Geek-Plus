package com.example.boardgamegeekplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.view.View
import android.widget.TextView
import java.net.URL

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.net.MalformedURLException
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    lateinit var userNameMain: TextView
    lateinit var countGames: TextView
    lateinit var countAddons: TextView
    lateinit var lastSyncDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userNameMain = findViewById(R.id.userNameMain)
        countGames = findViewById(R.id.countGames)
        countAddons = findViewById(R.id.countAddons)
        lastSyncDate = findViewById(R.id.lastSyncDate)


        userNameMain.text = SharedData.userName
        downloadData()
    }

    @Suppress("DEPRECATION")
    private inner class DataDownloader: AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            loadData()
            showData()
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

    }

    fun loadData() {
        val fileName = "data.xml"
        val path = filesDir
        val inDir = File(path, "XML")

        if (inDir.exists()) {
            val file = File(inDir, fileName)
            if (file.exists()) {
                val xml: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xml.documentElement.normalize()

                var ranks_i = 0

                var count_games = 0
                var count_addons = 0

                val items: NodeList = xml.getElementsByTagName("item")
                val ranks: NodeList = xml.getElementsByTagName("rank")

                for (i in 0 until items.length) {
                    var itemNode: Node = items.item(i)

                    if (itemNode.getAttributes().getNamedItem("subtype").getNodeValue() == "boardgame") count_games++
                    else count_addons++

                    var itemName = ""
                    var itemYear = ""
                    var itemImage = ""
                    var itemRank = ""

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
                                        itemYear = childNode.textContent
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
                                itemRank = rankNode.getAttributes().getNamedItem("value").getNodeValue()
                                break
                            }
                        }
                    }
                    println(itemName)
                    println(itemYear)
                    println(itemImage)
                    println(itemRank)
                }



                countGames.text = count_games.toString()
                countAddons.text = count_addons.toString()
            }
        }
    }

    fun showData() {

    }
}
//        val xmlFactory = DocumentBuilderFactory.newInstance()
//        val xmlBuilder = xmlFactory.newDocumentBuilder()
//        val xml = xmlBuilder.parse(xmlString)
////                val xml: Document = .newInstance().newDocumentBuilder().parse(xmlString)
//
////                xml.documentElement.normalize()
////
////                val items: NodeList = xml.getElementsByTagName("image")
//
//        println(xml)
//        userNameMain.text = "hujwieco"



//    fun lookupGame(v: View) {
//        val gameUrl = "https://boardgamegeek.com/xmlapi2/search?query=" + productName.text + "&type=boardgame"
//        val request = Request.Builder().url(gameUrl).build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//            override fun onResponse(call: Call, response: Response) {
//                val xml = response.body()?.string()
//                println(xml)
////                pasteHere.appe
//            }
//
//        })
//    }

//    fun newProduct(v: View) {
//        val dbHandler = MyDBHandler(this, null, null, 1)
//        val quantityStr = productQuantity.text.toString()
//
//        if (quantityStr == "") {
//            Toast.makeText(this, "Wybierz ilość", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val quantity = Integer.parseInt(quantityStr)
//        val product = Product(productName.text.toString(), quantity)
//
//        dbHandler.addProduct(product)
//        productName.setText("")
//        productQuantity.setText("")
//        productID.setText("")
//        Toast.makeText(this, "Produkt dodano do bazy", Toast.LENGTH_SHORT).show()
//    }
//
//    fun lookupProduct(v: View) {
//        val dbHandler = MyDBHandler(this, null, null, 1)
//        val product = dbHandler.findProduct(productName.text.toString())
//
//        if (product != null) {
//            productID.text = product.id.toString()
//            productQuantity.setText(product.quantity.toString())
//        } else {
//            productID.text = "Nie znaleziono"
//        }
//    }
//
//    fun removeProduct(v: View) {
//        val dbHandler = MyDBHandler(this, null, null, 1)
//        val result = dbHandler.deleteProduct(productName.text.toString())
//
//        if (result) {
//            productID.text = "Produkt usunięty"
//            productName.setText("")
//            productQuantity.setText("")
//        } else {
//            productID.text = "Nie znaleziono"
//        }
//    }


//class MyDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
//    : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
//    companion object {
//        private val DATABASE_VERSION = 1
//        private val DATABASE_NAME = "gamesDB.db"
//        val TABLE_GAMES = "games"
//        val COLUMN_ID = "_id"
//        val COLUMN_NAME = "name"
//        val COLUMN_NAMEORIGINAL = "nameoriginal"
//        val COLUMN_RELEASEDATE = "releasedate"
//        val COLUMN_BGGID = "bgg_id"
//        val COLUMN_RANK = "rank"
//    }
//
//    override fun onCreate(db: SQLiteDatabase) {
//        val CREATE_GAMES_TABLE = (
//            "CREATE TABLE " + TABLE_GAMES
//            + "("
//            + COLUMN_ID + " INTEGER PRIMARY KEY,"
//            + COLUMN_NAME + " TEXT,"
//            + COLUMN_NAMEORIGINAL + " TEXT,"
//            + COLUMN_RELEASEDATE + " INTEGER"
//            + COLUMN_BGGID + " INTEGER"
//            + COLUMN_RANK + " INTEGER"
//            + ")"
//        )
//        db.execSQL(CREATE_GAMES_TABLE)
//    }
//
////    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
////        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
////        onCreate(db)
////    }
////
////    fun addProduct(product: Product) {
////        val values = ContentValues()
////        values.put(COLUMN_PRODUCTNAME, product.productName)
////        values.put(COLUMN_QUANTITY, product.quantity)
////        val db = this.writableDatabase
////        db.insert(TABLE_PRODUCTS, null, values)
////        db.close()
////    }
////
////    fun findProduct(productname: String): Product? {
////        val query = "SELECT * FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCTNAME LIKE \"$productname\""
////        val db = this.writableDatabase
////        val cursor = db.rawQuery(query, null)
////        var product: Product? = null
////
////        if (cursor.moveToFirst()) {
////            val id = Integer.parseInt(cursor.getString(0))
////            val name = cursor.getString(1)
////            val quantity = cursor.getInt(2)
////            product = Product(id, name, quantity)
////            cursor.close()
////        }
////        db.close()
////        return product
////    }
////
////    fun deleteProduct(productname: String) : Boolean {
////        var result = false
////        val query = "SELECT * FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCTNAME LIKE \"$productname\""
////        val db = this.writableDatabase
////        val cursor = db.rawQuery(query, null)
////        if (cursor.moveToFirst()) {
////            val id = cursor.getInt(0)
////            db.delete(TABLE_PRODUCTS, COLUMN_ID+ " = ?", arrayOf(id.toString()))
////            cursor.close()
////            result=true
////        }
////        db.close()
////        return result
////    }
//}


//class Product {
//    var id: Int = 0
//    var productName: String? = null
//    var quantity: Int = 0
//
//    constructor(id: Int, productname: String, quantity: Int) {
//        this.id = id
//        this.productName = productname
//        this.quantity = quantity
//    }
//
//    constructor(productname: String, quantity: Int) {
//        this.productName = productname
//        this.quantity = quantity
//    }
//}
