package com.boolder.boolder.offline

import android.content.Context
import java.io.File

class FileExplorer(private val context: Context) {

    fun getTopoImageFile(areaId: Int, topoId: Int): File? {
        val areaFolder = context.getDir(areaId.toString(), Context.MODE_PRIVATE)
        val imageFile = File(areaFolder, topoId.toString())

        return imageFile.takeIf { it.exists() }
    }

    fun areaFolderSize(areaId: Int): Long {
        val areaFolder = context.getDir(areaId.toString(), Context.MODE_PRIVATE)

        return areaFolder
            ?.listFiles()
            ?.sumOf { it.length() }
            ?: 0L
    }

    fun deleteFolder(areaId: Int) {
        val areaFolder = context.getDir(areaId.toString(), Context.MODE_PRIVATE)
        val folderContents = areaFolder?.listFiles() ?: emptyArray()

        folderContents.forEach { it.delete() }
        areaFolder.delete()
    }
}
