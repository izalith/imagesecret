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

import com.izalith.imagesecret.lib.dto.ImageType
import com.izalith.imagesecret.lib.dto.payload.FilePayload
import com.izalith.imagesecret.lib.dto.payload.PayloadType
import com.izalith.imagesecret.lib.dto.payload.TextPayload
import org.apache.commons.lang3.StringUtils
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA


@RestController
@RequestMapping("/api")
class RestController(val stegoService: StegoService) {

    @PostMapping(
        "/encodeText",
        consumes = [MULTIPART_FORM_DATA], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ResponseBody
    fun encode(
        @RequestParam payload: String,
        @RequestParam carrierImageFile: MultipartFile
    ): ResponseEntity<ByteArray> {
        val carrierFilename = notNullFileName(carrierImageFile.originalFilename)
        val encoded = stegoService.encode(carrierImageFile.bytes, payload, carrierFilename)
        val headers = createFileContentHttpHeaders(carrierFilename)
        return ResponseEntity(encoded, headers, HttpStatus.OK)
    }

    @PostMapping(
        "/encodeFile",
        consumes = [MULTIPART_FORM_DATA], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @ResponseBody
    fun encode(
        @RequestParam payloadImageFile: MultipartFile,
        @RequestParam carrierImageFile: MultipartFile
    ): ResponseEntity<ByteArray> {
        val carrierFilename = notNullFileName(carrierImageFile.originalFilename)
        val payloadFilename = notNullFileName(carrierImageFile.originalFilename)
        val encoded = stegoService.encode(
            carrierImageFile.bytes, carrierFilename,
            payloadImageFile.bytes, payloadFilename
        )
        val headers = createFileContentHttpHeaders(carrierImageFile.originalFilename!!)
        return ResponseEntity(encoded, headers, HttpStatus.OK)
    }

    @PostMapping(
        "/decode",
        consumes = [MULTIPART_FORM_DATA],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun decode(@RequestParam carrierImageFile: MultipartFile): ResponseEntity<Any> {
        val carrierFilename = notNullFileName(carrierImageFile.originalFilename)
        val payload = stegoService.decode(carrierImageFile.bytes, carrierFilename)
        return when {
            payload.type.equals(PayloadType.TEXT) -> {
                ResponseEntity((payload as TextPayload).text, HttpStatus.OK)
            }
            payload.type.equals(PayloadType.FILE) -> {
                val filePayload = payload as FilePayload
                ResponseEntity(
                    filePayload.fileContent, createFileContentHttpHeaders(filePayload.filename),
                    HttpStatus.OK
                )
            }
            else -> {
                throw WebStegoException("Not supported payload type")
            }
        }
    }

    @PostMapping(
        "/getPayloadCapacity",
        consumes = [MULTIPART_FORM_DATA], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun checkCapacity(@RequestParam carrierImageFile: MultipartFile): MutableMap<String, Int> {
        val capacity = stegoService.getPayloadCapacity(carrierImageFile.bytes, carrierImageFile.originalFilename!!)
        return Collections.singletonMap("payloadCapacity", capacity)
    }

    private fun createFileContentHttpHeaders(originalFilename: String): HttpHeaders {
        val extensionSeparator = "."
        val fileName = StringUtils.substringBeforeLast(originalFilename, extensionSeparator)
        val extension = StringUtils.substringAfterLast(originalFilename, extensionSeparator)
        val headers = HttpHeaders()
        headers.contentDisposition = ContentDisposition.parse(
            "attachment; filename=\"$fileName.encoded.$extension\""
        )

        when {
            ImageType.JPEG.extensions.contains(extension) -> {
                headers.contentType = MediaType.IMAGE_JPEG
            }
            ImageType.PNG.extensions.contains(extension) -> {
                headers.contentType = MediaType.IMAGE_PNG
            }
            else -> {
                throw WebStegoException("Not supported image format $extension")
            }
        }
        return headers
    }

    private fun notNullFileName(fileName: String?) : String {
        if (StringUtils.isEmpty(fileName)) {
            throw WebStegoException("Empty file!")
        } else {
            return fileName!!
        }
    }

}
