package com.boolder.boolder.data.network

import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.highlighting.Highlightable
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searcher.hits.addHitsSearcher
import com.algolia.instantsearch.searcher.multi.MultiSearcher
import com.algolia.search.model.*
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

//TODO To be renamed, changed and moved
@Serializable
data class Movie(
    val title: String,
    override val objectID: ObjectID,
    override val _highlightResult: JsonObject?
) : Indexable, Highlightable {

    val highlightedTitle
        get() = getHighlight(Attribute("title"))
}

class AlgoliaClient {

    private val client = MultiSearcher(
        applicationID = ApplicationID("XNJHVMTGMF"),
        apiKey = APIKey("765db6917d5c17449984f7c0067ae04c")
    )

    private val index = client.addHitsSearcher(IndexName("index_name"))
    private val searchBoxConnector = SearchBoxConnector(client)
    private val connections = ConnectionHandler(searchBoxConnector)

//    val searchBoxState = SearchBoxState()
//    val problemState = HitsState<Movie>()

    init {
//        connections += searchBoxConnector.connectView(searchBoxState)
//        connections += index.connectHitsView(problemState) { it.hits.deserialize(Movie.serializer()) }
        client.searchAsync()
    }

    suspend fun searchForQuery(string: String) {

    }
}