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

import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException


enum class PayloadType(private val marker: UByteArray) {
    TEXT(ubyteArrayOf(0xFFu, 0xF0u)),
    FILE(ubyteArrayOf(0xFFu, 0xF1u)),
    BINARY(ubyteArrayOf(0xFFu, 0xF2u));

    companion object {
        const val MARKER_BYTES_COUNT: Int = 2

        fun getStegDataType(bytes: UByteArray): PayloadType {
            return values().singleOrNull { it.marker.contentEquals(bytes) }
                ?: throw StegoException(StegoErrorCode.ERROR_PAYLOAD_READ, "Can not extract the payload")
        }
    }

    fun markerByteArray() : ByteArray = marker.toByteArray()
}
