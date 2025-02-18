package com.example.tez

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(
            navController = navController,
            configuration = appBarConfiguration
        )*/

        val homeFragment = HomeFragment()
        val scanFragment = ScanFragment()
        val profileFragment = ProfileFragment()

       val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        replaceFragment(homeFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homeFragment -> replaceFragment(homeFragment)
                R.id.scanFragment -> replaceFragment(scanFragment)
                R.id.profileFragment -> replaceFragment(profileFragment)
            }
            true
        }

     //bottomNavigationView.setupWithNavController(navController)
     //   val navController = findNavController(R.id.fragmentContainerView)
     //   bottomNavigation.setupWithNavController(navController)
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }*/

    private fun replaceFragment(fragment: Fragment){
        if(fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, fragment)
            transaction.commit()
        }
    }

}