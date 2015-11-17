package test.io.jni;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.firenio.common.FileUtil;
import com.firenio.common.Unsafe;
import com.firenio.component.Native;

public class TestNative {

    static {
        try {
            //            loadNative("main.o");
            System.load("/home/test/git-rep/socket-epoll/debug/Native.o");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static void loadNative(String name) throws IOException {
        InputStream in = TestNative.class.getClassLoader().getResourceAsStream(name);
        File tmpFile = File.createTempFile(name, ".o");
        byte[] data = FileUtil.inputStream2ByteArray(in);
        FileUtil.writeByFile(tmpFile, data);
        System.load(tmpFile.getAbsolutePath());
        tmpFile.deleteOnExit();
    }

    public static void main(String[] args) {

    }

}
