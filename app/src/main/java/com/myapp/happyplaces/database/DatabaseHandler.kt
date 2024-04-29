package com.myapp.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.myapp.happyplaces.models.HappyPlace

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, HAPPY_PLACES_DATABASE, null, DATABASE_VERSION) {


    companion object {
        private const val DATABASE_VERSION = 1
        private const val HAPPY_PLACES_DATABASE = "happy_places_database"
        private  const val HAPPY_PLACES_TABLE = "happy_places_table"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_IMAGE = "image"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"

    }

    override fun onCreate(db: SQLiteDatabase?) {

        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $HAPPY_PLACES_TABLE (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_TITLE TEXT,
                $KEY_DESCRIPTION TEXT,
                $KEY_DATE TEXT,
                $KEY_LOCATION TEXT,
                $KEY_IMAGE TEXT,
                $KEY_LATITUDE REAL,
                $KEY_LONGITUDE REAL
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $HAPPY_PLACES_TABLE")
        onCreate(db)
//        In summary, when the app is updated, and the database version number changes, the onUpgrade method is called. It drops the existing table (if it exists) and then calls onCreate to recreate the database with the updated schema.
    }

    fun addToHappyPlace(happyPlace: HappyPlace) : Long {

        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(KEY_TITLE, happyPlace.title)
        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
        contentValues.put(KEY_DATE, happyPlace.date)
        contentValues.put(KEY_LOCATION, happyPlace.location)
        contentValues.put(KEY_IMAGE, happyPlace.image)
        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)

        val result = db.insert(HAPPY_PLACES_TABLE, null, contentValues)
//        The code val result = db.insert(HAPPY_PLACES_TABLE, null, contentValues) is used to insert a new record (row) into the SQLite database

//        The result variable is declared with the type Long.
//The insert method returns the row ID of the newly inserted row, which is a Long value.
//If the insertion is successful, result will contain the row ID of the new record. If there's an error or the insertion fails, it will typically be -1.
        db.close()

        return result
    }

    fun updateHappyPlace(happyPlace: HappyPlace) : Int {

        val db = this.writableDatabase

        val contentValues = ContentValues()

        contentValues.put(KEY_TITLE, happyPlace.title)
        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
        contentValues.put(KEY_DATE, happyPlace.date)
        contentValues.put(KEY_LOCATION, happyPlace.location)
        contentValues.put(KEY_IMAGE, happyPlace.image)
        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)

        val update = db.update(HAPPY_PLACES_TABLE, contentValues, "$KEY_ID = ${happyPlace.id}", null )
//        The code val result = db.insert(HAPPY_PLACES_TABLE, null, contentValues) is used to insert a new record (row) into the SQLite database

//        The result variable is declared with the type Long.
//The insert method returns the row ID of the newly inserted row, which is a Long value.
//If the insertion is successful, result will contain the row ID of the new record. If there's an error or the insertion fails, it will typically be -1.
        db.close()

        return update
    }

    fun deleteHappyPlace(happyPlaceId : Int) : Int {
        val db = this.writableDatabase


        val delete = db.delete(HAPPY_PLACES_TABLE, "$KEY_ID = $happyPlaceId", null)

        db.close()
        return delete
    }

    fun getHappyPlacesList(): ArrayList<HappyPlace> {

        // A list is initialize using the data model class in which we will add the values from cursor.
        val happyPlaceList: ArrayList<HappyPlace> = ArrayList()

        val selectQuery = "SELECT  * FROM $HAPPY_PLACES_TABLE" // Database select query

        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val idIndex = cursor.getColumnIndex(KEY_ID)
                    val titleIndex = cursor.getColumnIndex(KEY_TITLE)
                    val descriptionIndex = cursor.getColumnIndex(KEY_DESCRIPTION)
                    val imageIndex = cursor.getColumnIndex(KEY_IMAGE)
                    val dateIndex = cursor.getColumnIndex(KEY_DATE)
                    val locationIndex = cursor.getColumnIndex(KEY_LOCATION)
                    val latitudeIndex = cursor.getColumnIndex(KEY_LATITUDE)
                    val longitudeIndex = cursor.getColumnIndex(KEY_LONGITUDE)

                    if (idIndex >= 0 && titleIndex >= 0 && descriptionIndex >= 0 &&
                        locationIndex >= 0 && imageIndex >= 0 && latitudeIndex >= 0 && longitudeIndex >= 0
                    ) {
                        val happyPlace = HappyPlace(
                            cursor.getInt(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(imageIndex),
                            cursor.getString(descriptionIndex),
                            cursor.getString(dateIndex),
                            cursor.getString(locationIndex),
                            cursor.getDouble(latitudeIndex),
                            cursor.getDouble(longitudeIndex)
                        )

                        happyPlaceList.add(happyPlace)
                    }
                } while (cursor.moveToNext()) // Use do-while to correctly loop through all rows
                cursor.close()
            }
        } catch (e : SQLiteException) {
            e.printStackTrace()
        }

        return happyPlaceList
    }

}