package com.wavesplatform.we.app.deals.config

import com.wavesplatform.vst.security.commons.OAuth2TokenSupport
import com.wavesplatform.vst.security.commons.OAuth2TokenSupportImpl
import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("!test")
class ResourceServerConfig(
    @Value("\${security.oauth2.resource.id}") val resourceId: String,
    val tokenServices: ResourceServerTokenServices,
    val tokenStore: TokenStore
) : ResourceServerConfigurerAdapter() {

    @Bean
    fun oauth2FeignRequestInterceptor(
        oauth2ClientContext: OAuth2ClientContext,
        resource: OAuth2ProtectedResourceDetails
    ): RequestInterceptor {
        return OAuth2FeignRequestInterceptor(oauth2ClientContext, resource)
    }

    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources
            .resourceId(resourceId)
            .tokenServices(tokenServices)
    }

    override fun configure(http: HttpSecurity) {
        http
            .cors()
            .and()
            .requestMatchers()
            .and()
            .authorizeRequests()
            .antMatchers(
                "/actuator/**",
                "/api-docs/**",
                "/oauth/*",
                "/public/**",
                "/v2/api-docs/**",
                "/webjars/**",
                "/swagger-resources/**",
                "/swagger-ui.html/**").permitAll()
            .antMatchers("/**").authenticated()
            .and().exceptionHandling().accessDeniedHandler(OAuth2AccessDeniedHandler())
    }

    @Bean
    fun oauth2TokenSupport(): OAuth2TokenSupport {
        return OAuth2TokenSupportImpl(tokenStore)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.addAllowedOrigin("*")
        config.addAllowedHeader("Content-Type")
        config.addAllowedHeader("x-xsrf-token")
        config.addAllowedHeader("Authorization")
        config.addAllowedHeader("Access-Control-Allow-Headers")
        config.addAllowedHeader("Origin")
        config.addAllowedHeader("Accept")
        config.addAllowedHeader("X-Requested-With")
        config.addAllowedHeader("Access-Control-Сlaim-Method")
        config.addAllowedHeader("Access-Control-Сlaim-Headers")
        config.addAllowedMethod("OPTIONS")
        config.addAllowedMethod("GET")
        config.addAllowedMethod("PUT")
        config.addAllowedMethod("POST")
        config.addAllowedMethod("DELETE")
        config.addAllowedMethod("PATCH")
        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("*")
            }
        }
    }
}
