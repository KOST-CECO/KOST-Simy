; The name of the installer
Name "KOST-Simy v0.0.7"
; Sets the icon of the installer
Icon "simy.ico"
; remove the text 'Nullsoft Install System vX.XX' from the installer window 
BrandingText "Copyright © KOST/CECO"
; The file to write
OutFile "KOST-Simy_de.exe"
; The default installation directory
; InstallDir $DESKTOP
InstallDir $EXEDIR
; Request application privileges for Windows Vista
RequestExecutionLevel user
; Sets the text for the titlebar of the installer
Caption "$(^Name)"
; Makes the installer controls use the new XP style when running on Windows XP
XPStyle on
;--------------------------------
!include WinMessages.nsh
!include FileFunc.nsh
!include LogicLib.nsh
!include getJavaHome.nsh
!include langKOSTsimy_de.nsh
!include nsDialogs.nsh
!include XML.nsh

;--------------------------------
!define INIFILE       "KOSTsimy.ini"
!define KOSTHELP      "doc\KOST-Simy_Anwendungshandbuch_*.pdf"
!define CONFIG        "KOSTsimy.conf.xml"
!define CONFIGPATH    "configuration"
!define BACKUP        "~backup"
!define JARFILE       "kostsimy_de.jar"
!define XTRANS        "resources\XTrans_1.8.0.2\XTrans.exe"
!define JAVAPATH      "resources\jre6"

;--------------------------------
Var DIALOG
Var KOSTsimyO
Var KOSTsimyR
Var LOGFILE
Var WORKDIR
Var LOG
Var HEAPSIZE
Var JAVA
Var HWND

;--------------------------------
; Pages
#LicenseData license.txt
#Page license
#Page instfiles
Page Custom ShowDialog LeaveDialog

;--------------------------------
; Functions
Function .onInit
  ; looking for java home directory
  push ${JAVAPATH}
  Call getJavaHome
  pop $JAVA
  DetailPrint "java home: $JAVA"
  
  ; initial setting for validation folder/file
  StrCpy $KOSTsimyO $EXEDIR
  StrCpy $KOSTsimyR $EXEDIR
  
  ; create configuration backup
  CreateDirectory $EXEDIR\${CONFIGPATH}\${BACKUP}
  CopyFiles /SILENT /FILESONLY $EXEDIR\${CONFIGPATH}\*.* $EXEDIR\${CONFIGPATH}\${BACKUP}

  ; Initializes the plug-ins dir ($PLUGINSDIR) if not already initialized
  InitPluginsDir
  
  ; Assign to the user variable $DIALOG, the name of a temporary file
  GetTempFileName $DIALOG $PLUGINSDIR
  
  ; Adds file(s) to be extracted to the current output path
  ;   Use /oname=X switch to change the output name
  File /oname=$DIALOG ${INIFILE}
FunctionEnd

Function .onGUIEnd
  ; reset configuration and delete backup folder
  CopyFiles /SILENT /FILESONLY $EXEDIR\${CONFIGPATH}\${BACKUP}\*.* $EXEDIR\${CONFIGPATH}
  RMDir /r  $EXEDIR\${CONFIGPATH}\${BACKUP}
FunctionEnd

Function check4Dir
  StrCpy $WORKDIR ''
  StrCpy $LOG ''
  ${xml::LoadFile} "$EXEDIR\${CONFIGPATH}\${CONFIG}" $0
  ${xml::RootElement} $0 $1
  ${xml::XPathString} "//configuration/pathtoworkdir/text()" $WORKDIR $1
  ${xml::XPathString} "//configuration/pathtologfile/text()" $LOG $1
  ${xml::Unload}
  GetFullPathName $1 $WORKDIR
  IfFileExists $1 fex not_fex
fex:
  StrCpy $WORKDIR ''
not_fex:
FunctionEnd

Function checkHeapsize
  ${If} $HEAPSIZE == '-default'
    StrCpy $HEAPSIZE ''
  ${EndIf}
FunctionEnd

