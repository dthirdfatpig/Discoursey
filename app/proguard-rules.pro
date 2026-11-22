# Discoursey Proguard Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class org.discoursey.android.data.api.models.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
