@echo OFF
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

rem origin: https://raw.githubusercontent.com/BecauseQA/becauseQA-selenium/master/Selenium-Server-Standalone.bat
rem uses
rem wget.exe https://eternallybored.org/misc/wget/
rem xml.exe http://xmlstar.sourceforge.net/download.php
rem 7z.exe http://www.7-zip.org/
rem jq.exe https://stedolan.github.io/jq/ 

rem Define tool aliases
if defined PROGRAMFILES(X86) (
    set OS=64BIT
    set JQ="C:\TOOLS\jq-win64.exe"
    set WGET="C:\TOOLS\wget.exe"
    set XMLSTARLET="C:\TOOLS\XMLSTARLET-1.6.1\xml.exe"
) else (
    set OS=32BIT
    set WGET="C:\TOOLS\wget.exe"
    set XMLSTARLET="C:\TOOLS\XMLSTARLET-1.6.1\xml.exe"
)
rem Alternatively may add to the PATH environment
set PATH=%PATH%;C:\TOOLS
set PATH=%PATH%;C:\TOOLS\XMLSTARLET-1.6.1
set PATH=%PATH%;C:\PROGRAM FILES\7-ZIP

rem Stop selenium hub or standalone server
for /F "tokens=5 delims= " %%_ IN ('netstat -a -n -o ^| findstr :4444') do taskkill.exe /F /PID %%_

set CHROMEDRIVER_URL=https://chromedriver.storage.googleapis.com
set CHROMEDRIVER_ZIPNAME=chromedriver_win32.zip
set CHROMEDRIVER_PROCESS_NAME=chromedriver
taskkill.exe /F /IM %CHROMEDRIVER_PROCESS_NAME%.exe
set RESPONSE_FILE=response.txt
echo Determine the latest Chrome driver version
wget.exe %CHROMEDRIVER_URL%/LATEST_RELEASE -O %RESPONSE_FILE%
for /f "delims=" %%_ in (%RESPONSE_FILE%) do set BUILD=%%_
del /q %RESPONSE_FILE%
echo The latest release of Chrome driver is %BUILD%

set CHROMEDRIVER_WIN32_URL=%CHROMEDRIVER_URL%/%BUILD%/%CHROMEDRIVER_ZIPNAME%
set CHROMEDRIVER_PACKAGE=%BUILD%\%CHROMEDRIVER_ZIPNAME%
set RENAMED_CHROMEDRIVER_NAME=%CHROMEDRIVER_PROCESS_NAME%-%BUILD%.exe

if not exist "%BUILD%" (mkdir %BUILD% )
if not exist "%CHROMEDRIVER_PACKAGE:"=%" (
	echo Download Chrome driver %BUILD% from %CHROMEDRIVER_WIN32_URL%
	wget.exe -r -np -l 1 -A zip %CHROMEDRIVER_WIN32_URL% -O %CHROMEDRIVER_PACKAGE%
	7z.exe x %CHROMEDRIVER_PACKAGE% -y
  echo Chromedriver can be renamed to: %RENAMED_CHROMEDRIVER_NAME%
	rem optional - rename the exe file, and add to the path and system variables
	rem move %CHROMEDRIVER_PROCESS_NAME%.exe %RENAMED_CHROMEDRIVER_NAME%
  rem set webdriver.chrome.driver=%RENAMED_CHROMEDRIVER_NAME%
  rem setx PATH "%PATH%;%RENAMED_CHROMEDRIVER_NAME:"=%"
) else (
  echo Chrome driver %CHROMEDRIVER_PACKAGE:"=% already downloaded
)

set GECKODRIVER_URL=https://api.github.com/repos/mozilla/geckodriver/releases
echo Determine the latest Gecko driver version
set RESPONSE_FILE=response.json
wget.exe -O- -nv %GECKODRIVER_URL% > %RESPONSE_FILE%
rem TODO: correctly determine the browser_download_url
set ITEM=4
for /F "delims=" %%_ in ('%JQ:"=% [.[0].assets][0][%ITEM%].name ^< %RESPONSE_FILE:"=%') do set GECKODRIVER_NAME=%%_
for /F "delims=" %%_ in ('%JQ:"=% [.[0].assets][0][%ITEM%].browser_download_url ^< %RESPONSE_FILE:"=%') do set GECKODRIVER_URL=%%_
del /q %RESPONSE_FILE%

set GECKODRIVER_OUTPUT_FOLDER=%GECKODRIVER_NAME:~1,-11%
if not exist "%GECKODRIVER_OUTPUT_FOLDER%" (mkdir %GECKODRIVER_OUTPUT_FOLDER:"=% )
set GECKODRIVER_PACKAGE=%GECKODRIVER_OUTPUT_FOLDER%/%GECKODRIVER_NAME%

if not exist "%GECKODRIVER_PACKAGE:"=%" (
	echo Download Gecko driver from %GECKODRIVER_URL:"=%
	wget.exe -r -np -l 1 -A zip %GECKODRIVER_URL% -O %GECKODRIVER_PACKAGE%
	7z.exe x %GECKODRIVER_PACKAGE%  -y
  echo Gecko driver local zip file path is: %GECKODRIVER_PACKAGE:"=%
) else (
  echo Gecko driver %GECKODRIVER_PACKAGE:"=% is already downloaded
)
set RENAMED_GECKODRIVER_NAME=%GECKODRIVER_OUTPUT_FOLDER:"=%.exe
echo Gecko driver can be renamed as %RENAMED_GECKODRIVER_NAME:"=%
::set webdriver.gecko.driver=%RENAMED_GECKODRIVER_NAME:"=%
::echo "webdriver.gecko.driver=%webdriver.gecko.driver:"=%"

