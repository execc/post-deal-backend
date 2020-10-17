package com.wavesplatform.we.app.deals.controller

import com.wavesplatform.vst.security.commons.OAuth2TokenSupport
import com.wavesplatform.we.app.deals.api.dto.ChallengeResponse
import com.wavesplatform.we.app.deals.api.dto.GetVcResponse
import com.wavesplatform.we.app.deals.api.dto.SignVcRequest
import com.wavesplatform.we.app.deals.api.dto.VcShortDto
import com.wavesplatform.we.app.deals.service.VcCoordinatorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("vc")
class VcController(
    val vcCoordinatorService: VcCoordinatorService,
    val oAuth2TokenSupport: OAuth2TokenSupport
) {

    @GetMapping("{id}/challenge")
    fun challenge(@PathVariable id: String): ChallengeResponse {
        val key = oAuth2TokenSupport.currentUserPersonInfo.meta["publicKey"].asText()
        val signature = vcCoordinatorService.requestSignature(key, id)
        return ChallengeResponse(signature.first, signature.second)
    }

    @PostMapping("{id}/sign")
    fun sign(
        @PathVariable id: String,
        @RequestBody request: SignVcRequest
    ) {
        val key = oAuth2TokenSupport.currentUserPersonInfo.meta["publicKey"].asText()
        vcCoordinatorService.sign(key, request.signedChallenge, id)
    }

    @GetMapping("{id}")
    fun getVc(
        @PathVariable id: String
    ): GetVcResponse {
        val key = oAuth2TokenSupport.currentUserPersonInfo.meta["publicKey"].asText()
        val vc = vcCoordinatorService.getJwt(key, id)
        return GetVcResponse(
                jwt = vc.serialize(),
                payload = vc.payload.toJSONObject()
        )
    }

    @GetMapping
    fun getVcList(): List<VcShortDto> {
        val key = oAuth2TokenSupport.currentUserPersonInfo.meta["publicKey"].asText()
        val vcs = vcCoordinatorService.getDrafts(key)
        return vcs.map {
            VcShortDto(
                    id = it.id,
                    status = it.participantStatus(key)
            )
        }
    }
}
