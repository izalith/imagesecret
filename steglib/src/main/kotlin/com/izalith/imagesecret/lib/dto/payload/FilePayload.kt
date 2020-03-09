/*
 * Copyright 2020 Ilya Titovskiy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.izalith.imagesecret.lib.dto.payload

import org.apache.commons.io.FileUtils
import java.io.File

class FilePayload : Payload {
    override val type: PayloadType
        get() = PayloadType.FILE
    val fileContent: ByteArray
    val filename: String

    constructor(stegContent: ByteArray) {
        val filenameSize = stegContent[0]
        val minBitesForFile = 1 + filenameSize + 1
        Payload.checkByteArraySize(stegContent, minBitesForFile)
        val filenameBytes = stegContent.sliceArray(1..filenameSize)

        filename = filenameBytes.toString(Charsets.UTF_8)
        this.fileContent = stegContent.sliceArray(filenameSize + 1 until stegContent.size)
    }

    constructor(file: File) {
        this.fileContent = FileUtils.readFileToByteArray(file)
        this.filename = file.name
    }

    constructor(fileContent: ByteArray, filename: String) {
        this.fileContent = fileContent
        this.filename = filename
    }

    override fun write(): ByteArray {
        val filenameBytes = filename.toByteArray(Charsets.UTF_8)
        val size = filenameBytes.size.toByte()
        return type.markerByteArray() + byteArrayOf(size) + filenameBytes + fileContent
    }

}
