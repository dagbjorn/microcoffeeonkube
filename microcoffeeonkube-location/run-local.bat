@echo off

setlocal

set MICROCOFFEE_MONGODB_SERVICE_HOST=192.168.99.100
set MICROCOFFEE_MONGODB_SERVICE_PORT=27017

set | grep MICROCOFFEE

mvn spring-boot:run

endlocal
