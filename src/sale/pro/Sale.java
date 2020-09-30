/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */ 
package sale.pro;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nawed Akhtar
 */
@SuppressWarnings("serial")
public class Sale extends javax.swing.JPanel {

 private static  String bar_code="0";
  private Double stock_qty=0.0;
 private static String cus_id ="0";
    
    public Sale() {
        initComponents();
        jTable1.setRowHeight(jTable1.getRowHeight()+10);
        data_load();
    }
    
    public void pro_total_calc(){
        Double qt = Double.valueOf(p_qty.getText());
           Double price = Double.valueOf(u_price.getText());
           Double total;
           
           total= qt * price;  
           tot_p.setText(String.valueOf(total));
    }
    
    public void getCusid() throws SQLException
    {   String c_name = cs_name.getText();
          Statement s = db.mycon().createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM customer WHERE customer_name LIKE '%"+c_name+"%' ");
            if(rs.next())
            {
                cus_id = (rs.getString("cid"));
            }
              s.close();
              rs.close();
    }
 @SuppressWarnings("unchecked")
    public void print_btn_fn() throws SQLException
    {
                  try {
            DefaultTableModel dt = (DefaultTableModel) jTable1.getModel();
             int rc = dt.getRowCount();
              for(int i=0;i<rc;i++)
              {
            String inid = dt.getValueAt(i, 0).toString();
            String p_name = dt.getValueAt(i, 1).toString();
            String b_code = dt.getValueAt(i, 2).toString();
            String qty = dt.getValueAt(i, 3).toString();
            String un_price = dt.getValueAt(i, 4).toString();
            String tot_price = dt.getValueAt(i, 5).toString();
            
            
            Statement s = db.mycon().createStatement();
            s.executeUpdate("INSERT INTO cart (INID, product_name, bar_code, qty, unit_price, total_price) VALUES('"+inid+"','"+p_name+"','"+b_code+"','"+qty+"','"+un_price+"','"+tot_price+"')"); 
              s.close();

            // cart DB
        
              }
             JOptionPane.showMessageDialog(null, "Amount Paid & Saved");
       }
          catch (SQLException ex) {
                System.out.println(ex);
            }
          
       try {
            //SALE DB -->
        //`sale_id`, `INID`, `Cid`, `Customer_name`, `Total_qty`, `Status`, `Balance`
        String inv_id = in_id.getText();
        String cname = cs_name.getText();
        String Tot_qty = tot_qty.getText();
        Double Tot_bil = Double.valueOf(bill_tot.getText());
         String bal = balance.getText();
         getCusid();
           
         // paid check
        
        Double tot = Double.valueOf(bill_tot.getText());
        Double paid = Double.valueOf(paid_amt.getText());
        String Status = null;
        
        if (paid.equals(00.00))
        {
             Status = "NULL";
        }else if(tot>paid)
        {
            Status = "PARTIAL";
             
        }else if (paid>=tot)
        {
            Status = "PAID";
        }
        
       String pad = paid_amt.getText();
       
        
        Statement ss = db.mycon().createStatement();
        ss.executeUpdate("INSERT INTO sale (INID, Cid, Customer_name, Total_qty,Total_bill,paid_amt, Status, Balance) VALUES('"+inv_id+"','"+cus_id+"','"+cname+"','"+Tot_qty+"','"+Tot_bil+"','"+pad+"','"+Status+"','"+bal+"')");
        ss.close();
           
       }
         catch(NumberFormatException | SQLException e){
             
             System.out.println(e);
                  
         }  
            
       //invoice number update/save
       Statement statement = db.mycon().createStatement();
       try{
           String id = in_id.getText();
           statement.execute ("UPDATE extra SET val='"+id+"' WHERE exid= 1");
       }
       catch (SQLException e){
           System.out.println(e);
       }
     finally{
      statement.close();
     }
        
       //Print Invoice 
       
       try{
            HashMap para = new HashMap();
        para.put("inv_id", in_id.getText());
        ReportView r = new ReportView("src\\reports\\print.jasper",para);
        r.setVisible(true);
       }
        catch(Exception e)
        {
             System.out.println(e);
        }
            
        stock_update();
   
    }
    
    
    
