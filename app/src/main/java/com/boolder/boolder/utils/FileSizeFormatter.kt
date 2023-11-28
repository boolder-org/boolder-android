package com.boolder.boolder.utils

class FileSizeFormatter {

    fun formatBytesSize(bytesSize: Long): String {
        var currentSize = bytesSize.toDouble()

        if (currentSize < BYTES_UNIT_FACTOR) {
            return String.format("%.2f B", currentSize)
        }

        currentSize /= BYTES_UNIT_FACTOR

        if (currentSize < BYTES_UNIT_FACTOR) {
            return String.format("%.2f kB", currentSize)
        }

        currentSize /= BYTES_UNIT_FACTOR

        return String.format("%.2f MB", currentSize)
    }

    companion object {
        private const val BYTES_UNIT_FACTOR = 1024.0
    }
}
