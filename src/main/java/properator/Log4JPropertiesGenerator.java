package properator;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;

/**
 * Generates log4j.properties file for a given folder.
 * <p>
 * Main method takes two arguments a folder that is scanned recursively for
 * class files and the file the lo4gj properties should be writtent to.
 * 
 * @author Felix Berger
 */
public class Log4JPropertiesGenerator {
	
	private static final String TEMPLATE = "###\n# log4j configuration file.\n########\n\n#########\n# Valid thresholds can be:\n# OFF, FATAL, ERROR, WARN, INFO, DEBUG, ALL \n\n#\n# The default logger that is used prints out log statements\n# on the console.  If you want those redirected to a file,\n# enable TextFile logger.  If you want those stored in an xml\n# format (either for chainsaw or for inclusing in xml documents)\n# use the XMLFile logger.  If you want to use chainsaw to watch\n# the logs of a running application use the \"socket\" logger\n\n########\n\n# Set the root loggers\nlog4j.rootLogger=OFF, stdout\n\n\n######\n#  The TextFile logger\n# if you want to enable logging to file in standard format:\n# 1. comment out the first line\n# 2. uncomment/edit the other lines\n# to disable this type of logging do the oppposite.\n\n#log4j.appender.TextFile=org.apache.log4j.varia.NullAppender\nlog4j.appender.TextFile=org.apache.log4j.RollingFileAppender\nlog4j.appender.TextFile.File=log.txt\nlog4j.appender.TextFile.MaxFileSize=5000MB\nlog4j.appender.TextFile.MaxBackupIndex=5\nlog4j.appender.TextFile.layout=org.apache.log4j.PatternLayout\nlog4j.appender.TextFile.layout.ConversionPattern=%-6r %-5p [%t] %c{2}.%M - %m%n\nlog4j.appender.TextFile.ImmediateFlush=true\n########\n\n\n######\n# The XMLFile logger\n# if you want to enable logging to XML file for the chainsaw viwer:\n# 1. comment out the first line\n# 2. uncomment/edit the other lines\n# to disable this type of logging do the oppposite.\n\nlog4j.appender.XMLFile=org.apache.log4j.varia.NullAppender\n#log4j.appender.XMLFile=org.apache.log4j.RollingFileAppender\n#log4j.appender.XMLFile.File=log.xml\n#log4j.appender.XMLFile.MaxFileSize=100MB\n#log4j.appender.XMLFile.MaxBackupIndex=5\n#log4j.appender.XMLFile.layout=org.apache.log4j.xml.XMLLayout\n#log4j.appender.XMLFile.layout.LocationInfo=true\n#log4j.appender.XMLFile.ImmediateFlush=false\n#####\n\n\n\n#########\n# The \"socket\" logger\n# If you want to use the Chainsaw viewer on a running program:\n# 1. comment out the first line\n# 2. uncomment/edit the other lines\n# to disable this type of logging do the oppposite.\n\n#log4j.appender.socket=org.apache.log4j.varia.NullAppender\nlog4j.appender.socket=org.apache.log4j.net.SocketAppender\nlog4j.appender.socket.RemoteHost=localhost\nlog4j.appender.socket.port=4445\nlog4j.appender.socket.LocationInfo=true\n#########\n\n# stdout is set to be ConsoleAppender sending its output to System.out\n#log4j.appender.stdout=org.apache.log4j.varia.NullAppender\nlog4j.appender.stdout=org.apache.log4j.ConsoleAppender\n\n# stdout uses PatternLayout.\nlog4j.appender.stdout.layout=org.apache.log4j.PatternLayout\n\n# The conversion pattern is:\n# time elapsed since start of logging (left justified, pads extra spaces if less than 0)\n# logging priority (left justified, pads extra spaces if less than 5)\n# [thread name]\n# packagename.classname.methodname (only the last part of the package is kept)\n# - message\n# newline\n\nlog4j.appender.stdout.layout.ConversionPattern=%-6r %-5p [%t] %c{2}.%M - %m%n\n\n\n###  To set the value for specific classes/packages, use the following format:\n## log4j.logger.<package.class>=LEVEL\n\n{0}";

	public static void main(String[] args) throws IOException {
		Log4JPropertiesGenerator propertiesGenerator = new Log4JPropertiesGenerator();
		System.out.println("Scanning for loggers");
		File projectRoot = new File(args[0]);
		if (!projectRoot.isDirectory()) {
			System.out.println(projectRoot + " is not a directory");
			System.exit(1);
		}
		Set<String> logs = propertiesGenerator.getLogs(new File(args[0]));
		logs.addAll(propertiesGenerator.getJarLogs(new File(args[0])));
		Set<String> packages = propertiesGenerator.getPackages(logs);
		logs.addAll(packages);
		StringBuilder builder = new StringBuilder();
		for (String log : logs) {
			builder.append("#log4j.logger." + log + "=ALL").append('\n');
		}
		System.out.println("Writing log4j.properties");
		FileWriter writer = new FileWriter(new File(args[1]));
		writer.write(MessageFormat.format(TEMPLATE, builder.toString()));
		writer.flush();
		writer.close();
		System.out.println("Done");
	}
	
	private Set<String> getPackages(Set<String> logs) {
		Set<String> packages = new TreeSet<String>();
		for (String log : logs) {
			int lastDot = log.lastIndexOf('.');
			if (lastDot != -1) {
				packages.add(log.substring(0, lastDot));
			}
		}
		return packages;
	}

	public Set<String> getLogs(File path) throws IOException {
		Set<String> logs = new TreeSet<String>();
		for (File classFile : Files.getFilesRecursive(path, "class")) {
			ClassParser parser = new ClassParser(classFile.getPath());
			JavaClass clazz = parser.parse();
			String log = getLog(clazz);
			if (log != null) {
				logs.add(log);
			}
		}
		return logs;
	}
	
	public Set<String> getJarLogs(File path) throws IOException {
		Set<String> logs = new TreeSet<String>();
		for (File jar : Files.getFilesRecursive(path, "jar")) {
			JarFile jarFile = new JarFile(jar);
			String jarFilePath = jar.getPath();
			for (JarEntry entry : Iterators.iterable(jarFile.entries())) {
				String name = entry.getName();
				if (name.endsWith(".class")) {
					ClassParser parser = new ClassParser(jarFilePath, name);
					JavaClass clazz = parser.parse();
					String log = getLog(clazz);
					if (log != null) {
						logs.add(log);
					}	
				}
			}
			
		}
		return logs;
	}

	private String getLog(JavaClass clazz) {
		for (Field field : clazz.getFields()) {
			if (field.isStatic() && field.getType().toString().endsWith("Log")) {
				return clazz.getClassName();
			}
		}
		return null;
	}
	
}