;--------------------------------
Function ShowDialog
  ; Writes entry_name=value into [section_name] of ini file
  WriteINIStr $DIALOG "Settings" "NextButtonText" "${NextButtonText}"
  
  ;WriteINIStr $DIALOG "${INTRO}"                 "Text"  "${INTROTXT}"
  WriteINIStr $DIALOG "${HELP_Button}"           "Text"  "${HELP_ButtonTXT}"
  WriteINIStr $DIALOG "${ORIGINAL_FolderRequest}"   "Text"  "${ORIGINAL_FolderRequestTXT}"
  WriteINIStr $DIALOG "${ORIGINAL_FileRequest}"     "Text"  "${ORIGINAL_FileRequestTXT}"
  WriteINIStr $DIALOG "${ORIGINAL_SEL_FileFolder}"        "State" "${ORIGINAL_SEL_FileFolderTXT}"
  WriteINIStr $DIALOG "${JVM_Droplist}"          "Text"  "${JVM_DroplistTXT}"
 ; WriteINIStr $DIALOG "${INPUT_Group}"           "Text"  "${INPUT_GroupTXT}"
  WriteINIStr $DIALOG "${REPLICA_FolderRequest}"   "Text"  "${REPLICA_FolderRequestTXT}"
  WriteINIStr $DIALOG "${REPLICA_FileRequest}"     "Text"  "${REPLICA_FileRequestTXT}"
  WriteINIStr $DIALOG "${REPLICA_SEL_FileFolder}"        "State" "${REPLICA_SEL_FileFolderTXT}"
  WriteINIStr $DIALOG "${START_Validation}"      "Text"  "${START_ValidationTXT}"
  WriteINIStr $DIALOG "${EDIT_Konfiguration}"    "Text"  "${EDIT_KonfigurationTXT}"
  WriteINIStr $DIALOG "${RESET_Konfiguration}"   "Text"  "${RESET_KonfigurationTXT}"

  ; Display the validation options dialog
  InstallOptions::initDialog $DIALOG
  Pop $HWND
  
  ; set button "Cancel" active 
  #GetDlgItem $1 $HWNDPARENT 2
  #EnableWindow $1 1
  ; set button "Cancel" invisible 
  GetDlgItem $1 $HWNDPARENT 2
  ShowWindow $1 0
  ; set button "Back" invisible 
  GetDlgItem $1 $HWNDPARENT 3
  ShowWindow $1 0
  ; change button font
  GetDlgItem $1 $HWND 1209
  #CreateFont $R1 "Arial" "8" "600"
  #SendMessage $1 ${WM_SETFONT} $R1 0
  SetCtlColors $1 0x000000 0x05D62A 
 
  ; Display the validation options dialog
  InstallOptions::show
FunctionEnd

;--------------------------------
Function LeaveDialog
  ; If file, truncate KOSTsimyO to folder  name
  ${GetFileExt} $KOSTsimyO $R0
  ${If} $R0 != ''
    ${GetParent} $KOSTsimyO= $KOSTsimyO
  ${EndIf}

    ; If file, truncate KOSTsimyR to folder  name
  ${GetFileExt} $KOSTsimyR $R0
  ${If} $R0 != ''
    ${GetParent} $KOSTsimyR= $KOSTsimyR
  ${EndIf}

  ; To get the input of the user, read the State value of a Field 
  ReadINIStr $0 $DIALOG "Settings" "State"
  ReadINIStr $HEAPSIZE $DIALOG "${JVM_Value}" "State" 
  
  ${Switch} "Field $0"
    
    ${Case} '${HELP_Button}'
      GetFullPathName $1 ${KOSTHELP}
      ExecShell "open" $1
      Abort
    ${Break}

    ${Case} '${ORIGINAL_FileRequest}'
      nsDialogs::SelectFileDialog 'open' '$KOSTsimyO\*' ''
      Pop $R0
      ${If} $R0 == ''
        MessageBox MB_OK "${FILE_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${ORIGINAL_SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTsimyO $R0
      ${EndIf}
      Abort
    ${Break}
    
    ${Case} '${REPLICA_FileRequest}'
      nsDialogs::SelectFileDialog 'open' '$KOSTsimyR\*' ''
      Pop $R0
      ${If} $R0 == ''
        MessageBox MB_OK "${FILE_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${REPLICA_SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTsimyR $R0
      ${EndIf}
      Abort
    ${Break}
        
    ${Case} '${ORIGINAL_FolderRequest}'
      nsDialogs::SelectFolderDialog "${FOLDER_SelectTXT}" "$KOSTsimyO"
      Pop $R0
      ${If} $R0 == 'error'
        MessageBox MB_OK "${FOLDER_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${ORIGINAL_SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTsimyO $R0
      ${EndIf}
      Abort
    ${Break}
    
        ${Case} '${REPLICA_FolderRequest}'
      nsDialogs::SelectFolderDialog "${FOLDER_SelectTXT}" "$KOSTsimyR"
      Pop $R0
      ${If} $R0 == 'error'
        MessageBox MB_OK "${FOLDER_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${REPLICA_SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTsimyR $R0
      ${EndIf}
      Abort
    ${Break}

    ${Case} '${START_Validation}'
      ReadINIStr $R0 $DIALOG "${ORIGINAL_SEL_FileFolder}" "State"
      ; Trim path or file name
      StrCpy $R1 $R0 1 -1
      StrCmp $R1 '\' 0 +2
        StrCpy $R0 $R0 -1
        GetFullPathName $KOSTsimyO $R0
      ReadINIStr $R0 $DIALOG "${REPLICA_SEL_FileFolder}" "State"
      ; Trim path or file name
      StrCpy $R1 $R0 1 -1
      StrCmp $R1 '\' 0 +2
        StrCpy $R0 $R0 -1
        GetFullPathName $KOSTsimyR $R0
      Call RunJar
      Abort
    ${Break}

    ${Case} '${EDIT_Konfiguration}'
      ClearErrors
      ExecWait '${XTRANS} "$EXEDIR\${CONFIGPATH}\${CONFIG}"'
      ${If} ${Errors}
        ExecWait '"notepad.exe" "$EXEDIR\${CONFIGPATH}\${CONFIG}"'
      ${EndIf}
      ; ExecWait '"notepad.exe" "$EXEDIR\${CONFIGPATH}\${CONFIG}"'
      ; ExecShell "open" "$EXEDIR\${CONFIGPATH}\${CONFIG}"
      Abort
    ${Break}

    ${Case} '${RESET_Konfiguration}'
      CopyFiles /SILENT /FILESONLY $EXEDIR\${CONFIGPATH}\${BACKUP}\*.* $EXEDIR\${CONFIGPATH}
      Abort
    ${Break}

    ${Default}
      ; Abort prevents from leaving the current page
      ; Abort
    ${Break}
  ${EndSwitch}
  
