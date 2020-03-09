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
import com.izalith.imagesecret.lib.image.jpeg.marker.MarkerEnum.*
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment
import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.read2BytesInt
import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.readAllBytesToNextMarker
import com.izalith.imagesecret.lib.image.jpeg.common.Utils.Companion.readBytes
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception

open class Segment(val bytes: ByteArray, val marker: Marker) {
    companion object {
        fun read(inputStream: InputStream, marker: Marker): Segment {
            try {
                return when (marker) {
                    SOI -> Segment(byteArrayOf(), marker)
                    EOI -> Segment(byteArrayOf(), marker)
                    SOF0, SOF1, SOF2, SOF3, SOF5, SOF6, SOF7, SOF8, SOF9, SOF10, SOF11, SOF12, SOF13, SOF14, SOF15
                    -> SofNSegment(
                        readBytes(inputStream), marker
                    )
                    DHT -> DhtSegment(
                        readBytes(inputStream), marker
                    )
                    SOS -> SosSegment(
                        readBytes(inputStream), marker, readAllBytesToNextMarker(inputStream)
                    )
                    else -> Segment(
                        readBytes(inputStream), marker
                    )
                }
            } catch (e: Exception) {
                throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Error reading $marker", e)
            }
        }

        fun readSegments(inputStream: InputStream): SegmentGroup {
            val segments: MutableList<Segment> = mutableListOf()
            while (true) {
                val marker: Marker =
                    Marker.readNextMarker(inputStream)
                val segment = read(
                    inputStream,
                    marker
                )
                if (segments.isEmpty()) {
                    checkFirstSegment(
                        segment
                    )
                }
                segments.add(segment)
                if (segment.marker == SOS) {
                    segments.add(
                        read(
                            inputStream,
                            EOI
                        )
                    )
                    break
                }
            }
            return SegmentGroup(segments)
        }

        private fun checkFirstSegment(segment: Segment) {
            if (segment.marker != SOI) {
                throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid JPEG file")
            }
        }

        private fun readBytes(inputStream: InputStream): ByteArray {
            val length =
                readLength(inputStream)
            return readBytes(inputStream, length)
        }

        private fun readLength(inputStream: InputStream): Int {
            val fullLength = read2BytesInt(inputStream)
            if (fullLength < 2) {
                throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid segment")
            }
            return fullLength - 2
        }

    }

    fun write(output: ByteArrayOutputStream) {
        output.write(marker.bytes.toByteArray())
        //length includes size of length itself
        val length: Int = bytes.size + 2
        if (length > 2) {
            val lengthArray = ByteArray(2)
            lengthArray[1] = length.toByte()
            lengthArray[0] = (length shr 8).toByte()
            output.write(lengthArray)
            output.write(bytes)
        }
    }

}
