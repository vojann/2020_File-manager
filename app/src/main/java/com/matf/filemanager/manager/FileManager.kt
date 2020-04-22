package com.matf.filemanager.manager

import com.matf.filemanager.util.ClipboardMode
import com.matf.filemanager.versions.StateSaver
import com.matf.filemanager.util.FileManagerChangeListener
import com.matf.filemanager.util.MenuMode
import java.io.File

object FileManager {

    private var history: StateSaver<FileEntry> = StateSaver()

    val currentDirectory: File?
        get() = history.getCurrentInstance()?.file
    var entries: ArrayList<FileEntry> = ArrayList()
        private set

    var menuMode: MenuMode = MenuMode.OPEN
        private set
    var clipboardMode: ClipboardMode = ClipboardMode.NONE
        private set
    var clipboard: ArrayList<File> = ArrayList()
        private set

    private var listener: FileManagerChangeListener? = null

    fun goTo(entry: FileEntry): Boolean {
        if(!entry.file.exists())
            return false

        if(entry.file.isDirectory) {
            history.goTo(entry)
            refresh()
        } else {
            return requestFileOpen(entry.file)
        }
        return true
    }

    fun goBack(): Boolean {
        if(history.goBack()) {
            refresh()
            return true
        }
        return false
    }

    fun goForward(): Boolean {
        if(history.goForward()) {
            refresh()
            return true
        }
        return false
    }

    fun canGoBack() : Boolean = history.canGoBack()

    fun canGoForward() : Boolean = history.canGoForward()

    fun refresh() {
        // TODO Don't mutate entries in stateSaver
        entries.clear()
        entries.addAll(history.getCurrentInstance()?.listFileEntries().orEmpty())

        if(menuMode == MenuMode.SELECT) toggleSelectionMode()
        notifyEntriesChanged()
    }

    fun toggleSelectionMode(){
        when(menuMode) {
            MenuMode.OPEN -> {
                menuMode = MenuMode.SELECT
            }
            MenuMode.SELECT -> {
                menuMode = MenuMode.OPEN
                for (f in entries)
                    f.selected = false
            }
        }
        notifySelectionModeChanged()
        notifyEntriesChanged()
    }

    fun toggleSelectionAt(i: Int){
        entries[i].selected = !entries[i].selected
        notifyEntriesChanged()
        notifySelectionModeChanged()
    }

    fun moveSelectedToClipboard(mode: ClipboardMode) {
        clipboardMode = mode
        when(menuMode){
            MenuMode.SELECT -> {
                clipboard.clear()
                clipboard.addAll(entries.filter { e -> e.selected }.map { e -> e.file })
            }
            MenuMode.OPEN -> {
                // Nismo u modu za selekciju, ispraznicemo clipboard
                // Nikada ne bi trebalo da dodjemo ovde
                clipboard.clear()
            }

        }
        notifyClipboardChanged()
    }

    fun selectionEmpty(): Boolean {
        return entries.none { e -> e.selected }
    }

    private fun copy() {
        for(f in clipboard) {
            if(currentDirectory?.startsWith(f) == true){
                continue
                //TODO Nalazimo se unutar fajla koji kopiramo
            }
            var new_name = f.nameWithoutExtension
            //TODO Limit this by hardcoded value
            while(true) {
                if(currentDirectory?.resolve(new_name+"."+f.extension)?.exists() == true) {
                    new_name += "-copy"
                } else {
                    break;
                }
            }

            if(!f.isDirectory)
                new_name += "." + f.extension
            listener?.copyFile(f, currentDirectory?.resolve(new_name) as File)
        }
    }

    fun paste() {
        when(clipboardMode) {
            ClipboardMode.NONE -> {
                // Nikada ne bi trebalo da dodjemo ovde
            }
            ClipboardMode.COPY -> {
                copy()

                clipboard.clear()
                clipboardMode = ClipboardMode.NONE
                notifyClipboardChanged()
            }
        }
    }

    fun setListener(listener: FileManagerChangeListener) {
        this.listener = listener
    }

    private fun notifyEntriesChanged() {
        listener?.onEntriesChange()
    }

    private fun notifySelectionModeChanged() {
        listener?.onSelectionModeChange(menuMode)
    }

    private fun notifyClipboardChanged() {
        listener?.onClipboardChange(clipboardMode)
    }

    private fun requestFileOpen(file: File): Boolean {
        return listener?.onRequestFileOpen(file)==true
    }

}