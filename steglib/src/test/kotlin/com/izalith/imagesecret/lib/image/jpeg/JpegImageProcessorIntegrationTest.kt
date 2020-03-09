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

package com.izalith.imagesecret.lib.image.jpeg

import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import org.apache.commons.io.FileUtils

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import org.junit.jupiter.api.Assertions.*


class JpegImageProcessorIntegrationTest {

    private val landscapeFile = "/img/landscape_orig.jpg"
    private val treesFile = "/img/winter-trees-1639640-1918x1080.jpg"
    private val largePayloadFile = "/payload.txt"
    private val smallPayloadFile = "/smallPayload.txt"
    private var jpegDecoder = JpegImageProcessor()

    @Test
    fun decodeEncodeLandscapeWithMessage() {
        val carrierImage = File(this::class.java.getResource(landscapeFile).file)
        val carrierInputStream = FileInputStream(carrierImage)
        val payload = "encoded message 12345"
        val payloadBytes = payload.toByteArray(Charsets.UTF_8)

        val encodedImageBytes = jpegDecoder.encode(IOUtils.toByteArray(carrierInputStream), payloadBytes)
        val decodedPayload = jpegDecoder.decode(encodedImageBytes)

        val decodedString = decodedPayload.toString(Charsets.UTF_8)
        assertEquals(payload, decodedString)
    }

    /**
     *  Test that encoding and decoding process without payload doesn't change the image.
     */
    @Test
    fun decodeEncodeNoPayload() {
        val carrierImage = File(this::class.java.getResource(landscapeFile).file)
        val inputStream = FileInputStream(carrierImage)
        val emptyPayload = byteArrayOf()

        val result = jpegDecoder.encode(IOUtils.toByteArray(inputStream), emptyPayload)
        val original = IOUtils.toByteArray(FileInputStream(carrierImage))
        assertArrayEquals(original, result)
    }

    @Test
    fun testNotEnoughSpaceError() {
        val carrierImage = File(this::class.java.getResource(landscapeFile).file)
        val carrierInputStream = FileInputStream(carrierImage)
        val payloadFile = File(this::class.java.getResource(largePayloadFile).file)
        val payloadBytes = FileUtils.readFileToByteArray(payloadFile)

        val thrown = assertThrows(StegoException::class.java) {
            jpegDecoder.encode(IOUtils.toByteArray(carrierInputStream), payloadBytes)
        }

        assertEquals(StegoErrorCode.ERROR_NOT_ENOUGH_SPACE_FOR_PAYLOAD, thrown.errorCode)
    }

    @Test
    fun testEncodeDecodeWithSmallPayload() {
        val carrierImage = File(this::class.java.getResource(landscapeFile).file)
        val carrierInputStream = FileInputStream(carrierImage)
        val smallPayloadFile = File(this::class.java.getResource(smallPayloadFile).file)
        val payloadBytes = FileUtils.readFileToByteArray(smallPayloadFile)

        val encodedImageBytes = jpegDecoder.encode(IOUtils.toByteArray(carrierInputStream), payloadBytes)
        val decodedBytes = jpegDecoder.decode(encodedImageBytes)

        val decodedString = decodedBytes.toString(Charsets.UTF_8)
        val originalPayloadString = payloadBytes.toString(Charsets.UTF_8)
        assertEquals(originalPayloadString, decodedString)
    }

    @Test
    fun testEncodeDecodeWithLargePayload() {
        val file = File(this::class.java.getResource(treesFile).file)
        val carrierInputStream = FileInputStream(file)
        val payloadFile = File(this::class.java.getResource(largePayloadFile).file)
        val payloadBytes = FileUtils.readFileToByteArray(payloadFile)

        val encodedImageBytes = jpegDecoder.encode(IOUtils.toByteArray(carrierInputStream), payloadBytes)
        val decodedBytes = jpegDecoder.decode(encodedImageBytes)

        val decodedString = decodedBytes.toString(Charsets.UTF_8)
        val originalPayloadString = payloadBytes.toString(Charsets.UTF_8)
        assertEquals(originalPayloadString, decodedString)
    }

}
