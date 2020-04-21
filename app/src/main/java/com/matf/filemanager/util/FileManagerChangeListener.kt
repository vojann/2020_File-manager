package com.matf.filemanager.util

interface FileManagerChangeListener {
    fun onEntriesChange()
    fun onSelectionModeChange(mode : MenuMode)
    fun onClipboardChange(mode : ClipboardMode)
}