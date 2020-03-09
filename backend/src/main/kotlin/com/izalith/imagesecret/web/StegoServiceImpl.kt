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

package com.izalith.imagesecret.web

import com.izalith.imagesecret.lib.DefaultImageStegoProcessor
import com.izalith.imagesecret.lib.StegoProcessor
import com.izalith.imagesecret.lib.dto.FileCarrier
import com.izalith.imagesecret.lib.dto.payload.FilePayload
import com.izalith.imagesecret.lib.dto.payload.Payload
import com.izalith.imagesecret.lib.dto.payload.TextPayload
import com.izalith.imagesecret.lib.exception.StegoException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StegoServiceImpl : StegoService {
    private val processor: StegoProcessor<FileCarrier, Payload>

    constructor() {
        processor = DefaultImageStegoProcessor()
    }

    constructor(processor: StegoProcessor<FileCarrier, Payload>) {
        this.processor = processor
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(StegoServiceImpl::class.java)
    }

    override fun encode(source: ByteArray, payload: String, sourceName: String): ByteArray {
        return doWithErrorHandling {
            val encoded = processor.embed(FileCarrier(source, sourceName), TextPayload(payload))
            encoded.content
        }
    }

    override fun encode(source: ByteArray, sourceName: String, payload: ByteArray, payloadFileName: String): ByteArray {
        return doWithErrorHandling {
            val encoded = processor.embed(FileCarrier(source, sourceName), FilePayload(payload, payloadFileName))
            encoded.content
        }
    }

    override fun decode(sourceFile: ByteArray, sourceName: String): Payload {
        return doWithErrorHandling {
            processor.extract(FileCarrier(sourceFile, sourceName))
        }
    }

    override fun getPayloadCapacity(source: ByteArray, sourceName: String): Int {
        return doWithErrorHandling {
            processor.maxPayloadSize(FileCarrier(source, sourceName))
        }
    }

    private fun <T> doWithErrorHandling(function: () -> T): T {
        try {
            return function()
        } catch (e: StegoException) {
            log.error("getPayloadCapacity error", e)
            throw WebStegoException(e.errorCode.description, e)
        }
    }
}
