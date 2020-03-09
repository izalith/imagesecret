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

package com.izalith.imagesecret.lib.image.jpeg.marker

import com.izalith.imagesecret.lib.image.jpeg.marker.Marker.Companion.MARKER_FIRST_BYTE


enum class MarkerEnum(override val bytes: UByteArray) :
    Marker {
    SOS(ubyteArrayOf(MARKER_FIRST_BYTE, 0xDAu)),
    SOI(ubyteArrayOf(MARKER_FIRST_BYTE, 0xD8u)),
    COM(ubyteArrayOf(MARKER_FIRST_BYTE, 0xFEu)),
    SOF0(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC0u)),
    SOF1(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC1u)),
    SOF2(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC2u)),
    SOF3(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC3u)),
    SOF5(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC5u)),
    SOF6(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC6u)),
    SOF7(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC7u)),
    SOF8(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC8u)),
    SOF9(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC9u)),
    SOF10(ubyteArrayOf(MARKER_FIRST_BYTE, 0xCAu)),
    SOF11(ubyteArrayOf(MARKER_FIRST_BYTE, 0xCBu)),
    SOF12(ubyteArrayOf(MARKER_FIRST_BYTE, 0xCCu)),
    SOF13(ubyteArrayOf(MARKER_FIRST_BYTE, 0xCDu)),
    SOF14(ubyteArrayOf(MARKER_FIRST_BYTE, 0xCEu)),
    SOF15(ubyteArrayOf(MARKER_FIRST_BYTE, 0xCFu)),
    DHT(ubyteArrayOf(MARKER_FIRST_BYTE, 0xC4u)),
    DQT(ubyteArrayOf(MARKER_FIRST_BYTE, 0xDBu)),
    DNL(ubyteArrayOf(MARKER_FIRST_BYTE, 0xDCu)),
    EOI(ubyteArrayOf(MARKER_FIRST_BYTE, 0xD9u));

}
