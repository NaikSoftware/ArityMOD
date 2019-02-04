// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.service.model

import java.io.*

class HistoryEntry {

    var line: String? = null
        private set

    var editLine: String? = null

    var result: String? = null
        private set

    @Throws(IOException::class)
    constructor(inputStream: DataInputStream) {
        line = inputStream.readUTF()
        editLine = inputStream.readUTF()
        editLine?.apply {
            if (this.isEmpty()) {
                editLine = line
            }
        }
        result = inputStream.readUTF()
    }

    constructor(text: String, result: String?) {
        line = text
        editLine = text
        this.result = result ?: ""
    }

    @Throws(IOException::class)
    fun save(os: DataOutputStream) {
        os.writeUTF(line)
        os.writeUTF(if (editLine == line) "" else editLine)
        os.writeUTF(result)
    }

    fun onEnter() {
        editLine = line
    }
}
