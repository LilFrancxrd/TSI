
import java.awt.Color;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author oct88
 */
public class HistorialFacturas extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MenuPrincipal.class.getName());
    
    private String usuario = "hello";
    private boolean permisos = false;
    Connection conex=null;
    Statement stm=null;
    /**
     * Creates new form MainMenu
     */
    public HistorialFacturas() {
        initComponents();
    }
    
    public HistorialFacturas(String nombre, boolean estado) {
        this.usuario = nombre;
        this.permisos = estado;
        conectar();
        initComponents();
        customClose();
        tblFacturas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblDetalles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        llenarCombo();
        cmbProveedor.setSelectedIndex(0);
        llenarCombo2();
        cmbFecha.setSelectedIndex(cmbFecha.getItemCount()-1);
        llenarTabla();
        Object[] opciones = {"Sí", "No"};
        int respuesta =JOptionPane.showOptionDialog(
                null,
                "¿Desea Que el Sistema Le Advierta de Productos Vencidos No Revisados?",
                "Confirmar Factura",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        if(respuesta==1){
            rdoNo.setSelected(true);
        }
        else{
            rdoSi.setSelected(true);
        }
        DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
        modelo.setRowCount(0);
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
    
    public void llenarCombo(){
        try{
            stm=conex.createStatement();
            ResultSet fila = stm.executeQuery("SELECT nomEmpresa FROM proveedores ORDER BY nomEmpresa ASC");
            while(fila.next()){
                cmbProveedor.addItem(fila.getString("nomEmpresa"));
            }
            stm.close();
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void llenarTabla(){
        try{
            DefaultTableModel modelo = (DefaultTableModel) tblFacturas.getModel();
            modelo.setRowCount(0);
            stm=conex.createStatement();
            ResultSet fila;
            int indexProveedor = cmbProveedor.getSelectedIndex();
            int indexFecha = cmbFecha.getSelectedIndex();
            int ultimoElemento = cmbFecha.getItemCount()-1;
            int yearSelected, semesterSelected;
            String providerSelected = cmbProveedor.getSelectedItem().toString();
            if(indexFecha==ultimoElemento && indexProveedor==0){
                fila = stm.executeQuery("SELECT fp.idFactura, p.nomEmpresa, u.nomUsuario, fp.fechaTramite, fp.fechaVencimiento, fp.medioPago, fp.total FROM facturaProveedores fp JOIN usuarios u ON u.rutUsuario = fp.rutUsuario JOIN proveedores p ON p.rutEmpresa = fp.rutEmpresa ORDER BY fp.fechaTramite DESC");
            }else if(indexProveedor!=0 && indexFecha==ultimoElemento){
                fila = stm.executeQuery("SELECT fp.idFactura, p.nomEmpresa, u.nomUsuario, fp.fechaTramite, fp.fechaVencimiento, fp.medioPago, fp.total FROM facturaProveedores fp JOIN usuarios u ON u.rutUsuario = fp.rutUsuario JOIN proveedores p ON p.rutEmpresa = fp.rutEmpresa WHERE p.nomEmpresa = '" + providerSelected + "' ORDER BY fp.fechaTramite DESC");
            }else{
                String parts[] = cmbFecha.getSelectedItem().toString().split("-");
                yearSelected = Integer.parseInt(parts[0]);
                semesterSelected = Integer.parseInt(parts[1]);
                if(indexProveedor==0){
                    if(semesterSelected == 1){
                        fila = stm.executeQuery("SELECT fp.idFactura, p.nomEmpresa, u.nomUsuario, fp.fechaTramite, fp.fechaVencimiento, fp.medioPago, fp.total FROM facturaProveedores fp JOIN usuarios u ON u.rutUsuario = fp.rutUsuario JOIN proveedores p ON p.rutEmpresa = fp.rutEmpresa WHERE fechaTramite BETWEEN '" + yearSelected + "-01-01' AND '" + yearSelected + "-06-30' ORDER BY fp.fechaTramite DESC");
                    }
                    else{
                        fila = stm.executeQuery("SELECT fp.idFactura, p.nomEmpresa, u.nomUsuario, fp.fechaTramite, fp.fechaVencimiento, fp.medioPago, fp.total FROM facturaProveedores fp JOIN usuarios u ON u.rutUsuario = fp.rutUsuario JOIN proveedores p ON p.rutEmpresa = fp.rutEmpresa WHERE fechaTramite BETWEEN '" + yearSelected + "-07-01' AND '" + yearSelected + "-12-31' ORDER BY fp.fechaTramite DESC");
                    }
                }
                else{
                    if(semesterSelected == 1){
                        fila = stm.executeQuery("SELECT fp.idFactura, p.nomEmpresa, u.nomUsuario, fp.fechaTramite, fp.fechaVencimiento, fp.medioPago, fp.total FROM facturaProveedores fp JOIN usuarios u ON u.rutUsuario = fp.rutUsuario JOIN proveedores p ON p.rutEmpresa = fp.rutEmpresa WHERE fechaTramite BETWEEN '" + yearSelected + "-01-01' AND '" + yearSelected + "-06-30' AND p.nomEmpresa = '" + providerSelected + "' ORDER BY fp.fechaTramite DESC");
                    }
                    else{
                        fila = stm.executeQuery("SELECT fp.idFactura, p.nomEmpresa, u.nomUsuario, fp.fechaTramite, fp.fechaVencimiento, fp.medioPago, fp.total FROM facturaProveedores fp JOIN usuarios u ON u.rutUsuario = fp.rutUsuario JOIN proveedores p ON p.rutEmpresa = fp.rutEmpresa WHERE fechaTramite BETWEEN '" + yearSelected + "-07-01' AND '" + yearSelected + "-12-31'  AND p.nomEmpresa = '" + providerSelected + "' ORDER BY fp.fechaTramite DESC");
                    }
                }
            }
            while(fila.next()){
                Object data[] = {
                    fila.getInt("idFactura"),
                    fila.getString("nomEmpresa"),
                    fila.getString("nomUsuario"),
                    fila.getDate("fechaTramite"),
                    fila.getString("medioPago"),
                    fila.getInt("total"),
                    fila.getDate("fechaVencimiento")
                };
                modelo.addRow(data);
            }
            stm.close();
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void llenarTabla2(int codigo){
        try{
            DefaultTableModel modelo = (DefaultTableModel) tblDetalles.getModel();
            modelo.setRowCount(0);
            stm = conex.createStatement();
            ResultSet fila = stm.executeQuery("SELECT p.nomProducto, dfp.cantidadUnitaria, dfp.cantidad, dfp.precioCompra, dfp.total, dfp.fechaVencimiento, dfp.detalleRevisado FROM detalleFacturaProductos dfp JOIN productos p ON dfp.codProducto = p.codProducto WHERE dfp.idFactura = " + codigo);
            boolean warning = false;
            while(fila.next()){
                String estadoProducto;
                boolean listo = fila.getBoolean("detalleRevisado");
                LocalDate fechaV = fila.getDate("fechaVencimiento").toLocalDate();
                LocalDate hoy = LocalDate.now();
                if(listo){
                    estadoProducto = "Revisado";
                }
                else{
                    estadoProducto = "No Revisado";
                }
                if(fechaV.isBefore(hoy) || fechaV.isEqual(hoy) || !fechaV.isAfter(hoy.plusDays(3))){
                    if(!listo){
                        warning = true;
                    }
                }
                Object data[] = {
                    fila.getString("nomProducto"),
                    fila.getInt("cantidad"),
                    fila.getInt("cantidadUnitaria"),
                    fila.getInt("precioCompra"),
                    fila.getInt("total"),
                    fila.getDate("fechaVencimiento"),
                    estadoProducto
                };
                modelo.addRow(data);
            }
            stm.close();
            if(warning && rdoSi.isSelected()){
                JOptionPane.showMessageDialog(null,"Productos Vencidos Sin Revisar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void llenarCombo2(){
        try{
            stm=conex.createStatement();
            ResultSet fila = stm.executeQuery("SELECT MIN(fechaTramite) AS fechaMin, MAX(fechaTramite) AS fechaMax FROM facturaProveedores");
            LocalDate fechaMinima = null, fechaMaxima = null;
            while(fila.next()){
                fechaMinima = fila.getDate("fechaMin").toLocalDate();
                fechaMaxima = fila.getDate("fechaMax").toLocalDate();
            }
            int yearStart = fechaMinima.getYear();
            int yearEnd = fechaMaxima.getYear();
            int semestralStart = fechaMinima.getMonthValue();
            if(semestralStart <= 6){
                semestralStart = 1;
            }else{
                semestralStart = 2;
            }
            int semestralEnd = fechaMaxima.getMonthValue();
            if(semestralEnd <= 6){
                semestralEnd = 1;
            }else{
                semestralEnd = 2;
            }
            boolean status = true;
            String statement;
            while(status){
                statement = String.valueOf(yearEnd) + "-" + String.valueOf(semestralEnd);
                cmbFecha.addItem(statement);
                if(yearStart == yearEnd && semestralStart == semestralEnd){
                    status = false;
                }
                else{
                    if(semestralEnd == 2){
                        semestralEnd = 1;
                    }
                    else{
                        semestralEnd = 2;
                        yearEnd = yearEnd-1;
                    }
                }
            }
            cmbFecha.addItem("Todo");
            stm.close();
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
    
    public void actualizarRevisado(int codigo, String nombre, boolean todo){
        try{
            stm=conex.createStatement();
            if(todo){
                stm.executeUpdate("UPDATE detalleFacturaProductos SET detalleRevisado = true WHERE idFactura = " + codigo);
            }
            else{
                stm.executeUpdate("UPDATE detalleFacturaProductos dfp JOIN productos p ON dfp.codProducto = p.codProducto SET dfp.detalleRevisado = true WHERE dfp.idFactura = " + codigo + " AND p.nomProducto = '" + nombre + "'");
            }
            stm.close();
            JOptionPane.showMessageDialog(null,"Actualizado");
            llenarTabla2(codigo);
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btgRevisar = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        btnRegreso = new javax.swing.JButton();
        txtTitulo = new javax.swing.JLabel();
        btnSalir = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDetalles = new javax.swing.JTable();
        lblFactura = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFacturas = new javax.swing.JTable();
        btnDetalles = new javax.swing.JButton();
        btnRevisado = new javax.swing.JButton();
        btnRevisarTodo = new javax.swing.JButton();
        cmbProveedor = new javax.swing.JComboBox<>();
        lblProveedores = new javax.swing.JLabel();
        btnProveedores = new javax.swing.JButton();
        cmbFecha = new javax.swing.JComboBox<>();
        lblFecha = new javax.swing.JLabel();
        rdoSi = new javax.swing.JRadioButton();
        rdoNo = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnRegreso.setText("Cerrar Sesión");
        btnRegreso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresoActionPerformed(evt);
            }
        });

        txtTitulo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtTitulo.setText("Historial de Facturas");

        btnSalir.setBackground(new java.awt.Color(255, 51, 51));
        btnSalir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSalir.setForeground(new java.awt.Color(255, 255, 255));
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        tblDetalles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Producto", "Cantidad Paquete", "Cantidad Unitaria", "Precio Paquete", "Total", "Fecha Vencimiento", "Revisado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblDetalles);
        if (tblDetalles.getColumnModel().getColumnCount() > 0) {
            tblDetalles.getColumnModel().getColumn(0).setResizable(false);
            tblDetalles.getColumnModel().getColumn(0).setPreferredWidth(150);
            tblDetalles.getColumnModel().getColumn(1).setResizable(false);
            tblDetalles.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblDetalles.getColumnModel().getColumn(2).setResizable(false);
            tblDetalles.getColumnModel().getColumn(2).setPreferredWidth(100);
            tblDetalles.getColumnModel().getColumn(3).setResizable(false);
            tblDetalles.getColumnModel().getColumn(3).setPreferredWidth(50);
            tblDetalles.getColumnModel().getColumn(4).setResizable(false);
            tblDetalles.getColumnModel().getColumn(4).setPreferredWidth(50);
            tblDetalles.getColumnModel().getColumn(5).setResizable(false);
            tblDetalles.getColumnModel().getColumn(5).setPreferredWidth(100);
            tblDetalles.getColumnModel().getColumn(6).setResizable(false);
            tblDetalles.getColumnModel().getColumn(6).setPreferredWidth(80);
        }

        lblFactura.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblFactura.setText("Factura:");

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        tblFacturas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Id Factura", "Proveedor", "Usuario", "Fecha Registro", "Medio Pago", "Total", "Fecha Vencimiento Documento"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblFacturas);
        if (tblFacturas.getColumnModel().getColumnCount() > 0) {
            tblFacturas.getColumnModel().getColumn(0).setResizable(false);
            tblFacturas.getColumnModel().getColumn(0).setPreferredWidth(150);
            tblFacturas.getColumnModel().getColumn(1).setResizable(false);
            tblFacturas.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblFacturas.getColumnModel().getColumn(2).setResizable(false);
            tblFacturas.getColumnModel().getColumn(2).setPreferredWidth(100);
            tblFacturas.getColumnModel().getColumn(3).setResizable(false);
            tblFacturas.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblFacturas.getColumnModel().getColumn(4).setResizable(false);
            tblFacturas.getColumnModel().getColumn(4).setPreferredWidth(150);
            tblFacturas.getColumnModel().getColumn(5).setResizable(false);
            tblFacturas.getColumnModel().getColumn(5).setPreferredWidth(100);
            tblFacturas.getColumnModel().getColumn(6).setResizable(false);
            tblFacturas.getColumnModel().getColumn(6).setPreferredWidth(100);
        }

        btnDetalles.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDetalles.setText("<html>Ver<br>Detalles</html>");
        btnDetalles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetallesActionPerformed(evt);
            }
        });

        btnRevisado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRevisado.setText("<html>Marcar<br> Revisado</html>");
        btnRevisado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRevisadoActionPerformed(evt);
            }
        });

        btnRevisarTodo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRevisarTodo.setText("<html>Revisado<br> Todo</html>");
        btnRevisarTodo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRevisarTodoActionPerformed(evt);
            }
        });

        cmbProveedor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos" }));
        cmbProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbProveedorActionPerformed(evt);
            }
        });

        lblProveedores.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblProveedores.setText("Filtrar Por Proveedor");

        btnProveedores.setBackground(new java.awt.Color(51, 153, 255));
        btnProveedores.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnProveedores.setForeground(new java.awt.Color(255, 255, 255));
        btnProveedores.setText("<html>Regresar a <br>Proveedores</html>");
        btnProveedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedoresActionPerformed(evt);
            }
        });

        cmbFecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFechaActionPerformed(evt);
            }
        });

        lblFecha.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFecha.setText("Filtrar Por Fecha");

        rdoSi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoSi.setText("Advertir Productos No Revisados");

        rdoNo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        rdoNo.setText("No Advertir Productos No Revisados");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(217, 217, 217)
                        .addComponent(txtTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(202, 202, 202)
                        .addComponent(btnRegreso, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbFecha, 0, 195, Short.MAX_VALUE)
                                    .addComponent(cmbProveedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(118, 118, 118)
                                        .addComponent(rdoSi, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(btnDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnRevisado, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(58, 58, 58)
                                        .addComponent(btnRevisarTodo, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(264, 264, 264))
                                    .addComponent(jScrollPane2)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(160, 160, 160)
                                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(14, 14, 14)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(txtTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnRegreso, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(135, 135, 135)
                                .addComponent(btnDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRevisarTodo, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRevisado, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblProveedores)
                            .addComponent(cmbProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFecha))
                        .addGap(34, 34, 34)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdoSi)
                            .addComponent(rdoNo))
                        .addGap(64, 64, 64)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegresoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresoActionPerformed
        registrarRetiro();
        this.dispose();
        new MenuAcceso().setVisible(true); 
    }//GEN-LAST:event_btnRegresoActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        registrarRetiro();
        System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed

    private void cmbProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProveedorActionPerformed
        llenarTabla();
    }//GEN-LAST:event_cmbProveedorActionPerformed

    private void btnProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedoresActionPerformed
        this.dispose();
        new Proveedores(usuario,permisos).setVisible(true);
    }//GEN-LAST:event_btnProveedoresActionPerformed

    private void cmbFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFechaActionPerformed
        llenarTabla();
    }//GEN-LAST:event_cmbFechaActionPerformed

    private void btnDetallesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetallesActionPerformed
        int selectedRow = tblFacturas.getSelectedRow();
        if (selectedRow != -1) {
            int codigo = (int) tblFacturas.getValueAt(selectedRow, 0);
            lblCodigo.setText(String.valueOf(codigo));
            llenarTabla2(codigo);
        } 
        else {
            JOptionPane.showMessageDialog(null, "Escoja una Factura Para Revisar");
        }
    }//GEN-LAST:event_btnDetallesActionPerformed

    private void btnRevisadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevisadoActionPerformed
        int codigo = Integer.parseInt(lblCodigo.getText());
            int selectedRow2 = tblDetalles.getSelectedRow();
            if(selectedRow2 != -1){
                String estado = String.valueOf(tblDetalles.getValueAt(selectedRow2,6));
                String nombreProducto = String.valueOf(tblDetalles.getValueAt(selectedRow2,0));
                if(estado.equals("Revisado")){
                    JOptionPane.showMessageDialog(null, "Producto ya Revisado");
                    return;
                }
                else{
                    actualizarRevisado(codigo,nombreProducto,false);
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "Escoja un Producto para Revisar. Recuerde Presionar el Boton\n Ver Detalles Si No Lo Ha Hecho");
            }
    }//GEN-LAST:event_btnRevisadoActionPerformed

    private void btnRevisarTodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRevisarTodoActionPerformed
        int contador = tblDetalles.getRowCount();
        if(contador == 0){
            JOptionPane.showMessageDialog(null, "Tabla Vacia");
        }
        else{
            int codigo = Integer.parseInt(lblCodigo.getText());
            boolean warning = true;
            for(int i = 0; i<contador; i++){
              String estado = String.valueOf(tblDetalles.getValueAt(i,6));
              if(!estado.equals("Revisado")){
                    warning = false;
                }
            }
            if(warning == true){
                JOptionPane.showMessageDialog(null, "Tabla Ya Fue Revisada");
            }
            else{
                actualizarRevisado(codigo,"none",true);
            }
        }
    }//GEN-LAST:event_btnRevisarTodoActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btgRevisar;
    private javax.swing.JButton btnDetalles;
    private javax.swing.JButton btnProveedores;
    private javax.swing.JButton btnRegreso;
    private javax.swing.JButton btnRevisado;
    private javax.swing.JButton btnRevisarTodo;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<String> cmbFecha;
    private javax.swing.JComboBox<String> cmbProveedor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblFactura;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblProveedores;
    private javax.swing.JRadioButton rdoNo;
    private javax.swing.JRadioButton rdoSi;
    private javax.swing.JTable tblDetalles;
    private javax.swing.JTable tblFacturas;
    private javax.swing.JLabel txtTitulo;
    // End of variables declaration//GEN-END:variables
}
