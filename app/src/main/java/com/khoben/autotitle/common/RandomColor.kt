package com.khoben.autotitle.common

import java.util.*

object RandomColor {

    private val recycle: Stack<Int> = Stack()
    private val colors: Stack<Int> = Stack()

    val color: Int
        get() {
            if (colors.empty()) {
                while (!recycle.empty()) {
                    colors.push(recycle.pop())
                }
                Collections.shuffle(colors)
            }
            val color = colors.pop()
            recycle.push(color)
            return color
        }

    init {
        recycle.addAll(
            listOf(
                -0xbbcca, -0x16e19d, -0x63d850, -0x98c549,
                -0xc0ae4b, -0xde690d, -0xfc560c, -0xff432c,
                -0xff6978, -0xb350b0, -0x743cb6, -0x3223c7,
                -0x14c5, -0x3ef9, -0x6800, -0xa8de,
                -0x86aab8, -0x616162, -0x9f8275, -0xcccccd
            )
        )
    }
}