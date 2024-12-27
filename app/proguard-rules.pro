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
-keep,allowobfuscation class com.hp77.linkstash.presentation.**.Event
-keep,allowobfuscation class com.hp77.linkstash.presentation.**.Event$* {
    *;
}
-keep,allowobfuscation class com.hp77.linkstash.presentation.**.State
-keep,allowobfuscation class com.hp77.linkstash.presentation.**.State$* {
    *;
}
-keepclassmembers class * extends java.lang.Enum { *; }
-keepclassmembers class ** {
    public static ** INSTANCE;
    public static ** Companion;
    public static ** Default;
}

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
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

# Keep Retrofit interfaces and their implementations
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation interface com.hp77.linkstash.data.remote.GitHubDeviceFlowService { *; }
-keep,allowobfuscation interface com.hp77.linkstash.data.remote.GitHubService { *; }

# Keep GitHub API classes and their fields
-keep class com.hp77.linkstash.data.remote.** { *; }
-keepclassmembers class com.hp77.linkstash.data.remote.** {
    <init>(...);
}
-keepclassmembers class com.hp77.linkstash.data.remote.** {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepnames class com.hp77.linkstash.data.remote.**

# Keep Base64 utility class
-keep class android.util.Base64 { *; }

# Keep Pattern class for regex
-keep class java.util.regex.Pattern { *; }

# Keep GSON and Retrofit Response
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class retrofit2.Response { *; }
-keepclassmembers class retrofit2.Response$* { *; }

# Keep OkHttp and Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}

# Keep Moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
    ** valueOf(java.lang.String);
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Keep JSoup
-keeppackagenames org.jsoup.nodes
-keep class org.jsoup.** { *; }
-keep interface org.jsoup.** { *; }
-dontwarn org.jsoup.**

# Keep WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Keep DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Keep Markdown
-keep class org.commonmark.** { *; }
-keep interface org.commonmark.** { *; }

# Keep Coil
-dontwarn com.squareup.okhttp3.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keep class coil.** { *; }

# Keep HackerNews API models
-keep class com.hp77.linkstash.data.remote.HackerNewsUser { *; }

# Keep all models used in API responses and domain layer
-keep class com.hp77.linkstash.domain.model.** { *; }
-keep class com.hp77.linkstash.data.remote.** { *; }
-keep class com.hp77.linkstash.domain.usecase.** { *; }
-keep class com.hp77.linkstash.domain.repository.** { *; }
-keep class kotlin.Result { *; }
-keep class kotlin.Result$* { *; }

# Keep generic signatures and type parameters
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Keep Kotlin extension functions
-keep class com.hp77.linkstash.data.mapper.** { *; }
-keepclassmembers class * {
    @kotlin.jvm.JvmStatic *;
}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata { *; }

# Keep BuildConfig (for GitHub credentials)
-keep class com.hp77.linkstash.BuildConfig { *; }

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
