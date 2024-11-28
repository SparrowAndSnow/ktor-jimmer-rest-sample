package com.example.route

class Page(
    var enabled: Boolean = true,
    var pageIndex: Int = 0,
    var pageSize: Int = 10,
)

interface PageProvider {
    var page: Page

    class Impl : PageProvider {
        override var page: Page = Page()
    }
}

inline fun PageProvider.page(block: Page.() -> Unit) {
    block(page)
}
