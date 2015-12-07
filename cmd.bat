@rem java -Xms128m -Xmx512m -cp WEB-INF\classes;WEB-INF\lib\*; com.yoocent.mtp.jms.client.cmd.Portal %CD%

cd bin
java -Xms128m -Xmx512m -cp .;..\lib\*; com.yoocent.mtp.jms.client.cmd.Portal %CD%