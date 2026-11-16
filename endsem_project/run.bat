@echo off
cd /d "%~dp0"
echo Starting Bird Counting Application...
start "" javaw -cp "target\classes;%USERPROFILE%\.m2\repository\net\imagej\ij\1.54f\ij-1.54f.jar" Main
