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

import com.izalith.imagesecret.lib.image.jpeg.common.BitInputStream
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment.Companion.BLOCK_SIDE_SIZE
import com.izalith.imagesecret.lib.image.jpeg.segments.DhtSegment
import com.izalith.imagesecret.lib.image.jpeg.segments.SegmentGroup
import com.izalith.imagesecret.lib.image.jpeg.segments.SofNSegment
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException

/**
 * Some variables' names and "magic numbers" are used only to match the procedures of the specification ITU-T.81
 */
@Suppress("MagicNumber")
class SosBodyReader(segmentGroup: SegmentGroup) {

    private val acTables = segmentGroup.getAcDhtSegments().map { it.destinationId to it.huffmanTable }.toMap()
    private val dcTables = segmentGroup.getDcDhtSegments().map { it.destinationId to it.huffmanTable }.toMap()
    private val sosSegment = segmentGroup.getSosSegment()
    private val sofnSegment = segmentGroup.getSofnSegment()

    fun readSos(): Array<SosSegment.Mcu> {
        val hMax = sofnSegment.components.map { it.horizontalSamplingFactor }.max() ?: 0
        val vMax = sofnSegment.components.map { it.verticalSamplingFactor }.max() ?: 0
        val hSize = BLOCK_SIDE_SIZE * hMax
        val vSize = BLOCK_SIDE_SIZE * vMax
        val xMCUs = (sofnSegment.width + hSize - 1) / hSize
        val yMCUs = (sofnSegment.height + vSize - 1) / vSize
        val bitInputStream =
            BitInputStream.createFromByteArray(sosSegment.segmentBodyBytes)

        val result: MutableList<SosSegment.Mcu> = mutableListOf()
        repeat(yMCUs * xMCUs) {
            result.add(readMCU(bitInputStream))
        }
        return result.toTypedArray()
    }

    private fun readMCU(inputStream: BitInputStream): SosSegment.Mcu {
        val mcuArray = Array(sosSegment.components.size) { i ->
            readMcuComponent(inputStream, sosSegment.components[i])
        }
        return SosSegment.Mcu(mcuArray)
    }

    private fun readMcuComponent(
        inputStream: BitInputStream,
        scanComponent: SosSegment.SosComponent
    ): SosSegment.McuComponent {
        val frameComponent = findFrameComponent(scanComponent)
        val fullBlock = SosSegment.McuComponent(
            BLOCK_SIDE_SIZE * frameComponent.horizontalSamplingFactor,
            BLOCK_SIDE_SIZE * frameComponent.verticalSamplingFactor, scanComponent, frameComponent
        )
        for (y in 0 until frameComponent.verticalSamplingFactor) {
            for (x in 0 until frameComponent.horizontalSamplingFactor) {
                val zz = IntArray(BLOCK_SIDE_SIZE * BLOCK_SIDE_SIZE)
                // ITU-T.81 page 104
                val t = decode(
                    inputStream,
                    dcTables.getValue(scanComponent.dcCodingTableSelector)
                )
                var diff = receive(t, inputStream)
                diff = extend(diff, t)
                zz[0] = scanComponent.prevDc + diff
                scanComponent.prevDc = zz[0]

                decodeAcCoefficients(inputStream, scanComponent, zz)

                //Don't perform quantization and DCT
                val blockInt = ZigZag.zigZagToBlock(zz)
                var dstRowOffset =
                    (BLOCK_SIDE_SIZE * y * BLOCK_SIDE_SIZE * frameComponent.horizontalSamplingFactor) +
                            BLOCK_SIDE_SIZE * x
                var srcNext = 0
                for (yy in 0 until BLOCK_SIDE_SIZE) {
                    for (xx in 0 until BLOCK_SIDE_SIZE) {
                        val sample = blockInt[srcNext++]
                        fullBlock.samples[dstRowOffset + xx] = sample
                    }
                    dstRowOffset += BLOCK_SIDE_SIZE * frameComponent.horizontalSamplingFactor
                }
            }
        }
        return fullBlock
    }

    private fun findFrameComponent(scanComponent: SosSegment.SosComponent): SofNSegment.SofComponent {
        for (j in sofnSegment.components.indices) {
            if (sofnSegment.components[j].componentIdentifier == scanComponent.scanComponentSelector) {
                return sofnSegment.components[j]
            }
        }
        throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid component")
    }

    /**
     * ITU-T.81 section F.2.2.2, figure F.13, page 106, procedure "Decode_AC_coefficients"
     */
    private fun decodeAcCoefficients(
        inputStream: BitInputStream,
        scanComponent: SosSegment.SosComponent,
        zz: IntArray
    ) {
        var k = 1
        while (true) {
            val rs = decode(
                inputStream,
                acTables.getValue(scanComponent.acCodingTableSelector)
            )
            val ssss = rs and 0xf
            val rrrr = rs shr 4

            if (ssss == 0) {
                if (rrrr == 15) {
                    k += 16
                } else {
                    break
                }
            } else {
                k += rrrr

                //ITU-T.81 section F.2.2.2, figure F.14, page 107, procedure "Decode_ZZ(k)"
                zz[k] = receive(ssss, inputStream)
                zz[k] = extend(zz[k], ssss)

                if (k == 63) {
                    break
                } else {
                    k++
                }
            }
        }
    }

    /**
     * ITU-T.81 section F.2.2.1, figure F.12, page 105, procedure "EXTEND"
     * EXTEND is a procedure which converts the partially decoded DIFF value of precision T to the full precision
     * difference
     */
    private fun extend(value: Int, t: Int): Int {
        var v = value
        var vt = 1 shl t - 1
        if (v < vt) {
            vt = (-1 shl t) + 1
            v += vt
        }
        return v
    }

    /**
     * ITU-T.81 section F.2.2.4, figure F.17, page 110, procedure "RECEIVE"
     * The procedure which places the next T bits of the serial bit string into the low order bits of DIFF, MSB first
     */
    private fun receive(ssss: Int, inputStream: BitInputStream): Int {
        var i = 0
        var v = 0
        while (i != ssss) {
            i++
            v = (v shl 1) + inputStream.nextBit()
        }
        return v
    }

    /**
     * ITU-T.81 section F.2.2.3, figure F.16, page 109, procedure "EXTEND"
     * The procedure which returns the 8-bit value associated with the next Huffman code in the compressed image data
     */
    private fun decode(inputStream: BitInputStream, huffmanTable: DhtSegment.HuffmanTable): Int {
        var i = 1
        var code = inputStream.nextBit()
        while (code > huffmanTable.maxCode[i]) {
            i++
            code = code shl 1 or inputStream.nextBit()
        }
        var j = huffmanTable.valPtr[i]
        j += code - huffmanTable.minCode[i]
        return huffmanTable.huffVals[j]
    }

}
