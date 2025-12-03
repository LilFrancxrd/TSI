import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.logging.Logger;

public class Clientes extends java.awt.Frame {

    private Connection conexion;
    private DefaultTableModel modeloDeudores;
    private static final Logger logger = Logger.getLogger(Clientes.class.getName());
    
    private String usuario;
    private boolean permisos;
    
    public Clientes(String usuario, boolean permisos) {
        this.usuario = usuario;
        this.permisos = permisos;
        initComponents();
        inicializarComponentesAdicionales();
        cargarDatosClientes();
        configurarSegunUsuario();
    }
    public Clientes(){
        this("usuario", true);
    }
    
    private void configurarSegunUsuario() {
        setTitle("Gestión de Clientes - Usuario: " + usuario);
        
        if (!permisos) {
            btnNuevo.setEnabled(false);
            btnPagarDeuda.setEnabled(false);
            btnHistorialDeudas.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                "Usted tiene permisos de solo lectura",
                "Permisos Limitados",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void inicializarComponentesAdicionales() {
        modeloDeudores = (DefaultTableModel) tablaDeudores.getModel();
        
        tablaDeudores.setDefaultEditor(Object.class, null);
        
        tablaDeudores.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaDeudores.setRowHeight(25);
        tablaDeudores.setSelectionBackground(new Color(200, 220, 255));
        
        tablaDeudores.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirHistorialDeudas();
                }
            }
        });
        
    }    
    
    
    private void cargarDatosClientes() {
        // Limpiar tabla
        if (modeloDeudores != null) {
            modeloDeudores.setRowCount(0);
        }
        
        
        String sql = "SELECT d.rutDeudor, d.nomDeudor, d.telefono, " +
                     "MIN(deu.fechaDeuda) as primeraDeuda, " +
                     "SUM(CASE WHEN deu.estaPagado = 0 THEN (b.totalFiado - deu.montoPagado) ELSE 0 END) as deudaTotal " +
                     "FROM deudores d " +
                     "LEFT JOIN deudas deu ON d.rutDeudor = deu.rutDeudor " +
                     "LEFT JOIN boletas b ON deu.idBoleta = b.idBoleta " +
                     "GROUP BY d.rutDeudor, d.nomDeudor, d.telefono " +
                     "ORDER BY d.nomDeudor";
        
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int rut = rs.getInt("rutDeudor");
                String nombre = rs.getString("nomDeudor");
                int telefono = rs.getInt("telefono");
                Date fecha = rs.getDate("primeraDeuda");
                int deuda = rs.getInt("deudaTotal");
                
                String fechaStr = (fecha != null) ? fecha.toString() : "-";
                String deudaStr = String.format("$%,d", deuda);
                
                modeloDeudores.addRow(new Object[]{rut, nombre, telefono, fechaStr, deudaStr});
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar datos de la base de datos.\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Se mostrarán datos de prueba.",
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            logger.severe("Error SQL: " + e.getMessage());
            
            cargarDatosPrueba();
        }
    }
    
    private void cargarDatosPrueba() {
        modeloDeudores.setRowCount(0);
        modeloDeudores.addRow(new Object[]{12345678, "Juan Pérez", "912345678", "2024-01-15", "$50,000"});
        modeloDeudores.addRow(new Object[]{87654321, "María González", "987654321", "2024-02-20", "$25,000"});
        modeloDeudores.addRow(new Object[]{11111111, "Carlos López", "911111111", "2024-03-10", "$0"});
        modeloDeudores.addRow(new Object[]{22222222, "Ana Martínez", "922222222", "2024-01-05", "$100,000"});
    }
    
    private Connection obtenerConexion() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/vistaalmar";
        String user = "root";
        String password = "";
        
        return DriverManager.getConnection(url, user, password);
    }
    
    private void abrirNuevoDeudor() {
        NuevoDeudor nuevoDeudor = new NuevoDeudor();
        nuevoDeudor.setVisible(true);

        nuevoDeudor.setLocationRelativeTo(null);

        nuevoDeudor.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                cargarDatosClientes();
            }
        });
    }
    
    private void abrirHistorialDeudas() {
        int filaSeleccionada = tablaDeudores.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un cliente de la tabla",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object rutValue = modeloDeudores.getValueAt(filaSeleccionada, 0);
            Object nombreValue = modeloDeudores.getValueAt(filaSeleccionada, 1);

            int rutDeudor;
            String nombreDeudor;

            if (rutValue instanceof Integer) {
                rutDeudor = (Integer) rutValue;
            } else if (rutValue instanceof String) {
                String rutStr = ((String) rutValue).trim();
                rutStr = rutStr.replace(".", "").replace("-", "");
                rutDeudor = Integer.parseInt(rutStr);
            } else {
                throw new NumberFormatException("Formato de RUT inválido");
            }

            nombreDeudor = (nombreValue != null) ? nombreValue.toString() : "Cliente sin nombre";

            HistorialDeuda historial = new HistorialDeuda(rutDeudor, nombreDeudor, this, usuario, permisos);
            historial.setVisible(true);

            historial.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    cargarDatosClientes(); 
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al abrir historial: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    
    private void abrirPagarDeuda() {
        int filaSeleccionada = tablaDeudores.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un cliente de la tabla",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object rutValue = modeloDeudores.getValueAt(filaSeleccionada, 0);
            Object nombreValue = modeloDeudores.getValueAt(filaSeleccionada, 1);

            int rutDeudor;
            String nombreDeudor;

         
            if (rutValue instanceof Integer) {
                rutDeudor = (Integer) rutValue;
            } else if (rutValue instanceof String) {
                String rutStr = ((String) rutValue).replace(".", "").replace("-", "").trim();
                rutDeudor = Integer.parseInt(rutStr);
            } else {
                throw new NumberFormatException("Formato de RUT inválido");
            }

            nombreDeudor = (nombreValue != null) ? nombreValue.toString() : "Cliente sin nombre";

            int deudaTotal = obtenerDeudaTotalBD(rutDeudor);

            if (deudaTotal <= 0) {
                JOptionPane.showMessageDialog(this,
                    nombreDeudor + " no tiene deudas pendientes",
                    "Sin Deudas",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ABRIR PAGAR DEUDA DIALOG
            PagarDeudaDialog dialog = new PagarDeudaDialog(
                this,
                rutDeudor,
                nombreDeudor,
                deudaTotal,
                usuario,
                permisos
            );

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    cargarDatosClientes();
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private int obtenerDeudaTotalBD(int rutDeudor) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = obtenerConexion();
            String query = "SELECT SUM(b.totalFiado - d.montoPagado) as deudaTotal " +
                          "FROM deudas d " +
                          "JOIN boletas b ON d.idBoleta = b.idBoleta " +
                          "WHERE d.rutDeudor = ? " +
                          "AND (b.totalFiado - d.montoPagado) > 0 " +
                          "AND d.estaPagado = 0";

            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, rutDeudor);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("deudaTotal");
            }

            return 0;

        } catch (SQLException e) {
            System.err.println("Error al obtener deuda: " + e.getMessage());
            return 0;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void volverMenuPrincipal() {
        this.dispose();
        
        new MenuPrincipal(usuario, permisos).setVisible(true);
    }
    
    private void salirAplicacion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea salir de la aplicación?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    

    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnNuevo = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        btnHistorialDeudas = new javax.swing.JButton();
        btnPrincipal = new javax.swing.JButton();
        btnPagarDeuda = new javax.swing.JButton();
        scrollPaneDeudores = new javax.swing.JScrollPane();
        tablaDeudores = new javax.swing.JTable();
        lblClientes = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        btnNuevo.setText("Nuevo Deudor");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnSalir.setBackground(new java.awt.Color(255, 51, 51));
        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        btnHistorialDeudas.setText("Historial Deudas");
        btnHistorialDeudas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistorialDeudasActionPerformed(evt);
            }
        });

        btnPrincipal.setBackground(new java.awt.Color(51, 153, 255));
        btnPrincipal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnPrincipal.setForeground(new java.awt.Color(255, 255, 255));
        btnPrincipal.setText("Menu Principal");
        btnPrincipal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrincipalActionPerformed(evt);
            }
        });

        btnPagarDeuda.setText("Pagar Deuda");
        btnPagarDeuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPagarDeudaActionPerformed(evt);
            }
        });

        tablaDeudores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Rut", "Nombre", "Telefono", "Fecha", "Deuda"
            }
        ));
        scrollPaneDeudores.setViewportView(tablaDeudores);

        lblClientes.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblClientes.setText("Clientes");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnHistorialDeudas, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(btnNuevo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btnPagarDeuda, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(scrollPaneDeudores, javax.swing.GroupLayout.PREFERRED_SIZE, 516, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(269, 269, 269)
                        .addComponent(lblClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(120, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblClientes)
                .addGap(18, 18, 18)
                .addComponent(scrollPaneDeudores, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnHistorialDeudas, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnPagarDeuda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Exit the Application
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    private void btnHistorialDeudasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistorialDeudasActionPerformed
        abrirHistorialDeudas();
    }//GEN-LAST:event_btnHistorialDeudasActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
       salirAplicacion();
    }//GEN-LAST:event_btnSalirActionPerformed

    private void btnPagarDeudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPagarDeudaActionPerformed
       abrirPagarDeuda();
    }//GEN-LAST:event_btnPagarDeudaActionPerformed

    private void btnPrincipalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrincipalActionPerformed
       volverMenuPrincipal();
    }//GEN-LAST:event_btnPrincipalActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        abrirNuevoDeudor();
    }//GEN-LAST:event_btnNuevoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.severe("Error al configurar look and feel: " + e.getMessage());
        }   
        java.awt.EventQueue.invokeLater(() -> {
            new Clientes().setVisible(true);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHistorialDeudas;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnPagarDeuda;
    private javax.swing.JButton btnPrincipal;
    private javax.swing.JButton btnSalir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblClientes;
    private javax.swing.JScrollPane scrollPaneDeudores;
    private javax.swing.JTable tablaDeudores;
    // End of variables declaration//GEN-END:variables
}
