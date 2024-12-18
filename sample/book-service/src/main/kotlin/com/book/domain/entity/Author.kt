package com.book.domain.entity

import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.*

@Entity
interface Author : BaseEntity {

    @Key
    val firstName: String

    @Key
    val lastName: String

    @Formula(dependencies = ["firstName","lastName"])
    val name: String?
        get() = "$firstName $lastName"

    @ManyToMany(mappedBy = "authors")
    val books: List<Book>

    val gender: Gender
}
