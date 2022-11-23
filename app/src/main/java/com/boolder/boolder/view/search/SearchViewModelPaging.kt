package com.boolder.boolder.view.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.filterstate.connectPaginator
import com.algolia.instantsearch.android.paging3.flow
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.searcher.hits.addHitsSearcher
import com.algolia.instantsearch.searcher.multi.MultiSearcher
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.boolder.boolder.data.network.model.AreaRemote
import com.boolder.boolder.data.network.model.ProblemRemote
import com.boolder.boolder.domain.model.AlgoliaConfig

// TODO Understand with either flow or livedata aren't updated on query changes
// Issue open on Github
// https://github.com/algolia/instantsearch-android/issues/374
class SearchViewModelPaging : ViewModel() {

    private val searcher = MultiSearcher(
        applicationID = ApplicationID(AlgoliaConfig.applicationId),
        apiKey = APIKey(AlgoliaConfig.apiKey),
        coroutineScope = viewModelScope
    )

    private val pagingConfig = PagingConfig(pageSize = 50)
    private val indexProblem = IndexName("Problem")
    private val indexArea = IndexName("Area")
    private val problemSearcher = searcher.addHitsSearcher(indexName = indexProblem)
    private val areaSearcher = searcher.addHitsSearcher(indexName = indexArea)
    private val filterState = FilterState()
    private val connection = ConnectionHandler()
    private val problemPaginator = Paginator(
        problemSearcher,
        pagingConfig,
        transformer = { hit ->
            hit.deserialize(ProblemRemote.serializer())
        }
    )
    private val areaPaginator = Paginator(
        areaSearcher,
        pagingConfig,
        transformer = { hit ->
            println("Area transform")
            hit.deserialize(AreaRemote.serializer())
        }
    )


    init {
        connection += filterState.connectPaginator(problemPaginator)
        connection += filterState.connectPaginator(areaPaginator)
    }

    val problems
        get() = problemPaginator.flow.cachedIn(viewModelScope)

    val areas
        get() = areaPaginator.flow.cachedIn(viewModelScope)

    fun search(query: String? = "") {
        searcher.setQuery(query)
        searcher.searchAsync()
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.disconnect()
    }
}