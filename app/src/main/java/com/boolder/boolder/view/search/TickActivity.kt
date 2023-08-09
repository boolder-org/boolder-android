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

class TickActivity : AppCompatActivity(), NetworkObserver {

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    private val tickViewModel: TickViewModel = TickViewModel(get())
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
                this@TickActivity,
                R.drawable.ic_arrow_back
            )
            setImageDrawable(drawable)
            setOnClickListener { finish() }
        }

        binding.searchComponent.searchLastIcon.setOnClickListener {
            binding.searchComponent.searchBar.text.clear()
            tickViewModel.list()
        }

        binding.recyclerView.apply {
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(this@TickActivity, LinearLayoutManager.VERTICAL))
            layoutManager = LinearLayoutManager(this@TickActivity)
        }

        binding.searchComponent.searchBar.addTextChangedListener { query ->
            if (isQueryEmpty) {
                binding.searchComponent.searchLastIcon.visibility = View.GONE
                tickViewModel.list()
            } else{
                binding.searchComponent.searchLastIcon.visibility = View.VISIBLE
                tickViewModel.search(query.toString())
            }
        }
        tickViewModel.list()
        tickViewModel.searchResult.observe(this) {
            refreshNoResultVisibility(it.isEmpty())
            refreshSuggestionsVisibility(false)
            searchAdapter.submitList(it)
        }
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