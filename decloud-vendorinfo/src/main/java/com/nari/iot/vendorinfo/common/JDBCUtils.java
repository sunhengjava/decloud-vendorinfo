package com.nari.iot.vendorinfo.common;


import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JDBCUtils {
    //1.义成员变量 Datasounce
    private static DataSource dataSource;
    static{
        try {
            //1.加载配置文件
            Properties properties = new Properties();
            properties.load (JDBCUtils.class.getClassLoader().getResourceAsStream("druid.properties"));
            //2.获取Datasource
            dataSource=  DruidDataSourceFactory.createDataSource(properties);
        }catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 获取连接
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close(Statement stmt,Connection conn){
        close(null,stmt,conn);
    }
    public static void close(ResultSet rs,Statement stmt,Connection conn){
        if (rs != null){
            try {
                rs.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (stmt != null){
            try {
                stmt.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        if (conn != null){
            try {
                conn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取连接池方法
     * @return
     */
    public static DataSource getDataSource(){
        return dataSource;
    }

}