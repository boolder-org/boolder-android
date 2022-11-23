package com.boolder.boolder.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searcher.hits.addHitsSearcher
import com.algolia.instantsearch.searcher.multi.MultiSearcher
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.response.ResponseSearch
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.network.model.AreaRemote
import com.boolder.boolder.data.network.model.ProblemRemote
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.AlgoliaConfig
import kotlinx.coroutines.launch

class SearchViewModel(
    private val problemRepository: ProblemRepository,
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val searcher = MultiSearcher(
        applicationID = ApplicationID(AlgoliaConfig.applicationId),
        apiKey = APIKey(AlgoliaConfig.apiKey),
        coroutineScope = viewModelScope
    ).apply {
        addHitsSearcher(IndexName("Problem"))
        addHitsSearcher(IndexName("Area"))
    }
    private val searchBox = SearchBoxConnector(searcher)
    private val connection = ConnectionHandler(searchBox)

    private val _result = MutableLiveData<List<BaseObject>>()
    val searchResult: LiveData<List<BaseObject>> = _result

    init {
        searcher.response.subscribe { response ->
            val problemHits = (response?.results?.firstOrNull()?.response as? ResponseSearch)?.hits
            val areaHits = (response?.results?.getOrNull(1)?.response as? ResponseSearch)?.hits

            viewModelScope.launch {
                val problemsIds = problemHits?.associate {
                    val remote = it.deserialize(ProblemRemote.serializer())
                    Integer.parseInt(remote.objectID) to remote.areaName
                } ?: emptyMap()

                val problems = problemRepository.loadAllByIds(problemsIds.keys.toList())
                    .map { it.convert(problemsIds[it.id]) }

                val areasIds = areaHits?.map {
                    val id = it.deserialize(AreaRemote.serializer()).objectID
                    Integer.parseInt(id)
                } ?: emptyList()

                val areas = areaRepository.loadAllByIds(areasIds).map { it.convert() }

                _result.value = problems + areas
            }
        }
    }

    fun search(query: String?) {
        searcher.setQuery(query)
        searcher.searchAsync()
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.disconnect()
    }
}