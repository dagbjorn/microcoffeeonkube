@echo off

setlocal

set CREDITRATING_SERVICE_BEHAVIOR=0
set CREDITRATING_SERVICE_BEHAVIOR_FIXEDDELAY=10

mvn spring-boot:run

endlocal
