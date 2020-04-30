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
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import org.apache.commons.collections4.map.PassiveExpiringMap

/**
 * Limit rate of logging events to 1 event in `period` seconds.
 * After cache size reaches `maxSize` all events will be filtered.
 * Event hash based on first line of StackTrace or `loggerName` and `message`
 */
class RateLimitFilter : Filter<ILoggingEvent>() {

    private val cache: PassiveExpiringMap<Int, Long> by lazy {
        PassiveExpiringMap<Int, Long>(period * 1000L)
    }

    private val prime = 31
    var maxSize = 100
    var period = 60

    override fun decide(event: ILoggingEvent): FilterReply {
        val hash = computeHash(event)
        if (cache.containsKey(hash) || cache.size >= maxSize) {
            return FilterReply.DENY
        } else {
            cache[hash] = event.timeStamp
            return FilterReply.NEUTRAL
        }
    }

    private fun computeHash(event: ILoggingEvent): Int {
        var result = 1

        // If event contains stacktrace get first line from it
        event.throwableProxy
                ?.stackTraceElementProxyArray
                ?.firstOrNull { stackTraceElementProxy ->
                    result = result * prime + stackTraceElementProxy.steAsString.hashCode()
                    return result
                }

        // Otherwise compute hash from loggerName and message
        result = result * prime + event.loggerName.hashCode()
        result = result * prime + event.message.hashCode()

        return result
    }
}