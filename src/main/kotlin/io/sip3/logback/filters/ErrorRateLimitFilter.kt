/*
 * Copyright 2018-2020 SIP3.IO, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sip3.logback.filters

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import org.apache.commons.collections4.map.PassiveExpiringMap

/**
 * Limits the rate of logging events. One event will be sent in `periodInSeconds`.
 * After cache size reaches `maxSize` all events will be filtered.
 * Event hash is based on the first line of the stack trace. An event without `throwableProxy` is not filtered.
 */
class ErrorRateLimitFilter : Filter<ILoggingEvent>() {

    private val cache: PassiveExpiringMap<Int, Long> by lazy {
        PassiveExpiringMap<Int, Long>(periodInSeconds * 1000L)
    }

    var maxSize = 100
    var periodInSeconds = 60

    override fun decide(event: ILoggingEvent): FilterReply {
        if (event.throwableProxy == null) {
            return FilterReply.NEUTRAL
        }

        val hash = computeHash(event.throwableProxy)
        if (cache.containsKey(hash) || cache.size >= maxSize) {
            return FilterReply.DENY
        } else {
            cache[hash] = event.timeStamp
            return FilterReply.NEUTRAL
        }
    }

    private fun computeHash(throwableProxy: IThrowableProxy): Int {
        return throwableProxy.stackTraceElementProxyArray
                .first()
                .steAsString
                .hashCode()
    }
}