package com.arcnova.facebookfeed_ta.accounts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.arcnova.facebookfeed_ta.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_register.*
import java.io.File


class RegisterActivity : AppCompatActivity() {

    private var IMAGE_PICK_CODE = 1000

    private var PERMISSION_CODE = 1001

    private var storageRef: StorageReference? = null

    private var firestore = FirebaseFirestore.getInstance()

    private var imguri : Uri? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        storageRef = FirebaseStorage.getInstance().reference

        var btnPick = findViewById<ImageView>(R.id.img_account)

        btnPick.setOnClickListener{
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions,PERMISSION_CODE)
            }
            else{
                pickImage()
            }
        }

        var etName = findViewById<EditText>(R.id.name_account)
        var etEmail = findViewById<EditText>(R.id.email_account)
        var etPassword = findViewById<TextInputEditText>(R.id.password_account)

        submit.setOnClickListener{
            println("Nama : ${etName.text.toString()}, Email : ${etEmail.text.toString()}, Password : ${etPassword.text.toString()}")
            if(etName.text.toString() == "" || etEmail.text.toString() == "" || etPassword.text.toString() == "" || imguri == null){
                Toast.makeText(this, "Harus terisi semua beserta foto ${imguri}",Toast.LENGTH_SHORT).show()
            }else{

                uploadFile(etName.text.toString(),etEmail.text.toString(),etPassword.text.toString())
                Toast.makeText(this, "Berhasil",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                this.startActivity(intent)
            }

        }


        val btnBack : Button = findViewById<Button>(R.id.signin)

        btnBack.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
        }


    }

    private fun pickImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImage()
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imguri = data?.data
            img_account.setImageURI(imguri)
        }
    }

    fun uploadFile(name: String,email: String, password: String){
        val fileRef = storageRef?.child("profiles/${name}_${imguri?.lastPathSegment}")
        val uploadTask = fileRef?.putFile(imguri!!)
        uploadTask?.continueWithTask{it ->
            if(!it.isSuccessful){
                Toast.makeText(this, "Failed",Toast.LENGTH_SHORT).show()
            }
            fileRef!!.downloadUrl
        }?.addOnCompleteListener{ it ->
            print("Sukses? : ${it.isSuccessful}")
            if(it.isSuccessful){
                val downloadUri = it.result
                val url = downloadUri!!.toString()
                var u = Users(name,url,email,password)
                firestore.collection("users").add(u)
                Log.d("Direct Link: ",url)
            }

        }
    }

}