package com.uneed.yuni


interface A {

}

interface B {
    companion object b : A
}

class Test {

    fun main() {
        B.b
    }
}