package com.example.domain.entity

import org.babyfish.jimmer.sql.*

@Entity
interface BookStore : BaseEntity {

    @Key
    val name: String

    val website: String?
}
