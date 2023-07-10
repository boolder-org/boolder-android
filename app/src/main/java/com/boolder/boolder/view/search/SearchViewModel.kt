package com.boolder.boolder.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.domain.convert
import kotlinx.coroutines.launch
import java.text.Normalizer

class SearchViewModel(
    private val problemRepository: ProblemRepository,
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _result = MutableLiveData<List<BaseObject>>()
    val searchResult: LiveData<List<BaseObject>> = _result

    fun search(query: String?) {
        viewModelScope.launch {
            val pattern = query
                ?.takeIf { it.isNotBlank() }
                ?.let { "%${it.normalized()}%" }
                .orEmpty()

            val areas = areaRepository.areasByName(pattern)
                .map { it.convert() }

            val problems = problemRepository.problemsByName(pattern)
                .map { it.convert() }

            _result.value = buildList {
                if (areas.isNotEmpty()) {
                    add(CategoryHeader(R.string.category_area))
                    addAll(areas)
                }
                if (problems.isNotEmpty()) {
                    add(CategoryHeader(R.string.category_problem))
                    addAll(problems)
                }
            }
        }
    }

    private fun String.normalized() =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(REGEX_EXCLUDED_CHARS, "")
            .lowercase()

    companion object {
        private val REGEX_EXCLUDED_CHARS = Regex("[^0-9a-zA-Z]")
    }
}