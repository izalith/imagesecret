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

package com.izalith.imagesecret.lib.image.jpeg.payload

import com.google.common.primitives.Longs
import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.BITS_IN_BYTE
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import java.util.*


class JpegPayloadEmbedderImpl : JpegPayloadEmbedder {
    companion object {
        private const val PAYLOAD_LEN_BITS = 64
    }

    override fun extract(iterator: CoefficientIterator): ByteArray {
        val payloadLenBits = BitSet(PAYLOAD_LEN_BITS)
        readToBitSet(payloadLenBits, iterator)
        val payloadLen = Longs.fromByteArray(payloadLenBits.toByteArray())
        val payloadBits = BitSet(payloadLen.toInt())
        readToBitSet(payloadBits, iterator)

        return payloadBits.toByteArray()
    }

    /**
     * Get size in bits of data, possible to embed into the image.
     */
    override fun findMaxPayloadSize(iterator: CoefficientIterator): Int {
        var resultBits = 0
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (isContainerValue(next)) {
                resultBits += 2
            }
        }
        val maxPayloadSize = (resultBits - PAYLOAD_LEN_BITS) / BITS_IN_BYTE
        return if (maxPayloadSize > 0) maxPayloadSize else 0
    }


    override fun embed(iterator: CoefficientIterator, message: ByteArray) {
        if (message.isEmpty()) {
            return
        }
        val payloadBits: BitSet = BitSet.valueOf(message)
        val payloadLen = payloadBits.size().toLong()
        val resultMessage = Longs.toByteArray(payloadLen) + message

        val resultPayloadBits = BitSet.valueOf(resultMessage)
        writePayloadToCoefficient(iterator, resultPayloadBits)
    }


    /**
     * Write 2 bits of each appropriate coefficient into bitSet.
     */
    private fun writePayloadToCoefficient(coeffIt: CoefficientIterator, payloadBits: BitSet): Int {
        var payloadOffset = 0

        while (coeffIt.hasNext()) {
            if (payloadOffset == payloadBits.size()) {
                return payloadOffset
            }
            val sampleValue = coeffIt.next()
            if (isContainerValue(sampleValue)) {
                var newValue = (sampleValue shr 2) shl 2

                if (payloadBits[payloadOffset++]) {
                    newValue = newValue or 1
                }
                if (payloadBits[payloadOffset++]) {
                    newValue = newValue or (1 shl 1)
                }
                coeffIt.set(newValue)
            }
        }
        return payloadOffset
    }

    /**
     * Read 2 bits from each appropriate coefficient into bitSet.
     */
    private fun readToBitSet(bitSet: BitSet, coeffIt: CoefficientIterator) {
        var payloadOffset = 0
        while (coeffIt.hasNext()) {
            if (payloadOffset >= bitSet.size()) {
                return
            }
            val coeff = coeffIt.next()
            if (isContainerValue(coeff)) {
                bitSet[payloadOffset++] = (coeff and 1) != 0
                if (payloadOffset >= bitSet.size()) {
                    return
                }
                bitSet[payloadOffset++] = (coeff and (1 shl 1)) != 0
            }
        }
        if (payloadOffset < bitSet.size()) {
            throw StegoException(StegoErrorCode.ERROR_PAYLOAD_READ)
        }
    }

    /**
     * Check if it is possible to store data in this coefficient.
     * The value after modification of 2 bits must not consist of only ones or only zeroes.
     */
    private fun isContainerValue(value: Int): Boolean {
        return value <= -5 || value >= 4
    }
}
