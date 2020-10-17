package com.wavesplatform.we.app.deals.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesplatform.vst.node.VstNodeClientWrapper
import com.wavesplatform.we.app.deals.api.exception.ErrorDtoDict.VC_NOT_FOUND
import com.wavesplatform.we.app.deals.api.exception.VstRestApiException
import com.wavesplatform.we.app.deals.domain.type.VcPublicStatus
import java.lang.Exception
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service

@Service
class VcBcService(
    val wrapper: VstNodeClientWrapper,
    val objectMapper: ObjectMapper
) {

    fun status(id: String): VcPublicStatus {
        return try {
            val value = wrapper.getLoadBalancingWeClient().getContractValue(
                    id,
                    "wc:we:$id"
            )

            objectMapper.readValue(
                    value!!.value as String,
                    VcPublicStatus::class.java
            )
        } catch (e: Exception) {
            throw VstRestApiException(NOT_FOUND, VC_NOT_FOUND.errorDto)
        }
    }
}
