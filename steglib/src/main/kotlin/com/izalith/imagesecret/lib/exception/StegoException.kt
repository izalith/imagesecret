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

package com.izalith.imagesecret.lib.exception

import java.lang.RuntimeException

class StegoException: RuntimeException {
    val errorCode: StegoErrorCode
    constructor(errorCode: StegoErrorCode) : super() {
        this.errorCode = errorCode
    }
    constructor(errorCode: StegoErrorCode, message: String?) : super(message) {
        this.errorCode = errorCode
    }
    constructor(errorCode: StegoErrorCode, message: String?, cause: Throwable?) : super(message, cause) {
        this.errorCode = errorCode
    }

}
