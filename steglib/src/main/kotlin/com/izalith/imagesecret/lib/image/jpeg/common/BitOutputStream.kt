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
import java.io.ByteArrayOutputStream


class BitOutputStream(val outputStream: ByteArrayOutputStream) {
    companion object {
        private const val START_FREE_POS = 7
    }

    private val emptyByte = 0
    var currentByte = emptyByte
    var currentFreePos = START_FREE_POS
    var rowOnesCount = 0
    var byteCounter = outputStream.toByteArray().size


    /**
     * Write [bitsCount] bits from [value] to the [outputStream]
     */
    fun write(value: Int, bitsCount: Int) {
        for (i in (bitsCount - 1) downTo 0) {
            if ((value and (1 shl i)) > 0) {
                currentByte = currentByte or (1 shl (currentFreePos))
                rowOnesCount++
            } else {
                rowOnesCount = 0
            }
            if (currentFreePos == 0) {
                pushByteToStream()
            } else {
                currentFreePos--
            }
        }
    }

    private fun pushByteToStream() {
        outputStream.write(currentByte)
        byteCounter++
        writeByteStuffing()
        currentFreePos = START_FREE_POS
        currentByte = emptyByte
    }

    //P91 F.1.2.3 Byte stuffing
    private fun writeByteStuffing() {
        if (currentByte == Marker.markerFirstByteAsInt()) {
            outputStream.write(0)
            byteCounter++
        }
    }

    /**
     * Byte alignment is achieved by padding incomplete bytes with 1-bits.
     */
    fun writeIncompleteByte() {
        for (i in currentFreePos downTo 0) {
            currentByte = currentByte or (1 shl (i))
            rowOnesCount++
        }
        pushByteToStream()
    }
}
