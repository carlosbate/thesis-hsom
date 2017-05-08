echo "Execution script..."
mvn package && java -jar target\mongoeb-1.0-fat.jar -conf .\src\main\config\config.json -cluster