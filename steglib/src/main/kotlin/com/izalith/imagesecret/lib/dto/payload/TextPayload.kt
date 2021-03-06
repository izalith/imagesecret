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

class TextPayload : Payload {
    override val type: PayloadType
        get() = PayloadType.TEXT
    val text: String

    constructor(content: ByteArray) {
        this.text = content.toString(Charsets.UTF_8)
    }

    constructor(text: String) {
        this.text = text
    }

    override fun write(): ByteArray {
        return type.markerByteArray() + text.toByteArray(Charsets.UTF_8)
    }

}
