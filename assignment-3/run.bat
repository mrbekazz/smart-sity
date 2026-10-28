@echo off
if not exist out mkdir out
javac -encoding UTF-8 -d out src\graph\Graph.java src\dsu\DSU.java src\mst\MST.java src\connectivity\Conn.java src\shortestpath\SP.java src\app\Bench.java src\app\Main.java src\test\Test.java
if errorlevel 1 exit /b 1
if "%1"=="test" java -cp out test.Test
if "%1"=="bench" java -cp out app.Bench
if "%1"=="" java -cp out app.Main
