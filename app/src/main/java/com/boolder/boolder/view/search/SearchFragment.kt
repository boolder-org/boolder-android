package com.boolder.boolder.view.search

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentSearchBinding
import com.boolder.boolder.utils.extension.setOnApplyWindowTopInsetListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var binding: FragmentSearchBinding? = null

    private val searchViewModel by viewModel<SearchViewModel>()

    private val searchAdapter = SearchAdapter(
        onAreaClicked = { area ->
            setFragmentResult(REQUEST_KEY, bundleOf("AREA" to area))
            findNavController().popBackStack()
        },
        onProblemClicked = { problem ->
            setFragmentResult(REQUEST_KEY, bundleOf("PROBLEM" to problem))
            findNavController().popBackStack()
        }
    )

    private val suggestions = listOf("Isatis", "La Marie-Rose", "Cul de Chien")

    private lateinit var textWatcher: TextWatcher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentSearchBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding ?: return

        binding.root.setOnApplyWindowTopInsetListener { topInset ->
            val topMargin = topInset + resources.getDimensionPixelSize(R.dimen.margin_search_component)

            binding.searchComponent
                .searchContainer
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { updateMargins(top = topMargin) }
        }

        binding.searchComponent.searchBar.requestFocus()

        binding.searchComponent.searchFirstIcon.apply {
            val drawable = ContextCompat.getDrawable(view.context, R.drawable.ic_arrow_back)

            setImageDrawable(drawable)
            setOnClickListener { findNavController().popBackStack() }
        }

        binding.searchComponent.searchLastIcon.setOnClickListener {
            binding.searchComponent.searchBar.text.clear()
            refreshSuggestionsVisibility(true)
            refreshNoResultVisibility(false)
            searchAdapter.submitList(emptyList())
        }

        binding.recyclerView.apply {
            adapter = searchAdapter
            addItemDecoration(DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL))
        }

        textWatcher = binding.searchComponent.searchBar.doAfterTextChanged { query ->
            if (query.isNullOrBlank()) {
                binding.searchComponent.searchLastIcon.visibility = View.GONE
                searchAdapter.submitList(emptyList())
                refreshSuggestionsVisibility(true)
            } else {
                binding.searchComponent.searchLastIcon.visibility = View.VISIBLE
                searchViewModel.search(query.toString())
            }
        }

        applySuggestions()

        searchViewModel.searchResult.observe(viewLifecycleOwner) {
            refreshNoResultVisibility(it.isEmpty())
            refreshSuggestionsVisibility(false)
            if (binding.searchComponent.searchBar.text.isNullOrBlank()) {
                searchAdapter.submitList(emptyList())
            } else {
                searchAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() {
        textWatcher.let {
            binding?.searchComponent?.searchBar?.removeTextChangedListener(it)
        }

        super.onDestroyView()
    }

    private fun applySuggestions() {
        val binding = binding ?: return

        binding.suggestionFirst.text = suggestions[0]
        binding.suggestionSecond.text = suggestions[1]
        binding.suggestionThird.text = suggestions[2]

        binding.suggestionFirst.setOnClickListener { onSuggestionClick(suggestions[0]) }
        binding.suggestionSecond.setOnClickListener { onSuggestionClick(suggestions[1]) }
        binding.suggestionThird.setOnClickListener { onSuggestionClick(suggestions[2]) }
    }

    private fun onSuggestionClick(text: String) {
        val binding = binding ?: return

        binding.searchComponent.searchBar.setText(text)
        binding.searchComponent.searchBar.setSelection(text.length)
    }

    private fun refreshSuggestionsVisibility(show: Boolean) {
        binding?.suggestionContainer?.isVisible = show
    }

    private fun refreshNoResultVisibility(show: Boolean) {
        binding?.emptyQueryMessage?.isVisible = show
    }

    companion object {
        const val REQUEST_KEY = "search"
    }
}
