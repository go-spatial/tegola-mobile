#!/usr/bin/env bash

arch=arm64
apilevel=21
OUTPUT_DIR=$GOPATH/pkg/github.com/terranodo/tegola/android/api-$apilevel/$arch
echo "Cleaning OUTPUT_DIR: $OUTPUT_DIR..."
rm -rf $OUTPUT_DIR
if [ ! -r $OUTPUT_PATH ]
then
	echo "$OUTPUT_DIR cleaned"
else
	echo "Nothing to clean - $OUTPUT_DIR does not exist"
fi