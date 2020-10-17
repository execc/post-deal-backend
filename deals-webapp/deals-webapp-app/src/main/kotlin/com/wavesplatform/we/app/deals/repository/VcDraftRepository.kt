package com.wavesplatform.we.app.deals.repository

import com.wavesplatform.we.app.deals.domain.VcDraft
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VcDraftRepository : JpaRepository<VcDraft, String> {

    @Query
    fun findByParticipantsAndIssuedFalse(
        participant: String
    ): List<VcDraft>

    @Query
    fun findByParticipants(
        participant: String
    ): List<VcDraft>
}
