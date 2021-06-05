package com.arcnova.facebookfeed_ta.pages.appears

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcnova.facebookfeed_ta.R
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_comments.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList


class CommentsActivity : AppCompatActivity() {
    var firestore = FirebaseFirestore.getInstance()
    var name: String? = null
    var docID: String? = null
    var fid: String? = null
    var docKey: String? = null


    private var IMAGE_PICK_CODE = 1000

    private var PERMISSION_CODE = 1001

    private var storageRef: StorageReference? = null

    private var imguri : Uri? =null




    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        name = intent.getStringExtra("name")
        docID = intent.getStringExtra("docID")
        fid = intent.getStringExtra("fid")
        docKey = intent.getStringExtra("docKey")

        setSupportActionBar(findViewById(R.id.comment_actionbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val listComment = ArrayList<Comment>()

        val rvComment= findViewById<RecyclerView>(R.id.rvComment)
        rvComment.layoutManager = LinearLayoutManager(this)
        val adapter = listComment?.let { docKey?.let { it1 -> CommentAdapter(it, it1) } }
        rvComment.adapter = adapter
        firestore.collection("feeds").whereEqualTo("fid", fid.toString()).addSnapshotListener{
                value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            for (dc : DocumentChange in value!!.documentChanges){
                when(dc.type){
                    DocumentChange.Type.ADDED -> {
                        val groups: List<Map<String, Object>> = dc.document.get("listComment") as List<Map<String, Object>>
                        for(group: Map<String, Object> in groups){

                            listComment.add(Comment(group["name"].toString(),group["content"].toString(),group["time"].toString(),group["img"].toString(),group["url"].toString()))
                            rvComment.adapter?.notifyDataSetChanged()
                        }

                        val groups2 : List<Map<String, Object>> = dc.document.get("listLike") as List<Map<String,Object>>
                        if(groups2.size == 0){
                            findViewById<TextView>(R.id.keterangan_like).text = "Jadilah orang pertama yang menyukai ini"
                            icon_emote.visibility = View.GONE
                        }else if(groups2.size > 0){
                            if(groups2.size == 1){
                                keterangan_like.text = "${groups2[0]["name"].toString()}"
                                icon_emote.visibility = View.VISIBLE
                                icon_emote.setImageResource(R.mipmap.ic_like_feed)
                            }else if (groups2.size == 2){
                                keterangan_like.text = "${groups2[0]["name"].toString()} dan ${groups2[1]["name"].toString()}"
                                icon_emote.visibility = View.VISIBLE
                                icon_emote.setImageResource(R.mipmap.ic_like_feed)
                            }else{
                                keterangan_like.text = "${groups2[0]["name"].toString()} dan ${(groups2.size-1)} lainnya"
                                icon_emote.visibility = View.VISIBLE
                                icon_emote.setImageResource(R.mipmap.ic_like_feed)
                            }
                        }

                    }
                    DocumentChange.Type.MODIFIED -> {
                        val groups: List<Map<String, Object>> = dc.document.get("listComment") as List<Map<String, Object>>
                        var i = groups.size -1
                        if( listComment.size -1< groups.size-1){
                            listComment.add(Comment(groups[i]["name"].toString(),groups[i]["content"].toString(),groups[i]["time"].toString(),groups[i]["img"].toString(),groups[i]["url"].toString()))
                            rvComment.adapter?.notifyDataSetChanged()
                        }else{
                            listComment.clear()
                            for(group: Map<String, Object> in groups){

                                listComment.add(Comment(group["name"].toString(),group["content"].toString(),group["time"].toString(),group["img"].toString(),group["url"].toString()))
                                rvComment.adapter?.notifyDataSetChanged()
                            }
                        }

                        val groups2 : List<Map<String, Object>> = dc.document.get("listLike") as List<Map<String,Object>>
                        if(groups2.size == 0){
                            findViewById<TextView>(R.id.keterangan_like).text = "Jadilah orang pertama yang menyukai ini"
                            icon_emote.visibility = View.GONE
                        }else if(groups2.size > 0){
                            if(groups2.size == 1){
                                keterangan_like.text = "${groups2[0]["name"].toString()}"
                                icon_emote.visibility = View.VISIBLE
                                icon_emote.setImageResource(R.mipmap.ic_like_feed)
                            }else if (groups2.size == 2){
                                keterangan_like.text = "${groups2[0]["name"].toString()} dan ${groups2[1]["name"].toString()}"
                                icon_emote.visibility = View.VISIBLE
                                icon_emote.setImageResource(R.mipmap.ic_like_feed)
                            }else{
                                keterangan_like.text = "${groups2[0]["name"].toString()} dan ${(groups2.size-1)} lainnya"
                                icon_emote.visibility = View.VISIBLE
                                icon_emote.setImageResource(R.mipmap.ic_like_feed)
                            }
                        }
                    }
                    DocumentChange.Type.REMOVED -> {

                    }
                }
            }
        }


        btnPhoto.setOnClickListener{
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions,PERMISSION_CODE)
            }
            else{
                pickImage()
            }
        }
        closePhoto.setOnClickListener{
            imguri = null
            framePhoto.visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.submit).setOnClickListener{
            if(findViewById<EditText>(R.id.create_comment).text.toString().equals("")){
                Toast.makeText(this,"Upload ${imguri}",Toast.LENGTH_SHORT).show()

            }else{
                firestore.collection("users").document(docID.toString()).get().addOnSuccessListener {
                    document ->
                    val currentDateTime: String
                    val sdf2 = LocalDateTime.now()
                    currentDateTime = sdf2.toString()

                    var konten:String = findViewById<EditText>(R.id.create_comment).text.toString()
                    if(imguri == null){
                        firestore.collection("feeds").document(docKey.toString()).update("listComment",FieldValue.arrayUnion(Comment(name.toString(),konten,currentDateTime,
                            document.data!!["profile"].toString(),"")))
                        rvComment.adapter?.notifyDataSetChanged()
                        rvComment.smoothScrollToPosition(listComment.size)
                    }else{
                        uploadFile(name.toString(),konten,currentDateTime,document.data!!["profile"].toString())
//                        listComment.add(Comment(name.toString(),konten,currentDateTime,
//                            document.data!!["profile"].toString(),imguri.toString()))
                        rvComment.adapter?.notifyDataSetChanged()
                        rvComment.smoothScrollToPosition(listComment.size)
                    }

                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        firestore.collection("feeds").whereArrayContains("listLike",Like(name.toString(),docID.toString())).
        whereEqualTo("fid", fid.toString()).get().addOnSuccessListener {
                querySnapshot ->

            if(querySnapshot.size() != 0 ){
                menuInflater.inflate(R.menu.commentbar_menu,menu)
                menu?.getItem(0)?.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_filled_thumbup))
            }else{
                menuInflater.inflate(R.menu.commentbar_menu,menu)
                menu?.getItem(0)?.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_thumbup))
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
            R.id.btnLike -> {
                firestore.collection("feeds").whereArrayContains("listLike", Like(name.toString(),docID.toString())).whereEqualTo("fid",fid).get().addOnSuccessListener {
                        querySnapshot ->
                    if(querySnapshot.size() == 0){
                        item.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_filled_thumbup))
                        firestore.collection("feeds").document(docKey.toString()).update("listLike",FieldValue.arrayUnion(Like(name.toString(),docID.toString())))

                    }else{
                        item.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_thumbup))
                        firestore.collection("feeds").document(docKey.toString()).update("listLike",FieldValue.arrayRemove(Like(name.toString(),docID.toString())))

                    }

                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    override fun onBackPressed() {
        finish()
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
            photo.setImageURI(imguri)
            framePhoto.visibility = View.VISIBLE
        }
    }

    fun uploadFile(name: String, konten: String,currentDateTime: String, profile: String){
        val ref = FirebaseStorage.getInstance().getReference("comments/${name}_${imguri?.lastPathSegment}")
        ref.putFile(imguri!!).continueWithTask{it ->
            if(!it.isSuccessful){
                Toast.makeText(this, "Failed",Toast.LENGTH_SHORT).show()
            }
            ref!!.downloadUrl
        }?.addOnCompleteListener{ it ->
            print("Sukses? : ${it.isSuccessful}")
            if(it.isSuccessful){
                val downloadUri = it.result
                val url = downloadUri!!.toString()
                firestore.collection("feeds").document(docKey.toString()).update("listComment",FieldValue.arrayUnion(Comment(name,konten,currentDateTime,
                    profile,url)))

                Log.d("Direct Link: ",url)
            }

        }
    }

}
