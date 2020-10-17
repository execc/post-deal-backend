package com.wavesplatform.we.app.deals.vc

data class DealVcSubject(
    val id: String,
    val participants: List<String>,
    val signatures: Map<String, String>,
    val data: Map<String, Any>
)
