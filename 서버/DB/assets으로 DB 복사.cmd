@echo off
PUSHD %~DP0
set TARGET= ..\..\FishDic\app\src\main\assets
copy /Y FishDicDB.db %TARGET%
pause