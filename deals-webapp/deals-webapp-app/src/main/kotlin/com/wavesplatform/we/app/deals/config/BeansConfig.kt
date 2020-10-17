package com.wavesplatform.we.app.deals.config

import com.wavesplatform.vst.contract.factory.ContractAuthenticate
import com.wavesplatform.vst.contract.factory.ContractClientFactory
import com.wavesplatform.vst.contract.factory.ImageInfo
import com.wavesplatform.vst.contract.state.TxContext
import com.wavesplatform.vst.contract.utils.JsonUtils
import com.wavesplatform.vst.node.IVstNodeClientWrapper
import com.wavesplatform.vst.node.VstNodeApi
import com.wavesplatform.vst.node.WeNodeApi
import com.wavesplatform.vst.node.config.NodeCredsKeysProperties
import com.wavesplatform.vst.security.commons.OAuth2TokenSupport
import com.wavesplatform.we.app.deals.contract.DealsContract
import com.wavesplatform.we.app.deals.contract.impl.DealsContractImpl
import java.util.function.Supplier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails

@Configuration
class BeansConfig(
    val vstNodeClientWrapper: IVstNodeClientWrapper,
    val tokenSupport: OAuth2TokenSupport,
    @Value("\${deals-app.sender}")
    val techUserSender: String,
    @Value("\${deals-app.node-alias}")
    val techNodeAlias: String
) {

    @Bean
    fun vstNodeApi(): WeNodeApi = vstNodeClientWrapper.getWeClient(techNodeAlias)

    @Bean
    fun contractAuthentificate(nodeCredsKeys: NodeCredsKeysProperties): Supplier<ContractAuthenticate> {
        return Supplier {
            val auth = SecurityContextHolder.getContext().authentication
            val builder = if (auth == null || auth !is OAuth2AuthenticationDetails) {
                ContractAuthenticate.builder()
                        .sender(techUserSender)
                        .password(
                                nodeCredsKeys.config.getValue(techNodeAlias).keyStorePassword
                        )
            } else {
                ContractAuthenticate.builder()
                        .sender(tokenSupport.currentUserPersonInfo.participantAddress)
                        .password(
                                nodeCredsKeys.config.getValue(tokenSupport.currentUserPersonInfo.nodeAlias).keyStorePassword
                        )
            }
            builder.build()
        }
    }

    @Bean
    fun vstNodeApiSupplier(vstNodeApi: WeNodeApi): Supplier<VstNodeApi> {
        return Supplier {
            val auth = SecurityContextHolder.getContext().authentication
            if (auth == null || auth !is OAuth2AuthenticationDetails) {
                vstNodeApi
            } else {
                vstNodeClientWrapper.getWeClient(tokenSupport.currentUserPersonInfo.nodeAlias)
            }
        }
    }

    @Bean
    fun contractClientFactory(
        nodeCredsKeys: NodeCredsKeysProperties,
        @Value("\${deals-app.contract.image}") image: String,
        @Value("\${deals-app.contract.imageHash}") imageHash: String,
        @Value("\${deals-app.fee}") fee: Long,
        vstNodeApiSupplier: Supplier<VstNodeApi>,
        contractAuthenticate: Supplier<ContractAuthenticate>
    ): ContractClientFactory<*> {
        return ContractClientFactory.withType(DealsContract::class.java)
                .setImplementationBuilderForUpdate {
                    val context = TxContext(JsonUtils.toJson(it), vstNodeApiSupplier)
                    DealsContractImpl(context.contractState, context.tx)
                }
                .setImplementationBuilderForCreate {
                    val context = TxContext(JsonUtils.toJson(it), vstNodeApiSupplier)
                    DealsContractImpl(context.contractState, context.tx)
                }
                .setVstNodeApiSupplier(vstNodeApiSupplier)
                .setImageInfo(
                        ImageInfo.builder()
                                .image(image)
                                .imageHash(imageHash)
                                .build()
                )
                .setFee(fee)
                .setAuthenticateSupplier(contractAuthenticate)
                .setVersion(2)
    }
}
