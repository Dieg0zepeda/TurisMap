# =====================================================================
# Reglas R8 / ProGuard para TurisMap (Release)
# =====================================================================

# Atributos necesarios para reflexión, anotaciones, kotlinx.serialization y stack traces legibles
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable

# ---------------------------------------------------------------------
# kotlinx.serialization
# ---------------------------------------------------------------------
-keep,includedescriptorclasses class com.turismap.app.**$$serializer { *; }
-keepclassmembers class com.turismap.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.turismap.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Transient <fields>;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------
# Modelos y DTOs de TurisMap (Firestore + GitLive + reflexión)
# ---------------------------------------------------------------------
-keep class com.turismap.app.data.models.** { *; }
-keep class com.turismap.app.data.repository.** { *; }
-keepclassmembers class com.turismap.app.data.models.** { *; }
-keepclassmembers class com.turismap.app.data.repository.** { *; }

# ---------------------------------------------------------------------
# GitLive Firebase KMP y Firebase Android SDK
# ---------------------------------------------------------------------
-keep class dev.gitlive.firebase.** { *; }
-keepclassmembers class dev.gitlive.firebase.** { *; }
-dontwarn dev.gitlive.firebase.**
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# ---------------------------------------------------------------------
# Google Play Services (Maps, Location, Auth) y Coroutines
# ---------------------------------------------------------------------
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn kotlinx.coroutines.**
