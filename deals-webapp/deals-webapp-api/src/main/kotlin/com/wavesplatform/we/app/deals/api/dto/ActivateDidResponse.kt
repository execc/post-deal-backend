package com.wavesplatform.we.app.deals.api.dto

import com.wavesplatform.we.app.deals.domain.type.DidStatus

data class ActivateDidResponse(
    val status: DidStatus,
    val jwt: String,
    val payload: Any
)
