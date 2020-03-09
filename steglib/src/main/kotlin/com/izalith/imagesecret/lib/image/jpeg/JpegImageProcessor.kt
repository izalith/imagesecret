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

package com.izalith.imagesecret.lib.image.jpeg

import com.izalith.imagesecret.lib.image.ImageProcessor
import com.izalith.imagesecret.lib.image.jpeg.marker.MarkerEnum
import com.izalith.imagesecret.lib.image.jpeg.payload.CoefficientIterator
import com.izalith.imagesecret.lib.image.jpeg.payload.JpegPayloadEmbedder
import com.izalith.imagesecret.lib.image.jpeg.payload.JpegPayloadEmbedderImpl
import com.izalith.imagesecret.lib.image.jpeg.payload.McuCoefficientIterator
import com.izalith.imagesecret.lib.image.jpeg.segments.Segment
import com.izalith.imagesecret.lib.image.jpeg.segments.SegmentGroup
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosBodyFactory
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class JpegImageProcessor(
    private val payloadEmbedder: JpegPayloadEmbedder,
    private val sosBodyFactory: SosBodyFactory
) : ImageProcessor {

    constructor() : this(
        JpegPayloadEmbedderImpl(),
        SosBodyFactory()
    )

    override fun encode(carrier: ByteArray, payload: ByteArray): ByteArray {
        val byteArrayOutputStream = encode(ByteArrayInputStream(carrier), payload)
        return byteArrayOutputStream.toByteArray()
    }

    override fun decode(carrier: ByteArray): ByteArray {
        return decode(ByteArrayInputStream(carrier))
    }

    override fun maxPayloadSize(carrier: ByteArray): Int {
        return getAvailableSpaceSize(ByteArrayInputStream(carrier))
    }

    private fun decode(inputStream: InputStream): ByteArray {
        val segments = Segment.readSegments(inputStream)
        val coefficients = readCoefficients(segments)
        return payloadEmbedder.extract(
            McuCoefficientIterator.build(coefficients)
        )
    }

    private fun getAvailableSpaceSize(inputStream: InputStream): Int {
        val segments: SegmentGroup = Segment.readSegments(inputStream)
        val coefficients = readCoefficients(segments)
        return payloadEmbedder.findMaxPayloadSize(
            McuCoefficientIterator.build(
                coefficients
            )
        )
    }

    private fun encode(
        carrierInputStream: InputStream,
        payload: ByteArray
    ): ByteArrayOutputStream {
        val segmentGroup: SegmentGroup = Segment.readSegments(carrierInputStream)
        val coefficients = readCoefficients(segmentGroup)
        checkAvailableSpace(
            McuCoefficientIterator.build(coefficients), payload.size
        )
        payloadEmbedder.embed(
            McuCoefficientIterator.build(coefficients), payload
        )
        return buildJpegFileOutputStream(coefficients, segmentGroup)
    }

    private fun readCoefficients(segmentGroup: SegmentGroup): Array<SosSegment.Mcu> {
        return sosBodyFactory.buildReader(segmentGroup).readSos()
    }

    private fun checkAvailableSpace(samples: CoefficientIterator, messageSize: Int) {
        val maxEncodedSize = payloadEmbedder.findMaxPayloadSize(samples)
        if (maxEncodedSize < messageSize) {
            throw StegoException(
                StegoErrorCode.ERROR_NOT_ENOUGH_SPACE_FOR_PAYLOAD,
                "Not enough space in the image. Required: $messageSize bytes, " +
                        "available: $maxEncodedSize bytes"
            )
        }
    }

    private fun buildJpegFileOutputStream(
        mcus: Array<SosSegment.Mcu>,
        segmentGroup: SegmentGroup
    ): ByteArrayOutputStream {
        val output = ByteArrayOutputStream()
        segmentGroup.segments.forEach { segment ->
            if (segment.marker != MarkerEnum.SOS) {
                segment.write(output)
            } else {
                segment.write(output)
                sosBodyFactory.buildWriter(segmentGroup).writeSegmentSosBodyData(output, mcus)
            }
        }
        return output
    }
}
