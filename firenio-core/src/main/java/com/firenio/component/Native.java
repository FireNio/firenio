package com.firenio.component;

import java.io.File;
import java.io.IOException;

import com.firenio.Develop;
import com.firenio.Options;
import com.firenio.common.FileUtil;
import com.firenio.common.Unsafe;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

//not complete
public class Native {

    private static final Logger   logger          = LoggerFactory.getLogger(Native.class);

    //gcc -shared -g -fPIC -m64 -o obj/native.o src/Native.cpp
    //gcc -shared -fPIC -m64 -o obj/native.o src/Native.cpp

    public static final int SEEK_SET  = 0;  /* Seek from beginning of file.  */
    public static final int SEEK_CUR  = 1;  /* Seek from current position.  */
    public static final int SEEK_END  = 2;  /* Seek from end of file.  */
    public static final int SEEK_DATA = 3;  /* Seek to next data.  */
    public static final int SEEK_HOLE = 4;  /* Seek to next hole.  */

    public static final int O_ACCMODE  = 0003;
    public static final int O_RDONLY   = 00;
    public static final int O_WRONLY   = 01;
    public static final int O_RDWR     = 02;
    public static final int O_CREAT    = 0100;
    public static final int O_EXCL     = 0200;
    public static final int O_NOCTTY   = 0400;
    public static final int O_TRUNC    = 01000;
    public static final int O_APPEND   = 02000;
    public static final int O_NONBLOCK = 04000;
    public static final int O_DIRECT   = 040000;
    public static final int O_NDELAY   = O_NONBLOCK;
    public static final int O_SYNC     = 04010000;
    public static final int O_FSYNC    = O_SYNC;
    public static final int O_ASYNC    = 020000;

    public static final  int      EPOLL_ERR       = 1 << 3;
    public static final  int      EPOLL_ET        = 1 << 31;
    public static final  int      EPOLL_IN        = 1 << 0;
    public static final  int      EPOLL_OUT       = 1 << 2;
    public static final  int      EPOLL_HUP       = 1 << 4;
    public static final  int      EPOLL_RD_HUP    = 1 << 13;
    public static final  int      EPOLL_IN_ET     = EPOLL_IN | EPOLL_ET;
    public static final  int      EPOLL_IN_OUT    = EPOLL_IN | EPOLL_OUT;
    public static final  int      EPOLL_OUT_ET    = EPOLL_OUT | EPOLL_ET;
    public static final  int      EPOLL_IN_OUT_ET = EPOLL_IN_ET | EPOLL_OUT_ET;
    public static final  boolean  EPOLL_AVAILABLE = try_load_epoll();
    public static final  int      SIZEOF_EPOLL_EVENT;
    public static final  int      SIZEOF_SOCK_ADDR_IN;
    public static final  String[] ERRORS;

    static {
        if (EPOLL_AVAILABLE) {
            ERRORS = load_errors();
            SIZEOF_EPOLL_EVENT = size_of_epoll_event();
            SIZEOF_SOCK_ADDR_IN = size_of_sockaddr_in();
        } else {
            ERRORS = null;
            SIZEOF_EPOLL_EVENT = -1;
            SIZEOF_SOCK_ADDR_IN = -1;
        }
    }

    private static String[] load_errors() {
        String[] errors = new String[256];
        byte[]   temp   = new byte[1024];
        for (int i = 0; i < errors.length; i++) {
            int len = strerrno(i, temp);
            errors[i] = new String(temp, 0, len);
        }
        return errors;
    }

    private static boolean try_load_epoll() {
        if (!Unsafe.DIRECT_BUFFER_AVAILABLE) {
            return false;
        }
        if (Unsafe.IS_LINUX && Options.isEnableEpoll()) {
            boolean epollLoaded = false;
            if (Develop.EPOLL_DEBUG) {
                logger.info("load epoll from:{}", Develop.EPOLL_PATH);
                try {
                    System.load(Develop.EPOLL_PATH);
                    epollLoaded = true;
                } catch (Throwable ignore) {
                }
            } else {
                try {
                    load_native("native.o");
                    epollLoaded = true;
                } catch (Throwable e) {
                    if (Develop.NATIVE_DEBUG) {
                        logger.error("epoll load failed:" + e.getMessage(), e);
                    }
                }
            }
            if (epollLoaded) {
                try {
                    int fd = epoll_create(1);
                    if (fd != -1) {
                        close(fd);
                        return true;
                    }
                    if (Develop.NATIVE_DEBUG) {
                        logger.error("epoll create failed:" + Native.err_str());
                    }
                } catch (Throwable e) {
                }
            }
        }
        return false;
    }

    private static void load_native(String name) throws IOException {
        byte[] native_data = FileUtil.readBytesByCls(name, Native.class.getClassLoader());
        File   native_tmp  = File.createTempFile(name, ".o");
        FileUtil.writeByFile(native_tmp, native_data);
        System.load(native_tmp.getAbsolutePath());
        native_tmp.deleteOnExit();
    }

    public static int close_event() {
        return EPOLL_ERR | EPOLL_HUP | EPOLL_RD_HUP;
    }

    public static int accept(int epfd, int listen_fd, long address) {
        return print_exception(accept0(epfd, listen_fd, address));
    }

    public static int bind(String host, int port, int backlog) {
        return bind0(host, port, backlog);
    }

    public static int close(int fd) {
        if (fd == -1) {
            return -1;
        }
        return print_exception(close0(fd));
    }

    public static int connect(String host, int port) {
        return print_exception(connect0(host, port));
    }

