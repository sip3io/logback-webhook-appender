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
import ch.qos.logback.core.read.ListAppender
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

class RateLimitFilterTest {

    private lateinit var appender: ListAppender<ILoggingEvent>
    private val logger = LoggerFactory.getLogger(RateLimitFilterTest::class.java) as Logger

    @Before
    fun `Add appender`() {
        appender = ListAppender();
        val rateLimitFilter = RateLimitFilter().apply {
            maxSize = 2
            period =1
        }
        appender.addFilter(rateLimitFilter)
        appender.start();
        logger.addAppender(appender)
    }

    @Test
    fun `Verify rate limiting and cache maxSize`() {
        logger.info("Hello, Bob!")
        // Reach cache overflow
        repeat(2) {
            logger.error("Hello, World!", IllegalArgumentException("Bad username"))
            logger.error("Hello, World-1!", IllegalArgumentException("Bad username"))
            logger.error("Hello, World-2!", IllegalArgumentException("Bad username"))
        }

        // Verify passed events
        val messages = appender.list
        assertEquals(2, appender.list.size)
        assertEquals(-1, appender.list.indexOfFirst { it.message == "Hello, World-2!"})

        // After cache expire fire one more error
        Thread.sleep(1000L)
        logger.error("Hello, World-2!", IllegalArgumentException("Bad username"))
        assertEquals(3, messages.size)
        assertEquals(3, appender.list.distinctBy { it.message }.size)
    }

    @After
    fun `Detach appender`() {
        logger.detachAppender(appender);
    }
}