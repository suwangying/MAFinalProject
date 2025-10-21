package com.example.emergencyapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "EmergencyApp.db"
        private const val DATABASE_VERSION = 1

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

        // Table: Safe Locations
        const val TABLE_LOCATIONS = "SafeLocations"
        const val COL_LOCATION_ID = "id"
        const val COL_LOCATION_NAME = "name"
        const val COL_LOCATION_ADDRESS = "address"
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

        // Create Emergency Contacts Table
        val createContactsTable = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CONTACT_FIRSTNAME TEXT,
                $COL_CONTACT_LASTNAME TEXT,
                $COL_CONTACT_PHONE TEXT
            )
        """.trimIndent()
        db?.execSQL(createContactsTable)

        // Create Safe Locations Table
        val createLocationsTable = """
            CREATE TABLE $TABLE_LOCATIONS (
                $COL_LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOCATION_NAME TEXT,
                $COL_LOCATION_ADDRESS TEXT
            )
        """.trimIndent()
        db?.execSQL(createLocationsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        onCreate(db)
    }
//--------------------------------------------------------------Users------------------------------------------------------------------------------------
    // Insert a new user
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

    // Read user by phone
    fun getUser(phone: String): Cursor {
        val db = readableDatabase
        return db.query(TABLE_USER, null, "$COL_USER_PHONE=?", arrayOf(phone), null, null, null)
    }

    // Update user
    fun updateUser(firstname: String, lastname: String, email: String, password: String, phone: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_FIRSTNAME, firstname)
            put(COL_USER_LASTNAME, lastname)
            put(COL_USER_EMAIL, email)
            put(COL_USER_PASSWORD, password)
        }
        return db.update(TABLE_USER, values, "$COL_USER_PHONE=?", arrayOf(phone))
    }

    // Delete user
    fun deleteUser(phone: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_USER, "$COL_USER_PHONE=?", arrayOf(phone))
    }

//--------------------------------------------------------------Emergency Contacts--------------------------------------------------------------------------------
    // Insert an emergency contact
    fun addContact(firstname: String, lastname: String, phone: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTACT_FIRSTNAME, firstname)
            put(COL_CONTACT_LASTNAME, lastname)
            put(COL_CONTACT_PHONE, phone)
        }
        return db.insert(TABLE_CONTACTS, null, values)
    }

    // Read contact by ID
    fun getContact(id: Int): Cursor {
        val db = readableDatabase
        return db.query(TABLE_CONTACTS, null, "$COL_CONTACT_ID=?", arrayOf(id.toString()), null, null, null)
    }

    // Update contact
    fun updateContact(id: Int, firstname: String, lastname: String, phone: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTACT_FIRSTNAME, firstname)
            put(COL_CONTACT_LASTNAME, lastname)
            put(COL_CONTACT_PHONE, phone)
        }
        return db.update(TABLE_CONTACTS, values, "$COL_CONTACT_ID=?", arrayOf(id.toString()))
    }

    // Delete contact
    fun deleteContact(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_CONTACTS, "$COL_CONTACT_ID=?", arrayOf(id.toString()))
    }

//-------------------------------------------------------------------------Safe Locations---------------------------------------------------------------------------
    // Insert a safe location
    fun addLocation(name: String, address: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_NAME, name)
            put(COL_LOCATION_ADDRESS, address)
        }
        return db.insert(TABLE_LOCATIONS, null, values)
    }

    // Read location by ID
    fun getLocation(id: Int): Cursor {
        val db = readableDatabase
        return db.query(TABLE_LOCATIONS, null, "$COL_LOCATION_ID=?", arrayOf(id.toString()), null, null, null)
    }

    // Update location
    fun updateLocation(id: Int, name: String, address: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_NAME, name)
            put(COL_LOCATION_ADDRESS, address)
        }
        return db.update(TABLE_LOCATIONS, values, "$COL_LOCATION_ID=?", arrayOf(id.toString()))
    }

    // Delete location
    fun deleteLocation(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_LOCATIONS, "$COL_LOCATION_ID=?", arrayOf(id.toString()))
    }

}
