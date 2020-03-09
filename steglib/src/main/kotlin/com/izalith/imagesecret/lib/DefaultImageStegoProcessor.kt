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

package com.izalith.imagesecret.lib

import com.izalith.imagesecret.lib.image.ImageProcessor
import com.izalith.imagesecret.lib.image.jpeg.JpegImageProcessor
import com.izalith.imagesecret.lib.image.png.PngImageProcessor
import com.izalith.imagesecret.lib.dto.FileCarrier
import com.izalith.imagesecret.lib.dto.ImageType
import com.izalith.imagesecret.lib.dto.payload.Payload
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException

/**
 * Composite processor provides functions for working with an image carrier (only jpeg and png).
 */

class DefaultImageStegoProcessor(
    private val jpegProcessor: ImageProcessor,
    private val pngProcessor: ImageProcessor
) : StegoProcessor<FileCarrier, Payload> {

    constructor() : this(
        JpegImageProcessor(),
        PngImageProcessor()
    )

    override fun embed(carrier: FileCarrier, payload: Payload): FileCarrier {
        val payloadContent = payload.write()
        val encodedImageContent = getImageProcessor(carrier).encode(carrier.content, payloadContent)
        return FileCarrier(encodedImageContent, carrier.filename)
    }

    override fun extract(carrier: FileCarrier): Payload {
        val decodedContent = getImageProcessor(carrier).decode(carrier.content)
        return Payload.read(decodedContent)
    }

    override fun maxPayloadSize(carrier: FileCarrier): Int {
        return getImageProcessor(carrier).maxPayloadSize(carrier.content)
    }

    private fun getImageProcessor(image: FileCarrier): ImageProcessor {
        val extension = image.filename.substringAfterLast(".")
        return when {
            ImageType.JPEG.extensions.contains(extension) -> {
                jpegProcessor
            }
            ImageType.PNG.extensions.contains(extension) -> {
                pngProcessor
            }
            else -> {
                throw StegoException(
                    StegoErrorCode.ERROR_NOT_SUPPORTED_IMAGE_FORMAT,
                    "Not supported image format $extension"
                )
            }
        }
    }

}
