package com.wavesplatform.we.app.deals.mappers

import com.wavesplatform.we.app.deals.api.dto.IssueDidRequest
import com.wavesplatform.we.app.deals.domain.Did
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

fun IssueDidRequest.toDid() =
        Did(
                login = login,
                publicKey = publicKey,
                passwordHash = BCryptPasswordEncoder(10).encode(password),
                phoneNumber = phoneNumber,
                firstName = firstName,
                lastName = lastName,
                patronymic = patronymic,
                activationCode = "",
                challenge = "",
                biometricPublicKey = biometricPublicKey
        )
