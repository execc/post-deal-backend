package com.wavesplatform.we.app.deals.service

import com.metadium.vc.VerifiableCredential
import com.metadium.vc.VerifiableSignedJWT
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.SignedJWT
import java.net.URI
import java.sql.Date
import java.time.LocalDate
import java.util.Collections.singletonList
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VcService(
    @Value("\${vc.privateKey}") val privateKey: String,
    @Value("\${vc.issuer}") val issuerAddress: String,
    @Autowired val cryptoService: CryptoService
) {

    fun issueAndSign(
        id: String,
        type: String,
        subject: Any
    ): SignedJWT {
        return sign(issue(id, type, subject))
    }

    fun issue(
        id: String,
        type: String,
        subject: Any
    ): VerifiableCredential {
        return VerifiableCredential().apply {
            this.id = URI.create("https://deals.weintegrator.com/api/v0/deals-webapp-app/public/vc/$id")
            addTypes(singletonList(type))
            issuer = URI.create("did:we:$issuerAddress")
            setIssuanceDate(Date.valueOf(LocalDate.now()))
            setExpirationDate(Date.valueOf(LocalDate.now().plusYears(1)))
            credentialSubject = subject
        }
    }

    fun sign(vc: VerifiableCredential): SignedJWT {
        val privateKey = cryptoService.loadPrivateKey(privateKey, "secp256r1")

        return VerifiableSignedJWT.sign(
                vc,
                JWSAlgorithm.ES256,
                "did:we:$issuerAddress#owner",
                UUID.randomUUID().toString(),
                ECDSASigner(privateKey)
        )
    }
}
