package com.hruby.navmodule

interface Navigator {
    fun openStanovisteDetail(stanovisteId: Int)
    fun goBackToStanovisteList()
    fun openUlDetail(ulId: Int)
}