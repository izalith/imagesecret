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

import com.izalith.imagesecret.lib.dto.Carrier
import com.izalith.imagesecret.lib.dto.payload.Payload

interface StegoProcessor<C: Carrier, P: Payload> {
    /**
     * Find maximum payload size in bytes for the [carrier]
     */
    fun maxPayloadSize(carrier: C): Int

    /**
     * Embed [payload] in the [carrier]
     */
    fun embed(carrier: C, payload: P): C

    /**
     * Extract payload from the [carrier]
     */
    fun extract(carrier: C): P
}
