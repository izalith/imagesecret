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

import java.awt.image.BufferedImage

interface PngPayloadEmbedder {

    /**
     * Extract payload from the [image]
     */
    fun extract(image: BufferedImage): ByteArray

    /**
     * Embed [payload] in the [image]
     */
    fun embed(image: BufferedImage, payload: ByteArray): BufferedImage

    /**
     * Find maximum payload size in bytes for the [image]
     */
    fun findMaxPayloadSize(image: BufferedImage): Int
}
