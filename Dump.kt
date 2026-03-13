import com.google.android.gms.maps3d.GoogleMap3D
fun main() {
    GoogleMap3D::class.java.methods.forEach { println(it.name) }
}
