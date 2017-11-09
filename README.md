# About
This is an experimental support library to work with
[Mapbox Vector Tiles](https://github.com/mapbox/vector-tile-spec).

# Android
Experimentation on Android is possible by making the following dependency reference.

```groovy
    implementation ('uk.os.vt:vt:LATEST_VERSION') {
        exclude group: 'com.google.code.findbugs'
        exclude module: 'annotations'
        exclude group: 'org.json'
        exclude module: 'json'
    }
```

