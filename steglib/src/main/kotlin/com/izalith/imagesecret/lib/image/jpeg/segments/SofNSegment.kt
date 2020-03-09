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

import com.izalith.imagesecret.lib.image.jpeg.marker.Marker
import com.izalith.imagesecret.lib.image.jpeg.marker.MarkerEnum
import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.read2BytesInt
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException

/**
 * Define Huffman Table(s).
 * Specifies one or more Huffman tables.
 *
 * Some variables' names and "magic numbers" are used only to match the procedures of the specification ITU-T.81
 */
@Suppress("MagicNumber")
class SofNSegment(bytes: ByteArray, marker: Marker) : Segment(bytes, marker) {
    val precision: Int = bytes[0].toInt()
    val width: Int = read2BytesInt(bytes[3], bytes[4])
    val height: Int = read2BytesInt(bytes[1], bytes[2])
    val components: Array<SofComponent>

    init {
        if (MarkerEnum.SOF0 != marker) {
            throw StegoException(StegoErrorCode.ERROR_NOT_SUPPORTED_IMAGE_FORMAT,
                "Only non-differential, Huffman coding, baseline DCT is supported")
        }
        val numberOfComponents = bytes[5].toInt()
        var componentsStartPos = 6
        components = Array(numberOfComponents) {
            val componentIdentifier = bytes[componentsStartPos++].toInt()

            val hvSamplingFactors = bytes[componentsStartPos++].toInt()
            val horizontalSamplingFactor = hvSamplingFactors shr 4 and 0xf
            val verticalSamplingFactor = hvSamplingFactors and 0xf
            val quantTabDestSelector = bytes[componentsStartPos++].toInt()
            SofComponent(
                componentIdentifier,
                horizontalSamplingFactor,
                verticalSamplingFactor,
                quantTabDestSelector
            )
        }
        if (components.isEmpty()) {
            throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid JPEG: no components in sof0 segment")
        }
    }

    class SofComponent(
        val componentIdentifier: Int, val horizontalSamplingFactor: Int,
        val verticalSamplingFactor: Int, val quantTabDestSelector: Int
    )
}
