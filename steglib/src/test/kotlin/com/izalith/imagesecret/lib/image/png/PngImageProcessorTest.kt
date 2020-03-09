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

package com.izalith.imagesecret.lib.image.png

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.io.File

class PngImageProcessorTest {

    private val payload = "enconding text"
    private val carrierImage = "/img/landscape_orig.png"
    private val encodedImage = "/img/landscape_encoded.png"

    @Test
    fun testDecode() {
        val decoder = PngImageProcessor()
        val image = FileUtils.readFileToByteArray(File(this::class.java.getResource(encodedImage).file))

        val resultBinaryData = decoder.decode(image)

        val decodedString = resultBinaryData.toString(Charsets.UTF_8)
        Assertions.assertEquals(payload, decodedString)
    }

    @Test
    fun testEncodeDecode() {
        val encoder = PngImageProcessor()
        val payloadBytes = payload.toByteArray(Charsets.UTF_8)

        val carrierImageBytes = FileUtils.readFileToByteArray(File(this::class.java.getResource(carrierImage).file))

        val encodedImage = encoder.encode(carrierImageBytes, payloadBytes)
        val decodedBinaryData = encoder.decode(encodedImage)

        val decodedString = decodedBinaryData.toString(Charsets.UTF_8)
        Assertions.assertEquals(payload, decodedString)
    }
}
