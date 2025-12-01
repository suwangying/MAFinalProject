package com.example.ma_final_project

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EmergencyApp.db"
        private const val DATABASE_VERSION = 3 // incremented because of schema change

        // Table: User Profile
        const val TABLE_USER = "UserProfile"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_FIRSTNAME = "firstname"
        const val COL_USER_LASTNAME = "lastname"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PASSWORD = "password"

        // Table: Emergency Contacts
        const val TABLE_CONTACTS = "EmergencyContacts"
        const val COL_CONTACT_ID = "id"
        const val COL_CONTACT_FIRSTNAME = "firstname"
        const val COL_CONTACT_LASTNAME = "lastname"
        const val COL_CONTACT_PHONE = "phone"
        const val COL_CONTACT_USER_PHONE = "user_phone"

        // Table: Safe Locations
        const val TABLE_LOCATIONS = "SafeLocations"
        const val COL_LOCATION_ID = "id"
        const val COL_LOCATION_NAME = "name"
        const val COL_LOCATION_ADDRESS = "address"
        const val COL_LOCATION_USER_PHONE = "user_phone"
        const val COL_LOCATION_LAT = "latitude"
        const val COL_LOCATION_LNG = "longitude"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create User Profile Table
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COL_USER_PHONE TEXT PRIMARY KEY,
                $COL_USER_FIRSTNAME TEXT,
                $COL_USER_LASTNAME TEXT,
                $COL_USER_EMAIL TEXT,
                $COL_USER_PASSWORD TEXT
            )
        """.trimIndent()
        db?.execSQL(createUserTable)

        // Create Emergency Contacts Table (linked to user)
        val createContactsTable = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CONTACT_FIRSTNAME TEXT,
                $COL_CONTACT_LASTNAME TEXT,
                $COL_CONTACT_PHONE TEXT,
                $COL_CONTACT_USER_PHONE TEXT,
                FOREIGN KEY($COL_CONTACT_USER_PHONE) REFERENCES $TABLE_USER($COL_USER_PHONE)
            )
        """.trimIndent()
        db?.execSQL(createContactsTable)

        // Create Safe Locations Table (linked to user)
        val createLocationsTable = """
            CREATE TABLE $TABLE_LOCATIONS (
                $COL_LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOCATION_NAME TEXT,
                $COL_LOCATION_ADDRESS TEXT,
                $COL_LOCATION_USER_PHONE TEXT,
                $COL_LOCATION_LAT REAL,
                $COL_LOCATION_LNG REAL,
                FOREIGN KEY($COL_LOCATION_USER_PHONE) REFERENCES $TABLE_USER($COL_USER_PHONE)
            )
        """.trimIndent()
        db?.execSQL(createLocationsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun addUser(firstname: String, lastname: String, phone: String, email: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_FIRSTNAME, firstname)
            put(COL_USER_LASTNAME, lastname)
            put(COL_USER_PHONE, phone)
            put(COL_USER_EMAIL, email)
            put(COL_USER_PASSWORD, password)
        }
        return db.insert(TABLE_USER, null, values)
    }

    fun getUser(phone: String): Cursor {
        val db = readableDatabase
        return db.query(TABLE_USER, null, "$COL_USER_PHONE=?", arrayOf(phone), null, null, null)
    }
    // Update all user fields EXCEPT the phone number
    fun updateUserExceptPhone(firstname: String, lastname: String, email: String, password: String, oldPhone: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_FIRSTNAME, firstname)
            put(COL_USER_LASTNAME, lastname)
            put(COL_USER_EMAIL, email)
            put(COL_USER_PASSWORD, password)
        }
        return db.update(TABLE_USER, values, "$COL_USER_PHONE=?", arrayOf(oldPhone))
    }
    // Change the user's phone number (this is the primary key)
    fun updatePhone(oldPhone: String, newPhone: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_PHONE, newPhone)
        }
        return db.update(TABLE_USER, values, "$COL_USER_PHONE=?", arrayOf(oldPhone))
    }

    // Delete user profile row from DB (used for full account deletion)
    fun deleteUser(phone: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_USER, "$COL_USER_PHONE=?", arrayOf(phone))
    }
    // Add a new emergency contact for the given userPhone
    fun addContact(userPhone: String, firstname: String, lastname: String, phone: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTACT_FIRSTNAME, firstname)
            put(COL_CONTACT_LASTNAME, lastname)
            put(COL_CONTACT_PHONE, phone)
            put(COL_CONTACT_USER_PHONE, userPhone)
        }
        return db.insert(TABLE_CONTACTS, null, values)
    }

    // Get all contacts associated with a specific user
    fun getContactsForUser(userPhone: String): Cursor {
        val db = readableDatabase
        return db.query(TABLE_CONTACTS, null, "$COL_CONTACT_USER_PHONE=?", arrayOf(userPhone), null, null, null)
    }
    

    // Update an existing contact row
    fun updateContact(id: Int, firstname: String, lastname: String, phone: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTACT_FIRSTNAME, firstname)
            put(COL_CONTACT_LASTNAME, lastname)
            put(COL_CONTACT_PHONE, phone)
        }
        return db.update(TABLE_CONTACTS, values, "$COL_CONTACT_ID=?", arrayOf(id.toString()))
    }

    // Delete a contact based on its id
    fun deleteContact(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_CONTACTS, "$COL_CONTACT_ID=?", arrayOf(id.toString()))
    }

    // Add a safe location with name, address and coordinates for a given user
    fun addLocation(userPhone: String, name: String, address: String, lat: Double, lng: Double): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_NAME, name)
            put(COL_LOCATION_ADDRESS, address)
            put(COL_LOCATION_LAT, lat)
            put(COL_LOCATION_LNG, lng)
            put(COL_LOCATION_USER_PHONE, userPhone)
        }
        return db.insert(TABLE_LOCATIONS, null, values)
    }

    // Get all saved safe locations for a specific user
    fun getLocationsForUser(userPhone: String): Cursor {
        val db = readableDatabase
        return db.query(TABLE_LOCATIONS, null, "$COL_LOCATION_USER_PHONE=?", arrayOf(userPhone), null, null, null)
    }

    // Delete a safe location row
    fun deleteLocation(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_LOCATIONS, "$COL_LOCATION_ID=?", arrayOf(id.toString()))
    }

    // Update the name/address/coordinates for an existing safe location
    fun updateLocation(id: Int, name: String, address: String, lat: Double, lng: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_NAME, name)
            put(COL_LOCATION_ADDRESS, address)
            put(COL_LOCATION_LAT, lat)
            put(COL_LOCATION_LNG, lng)
        }
        return db.update(TABLE_LOCATIONS, values, "$COL_LOCATION_ID=?", arrayOf(id.toString()))
    }
}
