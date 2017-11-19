@echo off

setlocal

:: See application.properties for supported behaviors
set CREDITRATING_SERVICE_BEHAVIOR=0
set CREDITRATING_SERVICE_BEHAVIOR_DELAY=10

mvn spring-boot:run

endlocal
