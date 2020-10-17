package com.wavesplatform.we.app.deals.contract.impl

import com.wavesplatform.vst.contract.data.Transaction
import com.wavesplatform.vst.contract.spring.annotation.ContractHandlerBean
import com.wavesplatform.vst.contract.state.ContractState
import com.wavesplatform.we.app.deals.contract.DealsContract
import com.wavesplatform.we.app.deals.domain.type.VcPublicStatus
import com.wavesplatform.we.app.deals.domain.type.VcStatus.ACTIVE
import java.sql.Date
import java.time.LocalDate

@ContractHandlerBean
class DealsContractImpl(
    private val state: ContractState,
    private val tx: Transaction
) : DealsContract {

    override fun issueVc() {
        state.put("wc:we:${tx.id}", VcPublicStatus(
                id = tx.id,
                iat = Date.valueOf(LocalDate.now()).time,
                exp = Date.valueOf(LocalDate.now().plusYears(1)).time,
                status = ACTIVE
        ))
    }

    override fun issueDid(
        participantPublicKey: String
    ) {
        state.put("did:we:$participantPublicKey", """
            {
              "@context": "https://www.w3.org/ns/did/v1",
              "id": "did:we:$participantPublicKey",
              "authentication": [{
                
                "id": "did:we:$participantPublicKey#owner",
                "type": "Ed25519VerificationKey2018",
                "controller": "did:we:$${tx.sender}",
                "publicKeyBase58": "$participantPublicKey"
              }],
              "service": [{
                "id":"did:we:$participantPublicKey#owner",
                "type": "VerifiableCredentialService",
                "serviceEndpoint": "https://deals.weintegrator.com/api/v0/deals-webapp-app"
              }]
            }
        """.trimIndent())

        state.put("wc:we:${tx.id}", VcPublicStatus(
                id = tx.id,
                iat = Date.valueOf(LocalDate.now()).time,
                exp = Date.valueOf(LocalDate.now().plusYears(1)).time,
                status = ACTIVE
        ))
    }

    override fun create() {
        state.put("create", true)
    }

    override fun invoke() {
        state.put("invoke", true)
    }
}
