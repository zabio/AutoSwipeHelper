# Add project specific ProGuard rules here.

# Keep AccessibilityService
-keep class com.autoswipe.helper.accessibility.AutoSwipeService { *; }
-keep class com.autoswipe.helper.accessibility.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes
-keep class com.autoswipe.helper.detection.ScreenState { *; }
-keep class com.autoswipe.helper.config.AppConfig { *; }
-keep class com.autoswipe.helper.gesture.GestureConfig { *; }
