package com.boolder.boolder.domain

import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.offline.FileExplorer
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class PhotoUriRetrieverTest {

    @Mock private lateinit var topoRepository: TopoRepository
    @Mock private lateinit var fileExplorer: FileExplorer

    private lateinit var retriever: PhotoUriRetriever

    @Before
    fun setUp() {
        retriever = PhotoUriRetriever(
            topoRepository,
            fileExplorer
        ).let(::spy)
    }

    @Test
    fun `getPhotoUri() should return the local image file URI`() = runTest {
        // Given
        val areaId = 0
        val topoId = 0

        retriever.stub { on { getLocalImageUri(areaId, topoId) } doReturn "file:///path/to/image" }

        // When
        val uri = retriever.getPhotoUri(areaId, topoId)

        // Then
        verify(topoRepository, never()).getTopoPictureById(anyInt())
        assertEquals(uri, "file:///path/to/image" )
    }

    @Test
    fun `getPhotoUri() should return the image URL`() = runTest {
        // Given
        val areaId = 0
        val topoId = 0

        retriever.stub { on { getLocalImageUri(areaId, topoId) } doReturn null }
        topoRepository.stub { onBlocking { getTopoPictureById(topoId) } doReturn "https://www.boolder.com/path/to/image" }

        // When
        val uri = retriever.getPhotoUri(areaId, topoId)

        // Then
        assertEquals(uri, "https://www.boolder.com/path/to/image")
    }

    @Test
    fun `getPhotoUri() should return null`() = runTest {
        // Given
        val areaId = 0
        val topoId = 0

        retriever.stub { on { getLocalImageUri(areaId, topoId) } doReturn null }
        topoRepository.stub { onBlocking { getTopoPictureById(topoId) } doReturn null }

        // When
        val uri = retriever.getPhotoUri(areaId, topoId)

        // Then
        assertNull(uri)
    }
}
