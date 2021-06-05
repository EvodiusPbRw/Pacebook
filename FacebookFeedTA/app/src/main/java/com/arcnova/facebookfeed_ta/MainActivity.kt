package com.arcnova.facebookfeed_ta

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.arcnova.facebookfeed_ta.accounts.LoginActivity
import com.arcnova.facebookfeed_ta.pages.appears.Like
import com.arcnova.facebookfeed_ta.pages.screens.ViewPagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.item_comment.*
import kotlinx.android.synthetic.main.popup_window.*
import kotlinx.android.synthetic.main.popup_window.view.*


class MainActivity : AppCompatActivity() {
    val firestore = FirebaseFirestore.getInstance()
    var email: String? =null
    var url: String? = null
    var name: String? = null
    var docId: String? = null
    private var IMAGE_PICK_CODE = 1000

    private var PERMISSION_CODE = 1001

    private var storageRef: StorageReference? = null

    private var imguri : Uri? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = intent.getStringExtra("email")
        name = intent.getStringExtra("name")
        url = intent.getStringExtra("url")
        docId = intent.getStringExtra("docID")

        setSupportActionBar(findViewById(R.id.appbar_facebook))
        supportActionBar?.setDisplayShowTitleEnabled(false)



        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val pageAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = pageAdapter

        val nav: BottomNavigationView = findViewById(R.id.navbar)
        nav.setOnNavigationItemSelectedListener{item:MenuItem ->
            when(item.getItemId()){
                R.id.homepage -> {
                    viewPager.setCurrentItem(0)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.videopage -> {
                    viewPager.setCurrentItem(1)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.marketpage -> {
                    viewPager.setCurrentItem(2)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.notifpage -> {
                    viewPager.setCurrentItem(3)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.etcpage -> {
                    viewPager.setCurrentItem(4)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
            }

        viewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {

            override fun onPageSelected(position: Int) {
                nav!!.getMenu().getItem(position).setChecked(true)
            }

        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Glide.with(this).asBitmap().load(url).placeholder(R.mipmap.ic_profile_round).into(object :
            SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                //menu?.findItem(R.id.profile)?.icon = resource
                val widthLight: Int = resource.getWidth()
                val heightLight: Int = resource.getHeight()

                val output = Bitmap.createBitmap(
                    resource.getWidth(), resource.getHeight(),
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(output)
                val paintColor = Paint()
                paintColor.setFlags(Paint.ANTI_ALIAS_FLAG)

                val rectF = RectF(Rect(0, 0, widthLight, heightLight))

                canvas.drawRoundRect(rectF, (widthLight / 2).toFloat(),
                    (heightLight / 2).toFloat(), paintColor)

                val paintImage = Paint()
                paintImage.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP))
                canvas.drawBitmap(resource, 0f, 0f, paintImage)
                menu?.findItem(R.id.profile)?.icon = BitmapDrawable(resources,output)
            }

        })
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.search -> {
//            firestore.collection("feeds").document("Testing").set(Like("evo","test"))
//            Toast.makeText(this,"${url}", Toast.LENGTH_LONG).show()
            true
        }
        R.id.profile -> {
            //customView,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT

            val customView = layoutInflater.inflate(R.layout.popup_window,null)
            Glide.with(customView).load(url).into(customView.img_account)
            customView.name_account.text = this.name

            val popup = PopupWindow(customView,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true)
            popup.showAsDropDown(findViewById<androidx.appcompat.widget.Toolbar>(R.id.appbar_facebook),0,0,Gravity.RIGHT)
            //popup.showAtLocation(findViewById<androidx.appcompat.widget.Toolbar>(R.id.appbar_facebook),Gravity.RIGHT,0,0)
            customView.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                    //Close the window when clicked

                    popup.dismiss()
                    return true
                }
            })

            customView.logout.setOnClickListener{
                var intent = Intent(this,LoginActivity::class.java)
                this.startActivity(intent)
                this.onStop()
            }

            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

}