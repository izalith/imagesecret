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

import com.izalith.imagesecret.lib.dto.ImageType
import com.izalith.imagesecret.lib.image.ImageProcessor
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class PngImageProcessor(private val payloadEmbedder: PngPayloadEmbedder) : ImageProcessor {
    constructor() : this(PngPayloadEmbedderImpl())

    override fun maxPayloadSize(carrier: ByteArray): Int {
        val bufferedImage = ImageIO.read(ByteArrayInputStream(carrier))
        return payloadEmbedder.findMaxPayloadSize(bufferedImage)
    }

    override fun encode(carrier: ByteArray, payload: ByteArray): ByteArray {
        val carrierImage = ImageIO.read(ByteArrayInputStream(carrier))
        checkAvailableSpace(payload, carrierImage)
        val encodedImage = payloadEmbedder.embed(carrierImage, payload)
        val resultByteArrayOS = ByteArrayOutputStream()
        ImageIO.write(encodedImage, ImageType.PNG.extensions[0], resultByteArrayOS)
        return resultByteArrayOS.toByteArray()
    }

    override fun decode(carrier: ByteArray): ByteArray {
        val carrierImage = ImageIO.read(ByteArrayInputStream(carrier))
        return payloadEmbedder.extract(carrierImage)
    }

    private fun checkAvailableSpace(payload: ByteArray, image: BufferedImage) {
        val maxEncodedSize = payloadEmbedder.findMaxPayloadSize(image)
        if (maxEncodedSize < payload.size) {
            throw StegoException(
                StegoErrorCode.ERROR_NOT_ENOUGH_SPACE_FOR_PAYLOAD,
                "Not enough space in the image. Required: ${payload.size} bytes, " +
                        "available: $maxEncodedSize bytes"
            )
        }
    }

}
