@echo off
if not exist out mkdir out
javac -encoding UTF-8 -d out src\main\java\TaxCal.java src\main\java\Main.java
java -cp out Main