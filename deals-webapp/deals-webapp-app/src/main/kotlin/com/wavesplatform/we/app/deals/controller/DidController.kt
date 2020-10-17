package com.wavesplatform.we.app.deals.controller

import com.wavesplatform.we.app.deals.api.dto.ActivateDidRequest
import com.wavesplatform.we.app.deals.api.dto.ActivateDidResponse
import com.wavesplatform.we.app.deals.api.dto.IssueDidRequest
import com.wavesplatform.we.app.deals.api.dto.IssueDidResponse
import com.wavesplatform.we.app.deals.domain.type.DidStatus.NEW
import com.wavesplatform.we.app.deals.domain.type.DidStatus.PENDING
import com.wavesplatform.we.app.deals.mappers.toDid
import com.wavesplatform.we.app.deals.service.DidService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("public/did")
class DidController(
    private val didService: DidService
) {

    @PostMapping("issue")
    fun issue(@RequestBody request: IssueDidRequest): ResponseEntity<IssueDidResponse> {
        val challenge = didService.issue(request.toDid())
        return ResponseEntity.status(CREATED)
                .body(IssueDidResponse(
                        status = NEW,
                        challenge = challenge
                ))
    }

    @PostMapping("activate")
    fun activate(@RequestBody request: ActivateDidRequest): ResponseEntity<ActivateDidResponse> {
        val jwt = didService.activate(
                request.publicKey,
                request.activationCode,
                request.signedChallenge
        )

        return ResponseEntity.status(OK)
                .body(ActivateDidResponse(
                        status = PENDING,
                        jwt = jwt.serialize(),
                        payload = jwt.payload.toJSONObject()
                ))
    }
}
