package com.wavesplatform.we.app.deals.config

import io.swagger.annotations.ApiModelProperty
import java.util.Collections
import java.util.Collections.singletonList
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RestController
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.GrantType
import springfox.documentation.service.OAuth
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.SecurityConfiguration
import springfox.documentation.swagger.web.SecurityConfigurationBuilder
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
@Import(value = [BeanValidatorPluginsConfiguration::class])
class SwaggerConfig {

    @Value("\${swagger.oauth-url}")
    private val tokenUrl: String? = null
    @Value("\${security.oauth2.client.client-secret}")
    private val clientSecret: String? = null
    @Value("\${swagger.basePath}")
    private val basePath: String? = null

    @Bean
    fun apiDocs(): Docket = Docket(DocumentationType.SWAGGER_2)
            .pathMapping(basePath)
            .enable(true)
            .groupName("default")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.wavesplatform"))
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
            .build()
            .securityContexts(singletonList(securityContext()))
            .securitySchemes(singletonList(securitySchema()))
            .apiInfo(ApiInfoBuilder().title("deals-app")
                    .description("API of dApp deals-app")
                    .build())
            .directModelSubstitute(Pageable::class.java, SwaggerPageable::class.java)

    @Bean
    fun securityInfo(): SecurityConfiguration = SecurityConfigurationBuilder.builder()
            .clientId("demo-client")
            .clientSecret(clientSecret)
            .build()

    private fun securitySchema(): OAuth {
        val grantTypes = ArrayList<GrantType>()
        val passwordCredentialsGrant = ResourceOwnerPasswordCredentialsGrant(tokenUrl)
        grantTypes.add(passwordCredentialsGrant)

        return OAuth("oauth2", Collections.emptyList(), grantTypes)
    }

    private fun securityContext(): SecurityContext =
            SecurityContext.builder()
                    .securityReferences(defaultAuth())
                    .forPaths(PathSelectors.any())
                    .build()

    private fun defaultAuth(): List<SecurityReference> = singletonList(SecurityReference("oauth2", arrayOf()))

    data class SwaggerPageable(
        @ApiModelProperty("Number of records per page", example = "20")
        val size: Int?,
        @ApiModelProperty("Results page you want to retrieve (0..N)", example = "0")
        val page: Int?,
        @ApiModelProperty(
                "Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. " +
                        "Multiple sort criteria are supported.",
                example = "created,asc"
        )
        var sort: String?
    )
}
