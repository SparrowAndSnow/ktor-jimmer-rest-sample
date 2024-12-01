package com.example.route

import com.example.route.crud.Configuration

class Page(
    var enabled: Boolean = Configuration.defaultEnabledPage,
    var pageIndex: Int = Configuration.defaultPageIndex,
    var pageSize: Int = Configuration.defaultPageSize,
)

interface PageProvider {
    var page: Page
}

inline fun PageProvider.page(block: Page.() -> Unit) {
    block(page)
}
