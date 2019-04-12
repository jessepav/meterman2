@echo off
pushd %~dp0..
del meterman2-src.7z
"c:\Program Files\7-Zip\7z.exe" a meterman2-src.7z @scripts\srclist.txt -bb ^
    -xr!.*.marks -xr!.svn -xr!*.png -xr!*.jpg -xr!*.ttf -xr!*.ogg -xr!*.wav -xr!*.aseprite -xr!*.ase
popd
