package com.wavesplatform.we.app.deals.contract.service

import com.wavesplatform.vst.contract.factory.ContractClientFactory
import com.wavesplatform.we.app.deals.contract.DealsContract
import org.springframework.stereotype.Service

@Service
class DealsContractService(
    val factory: ContractClientFactory<DealsContract>
) {

    fun issueDid(publicKey: String): String {
        val api = factory.client { it.contractName("did-$publicKey") }
        api.contract().issueDid(publicKey)
        return api.lastTxId
    }

    fun issueVc(publicKey: String): String {
        val api = factory.client { it.contractName("vc-$publicKey") }
        api.contract().issueVc()
        return api.lastTxId
    }
}
