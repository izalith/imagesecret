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

import com.izalith.imagesecret.lib.image.jpeg.common.Utils
import com.izalith.imagesecret.lib.image.jpeg.marker.MarkerEnum.*
import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.read2BytesInt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream



class UtilsTest {
    @Test
    fun testRead2BytesInt() {
        val a = "1".toByte(16)
        val b = "40".toByte(16)
        val read2BytesInt = read2BytesInt(a, b)
        assertEquals(320, read2BytesInt)
    }

    @Test
    fun testReadAllBytesToNextMarker() {
        val textToRead = byteArrayOf(0xFa.toByte(), 0x01.toByte(), 0x02.toByte(), 0xC9.toByte())
        val markerByteArray = EOI.bytes.map { it.toByte() }.toByteArray()
        val textToSkip = byteArrayOf(0x01.toByte(), 0x01.toByte())
        val fullArray = textToRead + markerByteArray + textToSkip

        val bis = ByteArrayInputStream(fullArray)

        val readBytes = Utils.readAllBytesToNextMarker(bis)

        for (i in readBytes.indices) {
            assertEquals(textToRead[i], readBytes[i])
        }
    }
}