package com.wavesplatform.we.app.deals.domain

import com.wavesplatform.we.app.deals.domain.type.VcDraftStatus.AWAITING_SIGNATURE
import com.wavesplatform.we.app.deals.domain.type.VcDraftStatus.ISSUED
import com.wavesplatform.we.app.deals.domain.type.VcDraftStatus.SIGNED
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.MapKeyJoinColumn
import javax.persistence.Table

@Entity
@Table(name = "VC_DRAFT")
data class VcDraft(
    @Id
    @Column(name = "ID")
    val id: String,

    @ElementCollection
    @CollectionTable(
            name = "VC_DRAFT_PARTICIPANT",
            joinColumns = [ JoinColumn(name = "ID", referencedColumnName = "ID") ]
    )
    @Column(name = "PARTICIPANT")
    val participants: List<String>,

    @ElementCollection
    @CollectionTable(
            name = "VC_DRAFT_SIGNATURE",
            joinColumns = [ JoinColumn(name = "ID", referencedColumnName = "ID") ]
    )
    @MapKeyJoinColumn(name = "PARTICIPANT")
    @Column(name = "SIGNATURE")
    val signatures: MutableMap<String, String> = mutableMapOf(),

    @Column(name = "DATA")
    val data: String,

    @Column(name = "ISSUED")
    var issued: Boolean = false,

    @Column(name = "JWT")
    var jwt: String? = null
) {
    fun signed() = signatures.keys.containsAll(participants)

    fun signed(participant: String) = signatures.keys.contains(participant)

    fun participantStatus(participant: String) = when {
            issued -> ISSUED
            signed(participant) -> SIGNED
            else -> AWAITING_SIGNATURE
        }
}
