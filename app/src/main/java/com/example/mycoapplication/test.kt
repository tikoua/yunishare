package com.example.mycoapplication


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