-dontobfuscate
-dontoptimize
-keepattributes SourceFile,LineNumberTable

#-keep class !com.google.gson.** { *; }

-keep class com.zoffcc.applications.trifa.** { *; }
-keep class com.github.gfx.android.orma.Schema { *; }
-keep class javax.annotation.** { *; }
-keep class android.graphics.** { *; }
-keep class java.nio.file.** { *; }
-keep class okhttp3.** { *; }
-keep class org.codehaus.mojo.animal_sniffer.** { *; }

-dontwarn *
