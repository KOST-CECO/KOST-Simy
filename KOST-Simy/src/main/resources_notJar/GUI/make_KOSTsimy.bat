@ECHO OFF
SETLOCAL

REM Make KOST-Simy_de.exe
C:\Tools\NSIS\makensis.exe KOSTsimy_de.nsi

REM Make KOST-Simy_fr.exe
DEL KOSTsimy_fr.nsi
C:\Tools\PCUnixUtils\sed.exe -f KOSTsimy_fr.script KOSTsimy_de.nsi > KOSTsimy_fr.nsi
C:\Tools\NSIS\makensis.exe KOSTsimy_fr.nsi

REM Make KOST-Simy_en.exe
DEL KOSTsimy_en.nsi
C:\Tools\PCUnixUtils\sed.exe -f KOSTsimy_en.script KOSTsimy_de.nsi > KOSTsimy_en.nsi
C:\Tools\NSIS\makensis.exe KOSTsimy_en.nsi

MOVE /Y KOST-Simy_fr.exe ..\
MOVE /Y KOST-Simy_en.exe ..\
MOVE /Y KOST-Simy_de.exe ..\

PAUSE