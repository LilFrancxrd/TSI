
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author oct88
 */
public class ModificarProveedor extends javax.swing.JFrame {
    private String usuario = "hello";
    private boolean permisos = false;
    private int proveedorModificar = 0;
    private String proveedorNombre;
    Connection conex=null;
    Statement stm=null;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CrearProveedor.class.getName());

    /**
     * Creates new form ModificarProveedor
     */
    public ModificarProveedor() {
        initComponents();
    }
    
    public ModificarProveedor(String nombre, boolean permisos, int proveedor) {
        this.usuario = nombre;
        this.permisos = permisos;
        this.proveedorModificar = proveedor;
        initComponents();
        conectar();
        completarDatos();
        customClose();
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
    
    public void completarDatos(){
        try{
                int rut = proveedorModificar;
                int phone;
                stm=conex.createStatement();
                ResultSet fila = stm.executeQuery("SELECT * FROM proveedores WHERE rutEmpresa = "+rut); 
                while(fila.next()){
                    txtRut.setText(String.valueOf(rut));
                    txtNombre.setText(fila.getString("nomEmpresa"));
                    txtTipo.setText(fila.getString("tipoEmpresa"));
                    txtDireccion.setText(fila.getString("direccion"));
                    txtCorreo.setText(fila.getString("correo"));
                    phone = fila.getInt("telefono");
                    txtTelefono.setText(String.valueOf(phone));
                }
                proveedorNombre = txtNombre.getText();
                stm.close();                       
            }catch(SQLException ex){
                  JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);               
             }
    }
    
    public void update(int rut, String nombre, String tipo, String direccion, String correo, int telefono){
        try{
            stm=conex.createStatement();
            stm.executeUpdate("UPDATE proveedores SET nomEmpresa = '" + nombre + "', tipoEmpresa = '" + tipo + "', dirEmpresa = '" + direccion + "', correo = '" + correo + "', telefono = " + telefono + " WHERE rutEmpresa = " + rut);
            JOptionPane.showMessageDialog(null, "Proovedor Modificado");
            completarDatos();
            stm.close();
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);               
        }
    }
    
    public void hardUpdate(int rut, String nombre, String tipo, String direccion, String correo, int telefono){
        try{
            Object[] opciones = {"Sí", "No"};
            int respuesta =JOptionPane.showOptionDialog(
                null,
                "¿Esta Seguro Que Desea Modificar el Codigo?",
                "Confirmar Modificacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[0]
                );
            if(respuesta==1){
                return;
            }
            conex.setAutoCommit(false);
            stm=conex.createStatement();
            stm.executeUpdate("INSERT INTO proveedores VALUES("+rut+",'" + nombre + "','" + tipo + "','" + direccion + "','" + correo + "'," + telefono + ")");
            stm.close();
            stm=conex.createStatement();
            stm.executeUpdate("UPDATE facturaProveedores SET rutEmpresa = " + rut + " WHERE rutEmpresa = " + proveedorModificar);
            stm.close();
            stm=conex.createStatement();
            stm.executeUpdate("DELETE FROM proveedores WHERE rutEmpresa = " + proveedorModificar);
            JOptionPane.showMessageDialog(null, "Proveedor Modificado");
            conex.commit();
            proveedorModificar = rut;
            completarDatos();
        }catch(SQLException ex){
            try {
                conex.rollback(); 
                JOptionPane.showMessageDialog(null, "Error en Base de Datos, Operación Revertida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(null, "Error crítico: rollback falló: " + rollbackEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }               
        }finally{
            try{
                conex.setAutoCommit(true);
            }catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(null, "Error crítico Revisar Base De Datos" + rollbackEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }        
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
        btnRegresar = new javax.swing.JButton();
        btnPrincipal = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        lblRut = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        lblTipo = new javax.swing.JLabel();
        lblCorreo = new javax.swing.JLabel();
        lblTelefono = new javax.swing.JLabel();
        txtRut = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtTipo = new javax.swing.JTextField();
        btnProveedor = new javax.swing.JButton();
        lblDireccion = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnRegresar.setBackground(new java.awt.Color(255, 51, 51));
        btnRegresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegresar.setForeground(new java.awt.Color(255, 255, 255));
        btnRegresar.setText("Cerrar Sesión");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
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

        btnSalir.setBackground(new java.awt.Color(255, 51, 51));
        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        btnModificar.setBackground(new java.awt.Color(153, 153, 153));
        btnModificar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnModificar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificar.setText("Modificar");
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        lblRut.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRut.setText("Rut Empresa");

        lblNombre.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblNombre.setText("Nombre Empresa");

        lblTipo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTipo.setText("Tipo Empresa");

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblCorreo.setText("Correo");

        lblTelefono.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTelefono.setText("Telefono");

        btnProveedor.setBackground(new java.awt.Color(153, 153, 153));
        btnProveedor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnProveedor.setForeground(new java.awt.Color(255, 255, 255));
        btnProveedor.setText("Proveedores");
        btnProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedorActionPerformed(evt);
            }
        });

        lblDireccion.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDireccion.setText("Direccion Empresa");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblRut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTipo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(lblDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(txtRut, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(23, 23, 23))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(47, 47, 47)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 347, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRut)
                    .addComponent(txtRut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombre)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTipo)
                    .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDireccion)
                            .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCorreo)
                            .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTelefono)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(62, 62, 62)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        new MenuPrincipal(usuario,permisos).setVisible(true);
    }//GEN-LAST:event_btnPrincipalActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        registrarRetiro();
        System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        String nombre = txtNombre.getText().trim();
        String rut = txtRut.getText();
        String tipo = txtTipo.getText();
        String correo = txtCorreo.getText();
        String telefono = txtTelefono.getText();
        String direccion = txtDireccion.getText();
        if(rut.matches("\\d+") && telefono.matches("\\d+")){
            int valorRut = Integer.parseInt(rut);
            int valorTelefono = Integer.parseInt(telefono);
            if(nombre.length()==0 || tipo.length()==0 || correo.length()==0){
                JOptionPane.showMessageDialog(null, "Complete Datos", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try{
                int conteo = 0;
                ResultSet fila;
                if(valorRut == proveedorModificar && nombre.equals(proveedorNombre)){
                    //Se Actualiza el Mismo Proveedor
                    update(valorRut,nombre,tipo,direccion,correo,valorTelefono);
                    return;
                }
                if(valorRut == proveedorModificar && !nombre.equals(proveedorNombre)){
                    fila = stm.executeQuery("SELECT COUNT(*) FROM proveedores WHERE nomEmpresa = '" + nombre + "'");
                    while(fila.next()){
                        conteo = conteo + fila.getInt(1);
                    }
                    if(conteo>0){
                        //Nombre Existe En Otro Registro
                        JOptionPane.showMessageDialog(null, "Nombre Existe en Otro Proveedor", "Error de Datos", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    //Modificar Nombre y Otros Datos del Registro
                    fila.close();
                    conteo = 0;
                    update(valorRut,nombre,tipo,direccion,correo,valorTelefono);
                    return;
                }
                if(valorRut != proveedorModificar && nombre.equals(proveedorNombre)){
                    //Nombre No Esta Repetido Pero el Rut tal Vez
                    fila = stm.executeQuery("SELECT COUNT(*) FROM proveedores WHERE rutEmpresa = " + valorRut);
                    while(fila.next()){
                        conteo = conteo + fila.getInt(1);
                    }
                    if(conteo>0){
                        //Rut Existe En Otro Registro
                        JOptionPane.showMessageDialog(null, "Rut Existe en Otro Proveedor", "Error de Datos", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    //Actualizar Rut del Registro
                    fila.close();
                    conteo = 0;
                    hardUpdate(valorRut,nombre,tipo,direccion,correo,valorTelefono);
                    return;
                }
                if(valorRut != proveedorModificar && !nombre.equals(proveedorNombre)){
                    //Nombre o Rut pueden Existir En OTROS REGISTROS
                    fila = stm.executeQuery("SELECT COUNT(*) FROM proveedores WHERE nomEmpresa = '" + nombre + "' OR rutEmpresa = " + valorRut);
                    while(fila.next()){
                        conteo = conteo + fila.getInt(1);
                    }
                    if(conteo>0){
                        //Datos Existen EN OTROS REGISTROS
                        JOptionPane.showMessageDialog(null, "Nombre y/o Rut Existen En Otro Proveedor", "Error de Datos", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    fila.close();
                    conteo = 0;
                    hardUpdate(valorRut,nombre,tipo,direccion,correo,valorTelefono);
                    return;
                }
                
            }catch(SQLException ex){
                  JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);               
             }
        }
        else{
            JOptionPane.showMessageDialog(null, "Ingrese al Rut/Telefono SOLO NUMEROS (Rut Digitos Antes del Guion)", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedorActionPerformed
        this.dispose();
        new Proveedores(usuario,permisos).setVisible(true);
    }//GEN-LAST:event_btnProveedorActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new CrearProveedor().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnPrincipal;
    private javax.swing.JButton btnProveedor;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblRut;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRut;
    private javax.swing.JTextField txtTelefono;
    private javax.swing.JTextField txtTipo;
    // End of variables declaration//GEN-END:variables
}
