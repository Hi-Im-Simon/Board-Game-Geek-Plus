package com.example.boardgamegeekplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import android.content.ContentValues
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.net.URL

import android.util.Xml
import android.widget.TableRow
import okhttp3.*
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    lateinit var userNameMain: TextView

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userNameMain = findViewById(R.id.userNameMain)


        userNameMain.text = SharedData.userName
    }

    fun syncData(v: View) {
        val gameUrl = "https://boardgamegeek.com/xmlapi2/collection?username=" + SharedData.userName + "&type=user&stats=1"
        val request = Request.Builder().url(gameUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val xmlString = response.body()?.string()

                val xmlFactory = DocumentBuilderFactory.newInstance()
                val xmlBuilder = xmlFactory.newDocumentBuilder()
                val xml = xmlBuilder.parse(xmlString)
//                val xml: Document = .newInstance().newDocumentBuilder().parse(xmlString)

//                xml.documentElement.normalize()
//
//                val items: NodeList = xml.getElementsByTagName("image")

                println(xml)
                userNameMain.text = "hujwieco"
////                pasteHere.appe
            }

        })
    }

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

}


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
