# Add project specific ProGuard rules here.
-keep class com.mgm.lostfound.** { *; }
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}

