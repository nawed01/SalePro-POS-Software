/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sale.pro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Go
 */
public class db {
   
 public static Connection mycon(){
    String name,url,pass;
 
     Connection con = null;
     
     try {
         
         Class.forName("org.sqlite.JDBC");
         name="root";
         pass="12345678";
         url="jdbc:sqlite:sale pro.db";
         con = DriverManager.getConnection(url);
         return con;
         
         
     } catch (ClassNotFoundException | SQLException e) {
         JOptionPane.showMessageDialog(null, "Database Connection Not Available!");
         System.out.println(e);
           return null;
     }
   
 
 
 }   
    
    
    
}
