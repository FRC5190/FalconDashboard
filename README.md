# BBQ FalconDasboard Mod

This is a utility that can be used to generate AutonTask code and visualize the robot's position on the field live using 
NetworkTables. Docs coming soon. Written in Kotlin with inline CSS. 

Initial development credits go to FRC 5190.

### Instructions:
Import as gradle project. 
In order to compile, do `./gradlew run`.

Basic control instructions:-
1. Drag to move around robot position. (Ignore the waypoint table x and y, that is still in progress, the generated code is still correct.)
2. Double click to add new point (works perfectly fine with AutonTask code generation)
3. To change robot angle (initial set to 90 for now, initial can be changed in CodeFragment.kt) pull edge of robot node and twist. 

![](https://i.gyazo.com/6ede109e7241bba6b66ebbb984d63d69.gif)

