package com.arcnova.facebookfeed_ta

import com.arcnova.facebookfeed_ta.pages.appears.Comment
import com.arcnova.facebookfeed_ta.pages.appears.Like

data class Feed(val name: String, var timestamp: String, val konten: String, val photoprofile: String,
                var listComment: ArrayList<Comment>, var listLike: ArrayList<Like>, val email: String, val docID:String
                , var fid: String
     )