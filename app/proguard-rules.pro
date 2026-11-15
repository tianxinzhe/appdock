# AppDock ProGuard/R8 rules

# Keep annotations, generics signatures (reflection-friendly)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Room entities (R8 handles Room automatically; defensive keep)
-keep class com.soulscop.mostusedwidget.data.local.** { *; }

# WorkManager instantiates workers via reflection
-keep class com.soulscop.mostusedwidget.util.RefreshWorker { *; }

# Widget components referenced from AndroidManifest
-keep class com.soulscop.mostusedwidget.widget.MostUsedWidgetProvider { *; }

# Strip verbose/debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
