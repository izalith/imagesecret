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

package com.izalith.imagesecret.lib.dto

import com.izalith.imagesecret.lib.dto.payload.*
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

import org.junit.jupiter.api.Test
import java.io.File

class PayloadTest {
    private val payloadFile = "/payload.txt"

    @Test
    fun testTextPayloadRead() {
        val text = "string"
        val payload = TextPayload(text)
        val bytes = payload.write()

        val actualPayload = Payload.read(bytes)
        assertEquals(PayloadType.TEXT, actualPayload.type)
        assertEquals(text, (actualPayload as TextPayload).text)
    }

    @Test
    fun testFilePayloadRead() {
        val textFile = File(this::class.java.getResource(payloadFile).file)
        val expectedFileContent = FileUtils.readFileToByteArray(textFile)

        val payload = FilePayload(textFile)
        val bytes = payload.write()

        val actualPayload = Payload.read(bytes)
        assertEquals(PayloadType.FILE, actualPayload.type)
        assertEquals(textFile.name, (actualPayload as FilePayload).filename)
        assertTrue(expectedFileContent.contentEquals(actualPayload.fileContent))
    }

    @Test
    fun testBinaryPayloadRead() {
        val textFile = File(this::class.java.getResource(payloadFile).file)
        val expectedFileContent = FileUtils.readFileToByteArray(textFile)

        val payload = BinaryPayload(expectedFileContent)
        val bytes = payload.write()

        val actualPayload = Payload.read(bytes)
        assertEquals(PayloadType.BINARY, actualPayload.type)
        assertTrue(expectedFileContent.contentEquals((actualPayload as BinaryPayload).content))
    }

    @Test
    fun testTextPayloadWrite() {
        val text = "string"
        val payload = TextPayload(text)
        val bytes = payload.write()

        val actualMarker = byteArrayOf(bytes[0], bytes[1])
        assertTrue(PayloadType.TEXT.markerByteArray().contentEquals(actualMarker))

        val actualString = bytes.sliceArray(2 until bytes.size).toString(Charsets.UTF_8)
        assertEquals(text, actualString)
    }

    @Test
    fun testFilePayloadWrite() {
        val textFile = File(this::class.java.getResource(payloadFile).file)
        val expectedFileContent = FileUtils.readFileToByteArray(textFile)
        val payload = FilePayload(textFile)

        val bytes = payload.write()

        val actualMarker = byteArrayOf(bytes[0], bytes[1])
        assertTrue(PayloadType.FILE.markerByteArray().contentEquals(actualMarker))

        val filenameLength = bytes[2]

        val endOfFilename = filenameLength + 3
        val filename = bytes.sliceArray(3 until endOfFilename).toString(Charsets.UTF_8)
        assertEquals(textFile.name, filename)

        val actualFileContent = bytes.sliceArray(endOfFilename until bytes.size)
        assertTrue(expectedFileContent.contentEquals(actualFileContent))
    }

    @Test
    fun testBinaryPayloadWrite() {
        val textFile = File(this::class.java.getResource(payloadFile).file)
        val expectedFileContent = FileUtils.readFileToByteArray(textFile)

        val payload = BinaryPayload(expectedFileContent)
        val bytes = payload.write()

        val actualMarker = byteArrayOf(bytes[0], bytes[1])
        assertTrue(PayloadType.BINARY.markerByteArray().contentEquals(actualMarker))

        val actualFileContent = bytes.sliceArray(2 until bytes.size)

        assertTrue(expectedFileContent.contentEquals(actualFileContent))
    }
}
