Properator
==========

A log4j.properties file generator that takes a project directory and parses all
class files it finds to find Log definition. It generates a log4j.properties
file with all found packages and defined loggers commented out by default.

Usage
=====

	java -jar properator.jar /project/path /project/path/src/main/resources/log4j.properties

* The first argument is the directory that will be scanned for class files. 
* The second argument is the path name of the log4j properties file that is
  generated by the properator.

### Example output


	#log4j.logger.com.example.package=ALL
	#log4j.logger.com.example.package.ClassWithLogger1=ALL
	#log4j.logger.com.example.package.ClassWithLogger2=ALL
	#log4j.logger.com.example.package2=ALL
	#log4j.logger.com.exmaple.package2.ClassWithLogger3=ALL
