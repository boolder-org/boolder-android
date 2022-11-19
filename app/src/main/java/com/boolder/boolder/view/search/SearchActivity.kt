package com.boolder.boolder.view.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boolder.boolder.R.layout
import com.boolder.boolder.databinding.ActivitySearchBinding
import com.boolder.boolder.utils.viewBinding

class SearchActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_search)


        binding.searchComponent.searchBar.isFocusableInTouchMode = true
        binding.searchComponent.searchBar.requestFocus()

    }

    override fun onResume() {
        super.onResume()

    }
}