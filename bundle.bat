:: Optional Bat script to run the Chronical Archiver.
@ECHO OFF
PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0chronical.ps1'"; 
