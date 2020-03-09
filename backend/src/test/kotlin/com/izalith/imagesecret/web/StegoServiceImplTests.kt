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

import com.izalith.imagesecret.lib.StegoProcessor
import com.izalith.imagesecret.lib.dto.FileCarrier
import com.izalith.imagesecret.lib.dto.payload.Payload
import com.izalith.imagesecret.lib.exception.StegoErrorCode
import com.izalith.imagesecret.lib.exception.StegoException
import com.izalith.imagesecret.web.MockitoUtils.Companion.mock
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.Mockito


class StegoServiceImplTests {

    @Test
    fun testException() {
        val processorMock: StegoProcessor<FileCarrier, Payload> = mock()
        val service = StegoServiceImpl(processorMock)
        val errorCode = StegoErrorCode.ERROR_PAYLOAD_READ
        Mockito.`when`(processorMock.extract(MockitoUtils.any(FileCarrier::class.java))).thenThrow(StegoException(
            errorCode
        ))

        val thrown = assertThrows(WebStegoException::class.java) {
            service.decode(byteArrayOf(), "")
        }
        assertEquals(errorCode.description, thrown.message)

    }

}