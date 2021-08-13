/*
 * Copyright 2018-2020 SIP3.IO, Corp.
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

package io.sip3.logback

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class WebhookAppenderTest {

    private val logger = LoggerFactory.getLogger(WebhookAppenderTest::class.java)

    private lateinit var server: MockWebServer

    @Before
    fun `Configure mock server`() {
        server = MockWebServer()
        server.start(34343)
        server.url("/webhook")
    }

    @Test
    fun `Verify webhook call`() {
        // INFO must be ignored
        logger.info("Ignored 1")

        // ERROR with json
        repeat(2) {
            // Ignored by pattern
            logger.error("Bad news")

            // Sent from webhook
            logger.error("Hello, World:\n {\"name\": \"some string value in json\"}")

            // Ignored by rate limit
            logger.error("Hello, bad news!")

            val json = server.takeRequest(5, TimeUnit.SECONDS)
                ?.body
                ?.readString(Charset.defaultCharset())

            assertNotNull(json)
            assertTrue(json!!.contains("\\\"some string value in json\\\""))

            // Wait for cache expiring
            Thread.sleep(1100L)
        }

        assertEquals(2, server.requestCount)
    }

    @After
    fun `Shutdown webhook server`() {
        server.shutdown()
    }
}