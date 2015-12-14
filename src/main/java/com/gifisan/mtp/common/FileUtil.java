package com.gifisan.mtp.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class FileUtil {

	private static String ENCODING 					= "UTF-8";
	private static final char SYSTEM_SEPARATOR 		= File.separatorChar;

	public static void cleanDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}
		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}
		File[] files = directory.listFiles();
		if (files == null) {
			throw new IOException("Failed to list contents of " + directory);
		}
		IOException exception = null;
		for (File file : files) {
			try {
				forceDelete(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}
		if (null != exception) {
			throw exception;
		}
	}

	private static void cleanDirectoryOnExit(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}
		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}
		File[] files = directory.listFiles();
		if (files == null) {
			throw new IOException("Failed to list contents of " + directory);
		}
		IOException exception = null;
		for (File file : files) {
			try {
				forceDeleteOnExit(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}
		if (null != exception) {
			throw exception;
		}
	}

	public static void deleteDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}
		if (!isSymlink(directory)) {
			cleanDirectory(directory);
		}
		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";

			throw new IOException(message);
		}
	}

	private static void deleteDirectoryOnExit(File directory)
			throws IOException {
		if (!directory.exists()) {
			return;
		}
		directory.deleteOnExit();
		if (!isSymlink(directory)) {
			cleanDirectoryOnExit(directory);
		}
	}

	public static void deleteDirectoryOrFile(File file) throws IOException {
		if (file.isFile()) {
			file.delete();
		} else {
			deleteDirectory(file);
		}
	}

	public static void deleteDirectoryOrFileByCls(String file)
			throws IOException {
		File realFile = readFileByCls(file);
		deleteDirectoryOrFile(realFile);
	}

	public static boolean deleteQuietly(File file) {
		if (file == null) {
			return false;
		}
		try {
			if (file.isDirectory()) {
				cleanDirectory(file);
			}
		} catch (Exception ignored) {
		}
		try {
			return file.delete();
		} catch (Exception ignored) {
		}
		return false;
	}

	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: "
							+ file);
				}
				String message = "Unable to delete file: " + file;

				throw new IOException(message);
			}
		}
	}

	public static void forceDeleteOnExit(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectoryOnExit(file);
		} else {
			file.deleteOnExit();
		}
	}

	public static String input2String(InputStream input, String encoding)
			throws IOException {
		int length = input.available();
		
		byte [] array = new byte[length];
		
		input.read(array);
		
		return new String(array,encoding);
		
	}

	public static boolean isSymlink(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		if (isSystemWindows()) {
			return false;
		}
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}
		if (fileInCanonicalDir.getCanonicalFile().equals(
				fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		}
		return true;
	}

	private static boolean isSystemWindows() {
		return SYSTEM_SEPARATOR == '\\';
	}

	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file
						+ "' exists but is a directory");
			}
			if (!file.canRead()) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file
					+ "' does not exist");
		}
		return new FileInputStream(file);
	}

	public static FileOutputStream openOutputStream(File file, boolean append)
			throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file
						+ "' exists but is a directory");
			}
			if (!file.canWrite()) {
				throw new IOException("File '" + file
						+ "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if ((parent != null) && (!parent.mkdirs())
					&& (!parent.isDirectory())) {
				throw new IOException("Directory '" + parent
						+ "' could not be created");
			}
		}
		return new FileOutputStream(file, append);
	}


	public static byte[] readBytesByCls(String file) throws IOException {
		File file2 = readFileByCls(file);
		if (file2.exists()) {
			return readFileToByteArray(file2);
		}
		return null;
	}

	public static String readContentByCls(String file, String encoding)
			throws IOException {
		File file2 = readFileByCls(file);
		if (file2.exists()) {
			return readFileToString(file2, encoding);
		}
		return null;
	}

	public static File readFileByCls(String file)
			throws UnsupportedEncodingException {
		ClassLoader classLoader = FileUtil.class.getClassLoader();
		String path = classLoader.getResource(".").getFile();
		return new File(URLDecoder.decode(path + file, "UTF-8"));
	}

	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return toByteArray(in, file.length());
		} finally {
			CloseUtil.close(in);
		}
	}

	public static String readFileToString(File file, String encoding)
			throws IOException {

		InputStream in = null;
		try {
			in = openInputStream(file);
			return input2String(in, encoding);
		} finally {
			CloseUtil.close(in);
		}

	}

	public static List<String> readLines(File file) throws IOException {
		return readLines(file, ENCODING);
	}

	public static List<String> readLines(File file, String encoding)
			throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return readLines(in, encoding);
		} finally {
			CloseUtil.close(in);
		}
	}

	public static List<String> readLines(InputStream input) throws IOException {
		return readLines(input, ENCODING);
	}

	public static List<String> readLines(InputStream input, String encoding)
			throws IOException {
		InputStreamReader reader = new InputStreamReader(input, encoding);
		return readLines(reader);
	}

	public static List<String> readLines(Reader input) throws IOException {
		BufferedReader reader = toBufferedReader(input);
		List<String> list = new ArrayList();
		String line = reader.readLine();
		while (line != null) {
			list.add(line);
			line = reader.readLine();
		}
		return list;
	}

	public static Properties readProperties(File file) throws IOException {
		InputStream inputStream = new FileInputStream(file);
		Properties properties = readProperties(inputStream);
		if (properties == null) {
			throw new IOException(file.getAbsolutePath() + " not found!");
		}
		return properties;
	}

	public static Properties readProperties(InputStream inputStream)
			throws IOException {
		Properties properties = new Properties();
		try {
			if (inputStream == null) {
				throw new IOException("null inputstream!");
			}
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "UTF-8");
			properties.load(inputStreamReader);
			return properties;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public static Properties readProperties(String path) throws IOException {
		InputStream inputStream = FileUtil.class.getClassLoader()
				.getResourceAsStream(path);
		Properties properties = readProperties(inputStream);
		if (properties == null) {
			throw new IOException(path + " not found!");
		}
		return properties;
	}

	public static BufferedReader toBufferedReader(Reader reader) {
		return (reader instanceof BufferedReader) ? (BufferedReader) reader
				: new BufferedReader(reader);
	}

	public static byte[] toByteArray(InputStream input, int size)
			throws IOException {
		if (size < 0) {
			throw new IllegalArgumentException(
					"Size must be equal or greater than zero: " + size);
		}
		if (size == 0) {
			return new byte[0];
		}
		byte[] data = new byte[size];
		int offset = 0;
		int readed;
		while ((offset < size)
				&& ((readed = input.read(data, offset, size - offset)) != -1)) {
			offset += readed;
		}
		if (offset != size) {
			throw new IOException("Unexpected readed size. current: " + offset
					+ ", excepted: " + size);
		}
		return data;
	}

	public static byte[] toByteArray(InputStream input, long size)
			throws IOException {
		if (size > 2147483647L) {
			throw new IllegalArgumentException(
					"Size cannot be greater than Integer max value: " + size);
		}
		return toByteArray(input, (int) size);
	}

	public static void write(byte[] data, OutputStream output)
			throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	public static void write(byte[] data, Writer output) throws IOException {
		write(data, output, ENCODING);
	}

	public static void write(byte[] data, Writer output, String encoding)
			throws IOException {
		if (data != null) {
			output.write(new String(data, encoding));
		}
	}

	public static void write(char[] data, OutputStream output)
			throws IOException {
		write(data, output, ENCODING);
	}

	public static void write(char[] data, OutputStream output, String encoding)
			throws IOException {
		if (data != null) {
			output.write(new String(data).getBytes(encoding));
		}
	}

	public static void write(char[] data, Writer output) throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	public static void write(File file, String data) throws IOException {
		write(file, data, ENCODING, false);
	}

	public static void write(File file, String data, boolean append) throws IOException {
		write(file, data, ENCODING, append);
	}

	public static void write(File file, String data, String encoding)
			throws IOException {
		write(file, data, encoding, false);
	}

	public static void write(File file, String data, String encoding,
			boolean append) throws IOException {
		write(file, data, encoding, append);
	}

	public static void write(String data, OutputStream output)
			throws IOException {
		write(data, output, ENCODING);
	}

	public static void write(String data, OutputStream output, String encoding)
			throws IOException {
		if (data != null) {
			output.write(data.getBytes(encoding));
		}
	}

	public static void write(String data, Writer output) throws IOException {
		if (data != null) {
			output.write(data);
		}
	}

	public static void writeByteArrayToFile(File file, byte[] data)
			throws IOException {
		writeByteArrayToFile(file, data, false);
	}

	public static void writeByteArrayToFile(File file, byte[] data,
			boolean append) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append);
			out.write(data);
		} finally {
			CloseUtil.close(out);
		}
	}

	public static void writeBytesByCls(String file, byte[] bytes, boolean append)
			throws IOException {
		File realFile = readFileByCls(file);
		writeByteArrayToFile(realFile, bytes, append);
	}

	public static void writeContentByCls(String file, String content,
			String encoding, boolean append) throws IOException {
		File realFile = readFileByCls(file);
		writeStringToFile(realFile, content, encoding, append);
	}

	public static void writeProperties(Properties properties, String path)
			throws IOException {
		File file = readFileByCls(path);
		FileOutputStream fos = new FileOutputStream(file);
		properties.store(fos, "# this is admin's properties");
	}

	public static void writeStringToFile(File file, String content)
			throws IOException {
		writeStringToFile(file, content, ENCODING, false);

	}

	public static void writeStringToFile(File file, String data, boolean append)
			throws IOException {
		writeStringToFile(file, data, ENCODING, append);
	}

	public static void writeStringToFile(File file, String content,
			String encoding) throws IOException {

		writeStringToFile(file, content, encoding, false);
	}

	public static void writeStringToFile(File realFile, String content,
			String encoding, boolean append) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(realFile, append);
			write(content, out, encoding);
		} finally {
			CloseUtil.close(out);
		}

	}

}
