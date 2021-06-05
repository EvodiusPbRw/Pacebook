package com.arcnova.facebookfeed_ta

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.arcnova.facebookfeed_ta.pages.appears.Comment
import com.arcnova.facebookfeed_ta.pages.appears.CommentsActivity
import com.arcnova.facebookfeed_ta.pages.appears.Like
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_feed.view.*
import kotlinx.android.synthetic.main.item_status.view.*
import java.time.LocalDateTime

class FeedAdapter(): RecyclerView.Adapter<FeedAdapter.BaseViewHolder<*>>() {
    var firestore = FirebaseFirestore.getInstance()
    var feedList: ArrayList<Feed>? = null
    val STATUS_TYPE = 1
    var like: Like? =null
    var name: String? = null
    var docID : String? =null


    constructor(feedList: ArrayList<Feed>) : this() {
        this.feedList = feedList
    }


    inner class FeedHolder(val v: View): BaseViewHolder<Feed>(v){
        var docKey: String? = null
        var listLikeMap: List<Map<String, Any>>? = null
        @SuppressLint("ResourceAsColor", "SetTextI18n")
        override fun bind(feed: Feed){
            if(feed.listComment.size == 0 && feed.listLike.size == 0){
                v.findViewById<LinearLayout>(R.id.summary_review).setVisibility(View.GONE)
                v.findViewById<View>(R.id.divider_summary).setVisibility(View.GONE)
            }else{
                v.findViewById<LinearLayout>(R.id.summary_review).setVisibility(View.VISIBLE)
                v.findViewById<View>(R.id.divider_summary).setVisibility(View.VISIBLE)
            }


            if(feed.listComment.size == 0){
                v.findViewById<TextView>(R.id.jumlah_comment).setVisibility(View.GONE)

            }else{

                v.findViewById<TextView>(R.id.jumlah_comment).setVisibility(View.VISIBLE)
                v.findViewById<TextView>(R.id.jumlah_comment).text = "${feed.listComment.size} Komentar"
            }

            firestore.collection("feeds").whereEqualTo("fid",feed.fid).get().addOnSuccessListener {
                querySnapshot ->
                docKey = querySnapshot.documents[0].id
                val groups: List<Map<String, Any>> = querySnapshot.documents[0].data?.get("listLike") as List<Map<String, Any>>
                listLikeMap = groups
                if(groups.isEmpty()){
                    v.findViewById<ImageView>(R.id.icon_emote).setVisibility(View.GONE)
                    v.findViewById<TextView>(R.id.jumlah_like).setVisibility(View.GONE)
                }else{
                    if(groups.size == 1){
                        v.findViewById<ImageView>(R.id.icon_emote).setVisibility(View.VISIBLE)
                        v.findViewById<TextView>(R.id.jumlah_like).setVisibility(View.VISIBLE)
                        v.findViewById<ImageView>(R.id.icon_emote).setImageResource(R.mipmap.ic_like_feed)
                        v.findViewById<TextView>(R.id.jumlah_like).text = groups[0]["name"].toString()
                    }
                    else{
                        if(groups.size == 2){
                            v.findViewById<ImageView>(R.id.icon_emote).setVisibility(View.VISIBLE)
                            v.findViewById<TextView>(R.id.jumlah_like).setVisibility(View.VISIBLE)
                            v.findViewById<ImageView>(R.id.icon_emote).setImageResource(R.mipmap.ic_like_feed)
                            v.findViewById<TextView>(R.id.jumlah_like).text = "${groups[0]["name"].toString()} dan ${groups[1]["name"].toString()}"
                        }else{
                            v.findViewById<ImageView>(R.id.icon_emote).setVisibility(View.VISIBLE)
                            v.findViewById<TextView>(R.id.jumlah_like).setVisibility(View.VISIBLE)
                            v.findViewById<ImageView>(R.id.icon_emote).setImageResource(R.mipmap.ic_like_feed)
                            v.findViewById<TextView>(R.id.jumlah_like).text = "${groups[0]["name"].toString()} dan ${feed.listLike.size-1} lainnya"
                        }

                    }

                }

            }



            firestore.collection("feeds").whereArrayContains("listLike", Like(name.toString(),docID.toString())).whereEqualTo("fid",feed.fid).get().addOnSuccessListener {
                    querySnapshot ->
                if(querySnapshot.size() == 0){
                    v.findViewById<TextView>(R.id.btnLike_text).setTextColor(Color.BLACK)
                    v.findViewById<ImageView>(R.id.btnLike_img).setImageResource(R.drawable.ic_thumbup)
                }else{
                    v.findViewById<TextView>(R.id.btnLike_text).setTextColor(R.color.menuClicked)
                    v.findViewById<ImageView>(R.id.btnLike_img).setImageResource(R.drawable.ic_filled_thumbup)
                }
            }

            v.findViewById<TextView>(R.id.name_profile).text = feed.name
            v.findViewById<TextView>(R.id.timestamp).text = feed.timestamp
            v.findViewById<TextView>(R.id.content_profile).text = feed.konten
            Glide.with(v.context).load(feed.photoprofile).into(v.findViewById(R.id.img_profile))
            v.findViewById<TextView>(R.id.jumlah_share).setVisibility(View.GONE)


            v.findViewById<LinearLayout>(R.id.btnLike).setOnClickListener{
                firestore.collection("feeds").whereArrayContains("listLike", Like(name.toString(),docID.toString())).whereEqualTo("fid",feed.fid).get().addOnSuccessListener {
                    querySnapshot ->
                    if(querySnapshot.size() == 0){
                        firestore.collection("feeds").document(docKey!!).update("listLike",FieldValue.arrayUnion(Like(name.toString(),docID.toString())))
                        notifyDataSetChanged()
                    }else{
                        firestore.collection("feeds").document(docKey!!).update("listLike",FieldValue.arrayRemove(Like(name.toString(),docID.toString())))
                        notifyDataSetChanged()
                    }
                }
                firestore.collection("feeds").get().addOnSuccessListener {
                        documents ->
                    for(document in documents){
                        if(document.data["fid"] == feed.fid){
                            docKey = document.id
                        }
                    }
                }


            }
            if(feed.docID == docID){
                v.others.setOnClickListener{
                    val popup = PopupMenu(v.context, v.findViewById(R.id.others))
                    popup.inflate(R.menu.feedbar_menu)
                    popup.setOnMenuItemClickListener {
                            item ->
                        when(item.itemId){
                            R.id.delete -> {
                                Toast.makeText(v.context, "Feed ID: ${feed.fid} sudah dihapus",Toast.LENGTH_SHORT).show()
                                firestore.collection("feeds").document(docKey!!).delete()
                            }

                        }
                        true
                    }
                    popup.show()
                }

            }


            v.findViewById<LinearLayout>(R.id.btnComment).setOnClickListener{
                val intent = Intent(v.context,CommentsActivity::class.java)
                intent.putExtra("name",name.toString())
                intent.putExtra("docID",docID.toString())
                intent.putExtra("fid",feed.fid)
                intent.putExtra("docKey",docKey.toString())
                v.context.startActivity(intent)
            }
        }
    }

