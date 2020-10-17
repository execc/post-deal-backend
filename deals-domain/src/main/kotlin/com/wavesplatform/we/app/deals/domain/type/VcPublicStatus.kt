package com.wavesplatform.we.app.deals.domain.type

data class VcPublicStatus(
    val status: VcStatus,
    val id: String,
    val exp: Long,
    val iat: Long
)
