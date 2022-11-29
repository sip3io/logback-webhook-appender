/*
 * Copyright 2018-2022 SIP3.IO, Corp.
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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.encoder.Encoder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import kotlin.properties.Delegates.observable

/**
 * Calls configured webhook on every logging event.
 * Important: Designed to be used for matching, WARNING or ERROR events only.
 */
class WebhookAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {

    private val logger = LoggerFactory.getLogger(WebhookAppender::class.java)

    lateinit var encoder: Encoder<ILoggingEvent>

    lateinit var url: String
    lateinit var json: String

    var interval: Int by observable(60) { _, _, _ ->
        intervalInMillis = interval * 1000L
    }
    private var intervalInMillis = interval * 1000L

    var pattern: String by observable(".*") { _, _, v ->
        regex = Regex(v, RegexOption.DOT_MATCHES_ALL)
    }
    private var regex: Regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)

    private val client: OkHttpClient by lazy { OkHttpClient() }
    private var lastSentAt = 0L

    override fun append(event: ILoggingEvent) {
        // Threshold by ERROR level
        if (event.level != Level.ERROR) {
            return
        }

        // Limit send rate
        if (event.timeStamp < lastSentAt + intervalInMillis) {
            return
        }

        // Encode message
        val message = String(encoder.encode(event))

        // Check if message matches configured pattern
        if (message.matches(regex)) {
            send(message)
            lastSentAt = event.timeStamp
        }
    }

    private fun send(message: String) {
        // Replace JSON variables
        val payload = json.replace("{message}", StringEscapeUtils.escapeJson(message))

        // Create HTTP POST request
        val request = Request.Builder()
            .url(url)
            .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        // Execute HTTP POST request
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                logger.error("Webhook call failed. URL: $url, Payload: $payload", e)
            }

            override fun onResponse(call: Call, response: Response) {
                // Do nothing...
            }
        })
    }
}