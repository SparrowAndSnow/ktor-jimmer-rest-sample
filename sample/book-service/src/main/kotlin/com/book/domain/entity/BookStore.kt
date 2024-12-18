package com.book.domain.entity

import org.babyfish.jimmer.sql.*

@Entity
interface BookStore : BaseEntity {
    @Key
    val name: String

    val website: String?

    @OneToMany(mappedBy = "store")
    val books: List<Book>
}
