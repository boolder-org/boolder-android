package com.boolder.boolder.view.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R.layout
import com.boolder.boolder.databinding.ActivitySearchBinding
import com.boolder.boolder.utils.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    private val suggestions = listOf("Isatis", "La Marie-Rose", "Cul de Chien")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_search)


        // TODO Make it focus when activity start
        // Below code not working
        lifecycleScope.launch {
            delay(100)
            binding.searchComponent.searchBar.isFocusableInTouchMode = true
            binding.searchComponent.searchBar.requestFocus()
        }

        binding.searchComponent.searchBar.addTextChangedListener { query ->

        }

        applySuggestions()

    }

    private fun applySuggestions() {
        binding.suggestionFirst.text = suggestions[0]
        binding.suggestionSecond.text = suggestions[1]
        binding.suggestionThird.text = suggestions[2]

        binding.suggestionFirst.setOnClickListener { onQuerySelected() }
        binding.suggestionSecond.setOnClickListener { onQuerySelected() }
        binding.suggestionThird.setOnClickListener { onQuerySelected() }
    }

    private fun onQuerySelected() {

    }
}