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

import com.izalith.imagesecret.lib.image.jpeg.marker.Marker
import com.izalith.imagesecret.lib.image.jpeg.segments.Segment
import com.izalith.imagesecret.lib.image.jpeg.segments.SofNSegment


/**
 * Start of scan segment.
 * Includes scan header and scan body.
 *
 * Some variables' names and "magic numbers" are used only to match the procedures of the specification ITU-T.81
 */
@Suppress("MagicNumber")
class SosSegment(
    segmentHeaderBytes: ByteArray,
    marker: Marker,
    val segmentBodyBytes: ByteArray
) : Segment(segmentHeaderBytes, marker) {
    companion object {
        const val BLOCK_SIDE_SIZE = 8
    }

    val components: Array<SosComponent>
    private val startOfSpectralSelection: Int
    private val endOfSpectralSelection: Int
    private val successiveApproximationBitHigh: Int
    private val successiveApproximationBitLow: Int

    init {
        var bytesPos = 0
        val numberOfComponents = segmentHeaderBytes[bytesPos++].toInt()
        components = Array(numberOfComponents) {
            val scanComponentSelector = segmentHeaderBytes[bytesPos++].toInt()
            val acDcEntropyCodingTableSelector = segmentHeaderBytes[bytesPos++].toInt()

            val dcCodingTableSelector = acDcEntropyCodingTableSelector shr 4 and 0xf
            val acCodingTableSelector = acDcEntropyCodingTableSelector and 0xf

            SosComponent(
                scanComponentSelector,
                dcCodingTableSelector,
                acCodingTableSelector,
                0
            )
        }

        startOfSpectralSelection = segmentHeaderBytes[bytesPos++].toInt()
        endOfSpectralSelection = segmentHeaderBytes[bytesPos++].toInt()
        val successiveApproximationBitPosition = segmentHeaderBytes[bytesPos++].toInt()
        successiveApproximationBitHigh = successiveApproximationBitPosition shr 4 and 0xf
        successiveApproximationBitLow = successiveApproximationBitPosition and 0xf
    }

    class SosComponent(
        val scanComponentSelector: Int, val dcCodingTableSelector: Int, val acCodingTableSelector: Int,
        var prevDc: Int = 0
    )

    /**
     * Minimum coded unit: The smallest group of data units that is coded
     */
    class Mcu(val components: Array<McuComponent>)

    class McuComponent(
        width: Int,
        height: Int,
        val sosComponent: SosComponent,
        val sofComponent: SofNSegment.SofComponent
    ) {
        val samples: IntArray = IntArray(width * height)
    }
}
