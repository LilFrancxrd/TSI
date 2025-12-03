import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class HistorialBoletas extends javax.swing.JFrame {
    private Connection conexion;
    private DefaultTableModel modeloBoletas;
    private String usuario;
    private boolean permisos;
    
    /**
     * Creates new form HistorialBoletas
     */
    public HistorialBoletas(String usuario, boolean permisos) {
        this.usuario = usuario;
        this.permisos = permisos;
        initComponents();
        inicializarComponentes();
        cargarBoletas();
        configurarSegunUsuario();
    }
    
    private void inicializarComponentes() {
        modeloBoletas = (DefaultTableModel) tablaBoletas.getModel();
        
        tablaBoletas.setDefaultEditor(Object.class, null);
        tablaBoletas.getTableHeader().setReorderingAllowed(false);
        tablaBoletas.setAutoCreateRowSorter(true);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloBoletas);
        tablaBoletas.setRowSorter(sorter);
        
        txtBuscarBoleta.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabla();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabla();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabla();
            }
        });
        
        tablaBoletas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    verDetallesBoleta();
                }
            }
        });
    }
    
    private void configurarSegunUsuario() {
        setTitle("Historial de Boletas - Usuario: " + usuario);
        
        if (!permisos) {
            btnEliminarBoleta.setEnabled(false);
            btnEliminarBoleta.setToolTipText("No tiene permisos para eliminar boletas");
            btnEliminarBoleta.setBackground(new java.awt.Color(180, 180, 180));
        }
    }    
  
    private void filtrarTabla() {
        String texto = txtBuscarBoleta.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) tablaBoletas.getRowSorter();
        
        if (texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
    
    private void cargarBoletas() {
        modeloBoletas.setRowCount(0);
        
        String sql = "SELECT b.idBoleta, b.rutUsuario, b.fechaTramite, " +
                     "b.medioPago, b.totalPagados, b.totalFiado, " +
                     "COUNT(dbp.idBoleta) as numProductos " +
                     "FROM boletas b " +
                     "LEFT JOIN detalleboletaproductos dbp ON b.idBoleta = dbp.idBoleta " +
                     "GROUP BY b.idBoleta, b.rutUsuario, b.fechaTramite, b.medioPago, b.totalPagados, b.totalFiado " +
                     "ORDER BY b.fechaTramite DESC";
        
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            while (rs.next()) {
                int idBoleta = rs.getInt("idBoleta");
                String usuarioBoleta = rs.getString("rutUsuario");
                Timestamp fecha = rs.getTimestamp("fechaTramite");
                String medioPago = rs.getString("medioPago");
                double totalPagado = rs.getDouble("totalPagados");
                double totalFiado = rs.getDouble("totalFiado");
                int numProductos = rs.getInt("numProductos");
                
                String fechaStr = (fecha != null) ? sdf.format(new Date(fecha.getTime())) : "-";
                String totalVenta = String.format("$%,.0f", totalPagado + totalFiado);
                String totalPagadoStr = String.format("$%,.0f", totalPagado);
                String totalFiadoStr = String.format("$%,.0f", totalFiado);
                
                modeloBoletas.addRow(new Object[]{
                    idBoleta,
                    usuarioBoleta,
                    fechaStr,
                    medioPago,
                    totalVenta,
                    totalPagadoStr,
                    totalFiadoStr,
                    numProductos
                });
            }
            
            lblTotalBoletas.setText(String.valueOf(modeloBoletas.getRowCount()));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar boletas: " + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void verDetallesBoleta() {
        int filaSeleccionada = tablaBoletas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una boleta",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tablaBoletas.convertRowIndexToModel(filaSeleccionada);
        int idBoleta = (int) modeloBoletas.getValueAt(modelRow, 0);
        
        DetalleBoleta detalle = new DetalleBoleta(idBoleta);
        detalle.setVisible(true);
        detalle.setLocationRelativeTo(this);
    }
    
    
    private void eliminarBoleta() {
        if (!permisos) {
            JOptionPane.showMessageDialog(this,
                "No tiene permisos para eliminar boletas",
                "Acceso Denegado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int filaSeleccionada = tablaBoletas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una boleta para eliminar",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = tablaBoletas.convertRowIndexToModel(filaSeleccionada);
        int idBoleta = (int) modeloBoletas.getValueAt(modelRow, 0);
        String fecha = (String) modeloBoletas.getValueAt(modelRow, 2);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar la boleta #" + idBoleta + "\n" +
            "con fecha: " + fecha + "?\n\n" +
            "Esta acción NO se puede deshacer.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            eliminarBoletaBD(idBoleta);
        }
    }    
    
    private void eliminarBoletaBD(int idBoleta) {
        Connection conn = null;
        try {
            conn = obtenerConexion();
            conn.setAutoCommit(false);
            
            String sqlSelect = "SELECT codProducto, cantidad FROM detalleboletaproductos WHERE idBoleta = ?";
            PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect);
            pstmtSelect.setInt(1, idBoleta);
            ResultSet rs = pstmtSelect.executeQuery();
            
            String sqlUpdate = "UPDATE productos SET stock = stock + ? WHERE codProducto = ?";
            PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate);
            
            while (rs.next()) {
                int codProducto = rs.getInt("codProducto");
                int cantidad = rs.getInt("cantidad");
                
                pstmtUpdate.setInt(1, cantidad);
                pstmtUpdate.setInt(2, codProducto);
                pstmtUpdate.executeUpdate();
            }
            rs.close();
            pstmtSelect.close();
            pstmtUpdate.close();
            
            String sqlDeleteDetalles = "DELETE FROM detalleboletaproductos WHERE idBoleta = ?";
            PreparedStatement pstmtDetalles = conn.prepareStatement(sqlDeleteDetalles);
            pstmtDetalles.setInt(1, idBoleta);
            pstmtDetalles.executeUpdate();
            pstmtDetalles.close();
            
            String sqlDeleteDeudas = "DELETE FROM deudas WHERE idBoleta = ?";
            PreparedStatement pstmtDeudas = conn.prepareStatement(sqlDeleteDeudas);
            pstmtDeudas.setInt(1, idBoleta);
            pstmtDeudas.executeUpdate();
            pstmtDeudas.close();
            
            String sqlDeleteBoleta = "DELETE FROM boletas WHERE idBoleta = ?";
            PreparedStatement pstmtBoleta = conn.prepareStatement(sqlDeleteBoleta);
            pstmtBoleta.setInt(1, idBoleta);
            pstmtBoleta.executeUpdate();
            pstmtBoleta.close();
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this,
                "Boleta #" + idBoleta + " eliminada exitosamente.\n" +
                "El stock de productos ha sido restaurado.",
                "Eliminación Exitosa",
                JOptionPane.INFORMATION_MESSAGE);
            
            cargarBoletas();
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            JOptionPane.showMessageDialog(this,
                "Error al eliminar boleta: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void refrescar() {
        cargarBoletas();
        txtBuscarBoleta.setText("");
    }
    
    private void volverClientes() {
        this.dispose();
        new Venta(usuario,permisos).setVisible(true);
    }    
    
    private Connection obtenerConexion() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/vistaalmar";
        String user = "root";
        String password = "";
        
        return DriverManager.getConnection(url, user, password);
    }    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscarBoleta = new javax.swing.JTextField();
        btnRecargar = new javax.swing.JButton();
        btnDetalleBoleta = new javax.swing.JButton();
        btnEliminarBoleta = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        panelboleta = new javax.swing.JScrollPane();
        tablaBoletas = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        lbltituloTotal = new javax.swing.JLabel();
        lblTotalBoletas = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTitulo.setText("Historial de Boletas");

        lblBuscar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblBuscar.setText("Buscar:");

        btnRecargar.setBackground(new java.awt.Color(51, 255, 102));
        btnRecargar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRecargar.setForeground(new java.awt.Color(255, 255, 255));
        btnRecargar.setText("Recargar");
        btnRecargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecargarActionPerformed(evt);
            }
        });

        btnDetalleBoleta.setBackground(new java.awt.Color(0, 102, 255));
        btnDetalleBoleta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDetalleBoleta.setForeground(new java.awt.Color(255, 255, 255));
        btnDetalleBoleta.setText("Ver Detalles");
        btnDetalleBoleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetalleBoletaActionPerformed(evt);
            }
        });

        btnEliminarBoleta.setBackground(new java.awt.Color(255, 51, 51));
        btnEliminarBoleta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminarBoleta.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarBoleta.setText("Eliminar Boleta");
        btnEliminarBoleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarBoletaActionPerformed(evt);
            }
        });

        btnVolver.setBackground(new java.awt.Color(153, 153, 153));
        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVolver.setForeground(new java.awt.Color(255, 255, 255));
        btnVolver.setText("Volver a Clientes");
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });

        tablaBoletas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "N° Boleta", "Rut Usuario", "Fecha", "Medio de pago"
            }
        ));
        panelboleta.setViewportView(tablaBoletas);

        lbltituloTotal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lbltituloTotal.setText("Total de boletas:");

        lblTotalBoletas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalBoletas.setText("0example");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbltituloTotal)
                        .addGap(18, 18, 18)
                        .addComponent(lblTotalBoletas, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblBuscar)
                        .addGap(18, 18, 18)
                        .addComponent(txtBuscarBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRecargar)
                        .addGap(58, 58, 58)
                        .addComponent(btnDetalleBoleta)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminarBoleta)
                        .addGap(18, 18, 18)
                        .addComponent(btnVolver))
                    .addComponent(panelboleta)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(7, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitulo)
                    .addComponent(lbltituloTotal)
                    .addComponent(lblTotalBoletas))
                .addGap(5, 5, 5)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBuscar)
                    .addComponent(txtBuscarBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRecargar)
                    .addComponent(btnDetalleBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVolver, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminarBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(panelboleta, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Exit the Application
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    private void btnRecargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecargarActionPerformed
        refrescar();
    }//GEN-LAST:event_btnRecargarActionPerformed

    private void btnDetalleBoletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetalleBoletaActionPerformed
        verDetallesBoleta();
    }//GEN-LAST:event_btnDetalleBoletaActionPerformed

    private void btnEliminarBoletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarBoletaActionPerformed
        eliminarBoleta();
    }//GEN-LAST:event_btnEliminarBoletaActionPerformed

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        volverClientes();
    }//GEN-LAST:event_btnVolverActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HistorialBoletas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HistorialBoletas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HistorialBoletas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HistorialBoletas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HistorialBoletas("Usuario", true).setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDetalleBoleta;
    private javax.swing.JButton btnEliminarBoleta;
    private javax.swing.JButton btnRecargar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalBoletas;
    private javax.swing.JLabel lbltituloTotal;
    private javax.swing.JScrollPane panelboleta;
    private javax.swing.JTable tablaBoletas;
    private javax.swing.JTextField txtBuscarBoleta;
    // End of variables declaration//GEN-END:variables
}
