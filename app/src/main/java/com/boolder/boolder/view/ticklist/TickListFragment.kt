package com.boolder.boolder.view.ticklist

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.view.areadetails.KEY_PROBLEM
import com.boolder.boolder.view.areadetails.REQUEST_KEY_AREA_DETAILS
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.ticklist.model.ExportableTickList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class TickListFragment : Fragment() {

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ::readFileContentFromUri
    )

    private val viewModel by viewModel<TickListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    TickListScreen(
                        screenState = screenState,
                        onProblemClicked = ::onProblemClicked,
                        onExportTickListClicked = viewModel::onExportTickList,
                        onImportTickListClicked = viewModel::onChooseTickListToImport
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.launchAndCollectIn(viewLifecycleOwner) {
            when (it) {
                is TickListViewModel.Event.TickListExportGenerated -> onTickListFileReadyToExport(it.file)
                is TickListViewModel.Event.ChooseTickListToImport -> onChooseTickListFileToImport()
                is TickListViewModel.Event.ShowError -> onShowErrorDialog(it.errorMessageId)
                is TickListViewModel.Event.AskForReplacementWhenImporting -> onShowImportationDialog(it.exportableTickList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }

    private fun onProblemClicked(problem: Problem) {
        setFragmentResult(
            REQUEST_KEY_AREA_DETAILS,
            bundleOf(KEY_PROBLEM to problem)
        )

        findNavController().popBackStack(R.id.map_fragment, inclusive = false)
    }

    private fun onTickListFileReadyToExport(exportFile: File) {
        val context = context ?: return

        val fileUri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            exportFile
        )

        val intent = Intent(Intent.ACTION_SEND, fileUri).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, fileUri)
        }

        context.startActivity(intent)
    }

    private fun onChooseTickListFileToImport() {
        openDocumentLauncher.launch("application/json")
    }

    private fun readFileContentFromUri(uri: Uri?) {
        uri ?: return

        val context = context ?: return

        context.contentResolver.openInputStream(uri).use(viewModel::onImportTickList)
    }

    private fun onShowErrorDialog(@StringRes errorMessageId: Int) {
        val context = context ?: return

        MaterialAlertDialogBuilder(context)
            .setMessage(errorMessageId)
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun onShowImportationDialog(exportableTickList: ExportableTickList) {
        val context = context ?: return

        val icon = AppCompatResources.getDrawable(context, R.drawable.ic_error_outline)
            ?.mutate()

        icon?.setTint(Color.rgb(255, 149, 0))

        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> viewModel.proceedToTickListImportation(
                    exportableTickList = exportableTickList,
                    shouldReplaceAll = false
                )
                DialogInterface.BUTTON_NEGATIVE -> viewModel.proceedToTickListImportation(
                    exportableTickList = exportableTickList,
                    shouldReplaceAll = true
                )
            }

            dialog.dismiss()
        }

        MaterialAlertDialogBuilder(context)
            .setIcon(icon)
            .setTitle(R.string.tick_list_dialog_importation_title)
            .setMessage(R.string.tick_list_dialog_importation_message)
            .setPositiveButton(R.string.tick_list_dialog_importation_button_merge, dialogClickListener)
            .setNegativeButton(R.string.tick_list_dialog_importation_button_replace, dialogClickListener)
            .setNeutralButton(R.string.tick_list_dialog_importation_button_cancel, dialogClickListener)
            .show()
    }
}
