# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep your data models and states
-keep class com.hp77.linkstash.domain.model.** { *; }
-keep class com.hp77.linkstash.data.local.entity.** { *; }
-keep class com.hp77.linkstash.presentation.**.State { *; }
-keep class com.hp77.linkstash.presentation.**.Event { *; }
-keep class com.hp77.linkstash.presentation.addlink.AddEditLinkScreenState { *; }
-keepclassmembers class com.hp77.linkstash.presentation.addlink.AddEditLinkScreenState {
    <fields>;
    <methods>;
}

# Keep navigation arguments
-keepclassmembers class ** implements androidx.navigation.NavArgs {
    *;
}
-keep class com.hp77.linkstash.presentation.navigation.** { *; }

# Keep sealed classes and their subclasses
-keep,allowobfuscation class com.hp77.linkstash.presentation.addlink.AddEditLinkScreenEvent
-keep,allowobfuscation class com.hp77.linkstash.presentation.addlink.AddEditLinkScreenEvent$* {
    *;
}

# Keep data classes and their companions
-keepclassmembers class * {
    public static ** Companion;
    public static ** Default;
    public static ** Factory;
    public static ** INSTANCE;
}
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Kotlin Serialization and Reflection
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep Room entities and queries
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract androidx.room.Dao *;
}
-dontwarn androidx.room.paging.**

# Keep Hilt and dependency injection
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclasseswithmembers class * {
    @dagger.* <methods>;
    @javax.inject.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep SavedStateHandle
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Keep Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.** {
    *;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable implementations
-keepnames class * implements java.io.Serializable

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep StateFlow and SharedFlow
-keepclassmembers class kotlin.coroutines.Continuation {
    *;
}
-keepclassmembers class kotlinx.coroutines.flow.** {
    *;
}
