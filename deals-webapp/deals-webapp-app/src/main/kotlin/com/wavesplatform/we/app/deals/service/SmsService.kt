package com.wavesplatform.we.app.deals.service

import java.lang.IllegalStateException
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.Base64
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.OK
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class SmsService(
    @Value("\${smsc.login}") private val login: String,
    @Value("\${smsc.password}") private val password: String,
    @Value("\${smsc.enable}") private val enable: Boolean
) {

    val log: Logger = LoggerFactory.getLogger(SmsService::class.java)

    fun sendActivationMessage(phone: String, code: String) {
        log.info("Sending sms code $code to $phone")
        if (enable) {
            val builder = UriComponentsBuilder.fromUriString("https://gate.smsaero.ru/v2/sms/send")
                    .queryParam("numbers[]", phone)
                    .queryParam("text", code)
                    .queryParam("sign", URLEncoder.encode("SMS Aero", "UTF-8"))
                    .build()
            val rq: RequestEntity<*> = RequestEntity.get(URI.create(builder.toUriString()))
                    .headers {
                        val auth = "$login:$password"
                        val encodedAuth = Base64.getEncoder().encode(
                                auth.toByteArray(Charset.forName("US-ASCII")))
                        val authHeader = "Basic " + String(encodedAuth)
                        it.set("Authorization", authHeader)
                    }.build()
            val template = RestTemplate()
            val rs = template.exchange(rq, String::class.java)

            if (rs.statusCode == OK) {
                log.info("Success sending SMS: ${rs.body}")
            } else {
                log.error("Error sending SMS: ${rs.body}")
                throw IllegalStateException("Can not send SMS: ${rs.body}")
            }
        }
    }
}
