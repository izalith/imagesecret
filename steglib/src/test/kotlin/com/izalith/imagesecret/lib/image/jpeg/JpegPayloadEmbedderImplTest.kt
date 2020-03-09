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

import com.izalith.imagesecret.lib.image.jpeg.payload.CoefficientIterator
import com.izalith.imagesecret.lib.image.jpeg.payload.JpegPayloadEmbedderImpl
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream


class JpegPayloadEmbedderImplTest {
    private val faviconFile = "/img/favicon.jpg"
    private val message = "encoded"

    @Test
    fun testWriteRead() {
        val processor = JpegPayloadEmbedderImpl()

        val carrierImageFile = File(this::class.java.getResource(faviconFile).file)
        val carrierBytes = IOUtils.toByteArray(FileInputStream(carrierImageFile))
        val carrierIntArray = carrierBytes.map { it.toInt() }.toIntArray()
        val payloadBytes = message.toByteArray(Charsets.UTF_8)

        processor.embed(IntArrayIterator(carrierIntArray), payloadBytes)
        val decodedBytes = processor.extract(IntArrayIterator(carrierIntArray))

        val decodedString = decodedBytes.toString(Charsets.UTF_8)
        assertEquals(message, decodedString)
    }

    class IntArrayIterator(val array: IntArray):
        CoefficientIterator {
        var position: Int = -1

        override fun next(): Int {
            position++
            return array[position]
        }

        override fun hasNext(): Boolean {
            return position < array.size-1
        }

        override fun set(element: Int) {
            array[position] = element
        }
    }
}