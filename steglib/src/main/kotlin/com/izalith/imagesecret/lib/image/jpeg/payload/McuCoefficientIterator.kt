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

package com.izalith.imagesecret.lib.image.jpeg.payload

import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment

class McuCoefficientIterator private constructor(private val mcus: Array<SosSegment.Mcu>) :
    CoefficientIterator {
    private var mcu: SosSegment.Mcu
    private var mcuComponent: SosSegment.McuComponent
    private var samplePosition: Int = -1
    private var componentPosition: Int = 0
    private var mcuPosition: Int = 0

    init {
        mcu = mcus[0]
        mcuComponent = mcu.components[0]
    }

    override fun next(): Int {
        if (samplePosition < mcus[mcuPosition].components[componentPosition].samples.size - 1) {
            samplePosition++
        } else if (componentPosition < mcus[mcuPosition].components.size - 1) {
            componentPosition++
            samplePosition = 0
        } else  if (mcuPosition < mcus.size - 1) {
            mcuPosition++
            componentPosition = 0
            samplePosition = 0
        } else {
            throw NoSuchElementException()
        }
        return getCurrent()
    }

    private fun getCurrent(): Int {
        return mcus[mcuPosition].components[componentPosition].samples[samplePosition]
    }

    override fun hasNext(): Boolean {
        var hasNext = false
        if (samplePosition < mcus[mcuPosition].components[componentPosition].samples.size - 1) {
            hasNext = true
        }
        if (componentPosition < mcus[mcuPosition].components.size - 1) {
            hasNext = true
        }
        if (mcuPosition < mcus.size - 1) {
            hasNext = true
        }
        return hasNext
    }

    override fun set(element: Int) {
        mcus[mcuPosition].components[componentPosition].samples[samplePosition] = element
    }

    companion object {
        fun build(mcus: Array<SosSegment.Mcu>): CoefficientIterator {
            if (mcus.isEmpty() || mcus[0].components.isEmpty() || mcus[0].components[0].samples.isEmpty()) {
                return emptyIterator()
            }
            return McuCoefficientIterator(mcus)
        }

        private fun emptyIterator(): CoefficientIterator {
            return object : CoefficientIterator {
                override fun next(): Int {
                    throw NoSuchElementException()
                }

                override fun hasNext(): Boolean = false

                override fun set(element: Int) {
                    throw NoSuchElementException()
                }
            }
        }
    }

}
