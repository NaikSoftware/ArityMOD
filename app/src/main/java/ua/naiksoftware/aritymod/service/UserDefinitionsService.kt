// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.service

import android.content.Context
import io.reactivex.subjects.BehaviorSubject
import org.javia.arity.Symbols
import org.javia.arity.SyntaxException
import ua.naiksoftware.aritymod.core.util.FileHandler
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class UserDefinitionsService(context: Context, private val symbols: Symbols) : FileHandler(context, "defs", 1) {

    companion object {
        private const val SIZE_LIMIT = 50
    }

    val lines = ArrayList<String>()
    val linesSubject = BehaviorSubject.create<List<String>>()

    init {
        load()
        symbols.pushFrame()
    }

    fun clear() {
        lines.clear()
        symbols.popFrame()
        symbols.pushFrame()
        linesSubject.onNext(lines)
    }

    fun size(): Int {
        return lines.size
    }

    @Throws(IOException::class)
    public override fun doRead(inputStream: DataInputStream) {
        val size = inputStream.readInt()
        for (i in 0 until size) {
            val line = inputStream.readUTF()
            lines.add(line)
            try {
                symbols.define(symbols.compileWithName(line))
            } catch (e: SyntaxException) {
                // ignore
            }
        }
        linesSubject.onNext(lines)
    }

    @Throws(IOException::class)
    public override fun doWrite(outputStream: DataOutputStream) {
        outputStream.writeInt(lines.size)
        for (s in lines) {
            outputStream.writeUTF(s)
        }
    }

    fun add(text: String) {
        if (lines.size >= SIZE_LIMIT) {
            lines.removeAt(0)
        }
        lines.add(text)
        linesSubject.onNext(lines)
    }
}
