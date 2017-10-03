echo off
setlocal
set arch=arm
set apilevel=15
set OUTPUT_DIR=%GOPATH%\pkg\github.com\terranodo\tegola\android\api-%apilevel%\%arch%
echo "Cleaning OUTPUT_DIR: %OUTPUT_DIR%..."
rm -rf %OUTPUT_DIR%
if not exist "%OUTPUT_PATH%" (
	echo "%OUTPUT_DIR% cleaned"
) else (
	echo "Could not clean %OUTPUT_DIR%"
)