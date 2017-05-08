echo "Execution script..."
mvn package && java -jar target\ubisom.factory-1.2.1-fat.jar -conf .\src\main\config\config.json -cluster