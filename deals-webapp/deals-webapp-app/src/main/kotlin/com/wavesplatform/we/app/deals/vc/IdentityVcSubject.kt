package com.wavesplatform.we.app.deals.vc

import com.wavesplatform.we.app.deals.domain.Did
import java.util.Random

data class IdentityVcSubject(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val birthDayDate: String,
    val gender: String,
    val birthPlace: String,
    val passportSerial: String,
    val passportNumber: String,
    val passportIssuer: String,
    val passportIssuerCode: String,
    val txId: String
)

fun Did.toIdentityVc(txId: String) = IdentityVcSubject(
        id = publicKey,
        name = "$firstName $patronymic $lastName",
        phoneNumber = phoneNumber,
        birthDayDate = "${Random().nextInt(25) + 1}/${Random().nextInt(11) + 1}/198${Random().nextInt(8) + 1}",
        birthPlace = "Москва",
        passportSerial = "${Random().nextInt(8888) + 1000}",
        passportNumber = "${Random().nextInt(888888) + 100000}",
        passportIssuer = "ОВД по Району Богородское",
        passportIssuerCode = "${Random().nextInt(8888) + 1000}",
        gender = "M",
        txId = txId
)