       public void stock_update() throws SQLException
          {
          
                 
              DefaultTableModel dt = (DefaultTableModel) jTable1.getModel();
              int rc = dt.getRowCount();
           
              for(int i=0;i<rc;i++)
              {
          
               String pd_name = jTable1.getValueAt(i, 1).toString();
               double sale_qt =  Double.valueOf(jTable1.getValueAt(i, 3).toString());
               Statement s = db.mycon().createStatement();
               ResultSet rs = s.executeQuery("SELECT Qty FROM product WHERE p_name = '"+pd_name+"' ");
               if(rs.next())
               {
                setStock_qty(Double.valueOf(rs.getString("Qty")));
                   
               }
                 s.close();
              rs.close();
             
              //Stock null check
             if(getStock_qty() == 0)
             {
                     JOptionPane.showMessageDialog(null, "ZERO STOCK");
             } else{
                 double final_stock = getStock_qty() - sale_qt;
                 String fn_stock = String.valueOf(final_stock);
              
             s.executeUpdate("UPDATE product SET Qty ='"+fn_stock+"' WHERE p_name = '"+pd_name+"'");
              s.close();
             }
             
            
            
            
          }
             
               

       }
    
    public void tot_cart_calc()
    {
         int numofrow = jTable1.getRowCount();
         double total = 0;
          for(int i=0 ; i < numofrow; i++)
          {
              double value = Double.valueOf(jTable1.getValueAt(i, 5).toString());
              total+= value;
          }
     bill_tot.setText(Double.toString(total));
     
// total qty calc.
     
       int numofrows = jTable1.getRowCount();
         double totals = 0;
          for(int i=0 ; i < numofrows; i++)
          {
              double values = Double.valueOf(jTable1.getValueAt(i, 3).toString());
              totals+= values;
          }
     tot_qty.setText(Double.toString(totals));
     
     
     
    }
    
    
    public void due_balance()
    {
        Double paid = Double.valueOf(paid_amt.getText());
       Double tot = Double.valueOf(bill_tot.getText());
       Double due;
       due = paid - tot;
       balance.setText(String.valueOf(due));  
    }
    
 @SuppressWarnings("unchecked")
 public void data_load()
{  
    //product load
    
    try{
        Statement s = db.mycon().createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM product");
        Vector v = new Vector();
        
         while(rs.next()){
            v.add(rs.getString("p_name"));
            DefaultComboBoxModel com = new DefaultComboBoxModel(v);
            combo2.setModel(com);
        }
           s.close();
              rs.close();
    }
    catch(SQLException e)
    {
        System.out.println(e);
    }
    
    //load last invoice number
    
    try{
        Statement s = db.mycon().createStatement();
        ResultSet rs = s.executeQuery("SELECT * FROM extra WHERE exid = 1");
    
    if(rs.next())
    {
        in_id.setText(rs.getString("val"));
    }
            
         s.close();
              rs.close();     
    }
    catch(SQLException e)
    {
        System.out.println(e);
           
    }
    //invlice id  icreamnet
    
    int i= Integer.valueOf(in_id.getText());
    i++;
    in_id.setText(String.valueOf(i));
         
    
    
}    
 public void cs_feed()
 {
     //customer feed to dtabase
    try{
        String cn = cs_name.getText();
        String cp = tp_no.getText();
    Statement s = db.mycon().createStatement();
     s.executeUpdate(" INSERT INTO customer (customer_name,Tp_number) VALUES ('"+cn+"','"+cp+"')");
      s.close();
               
    
    }
     catch(SQLException e)
    {
        System.out.println(e);
    }
    
 }
          
