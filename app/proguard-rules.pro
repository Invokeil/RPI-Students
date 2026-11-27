# kotlinx-serialization DTOs
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class com.rpi.student.**$$serializer { *; }
-keep,includedescriptorclasses class com.rpi.student.**$$ExternalSyntheticLambda* { *; }

# Appwrite SDK (ktor transport + models keep)
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-keep class io.appwrite.** { *; }
-keep class de.julianbaer.** { *; }
