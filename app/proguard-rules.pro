# PharmTrack ProGuard Rules
# Keep Room entities
-keep class com.app.pharmtrack.data.local.entity.** { *; }
# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
# Keep Navigation SafeArgs generated classes
-keep class com.app.pharmtrack.presentation.**.** { *; }
