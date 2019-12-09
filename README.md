# flutter_compile_dynamic

Test the replacement libapp.so of Flutter in Android.

[![Platform](https://img.shields.io/badge/Platform-android-blue.svg)]()

## Getting Started

```java
// MyFlutterMain.java
File dir = applicationContext.getDir("libs", Activity.MODE_PRIVATE);
String libPath =  dir.getAbsolutePath() + File.separator + "libapp.so";
shellArgs.add("--aot-shared-library-name=" + libPath);
```

run adb push commend

`adb push xxx/lib/libapp.so /storage/emulated/0/`

## Note

Only build release is supported