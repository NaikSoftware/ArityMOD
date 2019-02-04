// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.service

import android.content.Context
import io.reactivex.subjects.BehaviorSubject

import java.io.*
import java.util.ArrayList

import ua.naiksoftware.aritymod.core.util.FileHandler
import ua.naiksoftware.aritymod.service.model.HistoryEntry

class HistoryService(context: Context) : FileHandler(context, "history", 1) {

    private val entries = ArrayList<HistoryEntry>()
    private var position: Int = 0
    private var aboveTop = HistoryEntry("", "")

    val historySubject = BehaviorSubject.create<List<HistoryEntry>>()


    val listPosition: Int
        get() = entries.size - 1 - position

    val text: String?
        get() = currentEntry().editLine

    init {
        load()
    }

    fun clear() {
        entries.clear()
        position = 0
        historySubject.onNext(entries)
    }

    fun size(): Int {
        return entries.size
    }

    @Throws(IOException::class)
    public override fun doRead(inputStream: DataInputStream) {
        aboveTop = HistoryEntry(inputStream)
        val loadSize = inputStream.readInt()
        for (i in 0 until loadSize) {
            entries.add(HistoryEntry(inputStream))
        }
        position = entries.size
        historySubject.onNext(entries)
    }

    @Throws(IOException::class)
    public override fun doWrite(outputStream: DataOutputStream) {
        aboveTop.save(outputStream)
        outputStream.writeInt(entries.size)
        for (entry in entries) {
            entry.save(outputStream)
        }
    }

    private fun currentEntry(): HistoryEntry {
        return if (position < entries.size) {
            entries[position]
        } else {
            aboveTop
        }
    }

    fun onEnter(text: String?, resultParam: String?): Boolean {
        var result = resultParam
        if (result == null) {
            result = ""
        }

        currentEntry().onEnter()
        position = entries.size
        if (text == null || text.isEmpty()) {
            return false
        }

        if (entries.size > 0) {
            val top = entries[entries.size - 1]
            if (text == top.line && result == top.result) {
                return false
            }
        }

        if (entries.size > SIZE_LIMIT) {
            entries.removeAt(0)
        }

        entries.add(HistoryEntry(text, result))
        position = entries.size

        historySubject.onNext(entries)

        return true
    }

    fun moveToPos(listPos: Int, text: String?) {
        currentEntry().editLine = text
        position = entries.size - listPos - 1
        historySubject.onNext(entries)
    }

    fun updateEdited(text: String?) {
        currentEntry().editLine = text
        historySubject.onNext(entries)
    }

    fun moveUp(text: String): Boolean {
        updateEdited(text)
        if (position >= entries.size) {
            return false
        }
        ++position
        return true
    }

    fun moveDown(text: String): Boolean {
        updateEdited(text)
        if (position <= 0) {
            return false
        }
        --position
        return true
    }

    fun getEntries(): List<HistoryEntry> {
        return entries
    }

    companion object {

        private val SIZE_LIMIT = 30
    }
}
