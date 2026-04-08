# Sorma<sup>2</sup> - Simple ORM(Android)<sup>2</sup> <sub>(also for Desktop)<sub>

sorma2 is a two-phase ORM system that generates Java code from annotated schema definitions during development, then uses that generated code at runtime for database operations.<br>

It is based on the wonderful https://github.com/maskarade/Android-Orma by [FUJI Goro](https://github.com/gfx)
<br>
<br>
This is a rewrite in pure java, to generate most stuff you need and then add it to your project
either as a ```.jar file``` or as ```java source```.<br>
You still need to add ```sqlite-jdbc-3.51.3.0.jar``` to your project to use it.<br>
<br><br>
~~sadly [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) project decided that it needs ```slf4j-api jar``` (for no good reason).~~
this was solved by: https://github.com/xerial/sqlite-jdbc/pull/1178
<br><br>
see: https://github.com/xerial/sqlite-jdbc/issues/1094<br>
<br>

[![build](https://github.com/zoff99/iocipher_pack/actions/workflows/ci.yml/badge.svg)](https://github.com/zoff99/iocipher_pack/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)
[![Liberapay](https://img.shields.io/liberapay/goal/zoff.svg?logo=liberapay)](https://liberapay.com/zoff/donate)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/zoff99/sorma2)

<img src="https://raw.githubusercontent.com/zoff99/sorma2/refs/heads/master/sorma2_coms.png" width="300">

## What is Sorma2?

sorma2 is a pure Java code generator designed for efficient SQLite database access. It is suitable for both Android and Desktop Java applications.

- **Purpose:** Generates Java source code for SQLite ORM based on schema definition files.
- **Approach:** Unlike runtime ORMs, Sorma2 generates all necessary code at development time, avoiding reflection overhead and ensuring compile-time type safety.


## Architecture

### 1. Development Phase
- Developers write schema files prefixed with `_sorm_`.
- Schema files contain annotated Java class definitions.
- The **Generator** class processes these files to produce complete ORM implementation code.

### 2. Runtime Phase
- The generated code offers a fluent API for database operations.
- Platform-specific JDBC drivers handle actual SQLite communication.


## Key Characteristics

- **No Reflection at Runtime:** Improves performance and type safety.
- **Readable Java Source Code:** Generated code is clear and maintainable.
- **Type-safe Query Building API:** Ensures compile-time correctness.
- **Support for Encrypted Databases:** Compatible with SQLCipher.
- **Multi-threaded Operation:** Supports WAL mode for concurrency.
- **Cross-platform Compatibility:** Works on Android API 21+ and Desktop Java 17+.


# Usage

create one file for each database table that you need.
<br>
create file for db table `Person` as `./gen/_sorm_Person.java`
<br>(don't worry it is not really Java, we just use the syntax here)
```Java
@Table
public class Person
{
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
    @Column
    public String address;
    @Column
    public int social_number;
}
```

now create the Java sources with the Java SORMA2 Generator. <b>you need at least java 17</b>.<br>
```bash
java -classpath ".:sqlite-jdbc-3.51.3.0.jar:sorma2.jar" \
  com/zoffcc/applications/sorm/Generator "gen"
```

your project is now ready to start.<br>
enter the generator directory:
```bash
cd ./gen/
ls -al
```

now move or copy all *.java files from the generator directory into your Android or Java project source tree
```bash
cd gen/
# remove class files
find . -name '*.class'|xargs rm -v
cp -av ./com /home/user/your/project/source/tree/
```

in your Java project you will need the `sqlite jdbc jar` and in your
Android project you will need `com.github.zoff99:pkgs_zoffccAndroidJDBC` from [jitpack.io](https://jitpack.io/#zoff99/pkgs_zoffccAndroidJDBC)

add this dependency to your `build.gradle` file

```
implementation 'com.github.zoff99:pkgs_zoffccAndroidJDBC:1.0.24'
```


Android Example App:
------------------------

see: https://github.com/zoff99/sorma2/tree/master/example_android

<img src="https://github.com/zoff99/sorma2/releases/download/nightly/android_screen01_21.png" height="300"></a><img src="https://github.com/zoff99/sorma2/releases/download/nightly/android_screen01_29.png" height="300"></a><img src="https://github.com/zoff99/sorma2/releases/download/nightly/android_screen01_33.png" height="300"></a><img src="https://github.com/zoff99/sorma2/releases/download/nightly/android_screen01_35.png" height="300"></a>
<br>


Linux Java Example App:
------------------------

see: https://github.com/zoff99/sorma2/tree/master/test

<img src="https://github.com/zoff99/sorma2/releases/download/nightly/console_screen.png" width="70%">

Use the `sorma_generated.jar` (that is generated in the `gen` directory) and `sqlite-jdbc-3.51.3.0.jar` for the Java project.<br>
Check `TestSorma.java` for an Example usage.
<br>

Desktop (Linux / Windows / macOS) Gradle Example Project
------------------------
see: https://github.com/zoff99/sorma2/tree/master/example_gradle_linux
<br>


<br>
Any use of this project's code by GitHub Copilot, past or present, is done
without our permission.  We do not consent to GitHub's use of this project's
code in Copilot.
<br>
No part of this work may be used or reproduced in any manner for the purpose of training artificial intelligence technologies or systems.

