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

import com.izalith.imagesecret.lib.image.jpeg.common.BitOutputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream


class BitOutputStreamTest {

    @Test
    fun testOneByte() {
        val jpegOutputStream =
            BitOutputStream(ByteArrayOutputStream())

        jpegOutputStream.write(15, 3) // 111
        jpegOutputStream.write(0, 5) // 00000

        val resultByteArray = jpegOutputStream.outputStream.toByteArray()
        assertEquals(1, resultByteArray.size)
        assertEquals("11100000".toInt(2), resultByteArray[0].toUByte().toInt())
    }

    @Test
    fun testByteStuffing() {
        val jpegOutputStream =
            BitOutputStream(ByteArrayOutputStream())

        jpegOutputStream.write(0xFF, 8)

        val resultByteArray = jpegOutputStream.outputStream.toByteArray()
        assertEquals(2, resultByteArray.size)
        assertEquals("11111111".toInt(2), resultByteArray[0].toUByte().toInt())
        assertEquals("00000000".toInt(2), resultByteArray[1].toUByte().toInt())
    }

    @Test
    fun testThreeBytes() {
        val jpegOutputStream =
            BitOutputStream(ByteArrayOutputStream())

        jpegOutputStream.write(15, 3)
        jpegOutputStream.write(0, 4)
        jpegOutputStream.write("110101010".toInt(2), 9)

        val resultByteArray = jpegOutputStream.outputStream.toByteArray()
        assertEquals(2, resultByteArray.size)
        assertEquals("11100001".toInt(2), resultByteArray[0].toUByte().toInt())
        assertEquals("10101010".toInt(2), resultByteArray[1].toUByte().toInt())
    }

    @Test
    fun testIncompleteByte() {
        val jpegOutputStream =
            BitOutputStream(ByteArrayOutputStream())

        jpegOutputStream.write(0, 3)
        jpegOutputStream.writeIncompleteByte()

        val resultByteArray = jpegOutputStream.outputStream.toByteArray()
        assertEquals(1, resultByteArray.size)
        assertEquals("00011111".toInt(2), resultByteArray[0].toUByte().toInt())
    }
}