    inner class StatusHolder(val v: View): BaseViewHolder<Feed>(v){
        override fun bind(item: Feed) {
            name = item.name
            docID = item.docID


            var konten: String =""
            val randId = (0 until 10000).random()
            val imgV= v.findViewById<ImageView>(R.id.img_profile)
            Glide.with(v.context).load(item.photoprofile).into(imgV)
            v.findViewById<Button>(R.id.submit).setOnClickListener{
                if(v.findViewById<EditText>(R.id.create_status).text.toString().equals("")){

                }else{
                    konten = v.findViewById<EditText>(R.id.create_status).text.toString()
                    var feed = Feed(item.name, LocalDateTime.now().toString(),konten,item.photoprofile,ArrayList<Comment>(),ArrayList<Like>(),item.email,item.docID,"88${randId}")
                    firestore.collection("feeds").add(feed)
                    
                }

                notifyDataSetChanged()
            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        if(feedList?.get(position)?.konten.equals("")){
            return 1
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>{
        return when(viewType){
            STATUS_TYPE -> {
                var v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_status,parent,false)
                StatusHolder(v)
            }
            else -> {
                var v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_feed,parent,false)
                FeedHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder){
            is StatusHolder -> {
                feedList?.get(position)?.let { holder.bind(it) }
            }
            is FeedHolder -> {
                feedList?.get(position)?.let { holder.bind(it) }
            }
        }
        
    }

    override fun getItemCount(): Int {
        return feedList?.size!!
    }

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

}