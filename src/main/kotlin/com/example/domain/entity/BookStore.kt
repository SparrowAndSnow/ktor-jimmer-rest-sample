package com.example.domain.entity
import org.babyfish.jimmer.sql.*

@Entity
interface BookStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String

    val website: String?
}