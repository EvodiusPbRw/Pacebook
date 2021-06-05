package com.arcnova.facebookfeed_ta.pages.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcnova.facebookfeed_ta.Feed
import com.arcnova.facebookfeed_ta.FeedAdapter
import com.arcnova.facebookfeed_ta.MainActivity
import com.arcnova.facebookfeed_ta.R
import com.arcnova.facebookfeed_ta.pages.appears.Comment
import com.arcnova.facebookfeed_ta.pages.appears.Like
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.item_comment.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class Homepage : Fragment() {
    lateinit var ACTIVITY: MainActivity
    lateinit var rvFeeds:RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ACTIVITY = context as MainActivity
    }

    fun parseDateToString(str: String): String{
        var waktuFeed = ""
        val sdf2 = LocalDateTime.now()
        val dateFB = LocalDateTime.parse(str)
        if(dateFB.minute.toString() == sdf2.minute.toString()
            && dateFB.hour.toString() == sdf2.hour.toString() && dateFB.dayOfMonth.toString() == sdf2.dayOfMonth.toString() &&
            dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            if((sdf2.second - dateFB.second) < 1){
                waktuFeed = "Baru saja"
            }else{
                waktuFeed = (sdf2.second - dateFB.second).toString() + " Detik"
            }

        }else if(dateFB.hour.toString() == sdf2.hour.toString() && dateFB.dayOfMonth.toString() == sdf2.dayOfMonth.toString() &&
            dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            waktuFeed = (sdf2.minute - dateFB.minute).toString() + " Menit"
        }else if(dateFB.dayOfMonth.toString() == sdf2.dayOfMonth.toString() &&
            dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            waktuFeed = (sdf2.hour - dateFB.hour).toString() + " Jam"
        }else if(dateFB.month.toString() == sdf2.month.toString() && dateFB.year.toString() == sdf2.year.toString()){
            waktuFeed = (sdf2.dayOfMonth - dateFB.dayOfMonth).toString() + " Hari"
        }else if(dateFB.year.toString() == sdf2.year.toString()){
            waktuFeed = (sdf2.monthValue - dateFB.monthValue).toString() + " Bulan"
        }else {
            waktuFeed = (sdf2.hour - dateFB.hour).toString() + " Tahun"
        }
        return waktuFeed
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Toast.makeText(activity,"${ACTIVITY.email} & ${ACTIVITY.docId}",Toast.LENGTH_SHORT).show()
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_homepage, container, false)

        val listFeed = ArrayList<Feed>()
        listFeed.add(Feed(ACTIVITY.name.toString(),"", "",ACTIVITY.url.toString(),ArrayList<Comment>(),ArrayList<Like>(),ACTIVITY.email.toString(),ACTIVITY.docId.toString(),""))

        rvFeeds = view.findViewById(R.id.newsFeed)
        rvFeeds.layoutManager = LinearLayoutManager(view.getContext())

        val adapter = FeedAdapter(listFeed)
        rvFeeds.adapter = adapter

        ACTIVITY.firestore.collection("feeds").orderBy("timestamp",Query.Direction.ASCENDING).addSnapshotListener{
                value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                Log.w("failed", "Listen failed.", error);
            }
            for (dc: DocumentChange in value!!.getDocumentChanges()){
                when(dc.type){
                    DocumentChange.Type.ADDED -> {

                        listFeed.add(1,Feed(dc.document.data["name"].toString(),parseDateToString(dc.document["timestamp"].toString()),dc.document.data["konten"].toString(),dc.document.data["photoprofile"].toString(),
                            dc.document.data["listComment"] as ArrayList<Comment>,dc.document.data["listLike"] as ArrayList<Like>,
                            dc.document.data["email"].toString(),dc.document.data["docID"].toString(),dc.document.data["fid"].toString()))
                        rvFeeds.refreshDrawableState()
                        (rvFeeds.adapter as FeedAdapter).notifyDataSetChanged()
                        rvFeeds.adapter = FeedAdapter(listFeed)
                    }
                    DocumentChange.Type.MODIFIED ->{
                        for(i in 0..listFeed.size-1){
                            if(listFeed[i].fid == dc.document.data["fid"]){
                                listFeed[i].timestamp = parseDateToString(dc.document.data["timestamp"].toString())
                                listFeed[i].listLike = dc.document.data["listLike"] as ArrayList<Like>
                                listFeed[i].listComment = dc.document.data["listComment"] as ArrayList<Comment>
                                (rvFeeds.adapter as FeedAdapter).notifyItemChanged(i)
                            }
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        listFeed.clear()
                        listFeed.add(Feed(ACTIVITY.name.toString(),"", "",ACTIVITY.url.toString(),ArrayList<Comment>(),ArrayList<Like>(),ACTIVITY.email.toString(),ACTIVITY.docId.toString(),""))
                        for(document in value.documents){
                            listFeed.add(1,Feed(
                                document.data!!["name"].toString(),parseDateToString(document["timestamp"]!!.toString()),document.data!!["konten"].toString(),document.data!!["photoprofile"].toString(),
                                document.data!!["listComment"] as ArrayList<Comment>,document.data!!["listLike"] as ArrayList<Like>,
                                document.data!!["email"].toString(),document.data!!["docID"].toString(),document.data!!["fid"].toString()))

                        }
                        (rvFeeds.adapter as FeedAdapter).notifyDataSetChanged()
                        rvFeeds.adapter = FeedAdapter(listFeed)

                    }
                }
            }
        }

        return view
    }
}