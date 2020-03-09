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

package com.izalith.imagesecret.lib.image.jpeg.marker

import com.izalith.imagesecret.lib.image.jpeg.common.Utils
import com.izalith.imagesecret.lib.image.jpeg.common.toIntArray
import java.io.InputStream

interface Marker {
    val name: String
    val bytes: UByteArray

    fun bytesAsInts(): IntArray = bytes.toIntArray()

    companion object {
        const val MARKER_FIRST_BYTE: UByte = 0xFFu
        private const val MARKER_STUFFING = 0x00

        fun findMarker(first: Int, second: Int): Marker {
            val marker: Marker = MarkerEnum.values().find {
                it.bytes.size == 2 && it.bytes[0] == first.toUByte() && it.bytes[1] == second.toUByte()
            }
                ?: UnknownMarker(
                    "unknown",
                    ubyteArrayOf(first.toUByte(), second.toUByte())
                )
            return marker
        }

        fun isMarker(first: Int, second: Int): Boolean {
            return first == markerFirstByteAsInt() && second != markerFirstByteAsInt() && second != MARKER_STUFFING
        }

        fun readNextMarker(inputStream: InputStream): Marker {
            val markerBytes = IntArray(2)
            do {
                markerBytes[0] = markerBytes[1]
                markerBytes[1] =
                    Utils.readByte(inputStream)
            } while (!isMarker(
                    markerBytes[0],
                    markerBytes[1]
                )
            )
            return findMarker(
                markerBytes[0],
                markerBytes[1]
            )
        }

        fun markerFirstByteAsInt() : Int = MARKER_FIRST_BYTE.toInt()
    }
}
