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

package com.izalith.imagesecret.lib.image.jpeg.segments

import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.uByteArrayToIntArray
import com.izalith.imagesecret.lib.image.jpeg.marker.Marker
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException

/**
 * Define Huffman Table.
 * Specifies one or more Huffman tables.
 *
 * Some variables' names and "magic numbers" are used only to match the procedures of specification ITU-T.81
 */
@Suppress("MagicNumber")
class DhtSegment(bytes: ByteArray, marker: Marker) :
    Segment(bytes, marker) {
    val tableClass: Int
    val destinationId: Int
    val huffmanTable: HuffmanTable

    init {
        val lengthEnd = 16
        if (bytes.size < lengthEnd) {
            throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid JPEG file!")
        }
        val tableClassAndDestinationId = bytes[0]
        tableClass = tableClassAndDestinationId.toInt() shr 4 and 0x0F
        destinationId = tableClassAndDestinationId.toInt() and 0x0F
        val huffmanCodesLengths: IntArray = uByteArrayToIntArray(bytes.sliceArray(1..lengthEnd))
        val huffLengthsSum = huffmanCodesLengths.sum()
        if (lengthEnd + huffLengthsSum < 16) {
            throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid JPEG file!")
        }
        val valuesStart = lengthEnd + 1
        val huffVals: IntArray = uByteArrayToIntArray(
            bytes.sliceArray(valuesStart until (valuesStart + huffLengthsSum))
        )

        huffmanTable = HuffmanTable(
            huffVals,
            huffmanCodesLengths
        )
    }

    fun isDcTableClass(): Boolean {
        return tableClass == 0
    }

    class HuffmanTable(val huffVals: IntArray, huffmanCodesLengths: IntArray) {
        val ehufsi = IntArray(16 * 256)
        val ehufco = IntArray(16 * 256)
        val minCode = IntArray(1 + 16) // 1-based
        val maxCode = IntArray(1 + 16) // 1-based
        val valPtr = IntArray(1 + 16) // 1-based

        init {
            val huffSize = IntArray(16 * 256)

            //ITU-T.81 section C.2, figure C.1, page 51, procedure "generate_size_table"
            var k = 0
            var i = 1
            var j = 1
            val lastK: Int
            while (true) {
                if (j > huffmanCodesLengths[i - 1]) {
                    i++
                    j = 1
                    if (i > 16) {
                        huffSize[k] = 0
                        lastK = k
                        break
                    }
                } else {
                    huffSize[k] = i
                    k++
                    j++
                }
            }

            //ITU-T.81 section C.2, figure C.2, page 52, procedure "generate_code_table"
            k = 0
            var code = 0
            var si = huffSize[0]
            val huffCode = IntArray(lastK)

            while (true) {
                if (k >= lastK) {
                    break
                }
                huffCode[k] = code
                code++
                k++

                if (huffSize[k] == si) {
                    continue
                }
                if (huffSize[k] == 0) {
                    break
                }
                do {
                    code = code shl 1
                    si++
                } while (huffSize[k] != si)
            }

            //ITU-T.81 section F.2.2.3, figure F.15, page 108, procedure "Decoder_tables"
            i = 0
            j = 0
            while (true) {
                i++
                if (i > 16) {
                    break
                }
                if (huffmanCodesLengths[i - 1] == 0) {
                    maxCode[i] = -1
                } else {
                    valPtr[i] = j
                    minCode[i] = huffCode[j]
                    j += huffmanCodesLengths[i - 1] - 1
                    maxCode[i] = huffCode[j]
                    j++
                }
            }

            //ITU-T.81 section C.2, figure C.3, page 53, procedure "Order_codes"
            for (k1 in 0 until lastK) {
                val i1 = huffVals[k1]
                ehufco[i1] = huffCode[k1]
                ehufsi[i1] = huffSize[k1]
            }
        }

    }

}
