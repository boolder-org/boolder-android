package com.boolder.boolder.view.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
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

    private val problemAdapter = ProblemAdapter()
    private val areaAdapter = AreaAdapter()

    private val suggestions = listOf("Isatis", "La Marie-Rose", "Cul de Chien")

    private val isQueryEmpty: Boolean
        get() = binding.searchComponent.searchBar.text?.isBlank() == true

    private val isQueryProduceResult: Boolean
        get() = true // TODO impl + rename

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
            refreshSuggestionsVisibility(isQueryEmpty)
            refreshNoResultVisibility(isQueryProduceResult)
        }

        binding.recyclerView.apply {
            adapter = ConcatAdapter(problemAdapter, areaAdapter)
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        binding.searchComponent.searchBar.addTextChangedListener { query ->
            refreshSuggestionsVisibility(isQueryEmpty)
            refreshNoResultVisibility(isQueryProduceResult)
            searchViewModel.search(query?.toString())
        }

        applySuggestions()


        lifecycleScope.launch {
            searchViewModel.problems.collect {
                println("RESULT PROBLEM $it")
                problemAdapter.submitData(it)
            }
            searchViewModel.areas.collect {
                println("RESULT AREAS $it")
                areaAdapter.submitData(it)
            }

        }
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