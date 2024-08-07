# Mars for BUAA Compiler Technology

> This is a fork of the original MARS Assembler, see the original README [here](README_MARS.md).

## Overview

This is a special Mars version for the BUAA Compiler Technology course. It comes with extra instruction statistics feature and some minor modifications.

## Build and Run

This is an IntelliJ IDEA project, you should open it with IntelliJ IDEA. All source files are under `src` directory, and there is an existing run configuration called "Mars" to run Mars in GUI.

To build the artifact `Mars.jar`, you should follow these steps:

1. Build the project in IntelliJ IDEA, which will generate .class files under `out/production` directory.
2. Run `BuildMars.bat` in the root directory of the project. It will generate `Mars.jar` under the `out` directory.

You should be able to run the `Mars.jar` with double-click or `java -jar Mars.jar` with command-line arguments.

## Testing

You can test the generated `Mars.jar` in command-line mode using `TestMars.bat`. First, make sure you have the `Mars.jar` generated in the previous step. Then, you should prepare the following files in the `test` directory (create it if not exists):

- `mips.txt`: The MIPS assembly code you want to test.
- `input.txt`: The input for the MIPS assembly code, leave it blank if no input required.
- `answer.txt`: If standard answer is available, you can put it here.

Then, you can run `TestMars.bat` under the root directory of the project. The output of the assembly will be written to `output.txt`. If you want to compare the output and answer at the same time, run `TestMars.bat -c`.

Below is the explanation for the command-line arguments, you can refer to the official document [here][1].

| Option        | Explanation                                                        |
|---------------|--------------------------------------------------------------------|
| `nc`          | terminate MARS with integer exit code n if assembly error occurs   |
| `mc Default`  | set memory configuration                                           |
| `me`          | display MARS messages to standard err instead of standard out      |
| `we`          | assembler warnings will be considered errors                       |
| `aen` (`ae1`) | terminate MARS with integer exit code `n` if assembly error occurs |
| `sen` (`se2`) | terminate MARS with exit code `n` if simulate (run) error occurs   |
| `<file>`      | the MIPS assembly file to run                                      |

## Modifications

To change the instruction statistics feature, you can modify the `mars.tools.InstructionStatistisc` class by changing the `m_instWeights` field.

In GUI mode, it won't output `InstructionStatistics.txt`. In CLI mode, this feature is injected into `mars.MarsLaunch`.

## Troubleshooting

If you are not able to run the `Mars.jar` with double-click, check the following:

1. Make sure you have Java &ge; 1.8 installed.
2. Make sure `.jar` files are associated with Java. You can check this in the system settings.

If both are correct, you may need to check the registry `HKEY_CLASSES_ROOT\jarfile\shell\open\command`.

| Name        | Type     | Data                                             |
|-------------|----------|--------------------------------------------------|
| `(Default)` | `REG_SZ` | `"path\to\java\home\bin\javaw.exe" -jar "%1" %*` |

[1]: http://courses.missouristate.edu/KenVollmar/MARS/Help/MarsHelpCommand.html    "Mars command-line arguments"