set RESPONSE_FILE=response.xml
set SELENIUM_RELEASE_URL=https://selenium-release.storage.googleapis.com
set SELENIUM_LATEST_RELEASE_URL="%SELENIUM_RELEASE_URL%/?delimiter=/&prefix="
echo Determine the latest Selenium version
wget.exe -O- -nv %SELENIUM_LATEST_RELEASE_URL% > %RESPONSE_FILE%
for /F %%_ IN ('xml.exe sel -N "x=http://doc.s3.amazonaws.com/2006-03-01" -T -t -v "//x:Prefix[last()-1]" %RESPONSE_FILE%') do set VERSION=%%_
set VERSIONNUMBER=%VERSION:/=%
echo Latest Selenium version is %VERSIONNUMBER%
set SELENIUM_FOLDER_NAME=%VERSIONNUMBER%
if not exist %SELENIUM_FOLDER_NAME% (mkdir %SELENIUM_FOLDER_NAME% )

set SELENIUM_LATEST_URL="%SELENIUM_RELEASE_URL%/?delimiter=/&prefix=%VERSION%"

for /F "tokens=1,2 delims=-" %%a in ("%VERSIONNUMBER%") do (
   set PREFIXVERSION=%%a
   set BETAVERSION=%%b
)
if /i "%BETAVERSION%"=="" (
	set SELENIUM_JAR_NAME=selenium-server-standalone-%PREFIXVERSION%.0.jar
) else (
  set SELENIUM_JAR_NAME=selenium-server-standalone-%PREFIXVERSION%.0-%BETAVERSION%.jar
)

set SELENIUM_STANDALONE_URL=%SELENIUM_RELEASE_URL%/%VERSION%%SELENIUM_JAR_NAME%
set SELENIUM_PACKAGE=%SELENIUM_FOLDER_NAME%\%SELENIUM_JAR_NAME%
if not exist "%~dp0%SELENIUM_JAR_NAME%" (
  echo Downloading Selenium jar %SELENIUM_PACKAGE:"=% from %SELENIUM_STANDALONE_URL:"=%
	wget.exe -r -np -l 1 -A zip %SELENIUM_STANDALONE_URL% -O %SELENIUM_PACKAGE%
	echo f |xcopy /f /y %SELENIUM_PACKAGE% %SELENIUM_JAR_NAME%
  echo Selenium jar file is %SELENIUM_JAR_NAME%
)

echo Downloding the IEDriver
set IEDRIVERSERVER_NAME=IEDriverServer
taskkill.exe /FI "IMAGENAME eq %IEDRIVERSERVER_NAME%*"

set RESPONSE_FILE=RESPONSE.xml

set IEDRIVER_NAME=%IEDRIVERSERVER_NAME%-%VERSIONNUMBER%.0.exe

set IEDRIVER_WIN32_FILE=IEDriverServer_Win32_%VERSIONNUMBER%.0.zip
set DOWNLOAD_IEDRIVER_WIN32=%SELENIUM_RELEASE_URL%/%VERSIONNUMBER%/%IEDRIVER_WIN32_FILE%
set IEDRIVER_WIN32_LOCATION=%SELENIUM_FOLDER_NAME%\%IEDRIVER_WIN32_FILE%
set IEDRIVER_X64_FILE=IEDriverServer_x64_%VERSIONNUMBER%.0.zip
set DOWNLOAD_IEDRIVER_X64=%SELENIUM_RELEASE_URL%/%VERSIONNUMBER%/%IEDRIVER_X64_FILE%
set IEDRIVER_X64_LOCATION=%SELENIUM_FOLDER_NAME%\%IEDRIVER_X64_FILE%

echo "Begin to download selenium server and iedrriver: %~dp0%IEDRIVER_NAME%"
rem ie driver 32 bit
rem if not exist "%~dp0%IEDRIVER_NAME%" (
rem 	 wget.exe -O- -nv %SELENIUM_LATEST_URL% > %RESPONSE_FILE%
rem     rem for /F  %%i IN ('XML.EXE sel -N "x=http://doc.s3.amazonaws.com/2006-03-01" -T -t -v "//x:Key[text()[contains(.,%iedriver_win32_prefix%)]]" %selenium_lastest_version%') do (
rem     rem  echo %%i)
rem     del /q %RESPONSE_FILE%
rem 	echo "Not Found IEDriver download before: %~dp0%IEDRIVER_NAME% ,will request to download from server."
rem 	IF /i "%OS%"=="32BIT" (
rem 		echo "Download 32 bit iedrriver into %iedriver_win32_location%"
rem 		wget.exe -r -np -l 1 -A zip %download_iedriver_win32% -O %iedriver_win32_location%
rem 		7z.exe x %iedriver_win32_location% -y		
rem 	) else (
rem 		 echo "Download 64 bit iedrriver into %iedriver_x64_location%"
rem 		 wget.exe -r -np -l 1 -A zip %download_iedriver_X64% -O %iedriver_x64_location%
rem 		 7z.exe x %iedriver_x64_location% -y
rem 	)
rem 	move %IEDRIVERSERVER_NAME%.exe %IEDRIVER_NAME%
rem ) else (
rem     echo "Found IEDriver download before: %~dp0%IEDRIVER_NAME% ,will not request to download from server."
rem )
