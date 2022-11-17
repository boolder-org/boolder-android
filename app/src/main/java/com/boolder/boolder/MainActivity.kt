package com.boolder.boolder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.view.map.MapFragment

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(binding.hostFragment.id, MapFragment())
            .commit()

    }
}