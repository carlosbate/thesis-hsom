echo "Execution script..."
mvn package && java -jar target\hsom-3.0-fat.jar -conf .\src\main\config\config.json -cluster