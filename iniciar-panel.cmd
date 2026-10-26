@echo off
setlocal
cd /d "%~dp0"
call mvnw.cmd -Pdesktop spring-boot:run
