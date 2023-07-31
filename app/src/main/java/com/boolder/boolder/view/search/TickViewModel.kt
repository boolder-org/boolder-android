package com.boolder.boolder.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.repository.TickRepository
import com.boolder.boolder.domain.convert
import kotlinx.coroutines.launch

class TickViewModel(
    private val tickRepository: TickRepository,
) : ViewModel() {

    private val _result = MutableLiveData<List<BaseObject>>()
    val searchResult: LiveData<List<BaseObject>> = _result

    fun list(){
        viewModelScope.launch {
//            tickRepository.deleteAll()
            val problems = tickRepository.getProblemsWithAreaNames()
                .map { it.convert() }

            _result.value = buildList {
                if (problems.isNotEmpty()) {
                    add(CategoryHeader(R.string.category_problem))
                    addAll(problems)
                }
            }
        }

    }
}