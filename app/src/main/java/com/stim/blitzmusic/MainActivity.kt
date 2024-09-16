package com.stim.blitzmusic

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import com.stim.blitzmusic.adapter.CategoryAdapter
import com.stim.blitzmusic.adapter.SectionSongListAdapter
import com.stim.blitzmusic.databinding.ActivityMainBinding
import com.stim.blitzmusic.modals.CategoryModel
import com.stim.blitzmusic.modals.SongModel
import java.util.Locale.Category

class MainActivity : AppCompatActivity() {

    private var mInterstitialAd: InterstitialAd? = null

    lateinit var binding: ActivityMainBinding
    lateinit var mAdView : AdView;
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAdView = findViewById(R.id.adView)


        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        getCategories()
        setupSection("section_1",binding.section1MainLayout,binding.section1Title,binding.section1RecyclerView)
        setupSection("section_2",binding.section2MainLayout,binding.section2Title,binding.section2RecyclerView)
        setupSection("section_3",binding.section3MainLayout,binding.section3Title,binding.section3RecyclerView)
        setupSection("section_4",binding.section4MainLayout,binding.section4Title,binding.section4RecyclerView)
        setupSection("section_5",binding.section5MainLayout,binding.section5Title,binding.section5RecyclerView)
        setupMostlyPlayed("mostly_played",binding.mostlyPlayedMainLayout,binding.mostlyPlayedTitle,binding.mostlyPlayedRecyclerView)


        //option_btn(menu icon)
        binding.optionBtn.setOnClickListener{
            showPopupMenu()
        }
    }

    fun showPopupMenu(){
        val popupMenu = PopupMenu( this,binding.optionBtn)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu,popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.logout ->{
                    logout()
                    true
                }
            }
            false
        }
    }


    fun logout(){
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }



    override fun onResume() {
        super.onResume()
        showPlayerView()
    }

    //Dynamic for now playing section
    fun showPlayerView(){
        binding.playerView.setOnClickListener{
            startActivity(Intent(this,PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now Playing : " + it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        } ?: run{
            binding.playerView.visibility = View.GONE
        }
    }


    //Categories
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    fun setupCategoryRecyclerView(categoryList : List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    //sections
    fun setupSection(id : String, mainLayout : RelativeLayout,titleView : TextView, recyclerView : RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener{
                val section = it.toObject(CategoryModel::class.java)
                section?.apply{
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener{
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity,SongsListActivity::class.java))
                    }
                }
            }



    }

    //Mostly_played_section
    fun setupMostlyPlayed(id : String, mainLayout : RelativeLayout,titleView : TextView, recyclerView : RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener{
                //get mostly played song
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count",Query.Direction.DESCENDING)
                    .limit(10)
                    .get().addOnSuccessListener {songListSnapshot->
                      val songsModelList = songListSnapshot.toObjects<SongModel>()
                        val songsIdList = songsModelList.map{
                            it.id
                        }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply{
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener{
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity,SongsListActivity::class.java))
                            }
                        }
                    }

            }



    }



}