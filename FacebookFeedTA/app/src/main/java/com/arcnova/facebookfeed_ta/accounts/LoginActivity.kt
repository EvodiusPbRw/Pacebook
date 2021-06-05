package com.arcnova.facebookfeed_ta.accounts

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.arcnova.facebookfeed_ta.MainActivity
import com.arcnova.facebookfeed_ta.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var firestore = FirebaseFirestore.getInstance()

        var etEmail = findViewById<TextInputEditText>(R.id.email_account)
        var etPassword = findViewById<EditText>(R.id.password_account)

        submit.setOnClickListener{
            if(etEmail.text.toString() == "" || etPassword.text.toString() == ""){
                Toast.makeText(this,"Email atau Password tidak boleh kosong!",Toast.LENGTH_SHORT).show()
            }else{
                firestore.collection("users").whereEqualTo("email",etEmail.text.toString()).get().addOnSuccessListener {
                    documents ->
                    for(document in documents){
                        if(document.get("email").toString().equals(etEmail.text.toString()) && document.get("password").toString().equals(etPassword.text.toString())){
                            var intent = Intent(this,MainActivity::class.java)
                            intent.putExtra("email",etEmail.text.toString())
                            intent.putExtra("name",document.data["nama"].toString())
                            intent.putExtra("url",document.data["profile"].toString())
                            intent.putExtra("docID",document.id)
                            this.startActivity(intent)
                        }else{
                            Toast.makeText(this,"Email atau Password salah!",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }


        var btnSignUp: TextView = findViewById<TextView>(R.id.signup)

        btnSignUp.setOnClickListener{
            var intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }

    }
}