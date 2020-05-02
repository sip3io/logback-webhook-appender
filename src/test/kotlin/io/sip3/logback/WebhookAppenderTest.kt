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

package io.sip3.logback

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        logger.info("Aloha, World!")
        logger.info("Hello, %username%!")
        logger.error("Hello, World!")

        val json = server.takeRequest(5, TimeUnit.SECONDS)
                ?.body
                ?.readString(Charset.defaultCharset())

        assertNotNull(json)
        assertTrue(json!!.contains("Hello, World!"))
    }

    @After
    fun `Shutdown webhook server`() {
        server.shutdown()
    }
}