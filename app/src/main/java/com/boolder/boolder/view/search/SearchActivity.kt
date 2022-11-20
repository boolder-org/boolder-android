package com.boolder.boolder.view.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivitySearchBinding
import com.boolder.boolder.utils.NetworkObserver
import com.boolder.boolder.utils.NetworkObserverImpl
import com.boolder.boolder.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SearchActivity : AppCompatActivity(), NetworkObserver {

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    private val networkObserverImpl: NetworkObserverImpl by inject()

    private val suggestions = listOf("Isatis", "La Marie-Rose", "Cul de Chien")

    private val isQueryEmpty: Boolean
        get() = binding.searchComponent.searchBar.text?.isBlank() == true

    private val isQueryProduceResult: Boolean
        get() = true // TODO impl + rename

    private var isConnectedToNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        networkObserverImpl.subscribeOn(this, this)

        // TODO Make it focus when activity start
        // Below code not working
        lifecycleScope.launch {
            delay(100)
            binding.searchComponent.searchBar.isFocusableInTouchMode = true
            binding.searchComponent.searchBar.requestFocus()
        }

        binding.searchComponent.searchFirstIcon.apply {
            val drawable = ContextCompat.getDrawable(
                this@SearchActivity,
                R.drawable.ic_arrow_back
            )
            setImageDrawable(drawable)
            setOnClickListener { finish() }
        }

        binding.searchComponent.searchBar.addTextChangedListener { query ->
            refreshSuggestionsVisibility(isQueryEmpty)
            refreshNoResultVisibility(isQueryProduceResult)

            //TODO do the search
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

    override fun onConnectivityChange(connected: Boolean) {
        isConnectedToNetwork = connected
        lifecycleScope.launch(Dispatchers.Main) {
            if (connected) {
                binding.connectivityErrorMessage.visibility = View.GONE
                refreshSuggestionsVisibility(isQueryEmpty)
                refreshNoResultVisibility(!isQueryProduceResult)
            } else {
                binding.connectivityErrorMessage.visibility = View.VISIBLE
                refreshSuggestionsVisibility(false)
                refreshNoResultVisibility(false)
            }
        }
    }

    private fun refreshSuggestionsVisibility(show: Boolean) {
        binding.suggestionContainer.visibility = if (show && isConnectedToNetwork) View.VISIBLE else View.GONE
    }

    private fun refreshNoResultVisibility(show: Boolean) {
        binding.emptyQueryMessage.visibility = if (show && isConnectedToNetwork) View.VISIBLE else View.GONE
    }
}