package com.firenio.baseio.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.firenio.baseio.Develop;
import com.firenio.baseio.Options;
import com.firenio.baseio.common.FileUtil;
import com.firenio.baseio.common.Unsafe;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

//not complete
public class Native {

    //gcc -shared -g -fPIC -m64 -o obj/Native.o src/Native.cpp
    //gcc -shared -fPIC -m64 -o obj/Native.o src/Native.cpp

    private static final Logger  logger = LoggerFactory.getLogger(Native.class);

    public static final boolean  EPOLL_AVAIABLE;
    public static final int      EPOLLERR;
    public static final int      EPOLLET;
    public static final int      EPOLLIN;
    public static final int      EPOLLIN_ET;
    public static final int      EPOLLIN_OUT;
    public static final int      EPOLLIN_OUT_ET;
    public static final int      EPOLLOUT;
    public static final int      EPOLLOUT_ET;
    public static final int      EPOLLHUP;
    public static final int      EPOLLRDHUP;
    public static final String[] ERRORS;
    public static final boolean  IS_LINUX;
    public static final int      SIZEOF_EPOLL_EVENT;
    public static final int      SIZEOF_SOCKADDR_IN;

    static {
        EPOLLET = 1 << 31;
        EPOLLIN = 1 << 0;
        EPOLLOUT = 1 << 2;
        EPOLLERR = 1 << 3;
        EPOLLHUP = 1 << 4;
        EPOLLRDHUP = 1 << 13;
        EPOLLIN_OUT = EPOLLIN | EPOLLOUT;
        EPOLLIN_ET = EPOLLIN | EPOLLET;
        EPOLLOUT_ET = EPOLLOUT | EPOLLET;
        EPOLLIN_OUT_ET = EPOLLIN_ET | EPOLLOUT_ET;
        IS_LINUX = isLinux();
        EPOLL_AVAIABLE = IS_LINUX && tryLoadEpoll();
        if (EPOLL_AVAIABLE) {
            ERRORS = new String[256];
            byte[] temp = new byte[1024];
            for (int i = 0; i < ERRORS.length; i++) {
                int len = strerrno(i, temp);
                ERRORS[i] = new String(temp, 0, len);
            }
            SIZEOF_EPOLL_EVENT = size_of_epoll_event();
            SIZEOF_SOCKADDR_IN = size_of_sockaddr_in();
        } else {
            SIZEOF_EPOLL_EVENT = -1;
            SIZEOF_SOCKADDR_IN = -1;
            ERRORS = null;
        }
    }

    private static boolean tryLoadEpoll() {
        if (Options.isEnableEpoll()) {
            boolean epollLoaded = false;
            if (Develop.NATIVE_DEBUG) {
                try {
                    System.load("/home/test/git-rep/jni_epoll/obj/Native.o");
                    epollLoaded = true;
                } catch (Throwable e) {}
            } else {
                try {
                    loadNative("Native.o");
                    epollLoaded = true;
                } catch (Throwable e2) {}
            }
            if (epollLoaded) {
                try {
                    int fd = epoll_create(1);
                    if (fd != -1) {
                        close(fd);
                        return true;
                    }
                } catch (Throwable e) {}
            }
        }
        return false;
    }

    public static int all_event() {
        return EPOLLIN_OUT | EPOLLET;
    }

    public static int close_event() {
        return EPOLLERR | EPOLLHUP | EPOLLRDHUP;
    }

    public static int accept(int epfd, int listenfd, long address) {
        int res = accept0(epfd, listenfd, address);
        printException(res);
        return res;
    }

    public static int bind(String host, int port, int backlog) {
        int res = bind0(host, port, backlog);
        printException(res);
        return res;
    }

    public static int close(int fd) {
        if (fd == -1) {
            return 0;
        }
        int res = close0(fd);
        printException(res);
        return res;
    }

    public static int connect(String host, int port) {
        int res = connect0(host, port);
        printException(res);
        return res;
    }

    public static int epoll_add(int epfd, int fd, int state) {
        int res = epoll_add0(epfd, fd, state);
        printException(res);
        return res;
    }

    public static int epoll_create(int size) {
        int res = epoll_create0(size);
        printException(res);
        return res;
    }

    public static int epoll_del(int epfd, int fd) {
        if (fd == -1) {
            return 0;
        }
        int res = epoll_del0(epfd, fd);
        printException(res);
        return res;
    }

