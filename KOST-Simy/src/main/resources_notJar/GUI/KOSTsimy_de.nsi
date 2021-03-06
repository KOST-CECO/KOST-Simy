; The name of the installer
Name "KOST-Simy v0.0.10"
; Sets the icon of the installer
Icon "simy.ico"
; remove the text 'Nullsoft Install System vX.XX' from the installer window 
BrandingText "Copyright � KOST/CECO"
; The file to write
OutFile "KOST-Simy_de.exe"
; The default installation directory
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
!include NTProfiles.nsh

;--------------------------------
!define INIFILE       "KOSTsimy.ini"
!define KOSTHELP      "doc\KOST-Simy_Anwendungshandbuch_*.pdf"
!define WORKDIR       ".kost-simy\temp_KOST-Simy"
!define LOG	          ".kost-simy\logs"
!define JARFILE       "kostsimy_de.jar"
!define XTRANS        "resources\XTrans_1.8.0.4\XTrans.exe"
!define JAVAPATH      "resources\jre6"

;--------------------------------
Var USER
Var DIALOG
Var KOSTsimyO
Var KOSTsimyR
Var LOGFILE
Var HEAPSIZE
Var TOLERANCE
Var RANDOM
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
  
  System::Call "advapi32::GetUserName(t .r0, *i ${NSIS_MAX_STRLEN} r1) i.r2"
  ; MessageBox MB_OK "User name: $0 | Number of characters: $1 | Return value (OK if non-zero): $2"  
  ${ProfilePathDefaultUser} $3
  StrCpy $USER "$3$0"
  ; MessageBox MB_OK `Default User profile path:$\n"$USER"`
  
  IfFileExists "$USER\${LOG}\*.*" +4 0
  CreateDirectory "$USER\.kost-simy"
  CreateDirectory "$USER\${LOG}"

  ; Initializes the plug-ins dir ($PLUGINSDIR) if not already initialized
  InitPluginsDir
  
  ; Assign to the user variable $DIALOG, the name of a temporary file
  GetTempFileName $DIALOG $PLUGINSDIR
  
  ; Adds file(s) to be extracted to the current output path
  ;   Use /oname=X switch to change the output name
  File /oname=$DIALOG ${INIFILE}
FunctionEnd

Function .onGUIEnd
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
  
  ;WriteINIStr $DIALOG "${INTRO}"                  "Text"  "${INTROTXT}"
  WriteINIStr $DIALOG "${HELP_Button}"             "Text"  "${HELP_ButtonTXT}"
  WriteINIStr $DIALOG "${ORIGINAL_FolderRequest}"  "Text"  "${ORIGINAL_FolderRequestTXT}"
  WriteINIStr $DIALOG "${ORIGINAL_FileRequest}"    "Text"  "${ORIGINAL_FileRequestTXT}"
  WriteINIStr $DIALOG "${ORIGINAL_SEL_FileFolder}" "State" "${ORIGINAL_SEL_FileFolderTXT}"
  WriteINIStr $DIALOG "${JVM_Droplist}"            "Text"  "${JVM_DroplistTXT}"
 ; WriteINIStr $DIALOG "${INPUT_Group}"            "Text"  "${INPUT_GroupTXT}"
  WriteINIStr $DIALOG "${REPLICA_FolderRequest}"   "Text"  "${REPLICA_FolderRequestTXT}"
  WriteINIStr $DIALOG "${REPLICA_FileRequest}"     "Text"  "${REPLICA_FileRequestTXT}"
  WriteINIStr $DIALOG "${REPLICA_SEL_FileFolder}"  "State" "${REPLICA_SEL_FileFolderTXT}"
  WriteINIStr $DIALOG "${START_Validation}"        "Text"  "${START_ValidationTXT}"
  WriteINIStr $DIALOG "${TOLERANCE_Droplist}"      "Text"  "${TOLERANCE_DroplistTXT}"
  WriteINIStr $DIALOG "${RANDOM_Droplist}"         "Text"  "${RANDOM_DroplistTXT}"

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
  ReadINIStr $TOLERANCE $DIALOG "${TOLERANCE_Value}" "State" 
  ReadINIStr $RANDOM $DIALOG "${RANDOM_Value}" "State" 
  
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

    ${Default}
      ; Abort prevents from leaving the current page
      ; Abort
    ${Break}
  ${EndSwitch}
  
FunctionEnd

;--------------------------------
Function RunJar
  ; normalize java heap and stack
  Call checkHeapsize

  ; get logfile name
  ${GetFileName} $KOSTsimyO $LOGFILE

  ; Launch java program
  ClearErrors
  ExecWait '"$JAVA\bin\java.exe" $HEAPSIZE -jar ${JARFILE} "$KOSTsimyO" "$KOSTsimyR" $TOLERANCE $RANDOM'

  IfFileExists "$USER\${LOG}\$LOGFILE.kost-simy.log.xml" 0 prog_err
  IfErrors goto_err goto_ok
  
goto_err:
    ; validation with error
    IfFileExists "$USER\${LOG}\$LOGFILE.kost-simy.log.xml" 0 prog_err
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "$KOSTsimyO$\n${FORMAT_FALSE}" IDYES showlog
    Goto rm_workdir
  prog_err:
    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n$JAVA\bin\java.exe -jar ${JARFILE} $KOSTsimyO $KOSTsimyR  $TOLERANCE $RANDOM"
    Goto rm_workdir
goto_ok:
  ; validation without error completed
  MessageBox MB_YESNO "$KOSTsimyO$\n${FORMAT_OK}" IDYES showlog
  Goto rm_workdir
  showlog:
  ; read logfile in detail view
  GetFullPathName $1 $USER\${LOG}
  IfFileExists "$USER\${LOG}\$LOGFILE.kost-simy.log.xml" 0 +3
  ExecShell "" "iexplore.exe" "$1\$LOGFILE.kost-simy.log.xml"
  Goto rm_workdir
  ExecShell "" "iexplore.exe" "$1\$LOGFILE.kost-simy.log.xml"
  Goto rm_workdir
rm_workdir:
  GetFullPathName $1 $USER\${WORKDIR}
  RMDir /r $1
FunctionEnd

;--------------------------------
; Sections
Section "Install"
SectionEnd