 /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        in_id = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        u_price = new javax.swing.JLabel();
        combo2 = new javax.swing.JComboBox<>();
        p_qty = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tot_p = new javax.swing.JLabel();
        cs_name = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        tp_no = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        bill_tot = new javax.swing.JLabel();
        balance = new javax.swing.JLabel();
        tot_qty = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        paid_amt = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(51, 63, 89));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("INVOICE N0. :");

        in_id.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        in_id.setText("01");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(in_id, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(981, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(in_id))
                .addGap(38, 38, 38))
        );

        jPanel4.setBackground(new java.awt.Color(120, 136, 171));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTable1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "INVOICE ID", "PRODUCT NAME", "BAR CODE", "QUANTITY", "UNIT PRICE", "TOTAL PRICE"
            }
        ));
        jTable1.setToolTipText("");
        jTable1.setGridColor(new java.awt.Color(224, 224, 224));
        jTable1.setName(""); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setText("CART ");

        jButton1.setBackground(new java.awt.Color(252, 161, 3));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/pro/img/icons8-buy-40.png"))); // NOI18N
        jButton1.setText("Add To Cart");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(252, 161, 3));
        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/pro/img/icons8-remove-40.png"))); // NOI18N
        jButton2.setText("REMOVE");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(252, 161, 3));
        jButton3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/pro/img/icons8-remove-40 (1).png"))); // NOI18N
        jButton3.setText("REMOVE ALL");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 853, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(8, 8, 8))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(473, 473, 473)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        jPanel3.setBackground(new java.awt.Color(120, 136, 171));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setText("Customer Name :");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setText("Product :");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel5.setText("Qty. :");

        u_price.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        u_price.setText("00.00");

        combo2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        combo2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "select" }));
        combo2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combo2ActionPerformed(evt);
            }
        });

        p_qty.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        p_qty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p_qtyActionPerformed(evt);
            }
        });
        p_qty.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                p_qtyKeyReleased(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel6.setText("Unit Price (₹) :");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel7.setText("Toatal Price (₹) :");

        tot_p.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        tot_p.setText("00.00");

        cs_name.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel11.setText("Contact No. :");

        tp_no.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tp_no))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cs_name, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(p_qty, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 164, Short.MAX_VALUE))
                    .addComponent(combo2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(u_price, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tot_p, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(u_price, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(cs_name, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)
                        .addComponent(combo2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(p_qty, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tot_p)
                        .addComponent(jLabel7))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11)
                        .addComponent(tp_no, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(30, 30, 30))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel6.setBackground(new java.awt.Color(120, 136, 171));
        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel7.setBackground(new java.awt.Color(120, 136, 171));
        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel8.setText("Total Amount (₹) :");

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("Balance Due (₹) :");

        bill_tot.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        bill_tot.setForeground(new java.awt.Color(255, 255, 255));
        bill_tot.setText("00.00");
        bill_tot.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        balance.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        balance.setForeground(new java.awt.Color(255, 255, 255));
        balance.setText("00.00");
        balance.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tot_qty.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        tot_qty.setForeground(new java.awt.Color(255, 255, 255));
        tot_qty.setText("00.00");
        tot_qty.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel10.setText("Total Qty : ");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(balance, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bill_tot, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                            .addComponent(tot_qty, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(61, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tot_qty, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(11, 11, 11)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bill_tot, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(balance, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel8.setBackground(new java.awt.Color(120, 136, 171));
        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        paid_amt.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        paid_amt.setText("00.00");
        paid_amt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                paid_amtKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                paid_amtKeyReleased(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel12.setText("Paid Amount (₹) :");

        jButton4.setBackground(new java.awt.Color(55, 230, 69));
        jButton4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/pro/img/icons8-pay-40.png"))); // NOI18N
        jButton4.setText("Pay & Print");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paid_amt, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paid_amt, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26))
        );

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("SALE SECTION");

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sale/pro/img/icons8-sale-35.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1054, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13)
                        .addGap(429, 429, 429)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void combo2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combo2ActionPerformed

        String name = combo2.getSelectedItem().toString();
       try{
           Statement s = db.mycon().createStatement();
           ResultSet rs = s.executeQuery("SELECT barcode,price FROM product WHERE p_name = '"+name+"'");
           if(rs.next())
           {
               u_price.setText(rs.getString("price"));
                bar_code = (rs.getString("barcode"));
              
           }
             s.close();
              rs.close();
           //product calculation
           
           pro_total_calc();
           
       } catch (SQLException ex) {
           System.out.println(ex);
        }
    }//GEN-LAST:event_combo2ActionPerformed

    private void p_qtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p_qtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p_qtyActionPerformed

 @SuppressWarnings({"unchecked", "unchecked"})
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // data send to dadtad base;
        //cartid`, `INID`, `product_name`, `bar_code`, `qty`, `unit_price`, `total_price`
        //cart
        String name = cs_name.getText();
        String tp = tp_no.getText();
         if(name.isEmpty() || tp.isEmpty())
         {
              JOptionPane.showMessageDialog(null, "Please Fill The Missing Details");
         } else{
              try {
          
          
         cs_feed(); 
          print_btn_fn();
          
          } catch (SQLException ex) {
          Logger.getLogger(Sale.class.getName()).log(Level.SEVERE, null, ex);
       }
      
         }
            
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        DefaultTableModel dt =(DefaultTableModel) jTable1.getModel();
       dt.setRowCount(0);
        tot_cart_calc();
        int due = 0;
        balance.setText(String.valueOf(due));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       try{
                    DefaultTableModel dt =(DefaultTableModel) jTable1.getModel();
                    int rv = jTable1.getSelectedRow();
                   
                    dt.removeRow(rv);
                    tot_cart_calc();
                    due_balance();
       }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

 @SuppressWarnings({"unchecked", "unchecked"})
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        //barcode
        setBar_code();
        //stock check
        try {
            String p = combo2.getSelectedItem().toString();
            
            
            
            
            Statement s = db.mycon().createStatement();
            ResultSet rs = s.executeQuery("SELECT Qty FROM product WHERE p_name = '"+p+"' ");
            if(rs.next())
            {
                setStock_qty(Double.valueOf(rs.getString("Qty")));
                
            }
              s.close();
              rs.close();
            
            //Stock null check
            if(getStock_qty() == 0)
            {
                JOptionPane.showMessageDialog(null, p+" Zero Stock");
            }
            else{
                if(p_qty.getText().isEmpty())
                {
                    JOptionPane.showMessageDialog(this, "Please Fill The Quantity!");
                } else{
                    try{ //add product
                        DefaultTableModel dt =(DefaultTableModel) jTable1.getModel();
                        Vector v = new Vector();
                        
                        v.add(in_id.getText());
                        v.add(combo2.getSelectedItem().toString());
                        v.add(bar_code);
                        v.add(p_qty.getText());
                        v.add(u_price.getText());
                        v.add(tot_p.getText());
                        
                        dt.addRow(v);
                        
                        tot_cart_calc();
                        
                        due_balance();
                    }
                    catch(Exception e)
                    {
                        System.out.println(e);
                    }
                }
                
                
                
            }
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(Sale.class.getName()).log(Level.SEVERE, null, ex);
        }
      
  
    }//GEN-LAST:event_jButton1ActionPerformed

    private void p_qtyKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_p_qtyKeyReleased
         
           pro_total_calc();
    }//GEN-LAST:event_p_qtyKeyReleased

    private void paid_amtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paid_amtKeyReleased
    due_balance();
    }//GEN-LAST:event_paid_amtKeyReleased

    private void paid_amtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_paid_amtKeyPressed
        // TODO add your handling code here:
         due_balance();
    }//GEN-LAST:event_paid_amtKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel balance;
    private javax.swing.JLabel bill_tot;
    private javax.swing.JComboBox<String> combo2;
    private javax.swing.JTextField cs_name;
    private javax.swing.JLabel in_id;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField p_qty;
    private javax.swing.JTextField paid_amt;
    private javax.swing.JLabel tot_p;
    private javax.swing.JLabel tot_qty;
    private javax.swing.JTextField tp_no;
    private javax.swing.JLabel u_price;
    // End of variables declaration//GEN-END:variables

   
  

    public void setBar_code()  {
       
        String p = combo2.getSelectedItem().toString();
        
        
     try {
               Statement s = db.mycon().createStatement();
            ResultSet rs = s.executeQuery("SELECT barcode FROM product WHERE p_name = '"+p+"' ");
               if(rs.next())
               {
               bar_code  = ((rs.getString("barcode")));
                   
               }
                 s.close();
              rs.close();
     } catch (SQLException ex) {
         Logger.getLogger(Sale.class.getName()).log(Level.SEVERE, null, ex);
     }
            
        
        
        
    }

    /**
     * @return the stock_qty
     */
    public Double getStock_qty() {
        return stock_qty;
    }

    /**
     * @param stock_qty the stock_qty to set
     */
    public void setStock_qty(Double stock_qty) {
        this.stock_qty = stock_qty;
    }

}
