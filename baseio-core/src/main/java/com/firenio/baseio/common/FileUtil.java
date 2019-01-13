/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.common;

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

    private static final Charset     ENCODING         = Util.UTF8;

    private static final byte[]      SKIP_BYTE_BUFFER = new byte[2048];

    public static abstract class OnDirectoryScan {

        public boolean onDirectory(File directory) throws Exception {
            return true;
        }

        public abstract void onFile(File file) throws Exception;
    }

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files == null) {
                directory.delete();
                return;
            }
            for (File file : files) {
                cleanDirectory(file);
            }
        } else {
            directory.delete();
        }
    }

    public static void cleanDirectoryByCls(String file, ClassLoader classLoader)
            throws IOException {
        File realFile = readFileByCls(file, classLoader);
        cleanDirectory(realFile);
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

    public static boolean createDirectory(File file) throws IOException {
        if (file.exists()) {
            return true;
        }
        return file.mkdirs();
    }

    private static String createString(byte[] data, Charset encoding) {
        if (data == null) {
            return null;
        }
        return new String(data, encoding);
    }

    public static String decodeURL(String url, Charset charset) {
        try {
            return URLDecoder.decode(url, charset.name());
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    public static String getCurrentPath() {
        return getCurrentPath(CLASS_LOADER);
    }

    public static String getCurrentPath(ClassLoader classLoader) {
        URL url = classLoader.getResource(".");
        String path;
        if (url == null) {
            path = new File(".").getAbsoluteFile().getParent();
        } else {
            path = new File(url.getFile()).getAbsoluteFile().getPath();
        }
        try {
            return URLDecoder.decode(path, ENCODING.name());
        } catch (UnsupportedEncodingException e) {
            return path;
        }
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
        byte[] data = inputStream2ByteArray0(inputStream, inputStream.available());
        if (data == null) {
            return null;
        }
        byte[] temp = null;
        for (;;) {
            temp = inputStream2ByteArray0(inputStream, inputStream.available());
            if (temp == null) {
                break;
            }
            byte[] newData = new byte[data.length + temp.length];
            System.arraycopy(data, 0, newData, 0, data.length);
            System.arraycopy(temp, 0, newData, data.length, temp.length);
            data = newData;
        }
        return data;
    }

    private static byte[] inputStream2ByteArray0(InputStream inputStream, int size)
            throws IOException {
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
            Util.close(inputStream);
        }
    }

    public static byte[] readBytesByFile(File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return inputStream2ByteArray0(in, (int) file.length());
        } finally {
            Util.close(in);
        }
    }

    public static File readFileByCls(String file) throws IOException {
        return readFileByCls(file, CLASS_LOADER);
    }

    public static File readFileByCls(String file, ClassLoader classLoader) throws IOException {
        URL url = classLoader.getResource(file);
        if (url == null) {
            File root = new File(getCurrentPath(classLoader));
            return new File(root.getAbsolutePath() + "/" + file);
        }
        String path = url.getFile();
        return new File(URLDecoder.decode(path, ENCODING.name()));
    }

    public static int readInputStream(InputStream inputStream, byte[] cache) throws IOException {
        int read = 0;
        int cLength = cache.length;
        for (; read < cLength;) {
            int r = inputStream.read(cache, read, cLength - read);
            if (r == -1) {
                return read;
            }
            read += r;
        }
        return read;
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
            Util.close(in);
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
        BufferedReader reader = null;
        try {
            reader = toBufferedReader(input);
            List<String> list = new ArrayList<>();
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
            return list;
        } finally {
            Util.close(reader);
            Util.close(input);
        }
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
            Util.close(inputStream);
        }
        return properties;
    }

    public static Properties readPropertiesByCls(String file) throws IOException {
        return readPropertiesByCls(file, ENCODING);
    }

    public static Properties readPropertiesByCls(String file, Charset charset) throws IOException {
        return readPropertiesByCls(file, charset, CLASS_LOADER);
    }

    public static Properties readPropertiesByCls(String file, Charset charset,
            ClassLoader classLoader) throws IOException {
        InputStream inputStream = readInputStreamByCls(file, classLoader);
        if (inputStream == null) {
            throw new FileNotFoundException(file);
        }
        return readProperties(inputStream, charset);
    }

    public static Properties readPropertiesByCls(String file, ClassLoader classLoader)
            throws IOException {
        return readPropertiesByCls(file, ENCODING, classLoader);
    }

    public static Properties readPropertiesByFile(File file, Charset charset) throws IOException {
        return readProperties(new FileInputStream(file), charset);
    }

    public static Properties readPropertiesByFile(String file, Charset charset) throws IOException {
        return readPropertiesByFile(new File(file), charset);
    }

    public static String readStringByCls(String file) throws IOException {
        return readStringByCls(file, ENCODING);
    }

    public static String readStringByCls(String file, Charset encoding) throws IOException {
        return readStringByCls(file, encoding, CLASS_LOADER);
    }

    public static String readStringByCls(String file, Charset encoding, ClassLoader cl)
            throws IOException {
        return createString(readBytesByCls(file, cl), encoding);
    }

    public static String readStringByFile(File file, Charset encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return input2String(in, encoding);
        } finally {
            Util.close(in);
        }
    }

    public static String readStringByFile(String file) throws IOException {
        return readStringByFile(file, ENCODING);
    }

    public static String readStringByFile(String file, Charset encoding) throws IOException {
        return readStringByFile(new File(file), encoding);
    }

    public static void scanDirectory(File file, OnDirectoryScan onDirectoryScan) throws Exception {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            if (onDirectoryScan.onDirectory(file)) {
                File[] fs = file.listFiles();
                for (File f : fs) {
                    scanDirectory(f, onDirectoryScan);
                }
            }
        } else {
            onDirectoryScan.onFile(file);
        }
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

    public static void writeByCls(String file, byte[] content, boolean append) throws IOException {
        writeByCls(file, content, append, CLASS_LOADER);
    }

    public static void writeByCls(String file, byte[] bytes, boolean append,
            ClassLoader classLoader) throws IOException {
        File realFile = readFileByCls(file, classLoader);
        writeByFile(realFile, bytes, append);
    }

    public static void writeByCls(String file, String content) throws IOException {
        writeByCls(file, content, CLASS_LOADER);
    }

    public static void writeByCls(String file, String content, boolean append) throws IOException {
        writeByCls(file, content, append, CLASS_LOADER);
    }

    public static void writeByCls(String file, String content, boolean append,
            ClassLoader classLoader) throws IOException {
        writeByCls(file, content, ENCODING, append, classLoader);
    }

    public static void writeByCls(String file, String content, Charset encoding, boolean append)
            throws IOException {
        writeByCls(file, content.getBytes(encoding), append);
    }

    public static void writeByCls(String file, String content, Charset encoding, boolean append,
            ClassLoader classLoader) throws IOException {
        File realFile = readFileByCls(file, classLoader);
        writeByFile(realFile, content, encoding, append);
    }

    public static void writeByCls(String file, String content, ClassLoader classLoader)
            throws IOException {
        writeByCls(file, content, false, classLoader);
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
            Util.close(out);
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

    public static void writePropertiesByCls(java.util.Properties properties, String file)
            throws IOException {
        writePropertiesByCls(properties, file, CLASS_LOADER);
    }

    public static void writePropertiesByCls(java.util.Properties properties, String file,
            ClassLoader classLoader) throws IOException {
        File realFile = readFileByCls(file, classLoader);
        FileOutputStream fos = new FileOutputStream(realFile);
        properties.store(fos, "");
        Util.close(fos);
    }

}
