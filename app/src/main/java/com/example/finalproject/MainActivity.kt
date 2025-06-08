package com.example.finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var sId: String
    private lateinit var sName: String
    private lateinit var sPrice: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val id: EditText = findViewById(R.id.editId)
        val name: EditText = findViewById(R.id.editName)
        val price: EditText = findViewById(R.id.editPrice)

        val database = Firebase.database
        val productRef = database.getReference("Product")

        val save: Button = findViewById(R.id.btnSave)
        val update: Button = findViewById(R.id.btnUpdate)
        val delete: Button = findViewById(R.id.btnDelete)
        val search: Button = findViewById(R.id.btnSearch)
        val resultView: TextView = findViewById(R.id.resultView)


        save.setOnClickListener {
            sId = id.text.toString().trim()
            sName = name.text.toString().trim()
            sPrice = price.text.toString().trim()

            if (sId.isEmpty() || sName.isEmpty() || sPrice.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            productRef.child(sId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Product ID already exists
                        Toast.makeText(this@MainActivity, "Product ID already exists. Use update instead.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Product ID doesn't exist, so save
                        val product = Product(sId, sName, sPrice)
                        productRef.child(sId).setValue(product)
                            .addOnSuccessListener {
                                Toast.makeText(this@MainActivity, "Product saved successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MainActivity, "Failed to save product", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Database error", Toast.LENGTH_SHORT).show()
                }
            })
        }


        update.setOnClickListener {
            sId = id.text.toString().trim()
            sName = name.text.toString().trim()
            sPrice = price.text.toString().trim()

            val updatedProduct = Product(sId, sName, sPrice)

            productRef.child(sId).setValue(updatedProduct)
                .addOnSuccessListener {
                    Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
        }

        delete.setOnClickListener {
            sId = id.text.toString().trim()

            productRef.child(sId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                }
        }

        search.setOnClickListener {

            sId = id.text.toString().trim()

            productRef.child(sId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val product = snapshot.getValue(Product::class.java)
                        name.setText(product?.name)
                        price.setText(product?.price)


                        resultView.text = "Product found:\nName: ${product?.name}\nPrice: ₱${product?.price}"

                        Toast.makeText(this@MainActivity, "Product found", Toast.LENGTH_SHORT).show()
                    } else {
                        resultView.text = "Product not found"
                        Toast.makeText(this@MainActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Search failed", Toast.LENGTH_SHORT).show()
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
