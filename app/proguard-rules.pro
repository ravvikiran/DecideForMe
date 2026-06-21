# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.decideforme.**$$serializer { *; }
-keepclassmembers class com.decideforme.** {
    *** Companion;
}
-keepclasseswithmembers class com.decideforme.** {
    kotlinx.serialization.KSerializer serializer(...);
}
