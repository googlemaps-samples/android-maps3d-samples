package com.example.test
import org.junit.Test
import com.google.android.gms.maps3d.GoogleMap3D

class ReflectionTest {
    @Test
    fun testReflection() {
        println("--- GOOGLE MAP 3D METHODS ---")
        GoogleMap3D::class.java.methods.forEach { 
            println("METHOD: ${it.name}(${it.parameterTypes.joinToString { p -> p.simpleName }})") 
        }
        println("-----------------------------")
    }
}
