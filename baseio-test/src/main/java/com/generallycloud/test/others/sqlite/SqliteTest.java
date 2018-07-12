package com.generallycloud.test.others.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * 
 * @author qianj
 * @version 1.0.0 @2011-4-21 下午09:34:15
 */
public class SqliteTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SqliteTest.class);
    
    public static void main(String[] args) {
        test();
    }
    
    static void test(){
        
     // 加载驱动
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("数据库驱动未找到!");
        }
        // 得到连接 会在你所填写的目录建一个你命名的文件数据库
        Connection conn;
        try {
//            conn = DriverManager.getConnection("jdbc:sqlite:c:/ryms/test.db", null, null);
            conn = DriverManager.getConnection("jdbc:sqlite:memory", null, null);
            // 设置自动提交为false
            conn.setAutoCommit(false);

            //判断表是否存在
            ResultSet rsTables = conn.getMetaData().getTables(null, null, "student", null);
            if (rsTables.next()) {
                System.out.println("表存在");
            } else {
                Statement stmt = conn.createStatement();
                String createSql = "create table student (id int primary key,"
                        + "v01 varchar(32),"
                        + "v02 varchar(32),"
                        + "v03 varchar(32),"
                        + "v04 varchar(32),"
                        + "v05 varchar(32),"
                        + "v06 varchar(32),"
                        + "v07 varchar(32),"
                        + "v08 varchar(32),"
                        + "v09 varchar(32),"
                        + "v10 varchar(32),"
                        + "v11 varchar(32),"
                        + "v12 varchar(32),"
                        + "v13 varchar(32),"
                        + "v14 varchar(32),"     
                        + "v15 varchar(32),"
                        + "v16 varchar(32));";
                stmt.executeUpdate(createSql);
                stmt.close();
            }
            
            {
                String deleteSql = "delete from student where 1=1";
                PreparedStatement stmt = conn.prepareStatement(deleteSql);
                stmt.executeUpdate();
                stmt.close();
                conn.commit();
            }
            
            {
                ThreadUtil.exec(() ->{

                    try {
                        String sql = "select * from student order by id desc limit 0,20 ";
                        PreparedStatement s = conn.prepareStatement(sql);
                        for(;;){
                            ResultSet rs = s.executeQuery();
                            int i = 0;
                            while (rs.next()) {
                                if (i++ == 5) {
                                    break;
                                }
                                logger.info("query res , id : {},v1:{}",rs.getInt("id"),rs.getString("v01"));
                            }
                            rs.close();
                            logger.info("selected :"+sql);
                            ThreadUtil.sleep(1000);
                        }
                    } catch (SQLException e) {
                        logger.error(e.getMessage(),e);
                    }
                });
            }
            
            {
                ThreadUtil.exec(() ->{
                    for(;;){
                        try {
                            conn.commit();
                            logger.info("commited");
                            ThreadUtil.sleep(1000);
                        } catch (SQLException e) {
                            logger.error(e.getMessage(),e);
                            break;
                        }
                    }
                });
            }
            
            {
                String insertSql = "insert into student ("
                        + "id,"
                        + "v01,"
                        + "v02,"
                        + "v03,"
                        + "v04,"
                        + "v05,"
                        + "v06,"
                        + "v07,"
                        + "v08,"
                        + "v09,"
                        + "v10,"
                        + "v11,"
                        + "v12,"
                        + "v13,"
                        + "v14,"
                        + "v15,"
                        + "v16)"
                        + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                PreparedStatement stmt = conn.prepareStatement(insertSql);
                
                int count = 1024 * 1024 * 1;
                long start = System.currentTimeMillis();
                for (int i = 0; i < count; i++) {
                    String v = Integer.toString(i);
                    stmt.setInt(1, i);
                    for (int j = 2; j < 18; j++) {
                        stmt.setString(j, v);
                    }
                    stmt.executeUpdate();
                    if (i % 10000 == 0) {
                        logger.info("inserted "+i);
                    }
                }
                stmt.close();
                // 提交
                conn.commit();
                logger.info("inserted , cost:"+(System.currentTimeMillis() - start));
            }
            
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQL异常!");
        }
    }
    

}
