echo "Execution script..."
mvn package && java -jar target\data-2.2.3-fat.jar -conf .\src\main\config\config.json -cluster