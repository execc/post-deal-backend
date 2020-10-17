package com.wavesplatform.we.app.deals.api.dto

data class IssueDidRequest(
    val login: String,
    val publicKey: String,
    val biometricPublicKey: String,
    val password: String,
    val phoneNumber: String,
    val firstName: String,
    val patronymic: String,
    val lastName: String
)