    public static int epoll_mod(int epfd, int fd, int state) {
        int res = epoll_mod0(epfd, fd, state);
        printException(res);
        return res;
    }

    public static int epoll_wait(int fd, long address, int maxEvents, long timeout) {
        int res = epoll_wait0(fd, address, maxEvents, timeout);
        printException(res);
        return res;
    }

    public static native int errno();

    public static String errstr() {
        return errstr(errno());
    }

    private static String errstr(int errno) {
        if (errno < ERRORS.length) {
            return ERRORS[errno];
        } else {
            return "errno=" + errno;
        }
    }

    public static int event_fd_read(int fd) {
        int res = event_fd_read0(fd);
        printException(res);
        return res;
    }

    public static int event_fd_write(int fd, long value) {
        int res = event_fd_write0(fd, value);
        printException(res);
        return res;
    }

    private static boolean isLinux() {
        return Util.getStringProperty("os.name", "").toLowerCase().startsWith("lin");
    }

    private static void loadNative(String name) throws IOException {
        InputStream in = Native.class.getClassLoader().getResourceAsStream(name);
        File tmpFile = File.createTempFile(name, ".o");
        byte[] data = FileUtil.inputStream2ByteArray(in);
        FileUtil.writeByFile(tmpFile, data);
        System.load(tmpFile.getAbsolutePath());
        tmpFile.deleteOnExit();
    }

    public static final long new_epoll_event_array(int size) {
        return Unsafe.allocate(size * SIZEOF_EPOLL_EVENT);
    }

    public static int new_event_fd() {
        int res = new_event_fd0();
        printException(res);
        return res;
    }

    public static int read(int fd, long address, int len) {
        int res = read0(fd, address, len);
        printException(res);
        return res;
    }

    private static void printException(int res) {
        if (Develop.NATIVE_DEBUG && res == -1) {
            int errno = errno();
            if (errno != 0) {
                IOException e = new IOException(errstr(errno));
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static native int size_of_epoll_event();

    public static native int size_of_sockaddr_in();

    public static native int strerrno(int no, byte[] buf);

    public static void throwException() throws IOException {
        throw new IOException(errstr());
    }

    public static int write(int fd, long address, int len) {
        int res = write0(fd, address, len);
        printException(res);
        return res;
    }

    public static int get_port(int fd) {
        int res = get_port0(fd);
        printException(res);
        return Short.reverseBytes((short) res) & 0xffff;
    }

    public static long writev(int fd, long iovec, int count) {
        int res = writev0(fd, iovec, count);
        printException(res);
        return res;
    }

    public static int set_socket_opt(int fd, int type, int name, int value) {
        int res = set_socket_opt0(fd, type, name, value);
        printException(res);
        return res;
    }

    public static boolean finish_connect(int fd) {
        int type = SocketOptions.SOL_SOCKET >> 16;
        int res = get_socket_opt0(fd, type, SocketOptions.SO_ERROR & 0xff);
        if (res != 0) {
            if (res == -1) {
                printException(res);
            } else {
                IOException e = new IOException(errstr(res & 0x7fffffff));
                logger.error(e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    public static int get_socket_opt(int fd, int type, int name) {
        int res = get_socket_opt0(fd, type, name);
        printException(res);
        return res;
    }

    //---------------------------------------------------------------------------------------------------------
    private static native int get_socket_opt0(int fd, int type, int name);

    private static native int set_socket_opt0(int fd, int type, int name, int value);

    private static native int accept0(int epfd, int listenfd, long address);

    private static native int bind0(String host, int port, int backlog);

    private static native int close0(int fd);

    private static native int connect0(String host, int port);

    private static native int epoll_add0(int epfd, int fd, int state);

    private static native int epoll_create0(int size);

    private static native int epoll_del0(int epfd, int fd);

    private static native int epoll_mod0(int epfd, int fd, int state);

    private static native int epoll_wait0(int fd, long address, int maxEvents, long timeout);

    private static native int event_fd_read0(int fd);

    private static native int event_fd_write0(int fd, long value);

    private static native int get_port0(int fd);

    private static native int new_event_fd0();

    private static native int read0(int fd, long address, int len);

    private static native int write0(int fd, long address, int len);

    private static native int writev0(int fd, long iovec, int count);

}
