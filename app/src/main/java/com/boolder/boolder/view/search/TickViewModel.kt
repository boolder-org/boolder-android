package com.boolder.boolder.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.repository.TickRepository
import com.boolder.boolder.domain.convert
import kotlinx.coroutines.launch
import java.text.Normalizer

class TickViewModel(
    private val tickRepository: TickRepository,
) : ViewModel() {

    private val _result = MutableLiveData<List<BaseObject>>()
    val searchResult: LiveData<List<BaseObject>> = _result

    fun list(){
        viewModelScope.launch {
//            tickRepository.deleteAll()
            val areas_with_problems = tickRepository.getProblemsPerArea();

            _result.value = buildList {
                if (areas_with_problems.isNotEmpty()) {
                    add(CategoryHeader(R.string.category_ticks))
                    areas_with_problems.forEach { area_with_problems ->
                        add(area_with_problems.areaEntity.convert())
                        addAll(area_with_problems.problems.map{ it.convert()})
                    }
                }
            }
        }
    }

    fun search(query: String?) {
        viewModelScope.launch {
            val pattern = query
                ?.takeIf { it.isNotBlank() }
                ?.let { "%${it.normalized()}%" }
                .orEmpty()

            val areas_with_problems = tickRepository.getProblemsByNamePerArea(pattern)

            _result.value = buildList {
                if (areas_with_problems.isNotEmpty()) {
                    add(CategoryHeader(R.string.category_ticks))
                    areas_with_problems.forEach { area_with_problems ->
                        add(area_with_problems.areaEntity.convert())
                        addAll(area_with_problems.problems.map{ it.convert()})
                    }
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