package org.example;
import java.sql.Connection;
import org.example.utils.MyDataBase;

public class Main {
    public static void main(String[] args) {
        Connection conn = MyDataBase.getInstance().getConnection();

        if (conn != null) {
            System.out.println("DB CONNECTED ✔️");
        } else {
            System.out.println("DB NOT CONNECTED ❌");
        }
    }
}