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

package com.izalith.imagesecret.lib.image.jpeg.segments.sos

import com.izalith.imagesecret.lib.image.jpeg.common.BitOutputStream
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment.Companion.BLOCK_SIDE_SIZE
import com.izalith.imagesecret.lib.image.jpeg.segments.DhtSegment
import com.izalith.imagesecret.lib.image.jpeg.segments.SegmentGroup
import java.io.ByteArrayOutputStream


/**
 * Some variables' names and "magic numbers" are used only to match the procedures of the specification ITU-T.81
 */
@Suppress("MagicNumber")
open class SosBodyWriter(segmentGroup: SegmentGroup) {
    private val acTables = segmentGroup.getAcDhtSegments().map { it.destinationId to it.huffmanTable }.toMap()
    private val dcTables = segmentGroup.getDcDhtSegments().map { it.destinationId to it.huffmanTable }.toMap()

    open fun writeSegmentSosBodyData(
        output: ByteArrayOutputStream,
        mcus: Array<SosSegment.Mcu>
    ) {
        writeMcu(mcus, BitOutputStream(output))
    }

    private fun writeMcu(mcus: Array<SosSegment.Mcu>, output: BitOutputStream) {
        val prevs = IntArray(mcus.first().components.size)
        mcus.forEach { mcu ->
            for ((componentIndex, component) in mcu.components.withIndex()) {
                for (y in 0 until component.sofComponent.verticalSamplingFactor) {
                    for (x in 0 until component.sofComponent.horizontalSamplingFactor) {
                        val block = calculateBlock(y, x, component)
                        writeBlock(component, prevs, componentIndex, output, block)
                    }
                }
            }
        }
        output.writeIncompleteByte()
    }

    private fun calculateBlock(
        y: Int,
        x: Int,
        component: SosSegment.McuComponent
    ): IntArray {
        var dstRowOffset =
            (BLOCK_SIDE_SIZE * y * BLOCK_SIDE_SIZE * component.sofComponent.horizontalSamplingFactor) +
                    BLOCK_SIDE_SIZE * x
        var srcNext = 0
        val block = IntArray(BLOCK_SIDE_SIZE * BLOCK_SIDE_SIZE)
        for (yy in 0 until BLOCK_SIDE_SIZE) {
            for (xx in 0 until BLOCK_SIDE_SIZE) {
                block[srcNext++] = component.samples[dstRowOffset + xx]
            }
            dstRowOffset += BLOCK_SIDE_SIZE * component.sofComponent.horizontalSamplingFactor
        }
        return block
    }

    /**
     * ITU-T.81 section F.1.2.2.3, page 91, procedure "CSIZE"
     */
    private fun sizeOfBits(value: Int): Int {
        var temp = value
        if (temp < 0) {
            temp = -value
        }
        var nbits = 0
        while (temp != 0) {
            nbits++
            temp = temp shr 1
        }
        return nbits
    }

    private fun writeBlock(
        component: SosSegment.McuComponent,
        prevs: IntArray,
        componentIndex: Int,
        output: BitOutputStream,
        block: IntArray
    ) {
        val dcHuffmanTable: DhtSegment.HuffmanTable =
            dcTables.getValue(component.sosComponent.dcCodingTableSelector)
        val acHuffmanTable: DhtSegment.HuffmanTable =
            acTables.getValue(component.sosComponent.acCodingTableSelector)

        //ITU-T.81 section F.1.2.1.3, page 89, Huffman encoding procedures for DC coefficients
        val dcDiff = block[0] - prevs[componentIndex]

        val nBits = sizeOfBits(dcDiff)
        val code = dcHuffmanTable.ehufco[nBits]
        val size = dcHuffmanTable.ehufsi[nBits]
        output.write(code, size)

        var dcDiffValue = dcDiff
        if (dcDiff < 0) {
            dcDiffValue--
        }
        output.write(dcDiffValue, nBits)
        prevs[componentIndex] = block[0]

        //ac encoding
        //ITU-T.81 section F.1.4, figure F.2, page 92, procedure "Encode_AC_coefficients"
        val zz = ZigZag.blockToZigZag(block)
        var r = 0
        for (k in 1 until zz.size) {
            if (zz[k] == 0) {
                if (k == 63) {
                    val code1 = acHuffmanTable.ehufco[0x00]
                    val size1 = acHuffmanTable.ehufsi[0x00]
                    output.write(code1, size1)
                } else {
                    r++
                }
            } else {
                while (r > 15) {
                    val code1 = acHuffmanTable.ehufco[0xF0]
                    val size1 = acHuffmanTable.ehufsi[0xF0]
                    output.write(code1, size1)
                    r -= 16
                }

                //ITU-T.81 section F.1.4, figure F.3, page 93, procedure "Encode_R,ZZ(k)"
                val ac: Int = if (zz[k] > 0) zz[k] else zz[k] - 1

                //csize
                val zzs = if (zz[k] > 0) zz[k] else -zz[k]

                val ssss = sizeOfBits(zzs)
                val rs = (r * 16) + ssss
                val code1 = acHuffmanTable.ehufco[rs]
                val size1 = acHuffmanTable.ehufsi[rs]
                output.write(code1, size1)

                output.write(ac, ssss)
                r = 0
            }
        }
    }
}