    public static int epoll_add(int epfd, int fd, int state) {
        return epoll_add0(epfd, fd, state);
    }

    public static int epoll_create(int size) {
        return throwRuntimeException(epoll_create0(size));
    }

    public static int epoll_del(int epfd, int fd) {
        if (fd == -1) {
            return -1;
        }
        return print_exception(epoll_del0(epfd, fd));
    }

    public static int epoll_mod(int epfd, int fd, int state) {
        return print_exception(epoll_mod0(epfd, fd, state));
    }

    public static int epoll_wait(int fd, long address, int max_events, long timeout) {
        return print_exception(epoll_wait0(fd, address, max_events, timeout));
    }

    public static String err_str() {
        return err_str(errno());
    }

    private static String err_str(int errno) {
        if (errno < ERRORS.length) {
            return ERRORS[errno];
        } else {
            return "errno=" + errno;
        }
    }

    public static int event_fd_read(int fd) {
        return print_exception(event_fd_read0(fd));
    }

    public static int event_fd_write(int fd, long value) {
        return print_exception(event_fd_write0(fd, value));
    }

    public static long new_epoll_event_array(int size) {
        return Unsafe.allocate(size * SIZEOF_EPOLL_EVENT);
    }

    public static int new_event_fd() {
        return throwRuntimeException(new_event_fd0());
    }

    public static int read(int fd, long address, int len) {
        return print_exception(read0(fd, address, len));
    }

    public static int pread(int fd, long address, int len, long pos) {
        return print_exception(pread0(fd, address, len, pos));
    }

    private static int print_exception(int res) {
        if (Develop.NATIVE_DEBUG && res == -1) {
            int errno = errno();
            if (errno != 0) {
                IOException e = new IOException(err_str(errno));
                logger.error(e.getMessage(), e);
            }
        }
        return res;
    }

    private static long print_exception(long res) {
        if (Develop.NATIVE_DEBUG && res == -1) {
            int errno = errno();
            if (errno != 0) {
                IOException e = new IOException(err_str(errno));
                logger.error(e.getMessage(), e);
            }
        }
        return res;
    }

    public static int throwException(int res) throws IOException {
        if (res == -1) {
            throw new IOException(err_str());
        }
        return res;
    }

    public static int throwRuntimeException(int res) {
        if (res == -1) {
            throw new RuntimeException(err_str());
        }
        return res;
    }

    public static int write(int fd, long address, int len) {
        return print_exception(write0(fd, address, len));
    }

    public static int pwrite(int fd, long address, int len, long pos) {
        return print_exception(pwrite0(fd, address, len, pos));
    }

    public static int get_port(int fd) {
        return Short.reverseBytes((short) print_exception(get_port0(fd))) & 0xffff;
    }

    public static long writev(int fd, long iovec, int count) {
        return print_exception(writev0(fd, iovec, count));
    }

    public static int set_socket_opt(int fd, int type, int name, int value) {
        return print_exception(set_socket_opt0(fd, type, name, value));
    }

    public static boolean finish_connect(int fd) {
        int type = SocketOptions.RAW_SOL_SOCKET;
        int res  = get_socket_opt0(fd, type, SocketOptions.SO_ERROR & 0xff);
        if (res != 0) {
            if (res == -1) {
                print_exception(res);
            } else {
                if (Develop.NATIVE_DEBUG) {
                    if (res < 0) {
                        res = -res;
                    }
                    IOException e = new IOException(err_str(res));
                    logger.error(e.getMessage(), e);
                }
            }
            return false;
        }
        return true;
    }

    public static int get_socket_opt(int fd, int type, int name) {
        return print_exception(get_socket_opt0(fd, type, name));
    }

    public static int open(String path, int op, int pem) {
        return print_exception(open0(path, op, pem));
    }

    public static long posix_memalign_allocate(int len, int align) {
        return print_exception(posix_memalign_allocate0(len, align));
    }

    public static long file_length(int fd) {
        return print_exception(file_length0(fd));
    }

    public static long lseek(int fd, long off, int seek_mode) {
        return print_exception(lseek0(fd, off, seek_mode));
    }

    // epoll----------------------------------------------------------------------------------------

    public static native int errno();

    private static native int size_of_epoll_event();

    private static native int size_of_sockaddr_in();

    private static native int strerrno(int no, byte[] buf);

    private static native int get_socket_opt0(int fd, int type, int name);

    private static native int set_socket_opt0(int fd, int type, int name, int value);

    private static native int accept0(int epfd, int listen_fd, long address);

    private static native int bind0(String host, int port, int backlog);

    private static native int close0(int fd);

    private static native int connect0(String host, int port);

    private static native int epoll_add0(int epfd, int fd, int state);

    private static native int epoll_create0(int size);

    private static native int epoll_del0(int epfd, int fd);

    private static native int epoll_mod0(int epfd, int fd, int state);

    private static native int epoll_wait0(int fd, long address, int max_events, long timeout);

    private static native int event_fd_read0(int fd);

    private static native int event_fd_write0(int fd, long value);

    private static native int get_port0(int fd);

    private static native int new_event_fd0();

    private static native int read0(int fd, long address, int len);

    private static native int write0(int fd, long address, int len);

    private static native int writev0(int fd, long iovec, int count);

    // direct io----------------------------------------------------------------------------------------

    private static native int open0(String path, int op, int pem);

    private static native long posix_memalign_allocate0(int len, int align);

    private static native long file_length0(int fd);

    private static native long lseek0(int fd, long off, int seek_mode);

    private static native int pread0(int fd, long address, int len, long off);

    private static native int pwrite0(int fd, long address, int len, long off);

}
