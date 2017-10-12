/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.common;

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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    private static final ClassLoader CLASS_LOADER     = FileUtil.class.getClassLoader();

    private static final Charset     ENCODING         = Encoding.UTF8;

    private static final byte[]      SKIP_BYTE_BUFFER = new byte[2048];

    private static final char        SYSTEM_SEPARATOR = File.separatorChar;

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {}
        }
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[4096]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static long copyLarge(InputStream input, OutputStream output, long inputOffset,
            long length) throws IOException {
        return copyLarge(input, output, inputOffset, length, new byte[4096]);
    }

    public static long copyLarge(InputStream input, OutputStream output, long inputOffset,
            long length, byte[] buffer) throws IOException {
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
        while ((bytesToRead > 0) && (-1 != (read = input.read(buffer, 0, bytesToRead)))) {
            output.write(buffer, 0, read);
            totalRead += read;
            if (length > 0L) {
                bytesToRead = (int) Math.min(length - totalRead, bufferLength);
            }
        }
        return totalRead;
    }

    public static void createDirectory(File file) throws IOException {
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }
        file.mkdir();
    }

    public static String decodeURL(String url, Charset charset) {
        try {
            return URLDecoder.decode(url, charset.name());
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    private static void deleteDirectory(File directory) throws IOException {
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

    public static void deleteDirectoryOrFile(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            deleteDirectory(file);
        }
    }

    public static void deleteDirectoryOrFileByCls(String file, ClassLoader classLoader)
            throws IOException {
        File realFile = readFileByCls(file, classLoader);
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
        } catch (Exception ignored) {}
        try {
            return file.delete();
        } catch (Exception ignored) {}
        return false;
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                throw new IOException("Unable to delete file: " + file);
            }
        }
    }

    public static String getCurrentPath() {
        return getCurrentPath(CLASS_LOADER);
    }

    public static String getCurrentPath(ClassLoader classLoader) {

        URL url = classLoader.getResource(".");

        if (url == null) {
            return new File(".").getAbsoluteFile().getParent();
        }

        return new File(url.getFile()).getAbsoluteFile().getPath();
    }

    public static File getJarParentDirectory(File file) throws IOException {

        File parent = file;

        for (; parent != null;) {

            if (parent.getName().endsWith(".jar!")) {
                return parent.getParentFile();
            }

            parent = parent.getParentFile();
        }

        throw new IOException("not a jar");
    }

    public static File getParentDirectoryOrSelf(File file) throws IOException {

        if (file.isDirectory()) {
            return file;
        }

        if (inJarFile(file)) {
            return getJarParentDirectory(file);
        }

        return file.getParentFile();
    }

    public static String getPrettyPath(String path) {
        String newPath = path.replace("\\", "/");
        if (newPath.endsWith("/")) {
            return newPath;
        }
        return newPath + "/";
    }

    public static boolean inJarFile(File file) {

        File parent = file;

        for (; parent != null;) {

            if (parent.getName().endsWith(".jar!")) {
                return true;
            }

            parent = parent.getParentFile();
        }

        return false;
    }

    public static String input2String(InputStream input, Charset encoding) throws IOException {
        return createString(inputStream2ByteArray(input), encoding);
    }

    public static byte[] inputStream2ByteArray(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        int size = inputStream.available();
        if (size < 1) {
            return null;
        }
        byte[] data = new byte[size];
        int offset = 0;
        int readed;
        while ((offset < size)
                && ((readed = inputStream.read(data, offset, size - offset)) != -1)) {
            offset += readed;
        }
        if (offset != size) {
            throw new IOException(
                    "Unexpected readed size. current: " + offset + ", excepted: " + size);
        }
        return data;
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
        if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
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
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            createDirectory(file.getParentFile());
        }
        return new FileOutputStream(file, append);
    }

    public static byte[] readBytesByCls(String file) throws IOException {
        return readBytesByCls(file, CLASS_LOADER);
    }

    public static byte[] readBytesByCls(String file, ClassLoader classLoader) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(file);
        if (inputStream == null) {
            return null;
        }
        try {
            return inputStream2ByteArray(inputStream);
        } finally {
            CloseUtil.close(inputStream);
        }
    }

    public static byte[] readBytesByFile(File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return inputStream2ByteArray(in);
        } finally {
            CloseUtil.close(in);
        }
    }

    public static File readFileByCls(String file) throws IOException {
        return readFileByCls(file, CLASS_LOADER);
    }

    public static File readFileByCls(String file, ClassLoader classLoader) throws IOException {
        URL url = classLoader.getResource(file);
        if (url == null) {
            throw new FileNotFoundException(file);
        }
        String path = url.getFile();
        return new File(URLDecoder.decode(path, ENCODING.name()));
    }

    public static int readInputStream(InputStream inputStream, byte[] cache) throws IOException {
        int c = 0;
        int s = cache.length;
        for (; c < s;) {
            int r = inputStream.read(cache, c, s - c);
            if (r == -1) {
                return c;
            }
            c += r;
        }
        return c;
    }

    public static InputStream readInputStreamByCls(String file) {
        return readInputStreamByCls(file, CLASS_LOADER);
    }

    public static InputStream readInputStreamByCls(String file, ClassLoader classLoader) {
        return classLoader.getResourceAsStream(file);
    }

    public static List<String> readLines(File file) throws IOException {
        return readLines(file, ENCODING);
    }

    public static List<String> readLines(File file, Charset encoding) throws IOException {
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

    public static List<String> readLines(InputStream input, Charset encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, encoding);
        return readLines(reader);
    }

    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    public static Properties readProperties(InputStream inputStream, Charset charset)
            throws IOException {
        if (inputStream == null) {
            throw new IOException("null inputstream!");
        }
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(inputStream, charset));
        } finally {
            CloseUtil.close(inputStream);
        }
        return properties;
    }

    public static Properties readPropertiesByCls(String file) throws IOException {
        return readPropertiesByCls(file, ENCODING);
    }

    public static Properties readPropertiesByCls(String file, Charset charset)
            throws IOException {
        InputStream inputStream = readInputStreamByCls(file);
        if (inputStream == null) {
            throw new FileNotFoundException(file);
        }
        return readProperties(inputStream, charset);
    }

    public static Properties readPropertiesByCls(String file, Charset charset,
            ClassLoader classLoader) throws IOException {
        InputStream inputStream = readInputStreamByCls(file, classLoader);
        if (inputStream == null) {
            throw new FileNotFoundException(file);
        }
        return readProperties(inputStream, charset);
    }

    public static Properties readPropertiesByFile(File file, Charset charset)
            throws IOException {
        return readProperties(new FileInputStream(file), charset);
    }

    public static Properties readPropertiesByFile(String file, Charset charset)
            throws IOException {
        return readPropertiesByFile(new File(file), charset);
    }

    public static String readStringByCls(String file) throws IOException {
        return readStringByCls(file, ENCODING);
    }

    public static String readStringByCls(String file, Charset encoding) throws IOException {
        return createString(readBytesByCls(file), encoding);
    }

    private static String createString(byte[] data, Charset encoding) {
        if (data == null) {
            return null;
        }
        return new String(data, encoding);
    }

    public static String readStringByFile(File file, Charset encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return input2String(in, encoding);
        } finally {
            CloseUtil.close(in);
        }

    }

    public static void scanDirectory(File file, OnDirectoryScan onDirectoryScan) throws Exception {

        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {

            File[] fs = file.listFiles();

            for (File f : fs) {
                scanDirectory(f, onDirectoryScan);
            }

            onDirectoryScan.onDirectory(file);
            return;
        }

        onDirectoryScan.onFile(file);
    }

    public static long skip(InputStream input, long toSkip) throws IOException {
        if (toSkip < 0L) {
            throw new IllegalArgumentException(
                    "Skip count must be non-negative, actual: " + toSkip);
        }
        long remain = toSkip;
        while (remain > 0L) {
            long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, 2048L));
            if (n < 0L) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    public static void skipFully(InputStream input, long toSkip) throws IOException {
        if (toSkip < 0L) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return (reader instanceof BufferedReader) ? (BufferedReader) reader
                : new BufferedReader(reader);
    }

    public static void write(byte[] data, OutputStream output) throws IOException {
        if (data == null) {
            return;
        }
        output.write(data);
    }

    public static void writeByCls(String file, String content) throws IOException {
        writeByCls(file, content, CLASS_LOADER);
    }

    public static void writeByCls(String file, String content, ClassLoader classLoader)
            throws IOException {
        writeByCls(file, content, false, classLoader);
    }

    public static void writeByCls(String file, String content, boolean append) throws IOException {
        writeByCls(file, content, append, CLASS_LOADER);
    }

    public static void writeByCls(String file, String content, boolean append,
            ClassLoader classLoader) throws IOException {
        writeByCls(file, content, ENCODING, append, classLoader);
    }

    public static void writeByCls(String file, byte[] bytes, boolean append,
            ClassLoader classLoader) throws IOException {
        File realFile = readFileByCls(file, classLoader);
        writeByFile(realFile, bytes, append);
    }

    public static void writeByCls(String file, String content, Charset encoding, boolean append,
            ClassLoader classLoader) throws IOException {
        File realFile = readFileByCls(file, classLoader);
        writeByFile(realFile, content, encoding, append);
    }

    public static void writeByFile(File file, byte[] bytes) throws IOException {
        writeByFile(file, bytes, false);
    }

    public static void writeByFile(File file, byte[] bytes, boolean append) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file, append);
            out.write(bytes);
        } finally {
            CloseUtil.close(out);
        }
    }

    public static void writeByFile(File file, String content) throws IOException {
        writeByFile(file, content, ENCODING, false);

    }

    public static void writeByFile(File file, String data, boolean append) throws IOException {
        writeByFile(file, data, ENCODING, append);
    }

    public static void writeByFile(File file, String content, Charset encoding) throws IOException {
        writeByFile(file, content, encoding, false);
    }

    public static void writeByFile(File file, String content, Charset encoding, boolean append)
            throws IOException {
        write(content.getBytes(encoding), openOutputStream(file, append));
    }

    public static void writePropertiesByCls(Properties properties, String file,
            ClassLoader classLoader) throws IOException {
        File realFile = readFileByCls(file, classLoader);
        FileOutputStream fos = new FileOutputStream(realFile);
        properties.store(fos, "");
        CloseUtil.close(fos);
    }

    public static interface OnDirectoryScan {

        public abstract void onDirectory(File directory) throws Exception;

        public abstract void onFile(File file) throws Exception;
    }

}
