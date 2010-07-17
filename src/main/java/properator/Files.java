package properator;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * Files utility class.
 * 
 * @author Felix Berger
 */
public class Files {
	
	/**
	 * Finds all files matching one of extensions recursively.
	 */
	public static Iterable<File> getFilesRecursive(final File directory, final String...extensions) {
		return new Iterable<File>() {
			@Override
			public Iterator<File> iterator() {
				return new RecursiveFileIterator(directory, extensions != null ? extensions : new String[0]);
			}
		};
	}
	
	private static class RecursiveFileIterator implements Iterator<File> {
		
		private final Queue<File> directoriesToSearch = new ArrayDeque<File>();
		
		private final Queue<File> foundFiles = new ArrayDeque<File>();

		private final List<String> extensions;

		public RecursiveFileIterator(File directory, String[] extensions) {
			this.extensions = dotify(extensions);
			if (directory.isDirectory()) {
				directoriesToSearch.add(directory);
			}
		}

		private List<String> dotify(String[] extensions) {
			List<String> dotted = new ArrayList<String>(extensions.length);
			for (String extension : extensions) {
				dotted.add("." + extension);
			}
			return dotted;
		}

		@Override
		public boolean hasNext() {
			return !foundFiles.isEmpty() || (!directoriesToSearch.isEmpty() && findFiles());
		}

		private boolean findFiles() {
			while (!directoriesToSearch.isEmpty() && foundFiles.isEmpty()) {
				File directory = directoriesToSearch.remove();
				File[] files = directory.listFiles();
				if (files == null) {
					continue;
				}
				for (File file : files) {
					if (file.isDirectory()) {
						directoriesToSearch.add(file);
					} else if (matchesOneExtension(file)) { 
						foundFiles.add(file);
					}
				}
			}
			return !foundFiles.isEmpty();
		}

		private boolean matchesOneExtension(File file) {
			String name = file.getName();
			for (String extension : extensions) {
				if (name.endsWith(extension)) {
					return true;
				}
			}
			return extensions.isEmpty();
		}

		@Override
		public File next() {
			return foundFiles.remove();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
