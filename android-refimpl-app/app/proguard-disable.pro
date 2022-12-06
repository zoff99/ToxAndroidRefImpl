-dontobfuscate
-dontoptimize
-dontshrink
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-dontwarn *.**,**

-keep class javax.** { *; }
-keep class org.** { *; }
-keep class io.** { *; }
-keep class androidx.** { *; }
-keep class com.** { *; }
-keep class *.** { *; }
