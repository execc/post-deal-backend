package com.wavesplatform.we.app.deals.domain

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import java.time.OffsetDateTime
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@TypeDefs(
    TypeDef(name = BaseEntity.JSONB_TYPE, typeClass = JsonBinaryType::class)
)
abstract class BaseEntity {

    companion object {
        const val JSONB_TYPE: String = "jsonb"
    }

    @CreatedDate
    var created: OffsetDateTime? = null

    @LastModifiedDate
    var modified: OffsetDateTime? = null
}
