# Mars for BUAA Compiler Technology

> This is a fork of the original MARS Assembler, see the original README [here](README_MARS.md).

## Overview

This is a special Mars version for the BUAA Compiler Technology course.
It comes with extra instruction statistics feature.

## Build and Run

This is an IntelliJ IDEA project.
You should open it with IntelliJ IDEA and run the `Mars.java` file.
There is an existing run configuration called "Mars" for you to use.

To build the artifact `Mars.jar`, you should follow these steps:

1. Build the project in IntelliJ IDEA, which will generate the `out` directory.
2. Run `BuildMars.bat` in the root directory of the project. It will generate `Mars.jar` under the `out` directory.

You should be able to run the `Mars.jar` with double-click or `java -jar Mars.jar`.

## Troubleshooting

If you are not able to run the `Mars.jar` with double-click, check the following:

1. Make sure you have Java > 1.8 installed.
2. Make sure `.jar` files are associated with Java. You can check this in the system settings.

If both are correct, you may need to check the registry `HKEY_CLASSES_ROOT\jarfile\shell\open\command`.

| Name        | Type     | Data                                             |
|-------------|----------|--------------------------------------------------|
| `(Default)` | `REG_SZ` | `"path\to\java\home\bin\javaw.exe" -jar "%1" %*` |
