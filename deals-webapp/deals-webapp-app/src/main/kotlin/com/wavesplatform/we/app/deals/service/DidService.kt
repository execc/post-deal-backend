package com.wavesplatform.we.app.deals.service

import com.nimbusds.jwt.SignedJWT
import com.wavesplatform.vst.tx.observer.annotation.VstBlockListener
import com.wavesplatform.vst.tx.observer.annotation.VstKeyFilter
import com.wavesplatform.vst.tx.observer.api.model.VstKeyEvent
import com.wavesplatform.we.app.deals.contract.service.DealsContractService
import com.wavesplatform.we.app.deals.domain.Did
import com.wavesplatform.we.app.deals.domain.type.DidStatus.ACTIVE
import com.wavesplatform.we.app.deals.domain.type.DidStatus.PENDING
import com.wavesplatform.we.app.deals.repository.DidRepository
import com.wavesplatform.we.app.deals.vc.toIdentityVc
import java.util.Random
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DidService(
    private val didRepository: DidRepository,
    private val smsService: SmsService,
    private val userService: UserService,
    private val vcService: VcService,
    private val dealsContractService: DealsContractService,
    private val cryptoService: CryptoService
) {

    val log: Logger = LoggerFactory.getLogger(DidService::class.java)

    @Transactional
    fun issue(did: Did): String {
        val code = Random().ints(1000, 9999)
                .findFirst().asInt.toString()
        log.info("Generated activation code: $code")
        smsService.sendActivationMessage(did.phoneNumber, code)
        log.info("Finished sending sms")
        val challenge = UUID.randomUUID().toString()
        val didToSave = did.copy(
                activationCode = code,
                challenge = challenge
        )
        didRepository.save(didToSave)
        log.info("Saved DID [NEW] ${didToSave.publicKey}")
        return challenge
    }

    @Transactional
    fun activate(
        publicKey: String,
        activationCode: String,
        signedChallenge: String
    ): SignedJWT {
        log.info("Activating DID [NEW] $publicKey")
        val did = didRepository.getOne(publicKey)
        log.info("Got DID [NEW] $publicKey")
        require(activationCode == did.activationCode) {
            "Invalid activation code"
        }
        require(cryptoService.verify(
                signedChallenge,
                did.challenge,
                did.biometricPublicKey
        )) {
            "Invalid challenge signature"
        }
        require(ACTIVE != did.status) {
            "DID should not be activated"
        }

        log.info("Success checking activation key for: $publicKey")
        val didToSave = did.copy(
                status = PENDING
        )

        userService.register(didToSave)
        log.info("Success registering account for $publicKey")
        val txId = dealsContractService.issueDid(publicKey)
        log.info("Success sending tx $txId for $publicKey")
        val vc = vcService.issueAndSign(txId, "Identity", did.toIdentityVc(txId))
        log.info("Success issue vc for tx $txId for $publicKey")
        didRepository.save(didToSave)
        log.info("Success saving $publicKey")
        return vc
    }

    @Transactional
    @VstBlockListener
    fun handleDidOnContract(
        @VstKeyFilter(keyPrefix = "did:we:") e: VstKeyEvent<String>
    ) {
        log.info("Handling contract DID [PENDING] ${e.tx.id}")
        val key = e.tx.results[0].key
        val publicKey = key.substring("did:we:".length)
        val did = didRepository.findById(publicKey)
        if (did.isPresent) {
            val didToSave = did.get().copy(
                    status = ACTIVE
            )
            didRepository.save(didToSave)
            log.info("Success saving $publicKey")
        }
    }
}
