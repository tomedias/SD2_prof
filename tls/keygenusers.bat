@echo off
setlocal enabledelayedexpansion

REM Delete existing .jks files
mkdir certs
mkdir keystores
del /Q certs\*
del /Q keystores\*
del /Q *.jks

for /L %%i in (0,1,2) do (
  for /L %%j in (0,1,2) do (
    set "f=./keystores/feeds%%j-ourorg%%i.jks"
    set "h=./keystores/users%%i-ourorg%%j.jks"
    set "g=./certs/users%%i-ourorg%%j.cert"
    set "l=./certs/feeds%%j-ourorg%%i.cert"
    set "n=users%%i-ourorg%%j"
    set "m=feeds%%j-ourorg%%i"
    set "k=client-ts.jks"

    if %%i equ 0 (
      keytool -genkeypair -alias "!n!" -keyalg RSA -validity 365 -keystore "!h!" -storetype pkcs12 -ext SAN=dns:"!n!" -dname "CN=Users.Users, OU=TP2, O=SD2223, L=LX, S=LX, C=PT" -storepass 123users -keypass 123users

      keytool -exportcert -alias "!n!" -keystore "!h!" -file "!g!" -storepass 123users

      keytool -importcert -file "!g!" -alias "!n!" -keystore "!k!" -storepass changeit -noprompt
    )

    keytool -genkeypair -alias "!m!" -keyalg RSA -validity 365 -keystore "!f!" -storetype pkcs12 -ext SAN=dns:"!m!" -dname "CN=Feeds.Feeds, OU=TP2, O=SD2223, L=LX, S=LX, C=PT" -storepass 123users -keypass 123users

    keytool -exportcert -alias "!m!" -keystore "!f!" -file "!l!" -storepass 123users

    keytool -importcert -file "!l!" -alias "!m!" -keystore "!k!" -storepass changeit -noprompt
  )
)
