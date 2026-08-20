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

# Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ZXing QR Code generation
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
