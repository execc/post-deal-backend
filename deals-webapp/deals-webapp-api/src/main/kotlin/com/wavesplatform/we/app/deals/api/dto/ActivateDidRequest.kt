package com.wavesplatform.we.app.deals.api.dto

data class ActivateDidRequest(
    val publicKey: String,
    val activationCode: String,
    val signedChallenge: String
)
