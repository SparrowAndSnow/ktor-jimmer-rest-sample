package com.example.domain.entity

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.babyfish.jimmer.sql.*
import java.math.BigDecimal

@Entity
interface Book : BaseEntity{
    @Key
    val name: String

    @Key
    val edition: Int

    val price: BigDecimal
}