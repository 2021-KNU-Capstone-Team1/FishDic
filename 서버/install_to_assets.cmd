:: DB, banner, model�� �� ���丮�� assets�� ��ġ ���� ��ũ��Ʈ

@echo off
setlocal
PUSHD %~DP0
set TARGET_PATH=..\FishDic\app\src\main\assets

:: DB ó��
set TARGET_DB_PATH=%TARGET_PATH%\DB
if not exist %TARGET_DB_PATH% (
	mkdir %TARGET_DB_PATH%
)
echo Target DB Path : %TARGET_DB_PATH%
copy /Y .\DB\FishDicDB.db %TARGET_DB_PATH%

:: banner ó��
set TARGET_BANNER_PATH=%TARGET_PATH%\banner
if not exist %TARGET_BANNER_PATH% (
	mkdir %TARGET_BANNER_PATH%
)
echo Target Banner Path : %TARGET_BANNER_PATH%
copy /Y .\banner\*.jpeg %TARGET_BANNER_PATH%
copy /Y .\banner\*.jpg %TARGET_BANNER_PATH%
copy /Y .\banner\*bmp %TARGET_BANNER_PATH%
copy /Y .\banner\*.gif %TARGET_BANNER_PATH%

:: model ó��
set TARGET_MODEL_PATH=%TARGET_PATH%\model
if not exist %TARGET_MODEL_PATH% (
	mkdir %TARGET_MODEL_PATH%
)
echo Target Model Path : %TARGET_MODEL_PATH%
copy /Y .\model\model.tflite %TARGET_MODEL_PATH%
:: copy /Y .\model\class_names.txt %TARGET_MODEL_PATH%

pause