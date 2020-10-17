package com.wavesplatform.we.app.deals.api.dto

data class IssueVcRequest(
    val participants: List<String>,
    val data: Map<String, Any>
)
