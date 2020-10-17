package com.wavesplatform.we.app.deals.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import com.wavesplatform.we.app.deals.contract.service.DealsContractService
import com.wavesplatform.we.app.deals.domain.VcDraft
import com.wavesplatform.we.app.deals.repository.DidRepository
import com.wavesplatform.we.app.deals.repository.VcDraftRepository
import com.wavesplatform.we.app.deals.vc.DealVcSubject
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Suppress("UNCHECKED_CAST")
@Service
class VcCoordinatorService(
    val didRepository: DidRepository,
    val vcDraftRepository: VcDraftRepository,
    val dealsContractService: DealsContractService,
    val cryptoService: CryptoService,
    val vcService: VcService,
    val objectMapper: ObjectMapper
) {
    @Transactional
    fun initialize(
        participants: List<String>,
        data: Map<String, Any>
    ): String {
        participants.map {
            didRepository.getOne(it)
        }
        val draft = VcDraft(
                id = UUID.randomUUID().toString(),
                participants = participants,
                data = objectMapper.writeValueAsString(data)
        )
        vcDraftRepository.save(draft)
        return draft.id
    }

    @Transactional
    fun requestSignature(
        participant: String,
        id: String
    ): Pair<String, Map<String, Any>> {
        val draft = vcDraftRepository.getOne(id)
        require(draft.participants.contains(participant)) {
            "Not a signer of this VC"
        }
        val challenge = cryptoService.sha256(draft.data + participant)
        val dataMap = objectMapper.readValue(draft.data, Map::class.java) as Map<String, Any>
        return Pair(challenge, dataMap)
    }

    @Transactional
    fun sign(
        participant: String,
        signedChallenge: String,
        id: String
    ): Boolean {
        val did = didRepository.getOne(participant)
        val draft = vcDraftRepository.getOne(id)
        val challenge = cryptoService.sha256(draft.data + participant)
        require(draft.participants.contains(participant)) {
            "Not a signer of this VC"
        }
        require(!draft.signed(participant)) {
            "Alredy signed this VC"
        }
        require(cryptoService.verify(
                signedChallenge,
                challenge,
                did.biometricPublicKey
        )) {
            "Invalid challenge signature"
        }
        draft.signatures[participant] = signedChallenge
        if (draft.signed()) {
            finalize(draft)
        }
        vcDraftRepository.save(draft)
        return draft.issued
    }

    @Transactional
    fun getJwt(
        participant: String,
        id: String
    ): SignedJWT {
        val draft = vcDraftRepository.getOne(id)
        require(draft.participants.contains(participant)) {
            "Not a signer of this VC"
        }
        require(draft.issued) {
            "VC not issued"
        }
        return SignedJWT.parse(draft.jwt)
    }

    @Transactional
    fun getDrafts(
        participant: String
    ): List<VcDraft> {
        return vcDraftRepository.findByParticipants(participant)
    }

    private fun finalize(draft: VcDraft) {
        val txId = dealsContractService.issueVc(vcService.issuerAddress)
        val jwt = vcService.issueAndSign(
                txId,
                "Deal",
                DealVcSubject(
                        id = txId,
                        participants = draft.participants,
                        data = objectMapper.readValue(draft.data, Map::class.java) as Map<String, Any>,
                        signatures = draft.signatures
                )
        )
        draft.jwt = jwt.serialize()
        draft.issued = true
    }
}
