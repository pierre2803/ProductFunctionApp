package com.pierre2803.functionapp

import com.microsoft.azure.functions.OutputBinding

class CaptureOutputBinding<T> : OutputBinding<T> {
    private var t: T? = null
    override fun setValue(value: T) {
        t = value
    }
    override fun getValue(): T? = t
}
