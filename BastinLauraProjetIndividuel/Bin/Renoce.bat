cd..
set RenoceDir = %cd%
cd Bin
java -Xmx6000m -Djava.library.path=%RenoceDir//Legacy% -jar Renoce.jar
pause