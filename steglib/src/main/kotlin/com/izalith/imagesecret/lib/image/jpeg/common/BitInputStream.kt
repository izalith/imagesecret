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
import com.izalith.imagesecret.lib.image.jpeg.marker.MarkerEnum
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import java.io.ByteArrayInputStream
import java.io.InputStream

class BitInputStream(private val inputStream: InputStream) {

    private var readBitsCount: Int = 0
    private var currentByte: Int = 0

    fun nextBit(): Int {
        if (readBitsCount == 0) {
            currentByte = nextByte()
            readBitsCount = Byte.SIZE_BITS
            if (currentByte == Marker.markerFirstByteAsInt()) {
                skipByteStuffing()
            }
        }
        val bit = currentByte shr 7 and 0x1
        readBitsCount--
        currentByte = currentByte shl 1
        return bit
    }

    /**
     * skip for byte stuffing purpose.
     * a zero byte into the entropy-coded segment following the generation of an encoded hexadecimal X’FF’ byte.
     */
    private fun skipByteStuffing() {
        val b2 = nextByte()
        if (b2 != 0) {
            if (b2 == MarkerEnum.DNL.bytesAsInts()[1]) {
                throw StegoException(StegoErrorCode.ERROR_NOT_SUPPORTED_IMAGE_FORMAT, "DNL not supported")
            }
            throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE,
                "Invalid marker found ${currentByte.toString(HEX_RADIX)}${b2.toString(HEX_RADIX)} " +
                        "in entropy data"
            )
        }
    }

    private fun nextByte(): Int {
        val nextByte = inputStream.read()
        if (nextByte < 0) {
            throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Unexpected End of File")
        }
        return nextByte
    }

    companion object {
        const val HEX_RADIX = 16

        fun createFromByteArray(byteArray: ByteArray): BitInputStream {
            return BitInputStream(
                ByteArrayInputStream(
                    byteArray
                )
            )
        }
    }
}
