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

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Calls configured webhook on every logging event.
 * Important: Designed to be used for WARNING and ERROR events only.
 */
class WebhookAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {

    private val logger = LoggerFactory.getLogger(WebhookAppender::class.java)

    companion object {

        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }

    lateinit var url: String
    lateinit var json: String

    private val client: OkHttpClient by lazy { OkHttpClient() }

    override fun append(event: ILoggingEvent) {
        // Replace JSON variables
        val payload = json.replace("{event}", event.toString())

        // Create HTTP POST request
        val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody(MEDIA_TYPE_JSON))
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