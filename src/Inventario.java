import javax.swing.JOptionPane;
import seguridad.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import javax.swing.JFrame;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author oct88
 */
public class Inventario extends javax.swing.JFrame {
    Connection conex=null;
    Statement stm=null;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inventario.class.getName());
    private String usuario = "hello";
    private boolean estado = false;
    private boolean busqueda = false;
    /**
     * Creates new form Inventario
     */
    public Inventario() {
        initComponents();
    }
    
    public Inventario(String nuevo, boolean estado) {
        this.usuario = nuevo;
        this.estado = estado;
        initComponents();
        conectar();
        llenarCombo();
        crearTabla();
        customClose();
        tblProductos.setAutoCreateRowSorter(true);
    }
    
    public void customClose(){
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                registrarRetiro();
                System.exit(0);
            }
        });
    }
    
    public void registrarRetiro(){
        int valor = Integer.parseInt(usuario);
        try{
            LocalDateTime ahora = LocalDateTime.now();
            Timestamp tiempo = Timestamp.valueOf(ahora);
            stm=conex.createStatement();
            stm.executeUpdate("INSERT INTO accessLog (rutUsuario, tipoAccion, fechaAccion) VALUES("+valor+",0,'"+ tiempo +"')");
            stm.close();
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return;
    }
    
    public void conectar(){
        String url="jdbc:mysql://localhost:3306/vistaalmar";
        String usuario="root";
        String pass="";
        try{
            conex=DriverManager.getConnection(url,usuario,pass);
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"error en conexion "+ex,"error",1);
        }       
    }
    
    public void llenarCombo(){
        try{
                stm=conex.createStatement();
                ResultSet fila = stm.executeQuery("SELECT nombreTipo FROM tipoProductos");
                while(fila.next()){
                    cmbTipo.addItem(fila.getString("nombreTipo"));
                }             
            }catch(SQLException ex){
                  JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);               
             }
    }
    
    public void crearTabla(){
        try{
            DefaultTableModel modeloBorrar = (DefaultTableModel) tblProductos.getModel();
            modeloBorrar.setRowCount(0);
            stm = conex.createStatement();
            int index = cmbTipo.getSelectedIndex();
            ResultSet lista;
            if(index!=0){
                String sentencia = cmbTipo.getSelectedItem().toString();
                lista = stm.executeQuery(
                "SELECT p.codProducto, p.nomProducto, t.nombreTipo, p.precioActual, p.stock " +
                "FROM productos p " +
                "INNER JOIN tipoProductos t ON p.codTipo = t.codTipo " +
                "WHERE t.nombreTipo = '" + sentencia + "'"
                 );   
            }
            else{
                lista = stm.executeQuery(
                "SELECT p.codProducto, p.nomProducto, t.nombreTipo, p.precioActual, p.stock " +
                "FROM productos p " +
                "INNER JOIN tipoProductos t ON p.codTipo = t.codTipo"
            );
            }
            DefaultTableModel modelo = (DefaultTableModel) tblProductos.getModel();
            modelo.setRowCount(0);
            while (lista.next()) {
                Object data[] = {
                    lista.getInt("codProducto"),
                    lista.getString("nomProducto"),
                    lista.getString("nombreTipo"),
                    lista.getInt("precioActual"),
                    lista.getInt("Stock")
                };
                modelo.addRow(data);
            }
            
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"error en conexion "+ex,"error",1);
        }
    }
    
    public void crearFiltroTabla(){
        try{
            DefaultTableModel modeloBorrar = (DefaultTableModel) tblProductos.getModel();
            modeloBorrar.setRowCount(0);
            stm = conex.createStatement();
            int index = cmbTipo.getSelectedIndex();
            String nombre = txtBuscar.getText().trim();
            ResultSet lista;
            if(index!=0){
                String sentencia = cmbTipo.getSelectedItem().toString();
                lista = stm.executeQuery(
                "SELECT p.codProducto, p.nomProducto, t.nombreTipo, p.precioActual, p.stock " +
                "FROM productos p " +
                "INNER JOIN tipoProductos t ON p.codTipo = t.codTipo " +
                "WHERE t.nombreTipo = '" + sentencia + "' AND p.nomProducto LIKE '%" + nombre + "%'"
                 );   
            }
            else{
                lista = stm.executeQuery(
                "SELECT p.codProducto, p.nomProducto, t.nombreTipo, p.precioActual, p.stock " +
                "FROM productos p " +
                "INNER JOIN tipoProductos t ON p.codTipo = t.codTipo WHERE p.nomProducto LIKE '%" + nombre + "%'"
            );
            }
            DefaultTableModel modelo = (DefaultTableModel) tblProductos.getModel();
            modelo.setRowCount(0);
            while (lista.next()) {
                Object data[] = {
                    lista.getInt("codProducto"),
                    lista.getString("nomProducto"),
                    lista.getString("nombreTipo"),
                    lista.getInt("precioActual"),
                    lista.getInt("Stock")
                };
                modelo.addRow(data);
            }
            
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"error en conexion "+ex,"error",1);
        }
    }
    
    public int confirmarProducto(int codigo){
        int contador=0;
        try{
                stm=conex.createStatement();
                ResultSet fila = stm.executeQuery("SELECT COUNT(*) FROM detalleBoletaProductos WHERE codProducto =" + codigo);
                while(fila.next()){
                    contador = contador + fila.getInt(1);
                }
                stm.close();
                stm=conex.createStatement();
                ResultSet fila2 = stm.executeQuery("SELECT COUNT(*) FROM detalleFacturaProductos WHERE codProducto =" + codigo);
                while(fila2.next()){
                    contador = contador + fila2.getInt(1);
                }
                stm.close();
            }catch(SQLException ex){
                  JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);               
             }
        return contador;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btgFiltro = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        btnPrincipal = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        btnRegresar = new javax.swing.JButton();
        btnCompra = new javax.swing.JButton();
        btnVenta = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        btnCrear = new javax.swing.JButton();
        btnBorrar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        txtBuscar = new javax.swing.JTextField();
        cmbTipo = new javax.swing.JComboBox<>();
        btnReiniciar = new javax.swing.JButton();
        btnDetalles = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnPrincipal.setBackground(new java.awt.Color(51, 153, 255));
        btnPrincipal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnPrincipal.setForeground(new java.awt.Color(255, 255, 255));
        btnPrincipal.setText("Menu Principal");
        btnPrincipal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrincipalActionPerformed(evt);
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

        btnRegresar.setBackground(new java.awt.Color(255, 51, 51));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("Cerrar Sesión");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        btnCompra.setBackground(new java.awt.Color(153, 153, 153));
        btnCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCompra.setForeground(new java.awt.Color(255, 255, 255));
        btnCompra.setText("Compra Productos ");
        btnCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompraActionPerformed(evt);
            }
        });

        btnVenta.setBackground(new java.awt.Color(153, 153, 153));
        btnVenta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVenta.setForeground(new java.awt.Color(255, 255, 255));
        btnVenta.setText("Venta Productos");
        btnVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentaActionPerformed(evt);
            }
        });

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Codigo", "Nombre", "Tipo", "Precio", "Stock"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProductos);

        btnCrear.setBackground(new java.awt.Color(0, 204, 0));
        btnCrear.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCrear.setForeground(new java.awt.Color(255, 255, 255));
        btnCrear.setText("Crear Producto");
        btnCrear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearActionPerformed(evt);
            }
        });

        btnBorrar.setBackground(new java.awt.Color(255, 51, 51));
        btnBorrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBorrar.setForeground(new java.awt.Color(255, 255, 255));
        btnBorrar.setText("Borrar Producto");
        btnBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBorrarActionPerformed(evt);
            }
        });

        btnModificar.setBackground(new java.awt.Color(153, 153, 153));
        btnModificar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnModificar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificar.setText("Modificar Producto");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        cmbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos" }));
        cmbTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoActionPerformed(evt);
            }
        });

        btnReiniciar.setBackground(new java.awt.Color(153, 153, 153));
        btnReiniciar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnReiniciar.setForeground(new java.awt.Color(255, 255, 255));
        btnReiniciar.setText("<html>Reiniciar<br> Tabla</html>");
        btnReiniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReiniciarActionPerformed(evt);
            }
        });

        btnDetalles.setBackground(new java.awt.Color(153, 153, 153));
        btnDetalles.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDetalles.setForeground(new java.awt.Color(255, 255, 255));
        btnDetalles.setText("Detalles Producto");
        btnDetalles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetallesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(52, 52, 52)
                        .addComponent(btnCompra)
                        .addGap(73, 73, 73)
                        .addComponent(btnVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 551, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(208, 208, 208)
                                .addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnDetalles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(19, 19, 19))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnReiniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(29, 29, 29))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(btnModificar, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(btnRegresar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(btnCrear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(btnBorrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGap(19, 19, 19))))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72)
                        .addComponent(btnReiniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                        .addComponent(btnDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCrear, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscar))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        registrarRetiro();
        this.dispose();
        new MenuAcceso().setVisible(true);
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void btnPrincipalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrincipalActionPerformed
        this.dispose();
        new MenuPrincipal(usuario,estado).setVisible(true);
    }//GEN-LAST:event_btnPrincipalActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        registrarRetiro();
        System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed

    private void btnCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompraActionPerformed
        this.dispose();
        new Proveedores(usuario,estado).setVisible(true);
    }//GEN-LAST:event_btnCompraActionPerformed

    private void btnVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentaActionPerformed
        this.dispose();
        new VentaFianza(usuario,estado).setVisible(true);
    }//GEN-LAST:event_btnVentaActionPerformed

    private void btnCrearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearActionPerformed
        this.dispose();
        new CrearProducto(usuario,estado).setVisible(true);
    }//GEN-LAST:event_btnCrearActionPerformed

    private void cmbTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoActionPerformed
        if(busqueda==true){
            crearFiltroTabla();
            return;
        }
        crearTabla();
    }//GEN-LAST:event_cmbTipoActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        int selectedRow = tblProductos.getSelectedRow();
        if (selectedRow != -1) {
            int codigo = (int) tblProductos.getValueAt(selectedRow, 0);
            this.dispose();
            new ModificarProducto(usuario,estado,codigo).setVisible(true);
        } 
        else {
            JOptionPane.showMessageDialog(null, "Escoja un Producto de la Tabla Primero");
        }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnReiniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReiniciarActionPerformed
   
        cmbTipo.setSelectedIndex(0); 
        txtBuscar.setText("");
        this.busqueda = false;
        crearTabla();
    }//GEN-LAST:event_btnReiniciarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        this.busqueda = true;
        crearFiltroTabla();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnBorrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBorrarActionPerformed

       int fila = tblProductos.getSelectedRow();
       if(fila==-1){
           JOptionPane.showMessageDialog(null,"Seleccione un Producto");
           return;
       }
       int codigo = (int) tblProductos.getValueAt(fila, 0);
       if(confirmarProducto(codigo) != 0){
           JOptionPane.showMessageDialog(null,"Producto Existe en Otras Tablas", "Advertencia", JOptionPane.WARNING_MESSAGE);
           return;
       }
       Object[] opciones = {"Sí", "No"};
       int respuesta =JOptionPane.showOptionDialog(
               null,
               "¿Esta Seguro Que Desea Borrar el Producto?",
               "Confirmar Eliminacion",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE,
               null,
               opciones,
               opciones[0]
       );
       if(respuesta==1){
           return;
       }
       try{
            stm = conex.createStatement();
            stm.executeUpdate("DELETE FROM productos WHERE codProducto = " + codigo);
            JOptionPane.showMessageDialog(null,"Producto Borrado");
            if(cmbTipo.getSelectedIndex()==0){
                crearTabla();
            }
            else{
                cmbTipo.setSelectedIndex(0);
            }
            
            
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"error en conexion "+ex,"error",1);
        }
    }//GEN-LAST:event_btnBorrarActionPerformed

    private void tblProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductosMouseClicked
       
    }//GEN-LAST:event_tblProductosMouseClicked

    private void btnDetallesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetallesActionPerformed
        int opcion = tblProductos.getSelectedRow();
        if(opcion==-1){
           JOptionPane.showMessageDialog(null,"Seleccione un Producto");
           return;
        }
        int fila = tblProductos.getSelectedRow();
            int codProducto = (int) tblProductos.getValueAt(fila, 0);
            String enlace = "";
            String mensaje = "";
            try{
                stm=conex.createStatement();
                ResultSet rs = stm.executeQuery("SELECT * FROM productos WHERE codProducto=" + codProducto);
                while(rs.next()){
                    mensaje = rs.getString("comentarios");
                    enlace = "images\\" + rs.getString("imgLink") +".jpg"; 
                }
            }catch(SQLException ex){
                  JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                  return;
            }
            new Detalles(mensaje,enlace).setVisible(true);        
    }//GEN-LAST:event_btnDetallesActionPerformed

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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Inventario().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btgFiltro;
    private javax.swing.JButton btnBorrar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCompra;
    private javax.swing.JButton btnCrear;
    private javax.swing.JButton btnDetalles;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnPrincipal;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JButton btnReiniciar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnVenta;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
