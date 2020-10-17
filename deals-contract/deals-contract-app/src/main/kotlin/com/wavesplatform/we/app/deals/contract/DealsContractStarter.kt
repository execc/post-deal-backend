package com.wavesplatform.we.app.deals.contract

import com.wavesplatform.vst.contract.grpc.VstContractApplication
import com.wavesplatform.we.app.deals.contract.impl.DealsContractImpl
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@EnableAutoConfiguration
@ComponentScan("com.wavesplatform.we.app.deals.contract")
class DealsContractStarter : VstContractApplication() {

    override fun contractHandlerPackage(): Package {
        return DealsContractImpl::class.java.`package`
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(DealsContractStarter::class.java)
        }
    }
}
