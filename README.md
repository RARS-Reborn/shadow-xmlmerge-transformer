# Shadow XMLMerge Transformer

This repo contains an improved XML transformer for [shadowJar](https://github.com/johnrengelman/shadow) library

Compiled jar files are available on the [JitPack package repository](https://jitpack.io/#unaimillan/shadow-xmlmerge-transformer)

## Credits

The idea of the plugin came from [this issue](https://github.com/johnrengelman/shadow/issues/812)

The repository was constructed based on examples of the 
[json-transformer](https://github.com/LogicFan/shadow-json-transformer/tree/main)
, and
xml transformers in the 
[original shade](https://github.com/apache/maven-shade-plugin/tree/master/src/main/java/org/apache/maven/plugins/shade/resource)
library and 
[shadow](https://github.com/johnrengelman/shadow/tree/main/src/main/groovy/com/github/jengelman/gradle/plugins/shadow/transformers)
transformers

## Usage

The main (tested) usage for the plugin is to create a working fat jar file for
QtJambi library

To use the plugin, add the following lines to the beginning of your `build.gradle` file (before any `plugins`)
```groovy
buildscript {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.unaimillan:shadow-xmlmerge-transformer:v0.1.0'
    }
}
```

After that, update gradle build settings and add Transformer import
```groovy
import com.github.unaimillan.gradle.plugins.shadow.transformers.XMLMergeTransformer
```

and use it in `shadowJar` task as any other transformer, for example like this:
```groovy
shadowJar {
    transform(XMLMergeTransformer.class) {
        resource = 'META-INF/qtjambi-deployment.xml'
    }
}
```
