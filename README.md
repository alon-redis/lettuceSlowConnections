Install Java and Maven:

bashCopy# Update package list
sudo apt update

# Install Java Development Kit (JDK)
sudo apt install -y openjdk-17-jdk

# Install Maven
sudo apt install -y maven

# Verify installations
java --version
mvn --version

Create project directory and structure:

bashCopy# Create project directory
mkdir redis-parallel-tester
cd redis-parallel-tester

# Create Maven project structure
mkdir -p src/main/java/com/example

Create the Maven configuration file:

bashCopy# Create pom.xml in the root directory
touch pom.xml

Copy the project files:

bashCopy# Create all necessary Java files
touch src/main/java/com/example/Config.java
touch src/main/java/com/example/DataGenerator.java
touch src/main/java/com/example/RedisTester.java
touch src/main/java/com/example/Main.java

Copy the code from my previous response into each respective file:


Copy the pom.xml content into pom.xml
Copy each Java class into its respective file in src/main/java/com/example/


Build the project:

bashCopy# Build the JAR file
mvn clean package


Here's a comprehensive example of running the Redis testing script with all available options:
bashCopyjava -jar target/redis-parallel-tester-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --host localhost \
  --port 6379 \
  --data-size 2048 \
  --connections 20 \
  --slow-connections 5 \
  --keys-count 100000 \
  --hash-fields 500000 \
  --hash-field-size 200 \
  --recv-chunk-size-min 512 \
  --recv-chunk-size-max 4096 \
  --recv-sleep-time 0.1

# Or on a single line:
java -jar target/redis-parallel-tester-1.0-SNAPSHOT-jar-with-dependencies.jar --host localhost --port 6379 --data-size 2048 --connections 20 --slow-connections 5 --keys-count 100000 --hash-fields 500000 --hash-field-size 200 --recv-chunk-size-min 512 --recv-chunk-size-max 4096 --recv-sleep-time 0.1
