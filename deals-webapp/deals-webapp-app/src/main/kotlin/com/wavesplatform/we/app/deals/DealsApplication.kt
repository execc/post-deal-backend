package com.wavesplatform.we.app.deals

import com.wavesplatform.vst.tx.observer.api.model.BlockHeightInfo
import com.wavesplatform.we.app.deals.domain.Did
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication
@EntityScan(basePackageClasses = [
    Did::class,
    BlockHeightInfo::class
])
class DealsApplication

fun main(args: Array<String>) {
    SpringApplication.run(DealsApplication::class.java, *args)
}
