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

package com.izalith.imagesecret.lib.image

import com.izalith.imagesecret.lib.DefaultImageStegoProcessor
import com.izalith.imagesecret.lib.MockitoUtils
import com.izalith.imagesecret.lib.dto.FileCarrier
import com.izalith.imagesecret.lib.dto.payload.TextPayload
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import com.izalith.imagesecret.lib.image.ImageProcessor
import org.junit.jupiter.api.*

import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.MockitoAnnotations
import java.io.File

class DefaultImageStegoProcessorTest {

    private val landscapeFileJpg = "/img/landscape_orig.jpg"
    private val landscapeFilePng = "/img/landscape_orig.png"
    private val textFile = "/payload.txt"
    private val message = "encoded"

    @Mock
    lateinit var jpegProcessor: ImageProcessor

    @Mock
    lateinit var pngProcessor: ImageProcessor

    lateinit var processor: DefaultImageStegoProcessor

    @BeforeEach
    fun init() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(pngProcessor.encode(
            MockitoUtils.any(ByteArray::class.java),
            MockitoUtils.any(ByteArray::class.java))
        )
            .thenReturn(byteArrayOf())
        Mockito.`when`(jpegProcessor.encode(
            MockitoUtils.any(ByteArray::class.java),
            MockitoUtils.any(ByteArray::class.java))
        ).thenReturn(byteArrayOf())

        processor = DefaultImageStegoProcessor(jpegProcessor, pngProcessor)
    }

    @Test
    fun testJpegProcessor() {
        val payload = TextPayload(message)
        val carrier = FileCarrier(File(this::class.java.getResource(landscapeFileJpg).file))

        processor.embed(carrier, payload)

        Mockito.verify(jpegProcessor).encode(
            MockitoUtils.any(ByteArray::class.java),
            MockitoUtils.any(ByteArray::class.java))
        Mockito.verify(pngProcessor, never())!!.encode(
            MockitoUtils.any(ByteArray::class.java),
            MockitoUtils.any(ByteArray::class.java))
    }

    @Test
    fun testPngProcessor() {
        val payload = TextPayload(message)
        val carrier = FileCarrier(File(this::class.java.getResource(landscapeFilePng).file))

        processor.embed(carrier, payload)

        Mockito.verify(jpegProcessor, never())!!.encode(
            MockitoUtils.any(ByteArray::class.java),
            MockitoUtils.any(ByteArray::class.java))
        Mockito.verify(pngProcessor).encode(
            MockitoUtils.any(ByteArray::class.java),
            MockitoUtils.any(ByteArray::class.java))
    }

    @Test
    fun testWrongFileError() {
        val payload = TextPayload(message)
        val carrier = FileCarrier(File(this::class.java.getResource(textFile).file))

        val thrown: StegoException = Assertions.assertThrows(StegoException::class.java) {
            processor.embed(carrier, payload)
        }

        Assertions.assertEquals(StegoErrorCode.ERROR_NOT_SUPPORTED_IMAGE_FORMAT, thrown.errorCode)
    }
}
