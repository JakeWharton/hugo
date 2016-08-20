Hugo
====

Annotation-triggered method call logging for your debug builds.

As a programmer, you often add log statements to print method calls, their arguments, their return
values, and the time it took to execute. This is not a question. Every one of you does this.
Shouldn't it be easier?

Simply add `@DebugLog` to your methods and you will automatically get all of the things listed above
logged for free.

```java
@DebugLog
public String getName(String first, String last) {
  SystemClock.sleep(15); // Don't ever really do this!
  return first + " " + last;
}
```
```
V/Example: ⇢ getName(first="Jake", last="Wharton")
V/Example: ⇠ getName [16ms] = "Jake Wharton"
```

The logging will only happen in debug builds and the annotation itself is never present in the
compiled class file for any build type. This means you can keep the annotation and check it into
source control. It has zero effect on non-debug builds.

Add it to your project today!

```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.jakewharton.hugo:hugo-plugin:1.2.1'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'com.jakewharton.hugo'
```

Disable logging temporarily by adding the following:

```groovy
hugo {
  enabled false
}
```

If you want to toggle logging at runtime, use `Hugo.setEnabled(true|false)`


Local Development
-----------------

Working on this project? Here's some helpful Gradle tasks:

 * `install` - Install plugin, runtime, and annotations into local repo.
 * `cleanExample` - Clean the example project build.
 * `assembleExample` - Build the example project. Must run `install` first.
 * `installExample` - Build and install the example project debug APK onto a device.


License
--------

    Copyright 2013 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
