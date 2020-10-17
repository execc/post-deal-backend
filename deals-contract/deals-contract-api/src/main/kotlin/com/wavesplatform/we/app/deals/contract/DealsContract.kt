package com.wavesplatform.we.app.deals.contract

import com.wavesplatform.vst.contract.ContractAction
import com.wavesplatform.vst.contract.ContractInit
import com.wavesplatform.vst.contract.InvokeParam

interface DealsContract {

    @ContractInit
    fun create()

    @ContractInit
    fun issueDid(
        @InvokeParam(name = "participantPublicKey")
        participantPublicKey: String
    )

    @ContractInit
    fun issueVc()

    @ContractAction
    fun invoke()
}
