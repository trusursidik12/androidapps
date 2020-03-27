package com.ispumap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ispumap.ViewPagerAdapter.CitiesFragmentPagerAdapter
import com.ispumap.fragments.MyFrament
import kotlinx.android.synthetic.main.activity_main.*


class CitiesActivity : AppCompatActivity() {
    private lateinit var viewpager: ViewPager
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                val i = Intent(this@CitiesActivity, MainActivity::class.java)
                startActivity(i)
            }
            R.id.nav_city_detail -> {
                recreate()
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cities)
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottom_navigation.getMenu().findItem(R.id.nav_city_detail).setChecked(true)
        initViews()
        setupViewPager()
    }

    private fun initViews() {
        viewpager = findViewById(R.id.viewpager)
    }

    private fun setupViewPager() {
        val adapter = CitiesFragmentPagerAdapter(getSupportFragmentManager())
        var firstFragmet: MyFrament = MyFrament.newInstance("First Fragment")
        var secondFragmet: MyFrament = MyFrament.newInstance("Second Fragment")
        var thirdFragmet: MyFrament = MyFrament.newInstance("Third Fragment")

        adapter.addFragment(firstFragmet, "ONE")
        adapter.addFragment(secondFragmet, "TWO")
        adapter.addFragment(thirdFragmet, "THREE")

        viewpager!!.adapter = adapter
    }
}