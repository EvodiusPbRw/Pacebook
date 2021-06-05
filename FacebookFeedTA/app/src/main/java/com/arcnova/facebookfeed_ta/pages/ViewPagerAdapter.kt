package com.arcnova.facebookfeed_ta.pages.screens

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter (fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager){

    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> Homepage()
            1 -> Videopage()
            2 -> Marketpage()
            3 -> Notificationpage()
            4 -> etcpage()
            else -> Homepage()
        }
    }

    override fun getCount() = 5
}