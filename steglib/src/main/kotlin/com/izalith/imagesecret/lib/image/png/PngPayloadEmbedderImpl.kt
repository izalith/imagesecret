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

import com.google.common.primitives.Ints
import java.awt.image.BufferedImage
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class PngPayloadEmbedderImpl : PngPayloadEmbedder {
    companion object {
        private const val USED_BYTES_IN_RGBA_ELEM = 3
        private const val PAYLOAD_LEN_BITS_COUNT = 64
    }

    override fun findMaxPayloadSize(image: BufferedImage): Int {
        val rgbArray: IntArray = getRgbArray(image)
        return rgbArray.size * USED_BYTES_IN_RGBA_ELEM
    }

    /**
     * Write payload to the one last bit of every byte of RGB image representation.
     */
    override fun embed(image: BufferedImage, payload: ByteArray): BufferedImage {
        val rgbArray: IntArray = getRgbArray(image)

        val payloadBits: BitSet = BitSet.valueOf(payload)
        val payloadLen = payloadBits.size()
        val payloadLenBits = BitSet.valueOf(Ints.toByteArray(payloadLen))

        val stegRgbIntArrayWithLen = writePayloadToRgb(rgbArray, payloadLenBits, 0)
        val stegRgbIntArrayFull = writePayloadToRgb(stegRgbIntArrayWithLen, payloadBits, payloadLenBits.size())

        return convertToImage(image, stegRgbIntArrayFull)
    }

    override fun extract(image: BufferedImage): ByteArray {
        val rgbArray: IntArray = getRgbArray(image)
        val payloadLenBits = readBits(PAYLOAD_LEN_BITS_COUNT, rgbArray, 0)
        val payloadLen = Ints.fromByteArray(payloadLenBits.toByteArray())
        val payloadBits = readBits(payloadLen, rgbArray, payloadLenBits.size())
        return payloadBits.toByteArray()
    }

    /**
     * Write payload to last bit of R, G and B bytes.
     */
    private fun writePayloadToRgb(rgbArray: IntArray, payloadBits: BitSet, startPosition: Int): IntArray {
        var payloadOffset = 0
        val result = rgbArray.withIndex().map { rgbElem ->
            //r,g,b - 1,2 and 3 pixels of rgbBytes. 0 pixel - alpha
            val rgbBytes = Ints.toByteArray(rgbElem.value)
            for (rgbaByteIndex in 1..USED_BYTES_IN_RGBA_ELEM) {
                val rgbOffset = rgbElem.index * USED_BYTES_IN_RGBA_ELEM + rgbaByteIndex - 1
                if (rgbOffset >= startPosition && payloadOffset < payloadBits.size()) {
                    rgbBytes[rgbaByteIndex] = withBit(rgbBytes[rgbaByteIndex], payloadBits[payloadOffset++], 0)
                }
            }
            Ints.fromByteArray(rgbBytes)
        }
        return result.toIntArray()
    }

    private fun getRgbArray(image: BufferedImage) =
        image.getRGB(0, 0, image.width, image.height, null, 0, image.width)

    private fun convertToImage(originalImage: BufferedImage, encodedRgbIntArray: IntArray): BufferedImage {
        val image = BufferedImage(originalImage.width, originalImage.height, originalImage.type)
        image.setRGB(0, 0, image.width, image.height, encodedRgbIntArray, 0, image.width)
        return image
    }

    private fun withBit(byte: Byte, bit: Boolean, position: Int): Byte {
        val mask: Byte = (1 shl position).toByte()
        return if (bit) {
            byte or mask
        } else {
            byte and mask.inv()
        }
    }

    private fun readBits(nBits: Int, rgbArray: IntArray, startPosition: Int): BitSet {
        val bitSet = BitSet(nBits)
        var payloadOffset = 0
        rgbArray.withIndex().forEach { rgbElem ->
            val rgb = rgbElem.value
            val rgbBytes = Ints.toByteArray(rgb)
            for (rgbaByteIndex in 0 until USED_BYTES_IN_RGBA_ELEM) {
                val rgbOffset = rgbElem.index * USED_BYTES_IN_RGBA_ELEM + rgbaByteIndex
                if (rgbOffset >= startPosition && payloadOffset < bitSet.size()) {
                    bitSet[payloadOffset++] = getBit(rgbBytes[rgbaByteIndex + 1], 0)
                }
            }
        }
        return bitSet
    }

    private fun getBit(byte: Byte, position: Int): Boolean {
        val bitSet = BitSet.valueOf(byteArrayOf(byte))
        return bitSet.get(position)
    }
}
