package com.wavesplatform.we.app.deals.controller

import com.wavesplatform.we.app.deals.api.dto.IssueVcRequest
import com.wavesplatform.we.app.deals.api.dto.IssueVcResponse
import com.wavesplatform.we.app.deals.domain.type.VcPublicStatus
import com.wavesplatform.we.app.deals.service.VcBcService
import com.wavesplatform.we.app.deals.service.VcCoordinatorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("public/vc")
class VcPublicController(
    val vcBcService: VcBcService,
    val vcCoordinatorService: VcCoordinatorService
) {

    @GetMapping("{id}")
    fun status(@PathVariable id: String): VcPublicStatus {
        return vcBcService.status(id)
    }

    @PostMapping
    fun initialize(@RequestBody request: IssueVcRequest): IssueVcResponse {
        val draftId = vcCoordinatorService.initialize(request.participants, request.data)
        return IssueVcResponse(draftId)
    }
}
