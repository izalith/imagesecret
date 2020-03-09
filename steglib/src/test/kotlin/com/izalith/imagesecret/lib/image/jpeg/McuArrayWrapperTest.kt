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

package com.izalith.imagesecret.lib.image.jpeg

import com.izalith.imagesecret.lib.image.jpeg.payload.McuCoefficientIterator
import com.izalith.imagesecret.lib.image.jpeg.segments.SofNSegment
import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class McuArrayWrapperTest {
    @Test
    fun testRead2BytesInt() {
        val frameComponent = SofNSegment.SofComponent(1, 1, 1, 1)
        val scanComponent = SosSegment.SosComponent(1, 1, 1)
        val mcuComponent1 = SosSegment.McuComponent(
            2,
            2,
            scanComponent,
            frameComponent
        )
        val mcuComponent2 = SosSegment.McuComponent(
            2,
            2,
            scanComponent,
            frameComponent
        )
        val mcuComponent3 = SosSegment.McuComponent(
            2,
            2,
            scanComponent,
            frameComponent
        )
        val mcuComponent4 = SosSegment.McuComponent(
            2,
            2,
            scanComponent,
            frameComponent
        )

        mcuComponent1.samples[2] = 1
        mcuComponent2.samples[2] = 2
        mcuComponent3.samples[1] = 3
        mcuComponent4.samples[3] = 4

        val mcu1 =
            SosSegment.Mcu(arrayOf(mcuComponent1, mcuComponent2))
        val mcu2 =
            SosSegment.Mcu(arrayOf(mcuComponent3, mcuComponent4))
        val wrapper = McuCoefficientIterator.build(arrayOf(mcu1, mcu2))

        wrapper.next()
        wrapper.next()
        assertEquals(1, wrapper.next())
        wrapper.next()
        wrapper.next()
        wrapper.next()
        assertEquals(2, wrapper.next())
        wrapper.set(7)
        wrapper.next()
        wrapper.next()
        assertEquals(3, wrapper.next())
        wrapper.set(6)
        wrapper.next()
        wrapper.next()
        wrapper.next()
        wrapper.next()
        wrapper.next()
        assertTrue(wrapper.hasNext())
        assertEquals(4, wrapper.next())
        wrapper.set(5)
        assertFalse(wrapper.hasNext())

        assertEquals(7, mcuComponent2.samples[2])
        assertEquals(6, mcuComponent3.samples[1])
        assertEquals(5, mcuComponent4.samples[3])
    }
}
