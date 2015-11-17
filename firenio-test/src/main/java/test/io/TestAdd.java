package test.io;/**
 * Create by wangkai on 2019/4/4
 */

/**
 * @program: firenio
 * @description: 123
 * @author: wangkai
 * @create: 2019-04-04 17:21
 **/
public class TestAdd {


    public static void main(String[] args) throws  Exception {

        String path = "C:\\Users\\wangkai\\Downloads\\test";
        java.io.File file = new java.io.File(path);
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(1024 * 1024 * 32);


        com.firenio.common.FileUtil.scanDirectory(file, new com.firenio.common.FileUtil.OnDirectoryScan() {
            @Override
            public void onFile(java.io.File file) throws Exception {
                if(file.getName().endsWith(".png")){
                    byte [] data = com.firenio.common.FileUtil.readBytesByFile(file);
                    buf.put(data);
                }
            }
        });

        buf.flip();
        byte [] data = new byte[buf.limit()];
        buf.get(data);
        com.firenio.common.FileUtil.writeByCls("C:\\Users\\wangkai\\Downloads\\test\\aa.mp3", data,false );







    }











}
