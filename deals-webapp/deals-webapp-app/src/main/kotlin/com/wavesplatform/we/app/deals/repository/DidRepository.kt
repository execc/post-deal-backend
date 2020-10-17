package com.wavesplatform.we.app.deals.repository

import com.wavesplatform.we.app.deals.domain.Did
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DidRepository : JpaRepository<Did, String>
