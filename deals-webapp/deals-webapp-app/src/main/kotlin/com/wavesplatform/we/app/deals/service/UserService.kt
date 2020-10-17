package com.wavesplatform.we.app.deals.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesplatform.vst.api.identity.VstPersonApi
import com.wavesplatform.vst.api.identity.model.CreateOrUpdatePersonRequest
import com.wavesplatform.vst.api.oauth2.VstAccountApi
import com.wavesplatform.vst.api.oauth2.model.AccountRegistrationApiDto
import com.wavesplatform.we.app.deals.domain.Did
import java.lang.IllegalArgumentException
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.OK
import org.springframework.stereotype.Service

@Service
class UserService(
    val vstPersonApi: VstPersonApi,
    val vstAccountApi: VstAccountApi,
    val objectMapper: ObjectMapper
) {

    val log: Logger = LoggerFactory.getLogger(UserService::class.java)

    fun register(did: Did) {
        val account = try {
            val account = vstAccountApi.getByUsername(did.login)
            log.info("Got existing account for did ${did.publicKey}")
            account
        } catch (e: Exception) {
            val registrationRequest = AccountRegistrationApiDto().apply {
                username = did.login
                password = did.passwordHash
                email = "test@test.ru"
                roles = listOf(
                        "WE_IDENTITY_READ",
                        "WE_PRIVACY_READ",
                        "WE_OAUTH2_READ"
                )
            }

            log.info("Registering new account for did ${did.publicKey}")
            val account = vstAccountApi.register(registrationRequest)
            log.info("Success registering new account for did ${did.publicKey} accountId = ${account.accountId}")
            account
        }

        try {
            val rs = vstPersonApi.getOneByAccountId(account.accountId)
            if (rs.statusCode == OK) {
                rs.body!!
            } else {
                throw IllegalArgumentException()
            }
            log.info("Exists person for did ${did.publicKey} accountId = ${account.accountId} personId = ${rs.body!!.personId}")
        } catch (e: Exception) {
            val personId = UUID.randomUUID()
            val personCreationRequest = CreateOrUpdatePersonRequest.builder()
                    .accountId(account.accountId)
                    .personId(personId)
                    .companyId(UUID.fromString("03836828-4326-4726-ab18-04ec811dda74"))
                    .businessRoles(listOf("USER"))
                    .firstName(did.firstName)
                    .lastName(did.lastName)
                    .email("test@test.ru")
                    .participantAddress(did.publicKey)
                    .participantPublicKey("")
                    .phone(did.phoneNumber)
                    .meta(objectMapper.createObjectNode().apply {
                        put("patronymic", did.patronymic)
                        put("biometricPublicKey", did.biometricPublicKey)
                        put("publicKey", did.publicKey)
                    })
                    .build()
            val result = vstPersonApi.createOrUpdate(personId, personCreationRequest)
            log.info("Result [${result.statusCode}] creating person for did ${did.publicKey} accountId = ${account.accountId} personId = $personId")
        }
    }
}
