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

package com.izalith.imagesecret.lib.dto

import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import org.apache.commons.io.FileUtils
import java.io.File

class FileCarrier : Carrier {
    override val content: ByteArray
    val filename: String

    constructor(content: ByteArray, filename: String) {
        this.content = content
        this.filename = filename
    }

    constructor(file: File) {
        if (!file.isFile) {
            throw StegoException(StegoErrorCode.ERROR_INVALID_IMAGE, "Invalid file ${file.absolutePath}")
        }
        filename = file.name
        content = FileUtils.readFileToByteArray(file)
    }
}
