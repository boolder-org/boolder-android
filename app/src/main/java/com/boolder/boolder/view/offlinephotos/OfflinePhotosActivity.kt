package com.boolder.boolder.view.offlinephotos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class OfflinePhotosActivity : AppCompatActivity() {

    private val viewModel by viewModel<OfflinePhotosViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BoolderTheme {
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                OfflinePhotosScreen(
                    screenState = screenState,
                    onDownloadAreaClicked = viewModel::onDownloadAreaClicked,
                    onDownloadTerminated = viewModel::onDownloadTerminated,
                    onCancelDownload = viewModel::onCancelDownload,
                    onDeleteAreaClicked = viewModel::onDeleteAreaClicked
                )
            }
        }
    }
}
