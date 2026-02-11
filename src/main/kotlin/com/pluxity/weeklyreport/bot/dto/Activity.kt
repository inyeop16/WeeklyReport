package com.pluxity.weeklyreport.bot.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Activity(
    val type: String? = null,
    val id: String? = null,
    val text: String? = null,
    val from: ChannelAccount? = null,
    val recipient: ChannelAccount? = null,
    val conversation: ConversationAccount? = null,
    val channelId: String? = null,
    val serviceUrl: String? = null,
    val replyToId: String? = null,
    val value: Map<String, Any>? = null,
    val attachments: List<Attachment>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelAccount(
    val id: String? = null,
    val name: String? = null,
    @JsonProperty("aadObjectId")
    val aadObjectId: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConversationAccount(
    val id: String? = null,
    val name: String? = null,
    val isGroup: Boolean? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attachment(
    val contentType: String? = null,
    val content: Any? = null
)
