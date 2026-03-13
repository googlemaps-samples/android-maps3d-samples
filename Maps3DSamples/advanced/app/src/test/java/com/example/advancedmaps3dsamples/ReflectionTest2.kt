package com.example.test
import org.junit.Test
import com.google.android.gms.maps3d.model.Polygon

class ReflectionTest2 {
    @Test
    fun testReflection() {
        println("--- POLYGON METHODS ---")
        Polygon::class.java.methods.forEach { 
            println("METHOD: ${it.name}(${it.parameterTypes.joinToString { p -> p.simpleName }})") 
        }
        println("-----------------------------")
    }
}
