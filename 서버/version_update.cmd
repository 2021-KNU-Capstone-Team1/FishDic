:: DB, banner, model ���� ���� ���� ����

@echo off
setlocal
PUSHD %~DP0
set TARGET_PATH=.\

set UPDATE_ALL=0
SET YYMMDD=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%
:: ���� ������ SET YYMMDD=%DATE:~10,4%%DATE:~4,2%%DATE:~7,2%

:LOOP
echo 0 : DB, banner, model ���� ��� ����
echo 1 : DB ���� ����
echo 2 : banner ���� ����
echo 3 : model ���� ����
set /p INPUT=">>"

if "%INPUT%"=="0" (
	set UPDATE_ALL=1
	goto DB_PROC
)
if "%INPUT%"=="1" (
	goto DB_PROC
)
if "%INPUT%"=="2" (
	goto BANNER_PROC
)
if "%INPUT%"=="3" (
	goto MODEL_PROC
)

cls
goto LOOP

:: DB ó��
:DB_PROC
set TARGET_DB_PATH=%TARGET_PATH%\DB
if not exist %TARGET_DB_PATH% (
	goto TARGET_PATH_NOT_EXISTS_ERR
)

echo Target DB Path : %TARGET_DB_PATH%
echo | set /p="%YYMMDD%" > %TARGET_DB_PATH%\version

if %UPDATE_ALL%==1 (
	goto BANNER_PROC
) else (
	goto END_SUCCESS
)

:: banner ó��
:BANNER_PROC
set TARGET_BANNER_PATH=%TARGET_PATH%\banner
if not exist %TARGET_BANNER_PATH% (
	goto TARGET_PATH_NOT_EXISTS_ERR
)

echo Target Banner Path : %TARGET_BANNER_PATH%
echo | set /p="%YYMMDD%" > %TARGET_BANNER_PATH%\version

if %UPDATE_ALL%==1 (
	goto MODEL_PROC
) else (
	goto END_SUCCESS
)

:: model ó��
:MODEL_PROC
set TARGET_MODEL_PATH=%TARGET_PATH%\model
if not exist %TARGET_MODEL_PATH% (
	goto TARGET_PATH_NOT_EXISTS_ERR
)

echo Target Model Path : %TARGET_MODEL_PATH%
echo | set /p="%YYMMDD%" > %TARGET_MODEL_PATH%\version

if %UPDATE_ALL%==1 (
	goto END_SUCCESS;
) else (
	goto END_SUCCESS
)

:END_SUCCESS
pause
exit

:TARGET_PATH_NOT_EXISTS_ERR
echo ��� ��ΰ� �������� �ʽ��ϴ�.
pause