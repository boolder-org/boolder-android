package com.boolder.boolder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boolder.boolder.databinding.FragmentSearchBinding

class SearchActivity : AppCompatActivity() {

    private val binding by viewBinding(FragmentSearchBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search)


        binding.searchComponent.searchBar.isFocusableInTouchMode = true
        binding.searchComponent.searchBar.requestFocus()

    }

    override fun onResume() {
        super.onResume()

    }
}