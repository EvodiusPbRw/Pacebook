package com.arcnova.facebookfeed_ta.pages.appears

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.arcnova.facebookfeed_ta.R
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.lang.reflect.Field
import java.time.LocalDateTime

class CommentAdapter(val commentList: ArrayList<Comment>, val docKey: String): RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    inner class CommentHolder(val x: View):RecyclerView.ViewHolder(x){
        fun bindView(comment: Comment){
            x.findViewById<TextView>(R.id.name_profile).text = comment.name
            x.findViewById<TextView>(R.id.comment_content).text = comment.content
            x.findViewById<TextView>(R.id.timestamp).text = parseDateToString(comment.time.toString())
            Glide.with(x.context).load(comment.img).into(x.findViewById(R.id.img_profile))
            if(comment.url == ""){
                x.findViewById<CardView>(R.id.photoTagged).visibility = View.GONE
            }else{
                x.findViewById<CardView>(R.id.photoTagged).visibility = View.VISIBLE
                Glide.with(x.context).load(comment.url).into(x.findViewById(R.id.photoBound))
            }

            x.findViewById<TextView>(R.id.like).setOnClickListener{

            }
            x.findViewById<TextView>(R.id.reply).setOnClickListener{

            }
            x.findViewById<LinearLayout>(R.id.frameComment).setOnLongClickListener {
                Toast.makeText(x.context,"${comment.name}, ${comment.content}, ${comment.time}, ${comment.img}, ${comment.url}",Toast.LENGTH_SHORT).show()
                var comment1 = Comment(comment.name,comment.content,comment.time,comment.img,comment.url)
                val map: Map<String,Any> = hashMapOf("content" to comment.content.toString(), "img" to comment.img.toString(),"name" to comment.name.toString(),  "time" to comment.time.toString(),"url" to comment.url.toString())

                FirebaseFirestore.getInstance().collection("feeds").document(docKey).update("listComment",FieldValue.arrayRemove(map))
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        var x = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
        return CommentHolder(x)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentAdapter.CommentHolder, position: Int) {
        holder.bindView(commentList[position])
    }

    fun parseDateToString(str: String): String{
        var waktuComment = ""
        //val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val sdf2 = LocalDateTime.now()
        val dateFB = LocalDateTime.parse(str)
        if(dateFB.minute.toString() == sdf2.minute.toString()
            && dateFB.hour.toString() == sdf2.hour.toString() && dateFB.dayOfMonth.toString() == sdf2.dayOfMonth.toString() &&
            dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            if((sdf2.second - dateFB.second) < 1){
                waktuComment= "Baru saja"
            }else{
                waktuComment = (sdf2.second - dateFB.second).toString() + " Detik"
            }
        }else if(dateFB.hour.toString() == sdf2.hour.toString() && dateFB.dayOfMonth.toString() == sdf2.dayOfMonth.toString() &&
            dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            waktuComment = (sdf2.minute - dateFB.minute).toString() + " Menit"
        }else if(dateFB.dayOfMonth.toString() == sdf2.dayOfMonth.toString() &&
            dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            waktuComment = (sdf2.hour - dateFB.hour).toString() + " Jam"
        }else if(dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            waktuComment = (sdf2.dayOfMonth - dateFB.dayOfMonth).toString() + " Hari"
        }else if(dateFB.year.toString() == sdf2.year.toString()){
            waktuComment = (sdf2.monthValue - dateFB.monthValue).toString() + " Bulan"
        }else {
            waktuComment = (sdf2.hour - dateFB.hour).toString() + " Tahun"
        }
        return waktuComment
    }
}