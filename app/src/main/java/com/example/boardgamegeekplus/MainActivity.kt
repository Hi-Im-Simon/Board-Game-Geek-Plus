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

class MainActivity : AppCompatActivity() {
    lateinit var productName: EditText
    lateinit var productQuantity: EditText
    lateinit var productID: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        productName = findViewById(R.id.productName)
        productQuantity = findViewById(R.id.productQuantity)
        productID = findViewById(R.id.productID)
    }

    fun newProduct(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        val quantityStr = productQuantity.text.toString()

        if (quantityStr == "") {
            Toast.makeText(this, "Wybierz ilość", Toast.LENGTH_SHORT).show()
        }

        val quantity = Integer.parseInt(quantityStr)
        val product = Product(productName.text.toString(), quantity)

        dbHandler.addProduct(product)
        productName.setText("")
        productQuantity.setText("")
        productID.setText("")
        Toast.makeText(this, "Produkt dodano do bazy", Toast.LENGTH_SHORT).show()
    }

    fun lookupProduct(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        val product = dbHandler.findProduct(productName.text.toString())

        if (product != null) {
            productID.text = product.id.toString()
            productQuantity.setText(product.quantity.toString())
        } else {
            productID.text = "Nie znaleziono"
        }
    }

    fun removeProduct(v: View) {
        val dbHandler = MyDBHandler(this, null, null, 1)
        val result = dbHandler.deleteProduct(productName.text.toString())

        if (result) {
            productID.text = "Produkt usunięty"
            productName.setText("")
            productQuantity.setText("")
        } else {
            productID.text = "Nie znaleziono"
        }
    }

}



    class MyDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "productDB.db"
        val TABLE_PRODUCTS = "products"
        val COLUMN_ID = "_id"
        val COLUMN_PRODUCTNAME = "productname"
        val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PRODUCTS_TABLE = ("CREATE TABLE " +
                TABLE_PRODUCTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_PRODUCTNAME +
                " TEXT," + COLUMN_QUANTITY + " INTEGER" + ")")
        db.execSQL(CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    fun addProduct(product: Product) {
        val values = ContentValues()
        values.put(COLUMN_PRODUCTNAME, product.productName)
        values.put(COLUMN_QUANTITY, product.quantity)
        val db = this.writableDatabase
        db.insert(TABLE_PRODUCTS, null, values)
        db.close()
    }

    fun findProduct(productname: String): Product? {
        val query = "SELECT * FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCTNAME LIKE \"$productname\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var product: Product? = null

        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            val name = cursor.getString(1)
            val quantity = cursor.getInt(2)
            product = Product(id, name, quantity)
            cursor.close()
        }
        db.close()
        return product
    }

    fun deleteProduct(productname: String) : Boolean {
        var result = false
        val query = "SELECT * FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCTNAME LIKE \"$productname\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            db.delete(TABLE_PRODUCTS, COLUMN_ID+ " = ?", arrayOf(id.toString()))
            cursor.close()
            result=true
        }
        db.close()
        return result
    }
}


class Product {
    var id: Int = 0
    var productName: String? = null
    var quantity: Int = 0

    constructor(id: Int, productname: String, quantity: Int) {
        this.id = id
        this.productName = productname
        this.quantity = quantity
    }

    constructor(productname: String, quantity: Int) {
        this.productName = productname
        this.quantity = quantity
    }
}
