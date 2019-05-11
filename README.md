# pi-create
Test project controlling an IRobot create from a Raspberry Pi

Requires 4 peices of hardware.
1 - IRobot Create (version 1 based on the 400 series, later versiona are based on the 500 series and use a different protocol)
2 - IRobot Create communication cable
3 - XBox360 controller for PC (either wired or wireless with dongle)
4 - Computer (tested on both laptop and Raspberry Pi)

Building

Uses maven to build, call mvn clean install to build and package

This will generate a shaded jar under targets, and a bunch of native dlls for jinput


