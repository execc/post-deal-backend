package com.wavesplatform.we.app.deals.api.dto

import com.wavesplatform.we.app.deals.domain.type.VcDraftStatus

data class VcShortDto(
    val id: String,
    val status: VcDraftStatus
)
