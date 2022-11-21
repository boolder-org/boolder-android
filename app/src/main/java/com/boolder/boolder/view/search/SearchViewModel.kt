package com.boolder.boolder.view.search

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.flow
import com.algolia.instantsearch.android.paging3.liveData
import com.algolia.instantsearch.android.paging3.searchbox.connectPaginator
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searcher.hits.addHitsSearcher
import com.algolia.instantsearch.searcher.multi.MultiSearcher
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.response.ResponseSearch
import com.boolder.boolder.data.network.model.AreaRemote
import com.boolder.boolder.data.network.model.ProblemRemote

class SearchViewModel : ViewModel() {

    companion object {
        private const val INDEX_PROBLEM = "Problem"
        private const val INDEX_AREA = "Area"
    }

    private val searcher = MultiSearcher(
        applicationID = ApplicationID("XNJHVMTGMF"),
        apiKey = APIKey("765db6917d5c17449984f7c0067ae04c")
    )
    private val pagingConfig = PagingConfig(pageSize = 50)
    private val indexProblem = IndexName(INDEX_PROBLEM)
    private val indexArea = IndexName(INDEX_AREA)
    private val problemSearcher = searcher.addHitsSearcher(indexProblem)
    private val areaSearcher = searcher.addHitsSearcher(indexArea)
    private val filterState = FilterState()
    private val searchBoxConnector = SearchBoxConnector(searcher)
    private val connection = ConnectionHandler(searchBoxConnector)
    private val problemPaginator = Paginator(
        problemSearcher,
        pagingConfig,
        transformer = { hit ->
            println("Do we hit this ? (problem)")
            hit.deserialize(ProblemRemote.serializer())
        }
    )
    private val areaPaginator = Paginator(
        areaSearcher,
        pagingConfig,
        transformer = { hit ->
            println("Do we hit this ? (area)")
            hit.deserialize(AreaRemote.serializer())
        }
    )

    val problems
        get() = problemPaginator.flow

    val areas
        get() = areaPaginator.flow

    fun connect() {
//        connection += filterState.connectPaginator(problemPaginator)
//        connection += filterState.connectPaginator(areaPaginator)

        connection += searchBoxConnector.connectPaginator(problemPaginator)
        connection += searchBoxConnector.connectPaginator(areaPaginator)
    }

    fun search(query: String? = "") {
        searcher.setQuery(query)
        searcher.searchAsync()
        problemPaginator.liveData.observeForever {
            println("Observe forever get hit")
        }

        searcher.response.subscribe {
            println((it?.results?.firstOrNull()?.response as ResponseSearch).hitsOrNull?.firstOrNull())
        }
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.disconnect()
    }
}