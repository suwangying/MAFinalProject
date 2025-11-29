package com.example.ma_final_project

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ma_final_project.utils.ValidationUtils

class ContactsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvContacts: RecyclerView
    private lateinit var btnAddContact: Button
    private lateinit var adapter: ContactAdapter

    private lateinit var backButton: ImageView
    private var currentPhone: String? = null
    private val contactList = mutableListOf<Contact>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contacts)

        dbHelper = DatabaseHelper(this)
        rvContacts = findViewById(R.id.rvContacts)
        btnAddContact = findViewById(R.id.btnAddContact)
        backButton=findViewById(R.id.backButton)

        currentPhone = getSharedPreferences("UserSession", MODE_PRIVATE)
            .getString("USER_PHONE", null)

        rvContacts.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter(contactList)
        rvContacts.adapter = adapter

        loadContacts()

        btnAddContact.setOnClickListener {
            showContactDialog(null)
        }
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadContacts() {
        contactList.clear()
        val cursor = dbHelper.getContactsForUser(currentPhone!!)
        while (cursor.moveToNext()) {
            val contact = Contact(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_ID)),
                firstname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_FIRSTNAME)),
                lastname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_LASTNAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_PHONE))
            )
            contactList.add(contact)
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showContactDialog(existing: Contact?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact, null)
        val etFirst = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etLast = dialogView.findViewById<EditText>(R.id.etLastName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)

        if (existing != null) {
            etFirst.setText(existing.firstname)
            etLast.setText(existing.lastname)
            etPhone.setText(existing.phone)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing != null) "Edit Contact" else "Add Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val first = etFirst.text.toString().trim()
                val last = etLast.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (first.isEmpty() || last.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Validation
                if (!ValidationUtils.isValidName("$first $last")) {
                    Toast.makeText(this, "Invalid name format", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!ValidationUtils.isValidPhone(phone)) {
                    Toast.makeText(this, "Phone must be 10 digits", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }


                // Check for duplicate phone (only when adding new contact or changing phone)
                val cursor = dbHelper.getContactsForUser(currentPhone!!)
                var phoneExists = false
                while (cursor.moveToNext()) {
                    val existingPhone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_PHONE))
                    val existingId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_ID))
                    // if editing, ignore current contact id
                    if (existingPhone == phone && (existing == null || existing.id != existingId)) {
                        phoneExists = true
                        break
                    }
                }
                cursor.close()
                if (phoneExists) {
                    Toast.makeText(this, "A contact with this phone already exists", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (existing != null) {
                    dbHelper.updateContact(existing.id, first, last, phone)
                    Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show()
                } else {
                    dbHelper.addContact(currentPhone!!, first, last, phone)
                    Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show()
                }
                loadContacts()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class ContactAdapter(private val contacts: List<Contact>) :
        RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

        inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvContactName)
            val tvPhone: TextView = view.findViewById(R.id.tvContactPhone)
            val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
            val btnCall: ImageButton = view.findViewById(R.id.btnCall)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_row, parent, false)
            return ContactViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
            val contact = contacts[position]
            holder.tvName.text = "${contact.firstname} ${contact.lastname}"
            holder.tvPhone.text = contact.phone

            holder.btnEdit.setOnClickListener {
                showContactDialog(contact)
            }

            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(this@ContactsActivity)
                    .setTitle("Delete Contact")
                    .setMessage("Are you sure you want to delete this contact?")
                    .setPositiveButton("Delete") { _, _ ->
                        dbHelper.deleteContact(contact.id)
                        Toast.makeText(this@ContactsActivity, "Contact deleted", Toast.LENGTH_SHORT).show()
                        loadContacts()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            holder.btnCall.setOnClickListener {
                // Show confirmation dialog before calling
                AlertDialog.Builder(this@ContactsActivity)
                    .setTitle("Call Contact")
                    .setMessage("Do you want to call ${contact.firstname} ${contact.lastname}?")
                    .setPositiveButton("Call") { _, _ ->
                        // Use ACTION_DIAL so user can see the number
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                        intent.data = android.net.Uri.parse("tel:${contact.phone}")
                        holder.itemView.context.startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        override fun getItemCount(): Int = contacts.size
    }

    data class Contact(
        val id: Int,
        val firstname: String,
        val lastname: String,
        val phone: String
    )
}
