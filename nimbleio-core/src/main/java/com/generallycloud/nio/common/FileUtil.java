package com.generallycloud.nio.common;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.generallycloud.nio.Encoding;

public class FileUtil {

	private static Charset ENCODING = Encoding.DEFAULT;

	private static byte[] SKIP_BYTE_BUFFER;

	private static char[] SKIP_CHAR_BUFFER;

	private static final char SYSTEM_SEPARATOR = File.separatorChar;

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
	
	public static void createDirectory(File file) throws IOException{
		if (!file.exists()) {
			File parent = file.getParentFile();
			if ((parent != null) && (!parent.mkdirs())
					&& (!parent.isDirectory())) {
				throw new IOException("Directory '" + parent
						+ "' could not be created");
			}
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

	public static void copy(InputStream input, Writer output)
			throws IOException {
		copy(input, output, ENCODING);
	}

	public static void copy(InputStream input, Writer output, Charset encoding)
			throws IOException {
		InputStreamReader in = new InputStreamReader(input, encoding);
		copy(in, output);
	}

	public static void copy(Reader input, OutputStream output)
			throws IOException {
		copy(input, output, ENCODING);
	}

	public static void copy(Reader input, OutputStream output, Charset encoding)
			throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(output, encoding);
		copy(input, out);

		out.flush();
	}

	public static int copy(Reader input, Writer output) throws IOException {
		long count = copyLarge(input, output);
		if (count > 2147483647L) {
			return -1;
		}
		return (int) count;
	}

	public static long copyLarge(InputStream input, OutputStream output)
			throws IOException {
		return copyLarge(input, output, new byte[4096]);
	}

	public static long copyLarge(InputStream input, OutputStream output,
			byte[] buffer) throws IOException {
		long count = 0L;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static long copyLarge(InputStream input, OutputStream output,
			long inputOffset, long length) throws IOException {
		return copyLarge(input, output, inputOffset, length, new byte[4096]);
	}

	public static long copyLarge(InputStream input, OutputStream output,
			long inputOffset, long length, byte[] buffer) throws IOException {
		if (inputOffset > 0L) {
			skipFully(input, inputOffset);
		}
		if (length == 0L) {
			return 0L;
		}
		int bufferLength = buffer.length;
		int bytesToRead = bufferLength;
		if ((length > 0L) && (length < bufferLength)) {
			bytesToRead = (int) length;
		}
		long totalRead = 0L;
		int read;
		while ((bytesToRead > 0)
				&& (-1 != (read = input.read(buffer, 0, bytesToRead)))) {
			output.write(buffer, 0, read);
			totalRead += read;
			if (length > 0L) {
				bytesToRead = (int) Math.min(length - totalRead, bufferLength);
			}
		}
		return totalRead;
	}

	public static long copyLarge(Reader input, Writer output)
			throws IOException {
		return copyLarge(input, output, new char[4096]);
	}

	public static long copyLarge(Reader input, Writer output, char[] buffer)
			throws IOException {
		long count = 0L;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	public static long copyLarge(Reader input, Writer output, long inputOffset,
			long length) throws IOException {
		return copyLarge(input, output, inputOffset, length, new char[4096]);
	}

	public static long copyLarge(Reader input, Writer output, long inputOffset,
			long length, char[] buffer) throws IOException {
		if (inputOffset > 0L) {
			skipFully(input, inputOffset);
		}
		if (length == 0L) {
			return 0L;
		}
		int bytesToRead = buffer.length;
		if ((length > 0L) && (length < buffer.length)) {
			bytesToRead = (int) length;
		}
		long totalRead = 0L;
		int read;
		while ((bytesToRead > 0)
				&& (-1 != (read = input.read(buffer, 0, bytesToRead)))) {
			output.write(buffer, 0, read);
			totalRead += read;
			if (length > 0L) {
				bytesToRead = (int) Math.min(length - totalRead, buffer.length);
			}
		}
		return totalRead;
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

	public static String input2String(InputStream input, Charset encoding)
			throws IOException {
		byte [] bytes = new byte[input.available()];
		input.read(bytes);
		return new String(bytes,encoding);
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

	public static FileOutputStream openOutputStream(File file)
			throws IOException {
		return openOutputStream(file, false);
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

	public static String readContentByCls(String file, Charset encoding)
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
		return new File(URLDecoder.decode(path + file, ENCODING.name()));
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

	public static String readFileToString(File file, Charset encoding)
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

	public static List<String> readLines(File file, Charset encoding)
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

	public static List<String> readLines(InputStream input, Charset encoding)
			throws IOException {
		InputStreamReader reader = new InputStreamReader(input, encoding);
		return readLines(reader);
	}

	public static List<String> readLines(Reader input) throws IOException {
		BufferedReader reader = toBufferedReader(input);
		List<String> list = new ArrayList<String>();
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
					inputStream, ENCODING);
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

	public static long skip(InputStream input, long toSkip) throws IOException {
		if (toSkip < 0L) {
			throw new IllegalArgumentException(
					"Skip count must be non-negative, actual: " + toSkip);
		}
		if (SKIP_BYTE_BUFFER == null) {
			SKIP_BYTE_BUFFER = new byte[2048];
		}
		long remain = toSkip;
		while (remain > 0L) {
			long n = input.read(SKIP_BYTE_BUFFER, 0,
					(int) Math.min(remain, 2048L));
			if (n < 0L) {
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	public static long skip(Reader input, long toSkip) throws IOException {
		if (toSkip < 0L) {
			throw new IllegalArgumentException(
					"Skip count must be non-negative, actual: " + toSkip);
		}
		if (SKIP_CHAR_BUFFER == null) {
			SKIP_CHAR_BUFFER = new char[2048];
		}
		long remain = toSkip;
		while (remain > 0L) {
			long n = input.read(SKIP_CHAR_BUFFER, 0,
					(int) Math.min(remain, 2048L));
			if (n < 0L) {
				break;
			}
			remain -= n;
		}
		return toSkip - remain;
	}

	public static void skipFully(InputStream input, long toSkip)
			throws IOException {
		if (toSkip < 0L) {
			throw new IllegalArgumentException(
					"Bytes to skip must not be negative: " + toSkip);
		}
		long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Bytes to skip: " + toSkip + " actual: "
					+ skipped);
		}
	}

	public static void skipFully(Reader input, long toSkip) throws IOException {
		long skipped = skip(input, toSkip);
		if (skipped != toSkip) {
			throw new EOFException("Chars to skip: " + toSkip + " actual: "
					+ skipped);
		}
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

	public static void write(char[] data, OutputStream output)
			throws IOException {
		write(data, output, ENCODING);
	}

	public static void write(char[] data, OutputStream output, Charset encoding)
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

	public static void write(File file, byte[] data)throws IOException {
		write(file, data, false);
	}

	public static void write(File file, byte[] data,boolean append) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append);
			out.write(data);
			out.close();
		} finally {
			CloseUtil.close(out);
		}
	}
	
	
	

	public static void writeByCls(String file, byte[] bytes, boolean append)
			throws IOException {
		File realFile = readFileByCls(file);
		write(realFile, bytes, append);
	}

	public static void writeByCls(String file, String content,
			Charset encoding, boolean append) throws IOException {
		File realFile = readFileByCls(file);
		write(realFile, content, encoding, append);
	}

	public static void writeProperties(Properties properties, String path)
			throws IOException {
		File file = readFileByCls(path);
		FileOutputStream fos = new FileOutputStream(file);
		properties.store(fos, "# this is admin's properties");
	}

	public static void write(File file, String content)
			throws IOException {
		write(file, content, ENCODING, false);

	}

	public static void write(File file, String data, boolean append)
			throws IOException {
		write(file, data, ENCODING, append);
	}

	public static void write(File file, String content,Charset encoding) throws IOException {
		write(file, content, encoding, false);
	}

	public static void write(File file, String content,Charset encoding,
			boolean append) throws IOException {
		write(content.getBytes(encoding), openOutputStream(file, append));
	}
	

}
