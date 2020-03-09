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

package com.izalith.imagesecret.lib.dto.payload

import com.izalith.imagesecret.lib.dto.payload.PayloadType.Companion.MARKER_BYTES_COUNT
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException

/**
 *  Object to embed in an image.
 */
interface Payload {
    fun write(): ByteArray
    val type: PayloadType

    companion object {
        const val MIN_PAYLOAD_BYTES_COUNT = 3

        fun read(bytes: ByteArray): Payload {
            checkByteArraySize(bytes, MIN_PAYLOAD_BYTES_COUNT)
            val stegDataType = PayloadType.getStegDataType(
                bytes.sliceArray(0 until MARKER_BYTES_COUNT).toUByteArray()
            )
            val content = bytes.sliceArray(2 until bytes.size)
            return when (stegDataType) {
                PayloadType.TEXT -> {
                    TextPayload(content)
                }
                PayloadType.FILE -> {
                    FilePayload(content)
                }
                PayloadType.BINARY -> {
                    BinaryPayload(content)
                }
            }
        }

        fun checkByteArraySize(bytes: ByteArray, requiredSize: Int) {
            if(bytes.size < requiredSize) {
                throw StegoException(StegoErrorCode.ERROR_PAYLOAD_READ, "Incorrect payload data")
            }
        }
    }
}
