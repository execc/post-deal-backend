package com.wavesplatform.we.app.deals.service

import com.wavesplatform.we.app.deals.domain.Did
import com.wavesplatform.we.app.deals.vc.toIdentityVc
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VcServiceTest {

    @Test
    fun shouldIssueAndSignVc() {
        val service = VcService(
                privateKey = "1b9cdf53588f99cea61c6482c4549b0316bafde19f76851940d71babaec5e569",
                issuerAddress = "test",
                cryptoService = CryptoService()
        )
        val vc = service.issueAndSign(
                id = "test",
                type = "Test",
                subject = Did(
                        activationCode = "",
                        firstName = "Leonid",
                        lastName = "Rimeynih",
                        patronymic = "Sergeevitch",
                        biometricPublicKey = "bio",
                        login = "login",
                        passwordHash = "",
                        phoneNumber = "12345",
                        publicKey = "publicKey",
                        challenge = ""

                ).toIdentityVc("txId")
        )
        print(vc.payload.toJSONObject())
        Assertions.assertNotNull(vc)
    }

    val publicKey = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAETD34hndC8tJkenplV++RauteSfeDWmfw" +
            "cllXfWbpLOwvDDIe8+/AtfyOh/NttM7Uo44/W4hHsVz6bcex3Pc+VA=="

    val data = "ewe"
    val signature = "MEQCICim1gTKJagdMWomyNzFViCtxMgH9wxL+bQTmCgyFiFsAiBLO3V1S1bUGDhTvrJZTMzqJ4wyFPaDtKXi9L+4VY+8oQ=="

    @Test
    fun shouldValidateSignatuer() {
        val service = CryptoService()
        val result = service.verify(signature, data, publicKey)
        Assertions.assertTrue(result)
    }

    @Test
    fun shouldValidateSignatuer2() {
        val service = CryptoService()
        val result = service.verify(
                "MEYCIQDyoAldjtb86Z0ZLgq0/giIgENPJZXghBGLScHQ+254zgIhAPfjm9xKCTDbDfpnynQoQr/6SzEFQJYEgyEt6kpG0WzL",
                "hello",
                "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE1o5fvY5rJ/QTzcI34G+rFDfiYWHMYInUR1hQRBBqYm8EpH6c462TkUzSU5u6HlARDLs5NfX08zfwOm0QYWR+mA==")
        Assertions.assertTrue(result)
    }
}
