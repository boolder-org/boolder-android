package com.boolder.boolder.view.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivitySearchBinding
import com.boolder.boolder.utils.NetworkObserver
import com.boolder.boolder.utils.NetworkObserverImpl
import com.boolder.boolder.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity(), NetworkObserver {

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    private val searchViewModel: SearchViewModel by viewModel()
    private val networkObserverImpl: NetworkObserverImpl by inject()

    private val algoliaAdapter = AlgoliaAdapter()

    private val suggestions = listOf("Isatis", "La Marie-Rose", "Cul de Chien")

    private val query
        get() = binding.searchComponent.searchBar.text
    private val isQueryEmpty
        get() = query == null || query.isEmpty() || query.isBlank()


    private var isConnectedToNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Listen to network change
        networkObserverImpl.subscribeOn(this, this)

        binding.searchComponent.searchBar.requestFocus()

        binding.searchComponent.searchFirstIcon.apply {
            val drawable = ContextCompat.getDrawable(
                this@SearchActivity,
                R.drawable.ic_arrow_back
            )
            setImageDrawable(drawable)
            setOnClickListener { finish() }
        }

        binding.searchComponent.searchLastIcon.setOnClickListener {
            binding.searchComponent.searchBar.text.clear()
            refreshSuggestionsVisibility(true)
            refreshNoResultVisibility(false)
            algoliaAdapter.setHits(emptyList())
        }

        binding.recyclerView.apply {
            adapter = algoliaAdapter
            addItemDecoration(DividerItemDecoration(this@SearchActivity, LinearLayoutManager.VERTICAL))
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        binding.searchComponent.searchBar.addTextChangedListener { query ->
            if (isQueryEmpty) {
                algoliaAdapter.setHits(emptyList())
                refreshSuggestionsVisibility(true)
            } else {
                searchViewModel.search(query.toString())
            }
        }

        applySuggestions()

        searchViewModel.searchResult.observe(this) {
            refreshNoResultVisibility(it.isEmpty())
            refreshSuggestionsVisibility(false)
            if (isQueryEmpty) {
                algoliaAdapter.setHits(emptyList())
            } else {
                algoliaAdapter.setHits(it)
            }
        }

        // TODO Understand with either flow or livedata aren't updated on query changes
        // Issue open on Github
        // https://github.com/algolia/instantsearch-android/issues/374
        //lifecycleScope.launch {
        //    searchViewModel.problems.collect {
        //        problemAdapter.submitData(it)
        //    }
        //    searchViewModel.areas.collect {
        //        areaAdapter.submitData(it)
        //    }
        //}
    }

    private fun applySuggestions() {
        binding.suggestionFirst.text = suggestions[0]
        binding.suggestionSecond.text = suggestions[1]
        binding.suggestionThird.text = suggestions[2]

        binding.suggestionFirst.setOnClickListener { onSuggestionClick(binding.suggestionFirst.text.toString()) }
        binding.suggestionSecond.setOnClickListener { onSuggestionClick(binding.suggestionFirst.text.toString()) }
        binding.suggestionThird.setOnClickListener { onSuggestionClick(binding.suggestionFirst.text.toString()) }
    }

    private fun onSuggestionClick(text: String) {
        searchViewModel.search(text)
    }

    override fun onConnectivityChange(connected: Boolean) {
        isConnectedToNetwork = connected
        lifecycleScope.launch(Dispatchers.Main) {
            if (connected) {
                binding.connectivityErrorMessage.visibility = View.GONE
                val isNoResult = algoliaAdapter.items.isEmpty()
                refreshSuggestionsVisibility(isQueryEmpty)
                refreshNoResultVisibility(!isQueryEmpty && isNoResult)
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

    override fun onDestroy() {
        super.onDestroy()
        algoliaAdapter.setHits(emptyList())
    }
}