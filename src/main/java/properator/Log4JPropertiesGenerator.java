package properator;


import java.io.File;
import java.io.IOException;
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
	
	public Set<String> getPackages(Set<String> logs) {
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
