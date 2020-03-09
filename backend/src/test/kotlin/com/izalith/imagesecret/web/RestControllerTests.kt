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

import com.izalith.imagesecret.lib.dto.payload.TextPayload
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.ws.rs.core.MediaType


@WebMvcTest(RestController::class)
@RunWith(SpringJUnit4ClassRunner::class)
class RestControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var stegoService: StegoService

    @Test
    fun testEncodeText() {
        val encodedByteArray = "encodedImage".toByteArray()
        val carrierFile = MockMultipartFile(
            "carrierImageFile",
            "filename.jpg",
            "text/plain",
            "carrierImageFile".toByteArray()
        )

        `when`(
            stegoService.encode(
                MockitoUtils.any(ByteArray::class.java), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        ).thenReturn(encodedByteArray)

        val expectedPayload = "testPayload"
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/encodeText")
                .file(carrierFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("payload", expectedPayload)
        ).andExpect(status().`is`(200))
            .andExpect(MockMvcResultMatchers.content().bytes(encodedByteArray))
    }

    @Test
    fun testEncodeFile() {
        val encodedByteArray = "encodedImage".toByteArray()
        val carrierFile = MockMultipartFile(
            "carrierImageFile",
            "carrierImageFile.jpg",
            "text/plain",
            "carrierImageFile".toByteArray()
        )
        val payloadFile = MockMultipartFile(
            "payloadImageFile",
            "payloadFile.jpg",
            "text/plain",
            "payloadImageFile".toByteArray()
        )

        `when`(
            stegoService.encode(
                MockitoUtils.any(ByteArray::class.java), ArgumentMatchers.anyString(),
                MockitoUtils.any(ByteArray::class.java), ArgumentMatchers.anyString()
            )
        ).thenReturn(encodedByteArray)

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/encodeFile")
                .file(carrierFile)
                .file(payloadFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().`is`(200))
            .andExpect(MockMvcResultMatchers.content().bytes(encodedByteArray))
    }

    @Test
    fun testDecodeText() {
        val payload = "expectedPayload"
        val carrierFile = MockMultipartFile(
            "carrierImageFile",
            "filename.jpg",
            "text/plain",
            "carrierImageFile".toByteArray()
        )

        `when`(
            stegoService.decode(
                MockitoUtils.any(ByteArray::class.java), ArgumentMatchers.anyString()
            )
        ).thenReturn(TextPayload(payload))

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/decode")
                .file(carrierFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().`is`(200))
            .andExpect(MockMvcResultMatchers.content().string(payload))
    }

    @Test
    fun testCapacity() {
        val carrierFile = MockMultipartFile(
            "carrierImageFile",
            "filename.jpg",
            "text/plain",
            "carrierImageFile".toByteArray()
        )

        val expectedCapacity = 1024
        `when`(
            stegoService.getPayloadCapacity(MockitoUtils.any(ByteArray::class.java), ArgumentMatchers.anyString())
        ).thenReturn(expectedCapacity)

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/getPayloadCapacity")
                .file(carrierFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().`is`(200))
            .andExpect(MockMvcResultMatchers.content().json("{'payloadCapacity': $expectedCapacity}"))
    }
}