FunctionEnd

;--------------------------------
Function RunJar
  ; get workdir and logdir
  Call check4Dir
  
  ; normalize java heap and stack
  Call checkHeapsize

  ; get logfile name
  ${GetFileName} $KOSTsimyO $LOGFILE
  CreateDirectory $LOG
  IfFileExists "$LOG\*.*" +2 0
  CreateDirectory "$EXEDIR\$LOG"
  ClearErrors
  FileOpen $R0 "$LOG\$LOGFILE.kost-simy.log.tmp" w
  FileClose $R0
  ${If} ${Errors}
    Delete "$LOG\$LOGFILE.kost-simy.log.tmp"
    MessageBox MB_OK|MB_ICONEXCLAMATION "${LOG_ERR} $LOG"
    Abort
  ${EndIf}
  delete "$LOG\$LOGFILE.kost-simy.log*"

  ; Launch java program
  ClearErrors
  ; MessageBox MB_OK '"$JAVA\bin\java.exe" $HEAPSIZE -jar ${JARFILE} "$KOSTsimyO" "$KOSTsimyR"
  ExecWait '"$JAVA\bin\java.exe" $HEAPSIZE -jar ${JARFILE} "$KOSTsimyO" "$KOSTsimyR"'
  IfFileExists "$LOG\$LOGFILE.kost-simy.log*" 0 prog_err
  IfErrors goto_err goto_ok
  
goto_err:
    ; validation with error
    IfFileExists "$LOG\$LOGFILE.kost-simy.log*" 0 prog_err
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "$KOSTsimyO$\n${FORMAT_FALSE}" IDYES showlog
    Goto rm_workdir
  prog_err:
    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n$JAVA\bin\java.exe -jar ${JARFILE} $KOSTsimyO $KOSTsimyR"
    Goto rm_workdir
goto_ok:
  ; validation without error completed
  MessageBox MB_YESNO "$KOSTsimyO$\n${FORMAT_OK}" IDYES showlog
  Goto rm_workdir
  showlog:
  ; read logfile in detail view
  GetFullPathName $1 $LOG
  IfFileExists "$LOG\$LOGFILE.kost-simy.log.xml" 0 +3
  ExecShell "" "iexplore.exe" "$1\$LOGFILE.kost-simy.log.xml"
  Goto rm_workdir
  ExecShell "" "iexplore.exe" "$1\$LOGFILE.kost-simy.log"
  Goto rm_workdir
  ; ExecShell "open" "$LOG\$LOGFILE.kost-simy.log.xml"
  ; ExecShell "open" "$LOG\$LOGFILE.kost-simy.log"
rm_workdir:
  GetFullPathName $1 $WORKDIR
  RMDir /r $1
FunctionEnd

;--------------------------------
; Sections
Section "Install"
SectionEnd
