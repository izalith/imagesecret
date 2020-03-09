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

import com.izalith.imagesecret.lib.image.jpeg.payload.CoefficientIterator
import com.izalith.imagesecret.lib.image.jpeg.payload.JpegPayloadEmbedder
import com.izalith.imagesecret.lib.image.jpeg.payload.McuCoefficientIterator
import com.izalith.imagesecret.lib.image.jpeg.segments.SofNSegment
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosBodyFactory
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream


class JpegImageProcessorTest {

    private val faviconBlock1Samples = intArrayOf(
        2, 0, 3, 0, 0, 0, 0, 0, -2, 1, 1, 1, 0, 0, 0, 0,
        0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
        0, -1, -1, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0,
        1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        3, -1, 1, 0, 0, 0, 0, 0, -1, 2, 2, 1, 0, 0, 0, 0,
        -1, -2, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0, 0,
        0, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0,
        -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    )

    private val faviconBlock2Samples = intArrayOf(
        -1, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
    )

    private val faviconBlock3Samples = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        1, -1, 0, 0, 0, 0, 0, 0,
        1, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
    )

    private val landscapeBlock1Samples = intArrayOf(
        251, 4, -4, 0, 0, 0, 0, 0,
        5, 0, 0, -1, 0, 0, 0, 0,
        0, 1, 0, 0, 0, 0, 0, 0,
        1, -2, -1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        1, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
    )

    private val faviconFile = "/img/favicon.jpg"
    private val landscapeFile = "/img/landscape_orig.jpg"

    @Test
    fun decodeFavicon() {
        val scanComponent = SosSegment.SosComponent(1, 1, 1)
        val frameComponent = SofNSegment.SofComponent(1, 1, 1, 1)

        val faviconBlock1 = SosSegment.McuComponent(
            16,
            16,
            scanComponent,
            frameComponent
        )
        faviconBlock1.samples.indices.forEach {
            faviconBlock1.samples[it] = faviconBlock1Samples[it]
        }

        val faviconBlock2 = SosSegment.McuComponent(
            8,
            8,
            scanComponent,
            frameComponent
        )
        faviconBlock2.samples.indices.forEach {
            faviconBlock2.samples[it] = faviconBlock2Samples[it]
        }

        val faviconBlock3 = SosSegment.McuComponent(
            8,
            8,
            scanComponent,
            frameComponent
        )
        faviconBlock3.samples.indices.forEach {
            faviconBlock3.samples[it] = faviconBlock3Samples[it]
        }

        val file = File(this::class.java.getResource(faviconFile).file)
        val inputStream = FileInputStream(file)
        val jpegDecoder = JpegImageProcessor(
            TestCoefficientAnalyzer(
                arrayOf(
                    SosSegment.Mcu(
                        arrayOf(
                            faviconBlock1,
                            faviconBlock2,
                            faviconBlock3
                        )
                    )
                )
            ), SosBodyFactory()
        )

        jpegDecoder.decode(IOUtils.toByteArray(inputStream))
    }

    @Test
    fun decodeLandscape() {
        val scanComponent = SosSegment.SosComponent(1, 1, 1)
        val frameComponent = SofNSegment.SofComponent(1, 1, 1, 1)
        val landscapeBlock1 = SosSegment.McuComponent(8, 8, scanComponent, frameComponent)

        landscapeBlock1.samples.indices.forEach {
            landscapeBlock1.samples[it] = landscapeBlock1Samples[it]
        }

        val carrierImage = File(this::class.java.getResource(landscapeFile).file)
        val inputStream = FileInputStream(carrierImage)
        val jpegDecoder = JpegImageProcessor(
            TestCoefficientAnalyzer(
                arrayOf(
                    SosSegment.Mcu(arrayOf(landscapeBlock1))
                )
            ), SosBodyFactory()
        )

        jpegDecoder.decode(IOUtils.toByteArray(inputStream))
    }

    class TestCoefficientAnalyzer(private val expectedMcus: Array<SosSegment.Mcu>) :
        JpegPayloadEmbedder {
        override fun embed(iterator: CoefficientIterator, message: ByteArray) {
            throw NotImplementedError()
        }

        override fun findMaxPayloadSize(iterator: CoefficientIterator): Int {
            throw NotImplementedError()
        }

        override fun extract(iterator: CoefficientIterator): ByteArray {
            val expectedMcuWrapper = McuCoefficientIterator.build(expectedMcus)
            while (expectedMcuWrapper.hasNext()) {
                assertEquals(expectedMcuWrapper.next(), iterator.next())
            }
            return byteArrayOf()
        }
    }
}
