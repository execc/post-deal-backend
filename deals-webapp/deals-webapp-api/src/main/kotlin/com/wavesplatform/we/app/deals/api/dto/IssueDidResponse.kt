package com.wavesplatform.we.app.deals.api.dto

import com.wavesplatform.we.app.deals.domain.type.DidStatus

data class IssueDidResponse(
    val status: DidStatus,
    val challenge: String
)
