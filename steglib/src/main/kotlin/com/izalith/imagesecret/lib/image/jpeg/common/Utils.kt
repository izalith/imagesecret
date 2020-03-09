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

package com.izalith.imagesecret.lib.image.jpeg.common

import com.izalith.imagesecret.lib.image.jpeg.marker.Marker
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import java.io.IOException
import java.io.InputStream

class Utils private constructor() {
    companion object {
        const val BITS_IN_BYTE: Int = 8

        fun readByte(inputStream: InputStream): Int {
            val result = inputStream.read()
            if (result < 0) {
                throw IOException("Unexpected end of file")
            }
            return result
        }

        fun readBytes(inputStream: InputStream, length: Int): ByteArray {
            return (0 until length).map {
                val nextByte = inputStream.read()
                if (nextByte < 0) {
                    throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Unexpected end of file")
                }
                nextByte.toByte()
            }.toByteArray()
        }

        fun readAllBytesToNextMarker(inputStream: InputStream): ByteArray {
            val bytes: MutableList<Byte> = mutableListOf()
            var currentByte =
                readByte(inputStream)
            do {
                val nextByte =
                    readByte(inputStream)
                if (Marker.isMarker(currentByte, nextByte)) {
                    break
                }
                bytes.add(currentByte.toByte())
                currentByte = nextByte
            } while (true)

            return bytes.toByteArray()
        }

        fun read2BytesInt(inputStream: InputStream): Int {
            val bytes =
                readBytes(inputStream, 2)
            val byte1 = bytes[0]
            val byte2 = bytes[1]
            return read2BytesInt(byte1, byte2)
        }

        fun read2BytesInt(byte1: Byte, byte2: Byte): Int {
            return (byte1.toUByte().toInt() shl BITS_IN_BYTE) or byte2.toUByte().toInt()
        }

        fun uByteArrayToIntArray(bytes: ByteArray): IntArray =
            bytes.toUByteArray().toIntArray()

    }

}

fun UByteArray.toIntArray(): IntArray {
    return this.map { it.toInt() }.toIntArray()
}
