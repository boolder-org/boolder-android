package com.boolder.boolder.view.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivitySearchBinding
import com.boolder.boolder.utils.NetworkObserver
import com.boolder.boolder.utils.NetworkObserverImpl
import com.boolder.boolder.utils.extension.setOnApplyWindowTopInsetListener
import com.boolder.boolder.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class SearchActivity : AppCompatActivity(), NetworkObserver {

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    private val searchViewModel: SearchViewModel = SearchViewModel(get(), get())
    private val networkObserverImpl: NetworkObserverImpl by inject()

    private val searchAdapter = SearchAdapter(
        onAreaClicked = { area ->
            setResult(RESULT_OK, Intent().apply { putExtra("AREA", area) })
            finish()
        },
        onProblemClicked = { problem ->
            setResult(RESULT_OK, Intent().apply { putExtra("PROBLEM", problem) })
            finish()
        }
    )

    private val suggestions = listOf("Isatis", "La Marie-Rose", "Cul de Chien")

    private val query
        get() = binding.searchComponent.searchBar.text
    private val isQueryEmpty
        get() = query == null || query.isEmpty() || query.isBlank()


    private var isConnectedToNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.root.setOnApplyWindowTopInsetListener { topInset ->
            val topMargin = topInset + resources.getDimensionPixelSize(R.dimen.margin_search_component)

            binding.searchComponent
                .searchContainer
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { updateMargins(top = topMargin) }
        }

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
            searchAdapter.submitList(emptyList())
        }

        binding.recyclerView.apply {
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(this@SearchActivity, LinearLayoutManager.VERTICAL))
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        binding.searchComponent.searchBar.addTextChangedListener { query ->
            if (isQueryEmpty) {
                binding.searchComponent.searchLastIcon.visibility = View.GONE
                searchAdapter.submitList(emptyList())
                refreshSuggestionsVisibility(true)
            } else {
                binding.searchComponent.searchLastIcon.visibility = View.VISIBLE
                searchViewModel.search(query.toString())
            }
        }

        applySuggestions()

        searchViewModel.searchResult.observe(this) {
            refreshNoResultVisibility(it.isEmpty())
            refreshSuggestionsVisibility(false)
            if (isQueryEmpty) {
                searchAdapter.submitList(emptyList())
            } else {
                searchAdapter.submitList(it)
            }
        }
    }

    private fun applySuggestions() {
        binding.suggestionFirst.text = suggestions[0]
        binding.suggestionSecond.text = suggestions[1]
        binding.suggestionThird.text = suggestions[2]

        binding.suggestionFirst.setOnClickListener { onSuggestionClick(suggestions[0]) }
        binding.suggestionSecond.setOnClickListener { onSuggestionClick(suggestions[1]) }
        binding.suggestionThird.setOnClickListener { onSuggestionClick(suggestions[2]) }
    }

    private fun onSuggestionClick(text: String) {
        binding.searchComponent.searchBar.setText(text)
        binding.searchComponent.searchBar.setSelection(text.length)
    }

    override fun onConnectivityChange(connected: Boolean) {
        isConnectedToNetwork = connected
        lifecycleScope.launch(Dispatchers.Main) {
            if (connected) {
                binding.connectivityErrorMessage.visibility = View.GONE
                val isNoResult = searchAdapter.currentList.isEmpty()
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
}