package com.wavesplatform.we.app.deals.domain

import com.wavesplatform.we.app.deals.domain.type.DidStatus
import com.wavesplatform.we.app.deals.domain.type.DidStatus.NEW
import java.util.Date
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "DID")
data class Did(
    val created: Date = Date(),
    val modified: Date = Date(),
    val login: String,
    @Id
    val publicKey: String,
    val passwordHash: String,
    val phoneNumber: String,
    val firstName: String,
    val patronymic: String,
    val lastName: String,
    val activationCode: String,
    val biometricPublicKey: String,
    val challenge: String,
    @Enumerated(STRING)
    val status: DidStatus = NEW
)
