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

package com.izalith.imagesecret.lib.image.jpeg.segments

import com.izalith.imagesecret.lib.image.jpeg.segments.sos.SosSegment

class SegmentGroup(val segments: List<Segment>) {
    fun getSofnSegment(): SofNSegment {
        return segments.filterIsInstance<SofNSegment>().single()
    }

    fun getSosSegment(): SosSegment {
        return segments.filterIsInstance<SosSegment>().single()
    }

    fun getAcDhtSegments(): List<DhtSegment> {
        return segments.filterIsInstance<DhtSegment>().filter { !it.isDcTableClass() }
    }

    fun getDcDhtSegments(): List<DhtSegment> {
        return segments.filterIsInstance<DhtSegment>().filter { it.isDcTableClass() }
    }
}
