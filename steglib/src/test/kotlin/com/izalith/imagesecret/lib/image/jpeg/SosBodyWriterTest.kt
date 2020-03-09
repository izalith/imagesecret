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

import com.izalith.imagesecret.lib.MockitoUtils
import com.izalith.imagesecret.lib.image.jpeg.payload.JpegPayloadEmbedderImpl
import com.izalith.imagesecret.lib.image.jpeg.segments.SegmentGroup
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosBodyFactory
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosBodyWriter
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class SosBodyWriterTest {
    private val landscapeFile = "/img/landscape_orig.jpg"

    @Test
    fun sosBodyWriterTest() {
        val file = File(this::class.java.getResource(landscapeFile).file)
        val carrierInputStream = FileInputStream(file)

        val sosBodyFactory = Mockito.mock(SosBodyFactory::class.java)
        var segments: SegmentGroup? = null
        val encodedOutputStream = ByteArrayOutputStream()
        Mockito.doAnswer { invocation: InvocationOnMock? ->
            segments = invocation!!.arguments[0] as SegmentGroup?
            createSosBodyWriterSpy(segments, encodedOutputStream)
        }.`when`(sosBodyFactory).buildWriter(MockitoUtils.any(SegmentGroup::class.java))

        val jpegDecoder = JpegImageProcessor(JpegPayloadEmbedderImpl(), sosBodyFactory)
        jpegDecoder.encode(IOUtils.toByteArray(carrierInputStream), byteArrayOf())

        val originalSosBodyBytes = segments!!.segments.filterIsInstance<SosSegment>().single().segmentBodyBytes
        //hex representation for convenient manual debugging
        val hexOutput = encodedOutputStream.toByteArray().map { it.toUByte().toString(16) }.toTypedArray()
        val hexExpectedOutput = originalSosBodyBytes.map { it.toUByte().toString(16) }.toTypedArray()

        assertEquals(hexExpectedOutput.size, hexOutput.size)
        for (i in hexOutput.indices) {
            assertEquals(hexExpectedOutput[i], hexOutput[i], "position $i")
        }
    }

    /**
     * Spy is used to replace outputStream for result checking
     */
    private fun createSosBodyWriterSpy(
        segments: SegmentGroup?,
        outputStream: ByteArrayOutputStream
    ): SosBodyWriter {
        val sosBodyWriterSpy = Mockito.spy(
            SosBodyWriter(
                segments!!
            )
        )

        Mockito.doAnswer { invocation: InvocationOnMock? ->
            invocation!!.arguments[0] = outputStream
            invocation.callRealMethod()
        }.`when`(sosBodyWriterSpy!!)
            .writeSegmentSosBodyData(
                MockitoUtils.any(ByteArrayOutputStream::class.java),
                MockitoUtils.any(Array<SosSegment.Mcu>::class.java)
            )
        return sosBodyWriterSpy
    }
}
