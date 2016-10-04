ExtraCells 2
==========

A mighty Add-On for Applied Energistics 2

The code lives in one of two branches depending on your version of Applied Energistics. If you are using rv1, check out the rv1 branch. If you are using rv2, use the rv2 branch.
The master branch is a placeholder for people arriving at this page.

[Explore rv1 code here.](https://github.com/M3gaFr3ak/ExtraCells2/tree/rv1)  
[Explore rv2 code here.](https://github.com/M3gaFr3ak/ExtraCells2/tree/rv2)

### Build Instructions

As AE2 is currently a slightly volatile dependency, and we don't need any of the recursive dependencies, it is not declared in build.gradle. Download the correct API jar for AE2 and place it in libs/.

Then, to build using Gradle:

```
./gradlew setupDevWorkspace
./gradlew build
```

Follow up with `./gradlew eclipse` or `./gradlew idea` if necessary.

---

Thanks to arbrarsyed for ForgeGradle, to AlgorithmX2 for his massive support and his *amazing* API, cyntain and tgame14 for some very cool textures and Vexatos, crafteverywhere, SSCXM and VeryBigBro for the cool localizations! 

Drone.IO:
[![Build Status](https://drone.io/github.com/M3gaFr3ak/ExtraCells2/status.png)](https://drone.io/github.com/M3gaFr3ak/ExtraCells2/files)

ExtraCells 2 is licensed under the MIT license.
