package com.matf.filemanager.Versions

open class StateSaver<T> : Versionable<T>{
    private var history: ArrayList<T> = ArrayList()
    private var currentInstanceIndex = -1

    constructor(initialElement: T){
        history.add(initialElement)
        currentInstanceIndex = 0
    }

    override fun getCurrentInstance(): T {
        return history[currentInstanceIndex]
    }

    override fun goTo(newElement: T): Boolean {

        if (currentInstanceIndex == history.size - 1) {
            history.add(newElement)
            currentInstanceIndex++
        } else {
            for (i in history.size - 1 downTo currentInstanceIndex + 1) {
                history.removeAt(i)
            }
            history.add(newElement)
            currentInstanceIndex++
        }

        return true

    }

    override fun goBack(): Boolean {
        if (currentInstanceIndex == 0) return false
        currentInstanceIndex--
        return true
    }

    override fun goForward(): Boolean {
        if (currentInstanceIndex == history.size - 1) return false
        currentInstanceIndex++
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in history.indices) {
            if (i == currentInstanceIndex) {
                sb.append("[[")
            }
            sb.append(history[i].toString())
            if (i == currentInstanceIndex) {
                sb.append("]]")
            }
            if (i != history.size - 1) {
                sb.append(" -> ")
            }
        }
        return sb.toString()
    }
}