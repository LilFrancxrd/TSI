import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Venta extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Venta.class.getName());
    private String usuario = "hello";
    private boolean estado = false;
    Connection conex = null;
    Statement stm = null;
    
    private DefaultTableModel modeloTabla;
    private List<ProductoVenta> productosVenta;
    private double totalVenta = 0;
    private double totalPagado = 0;
    private double totalFiado = 0;
    
    // Clase interna para manejar productos en la venta
    private class ProductoVenta {
        int codProducto;
        String nomProducto;
        double precioActual;
        int cantidad;
        double subtotal;
        boolean fiado;
        
        ProductoVenta(int codProducto, String nomProducto, double precioActual, int cantidad, boolean fiado) {
            this.codProducto = codProducto;
            this.nomProducto = nomProducto;
            this.precioActual = precioActual;
            this.cantidad = cantidad;
            this.fiado = fiado;
            this.subtotal = precioActual * cantidad;
        }
    }
    
    public Venta() {
        initComponents();
        inicializarComponentesVenta();
    }
    
    public Venta(String usuario, boolean estado) {
        this.usuario = usuario;
        this.estado = estado;
        initComponents();
        inicializarComponentesVenta();
        conectar();
        customClose();
        cargarClientes();
    }
    
    private void inicializarComponentesVenta() {
        productosVenta = new ArrayList<>();

        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return Integer.class;
                if (columnIndex == 2 || columnIndex == 4) return Double.class;
                return String.class;
            }
        };

        String[] columnas = {"ID", "PRODUCTO", "P.UNIT", "CANTIDAD", "SUBTOTAL", "FIADO"};
        for (String columna : columnas) {
            modeloTabla.addColumn(columna);
        }

        tablaBoleta.setModel(modeloTabla);

        chkFiado.addActionListener(e -> actualizarVisibilidadCliente());

        lblProductoEncontrado.setText("Producto: ");
        
        lblTotalVenta.setText("$0");
        lblTotalPago.setText("$0");
        lblFiado.setText("$0");

        actualizarTotales();
        actualizarVisibilidadCliente();
    }
    
    private void cargarClientes() {
        cmbDeudor.removeAllItems();
        cmbDeudor.addItem("Seleccionar cliente");
        
        try {
            stm = conex.createStatement();
            ResultSet rs = stm.executeQuery("SELECT rutDeudor, nomDeudor FROM deudores ORDER BY nomDeudor");
            
            while (rs.next()) {
                String rut = rs.getString("rutDeudor");
                String nombre = rs.getString("nomDeudor");
                cmbDeudor.addItem(rut + " - " + nombre);
            }
            
            rs.close();
            stm.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage());
        }
        
        String[] mediosPago = {"Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Transferencia"};
        cmbMedioPago.removeAllItems();
        for (String medio : mediosPago) {
            cmbMedioPago.addItem(medio);
        }
    }
    
    private void actualizarVisibilidadCliente() {
        boolean visible = chkFiado.isSelected();
        lblNomCliente.setVisible(visible);
        cmbDeudor.setVisible(visible);
    }
    
    private void buscarProducto() {
        String busqueda = txtBuscar.getText().trim();

        if (busqueda.isEmpty()) {
            lblProductoEncontrado.setText("Producto: ");
            return;
        }

        try {
            stm = conex.createStatement();
            String query;

            try {
                int codigo = Integer.parseInt(busqueda);
                query = "SELECT codProducto, nomProducto, precioActual, stock FROM productos WHERE codProducto = " + codigo;
            } catch (NumberFormatException e) {
                query = "SELECT codProducto, nomProducto, precioActual, stock FROM productos WHERE nomProducto LIKE '%" + busqueda + "%' LIMIT 1";
            }

            ResultSet rs = stm.executeQuery(query);

            if (rs.next()) {
                int codigo = rs.getInt("codProducto");
                String nombre = rs.getString("nomProducto");
                double precio = rs.getDouble("precioActual");
                int stock = rs.getInt("stock");

                lblProductoEncontrado.setText("Producto: " + nombre + " | Stock: " + stock + " | Precio: $" + String.format("%,.0f", precio));

                txtBuscar.setToolTipText(codigo + ":" + nombre + ":" + precio + ":" + stock);

            } else {
                lblProductoEncontrado.setText("Producto: No encontrado");
                txtBuscar.setToolTipText("");
            }

            rs.close();
            stm.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar producto: " + ex.getMessage());
            lblProductoEncontrado.setText("Producto: Error");
        }
    }  
    
    private void agregarProducto() {
        String cantidadStr = txtCant.getText().trim();
        String busqueda = txtBuscar.getText().trim();

        if (busqueda.isEmpty() || txtBuscar.getToolTipText() == null || txtBuscar.getToolTipText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero busque un producto válido");
            return;
        }

        if (cantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0");
                return;
            }

            String datosProducto = txtBuscar.getToolTipText();
            String[] partes = datosProducto.split(":");
            if (partes.length < 3) {
                JOptionPane.showMessageDialog(this, "Error al obtener datos del producto");
                return;
            }

            int codProducto = Integer.parseInt(partes[0]);
            String nomProducto = partes[1];
            double precioActual = Double.parseDouble(partes[2]);
            int stock = partes.length > 3 ? Integer.parseInt(partes[3]) : 0;

            if (cantidad > stock) {
                JOptionPane.showMessageDialog(this, 
                    "Stock insuficiente. Disponible: " + stock + "\n" +
                    "Producto: " + nomProducto);
                return;
            }

            boolean esFiado = chkFiado.isSelected();

            System.out.println("DEBUG: Estado checkbox fiado: " + esFiado); // Para debug

            for (ProductoVenta pv : productosVenta) {
                if (pv.codProducto == codProducto) {
                    pv.cantidad += cantidad;
                    pv.subtotal = pv.precioActual * pv.cantidad;
                    pv.fiado = esFiado; 
                    actualizarTabla();
                    actualizarTotales();

                    txtBuscar.setText("");
                    txtCant.setText("");
                    lblProductoEncontrado.setText("Producto: ");
                    txtBuscar.setToolTipText("");
                    return;
                }
            }

            ProductoVenta nuevoProducto = new ProductoVenta(codProducto, nomProducto, precioActual, cantidad, esFiado);
            productosVenta.add(nuevoProducto);
            actualizarTabla();
            actualizarTotales();

            txtBuscar.setText("");
            txtCant.setText("");
            lblProductoEncontrado.setText("Producto: ");
            txtBuscar.setToolTipText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad válida (número entero)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        
        for (ProductoVenta pv : productosVenta) {
            Object[] fila = {
                pv.codProducto,
                pv.nomProducto,
                pv.precioActual,
                pv.cantidad,
                pv.subtotal,
                pv.fiado ? "Sí" : "No"
            };
            modeloTabla.addRow(fila);
        }
    }
    
    private void actualizarTotales() {
        totalVenta = 0;
        totalFiado = 0;
        
        for (ProductoVenta pv : productosVenta) {
            totalVenta += pv.subtotal;
            if (pv.fiado) {
                totalFiado += pv.subtotal;
            }
        }
        
        totalPagado = totalVenta - totalFiado;
        
        lbTotal.setText("Total Venta:");
        lblTotalVenta.setText("$" + String.format("%,.0f", totalVenta));
        
        lblTotalPagado.setText("Total Pagado:");
        lblTotalPago.setText("$" + String.format("%,.0f", totalPagado));
        
        lblTotalFiado.setText("Total Fiado:");
        lblFiado.setText("$" + String.format("%,.0f", totalFiado));
    }
    
    private void eliminarProducto() {
        int filaSeleccionada = tablaBoleta.getSelectedRow();
        
        if (filaSeleccionada >= 0) {
            productosVenta.remove(filaSeleccionada);
            actualizarTabla();
            actualizarTotales();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar");
        }
    }
    
    private void finalizarVenta() {
        System.out.println("=== DEBUG: FINALIZAR VENTA ===");
        System.out.println("Productos en venta: " + productosVenta.size());
        System.out.println("Checkbox fiado seleccionado: " + chkFiado.isSelected());
        System.out.println("Cliente seleccionado: " + cmbDeudor.getSelectedItem());

        double debugTotalFiado = 0;
        for (ProductoVenta pv : productosVenta) {
            System.out.println("  Producto: " + pv.nomProducto + 
                              ", Fiado: " + pv.fiado + 
                              ", Subtotal: " + pv.subtotal);
            if (pv.fiado) {
                debugTotalFiado += pv.subtotal;
            }
        }
        System.out.println("Total Fiado calculado: " + debugTotalFiado);
        System.out.println("Total Fiado variable: " + totalFiado);
        System.out.println("==============================");

        if (productosVenta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en la boleta");
            return;
        }
        if (productosVenta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en la boleta");
            return;
        }
        
        if (cmbMedioPago.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un medio de pago");
            return;
        }
        
        if (chkFiado.isSelected() && (cmbDeudor.getSelectedIndex() <= 0 || cmbDeudor.getSelectedItem().equals("Seleccionar cliente"))) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para la venta fiada");
            return;
        }
        
        try {
            conex.setAutoCommit(false);
            
            int idBoleta = insertarBoleta();
            
            for (ProductoVenta pv : productosVenta) {
                insertarDetalleProducto(idBoleta, pv);
                actualizarStockProducto(pv);
            }
            
            if (chkFiado.isSelected() && totalFiado > 0) {
                registrarDeuda(idBoleta);
            }
            
            conex.commit();
            
            String mensaje = "Venta realizada exitosamente!\n\n" +
                           "N° Boleta: " + idBoleta + "\n" +
                           "Total Venta: $" + String.format("%,.0f", totalVenta) + "\n" +
                           "Total Pagado: $" + String.format("%,.0f", totalPagado) + "\n" +
                           "Total Fiado: $" + String.format("%,.0f", totalFiado);
            
            if (chkFiado.isSelected()) {
                String clienteSeleccionado = cmbDeudor.getSelectedItem().toString();
                if (clienteSeleccionado.contains(" - ")) {
                    String nombreCliente = clienteSeleccionado.split(" - ")[1];
                    mensaje += "\n\nCliente: " + nombreCliente;
                }
            }
            
            JOptionPane.showMessageDialog(this, mensaje, "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);
            
            limpiarVenta();
            
        } catch (SQLException ex) {
            try {
                conex.rollback();
                JOptionPane.showMessageDialog(this, 
                    "❌ Error al procesar la venta:\n" + ex.getMessage(),
                    "Error en Venta",
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                logger.severe("Error en rollback: " + e.getMessage());
            }
        } finally {
            try {
                conex.setAutoCommit(true);
            } catch (SQLException e) {
                logger.severe("Error al restaurar auto-commit: " + e.getMessage());
            }
        }
    }
    
    private int insertarBoleta() throws SQLException {
        LocalDateTime ahora = LocalDateTime.now();
        Timestamp tiempo = Timestamp.valueOf(ahora);
        String medioPago = cmbMedioPago.getSelectedItem().toString();
        
        String query = "INSERT INTO boletas (rutUsuario, fechaTramite, medioPago, totalPagados, totalFiado) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = conex.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, usuario);
        pstmt.setTimestamp(2, tiempo);
        pstmt.setString(3, medioPago);
        pstmt.setDouble(4, totalPagado);
        pstmt.setDouble(5, totalFiado);
        
        pstmt.executeUpdate();
        
        ResultSet rs = pstmt.getGeneratedKeys();
        int idBoleta = 0;
        if (rs.next()) {
            idBoleta = rs.getInt(1);
        }
        
        rs.close();
        pstmt.close();
        
        return idBoleta;
    }
    
    private void insertarDetalleProducto(int idBoleta, ProductoVenta pv) throws SQLException {
        String query = "INSERT INTO detalleboletaproductos " +
                      "(idBoleta, codProducto, cantidad, cantidadFiado, precioUnitario, " +
                      "descuento, totalPago, totalFiado, tipoTransaccion) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conex.prepareStatement(query);

        pstmt.setInt(1, idBoleta);

        pstmt.setInt(2, pv.codProducto);

        pstmt.setInt(3, pv.cantidad);

        int cantidadFiada = pv.fiado ? pv.cantidad : 0;
        pstmt.setInt(4, cantidadFiada);

        pstmt.setDouble(5, pv.precioActual);

        pstmt.setDouble(6, 0.0);

        double totalPago;
        double totalFiado;

        if (pv.fiado) {
            totalPago = 0;              
            totalFiado = pv.subtotal;   
        } else {
            totalPago = pv.subtotal;    
            totalFiado = 0;            
        }

        pstmt.setDouble(7, totalPago);
        pstmt.setDouble(8, totalFiado);

        pstmt.setString(9, "VENTA");

        pstmt.executeUpdate();
        pstmt.close();
    }

    private void registrarDeuda(int idBoleta) throws SQLException {
        try {
            String itemSeleccionado = cmbDeudor.getSelectedItem().toString();

            if (itemSeleccionado.equals("Seleccionar cliente")) {
                throw new SQLException("Debe seleccionar un cliente");
            }

            String rutCliente = itemSeleccionado.split(" - ")[0].trim();

            LocalDateTime ahora = LocalDateTime.now();
            Timestamp tiempo = Timestamp.valueOf(ahora);

            String query = "INSERT INTO deudas (rutDeudor, idBoleta, fechaDeuda, estaPagado, montoPagado) " +
                          "VALUES (?, ?, ?, false, 0)";

            PreparedStatement pstmt = conex.prepareStatement(query);
            pstmt.setString(1, rutCliente);
            pstmt.setInt(2, idBoleta);
            pstmt.setTimestamp(3, tiempo);

            pstmt.executeUpdate();
            pstmt.close();

            System.out.println("DEBUG: Deuda registrada exitosamente");

        } catch (Exception e) {
            System.err.println("ERROR en registrarDeuda: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Error al registrar deuda: " + e.getMessage());
        }
    }
    
    private void limpiarVenta() {
        productosVenta.clear();
        actualizarTabla();
        actualizarTotales();

        txtBuscar.setText("");
        txtCant.setText("");
        lblProductoEncontrado.setText("Producto: ");
        txtBuscar.setToolTipText("");
        chkFiado.setSelected(false);
        cmbDeudor.setSelectedIndex(0);
        cmbMedioPago.setSelectedIndex(0);

        actualizarVisibilidadCliente();
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
    
    private void actualizarStockProducto(ProductoVenta pv) throws SQLException {
        String query = "UPDATE productos SET stock = stock - ? WHERE codProducto = ?";
        
        PreparedStatement pstmt = conex.prepareStatement(query);
        pstmt.setInt(1, pv.cantidad);
        pstmt.setInt(2, pv.codProducto);
        
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    public void conectar(){
        String url = "jdbc:mysql://localhost:3306/vistaalmar";
        String user = "root";
        String pass = "";
        try {
            conex = DriverManager.getConnection(url, user, pass);
        } catch(SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error en conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }       
    }
    
    public void registrarRetiro(){
        try {
            LocalDateTime ahora = LocalDateTime.now();
            Timestamp tiempo = Timestamp.valueOf(ahora);
            stm = conex.createStatement();
            stm.executeUpdate("INSERT INTO accessLog (rutUsuario, tipoAccion, fechaAccion) VALUES('" + usuario + "',0,'" + tiempo + "')");
            stm.close();
        } catch(SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error en database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        jButton1 = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        lblAgregar = new javax.swing.JLabel();
        lblBuscar = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        txtCant = new javax.swing.JTextField();
        lblCant = new javax.swing.JLabel();
        btnAgregarBoleta = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        chkFiado = new javax.swing.JCheckBox();
        lblNomCliente = new javax.swing.JLabel();
        cmbDeudor = new javax.swing.JComboBox<>();
        lblProductosBoleta = new javax.swing.JLabel();
        scrollBoleta = new javax.swing.JScrollPane();
        tablaBoleta = new javax.swing.JTable();
        jSeparator3 = new javax.swing.JSeparator();
        lblInfoPago = new javax.swing.JLabel();
        lblMedioPago = new javax.swing.JLabel();
        cmbMedioPago = new javax.swing.JComboBox<>();
        lbTotal = new javax.swing.JLabel();
        lblTotalPagado = new javax.swing.JLabel();
        lblTotalFiado = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        btnFinalizarVenta = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        lblProductoEncontrado = new javax.swing.JLabel();
        lblTotalVenta = new javax.swing.JLabel();
        lblFiado = new javax.swing.JLabel();
        lblTotalPago = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        jButton1.setText("Historial Boletas");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnInventario.setText("Inventario");
        btnInventario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInventarioActionPerformed(evt);
            }
        });

        lblAgregar.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblAgregar.setText("Agregar Producto");

        lblBuscar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblBuscar.setText("Buscar Producto(ID/Nombre):");

        lblCant.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblCant.setText("Cant:");

        btnAgregarBoleta.setBackground(new java.awt.Color(51, 204, 0));
        btnAgregarBoleta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarBoleta.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregarBoleta.setText("Agregar");
        btnAgregarBoleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarBoletaActionPerformed(evt);
            }
        });

        chkFiado.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        chkFiado.setText("¿Venta Fiada?");

        lblNomCliente.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblNomCliente.setText("Nombre Cliente");
        lblNomCliente.setToolTipText("");

        cmbDeudor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblProductosBoleta.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblProductosBoleta.setText("Productos en Boleta");

        tablaBoleta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "PRODUCTO", "P.UNIT", "CANTIDAD", "SUBTOTAL", "FIADO"
            }
        ));
        scrollBoleta.setViewportView(tablaBoleta);

        lblInfoPago.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblInfoPago.setText("Informacion de Pago");

        lblMedioPago.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblMedioPago.setText("Medio de Pago:");

        cmbMedioPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lbTotal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lbTotal.setText("Total Venta:");

        lblTotalPagado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotalPagado.setText("Total Pagado:");

        lblTotalFiado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotalFiado.setText("Total Fiado:");

        btnFinalizarVenta.setBackground(new java.awt.Color(0, 204, 51));
        btnFinalizarVenta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFinalizarVenta.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizarVenta.setText("Finalizar Venta");
        btnFinalizarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinalizarVentaActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(255, 0, 51));
        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar Seleccionado");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        lblProductoEncontrado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblProductoEncontrado.setText("Producto");

        lblTotalVenta.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotalVenta.setText("$0");

        lblFiado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblFiado.setText("$0");

        lblTotalPago.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTotalPago.setText("$0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jSeparator3)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(chkFiado)
                        .addGap(157, 157, 157))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(scrollBoleta, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(42, 42, 42)
                                .addComponent(btnInventario, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(43, 43, 43)
                                .addComponent(jButton1)
                                .addGap(57, 57, 57)
                                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(44, 44, 44))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblBuscar)
                                        .addGap(18, 18, 18)
                                        .addComponent(lblCant))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(78, 78, 78)
                                        .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(14, 14, 14)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(174, 174, 174)
                                        .addComponent(lblNomCliente))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(156, 156, 156)
                                        .addComponent(cmbDeudor, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(lblProductosBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblInfoPago)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblMedioPago)
                                        .addGap(66, 66, 66)
                                        .addComponent(cmbMedioPago, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(188, 188, 188)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblTotalFiado, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lblFiado, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblTotalPagado, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lbTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblTotalVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                                            .addComponent(lblTotalPago, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(lblProductoEncontrado, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCant, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAgregarBoleta)))
                        .addContainerGap(25, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnFinalizarVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(93, 93, 93))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnInventario, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRegresar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPrincipal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAgregar)
                    .addComponent(chkFiado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNomCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblBuscar)
                        .addComponent(lblCant)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtBuscar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbDeudor, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCant, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProductoEncontrado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblProductosBoleta)
                .addGap(18, 18, 18)
                .addComponent(scrollBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbTotal)
                            .addComponent(lblTotalVenta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTotalPagado)
                            .addComponent(lblTotalPago))
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTotalFiado)
                            .addComponent(lblFiado)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblInfoPago)
                        .addGap(51, 51, 51)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMedioPago)
                            .addComponent(cmbMedioPago, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnFinalizarVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Productos en Boleta");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(339, 339, 339)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(340, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(320, 320, 320)
                    .addComponent(jLabel6)
                    .addContainerGap(427, Short.MAX_VALUE)))
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

    private void btnInventarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInventarioActionPerformed
       this.dispose(); 
       new Inventario(usuario,estado).setVisible(true);
    }//GEN-LAST:event_btnInventarioActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnFinalizarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarVentaActionPerformed
        finalizarVenta();
    }//GEN-LAST:event_btnFinalizarVentaActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnAgregarBoletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarBoletaActionPerformed
        agregarProducto();
    }//GEN-LAST:event_btnAgregarBoletaActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarProducto();
    }//GEN-LAST:event_btnEliminarActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Venta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarBoleta;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnFinalizarVenta;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnPrincipal;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JCheckBox chkFiado;
    private javax.swing.JComboBox<String> cmbDeudor;
    private javax.swing.JComboBox<String> cmbMedioPago;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel lbTotal;
    private javax.swing.JLabel lblAgregar;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JLabel lblCant;
    private javax.swing.JLabel lblFiado;
    private javax.swing.JLabel lblInfoPago;
    private javax.swing.JLabel lblMedioPago;
    private javax.swing.JLabel lblNomCliente;
    private javax.swing.JLabel lblProductoEncontrado;
    private javax.swing.JLabel lblProductosBoleta;
    private javax.swing.JLabel lblTotalFiado;
    private javax.swing.JLabel lblTotalPagado;
    private javax.swing.JLabel lblTotalPago;
    private javax.swing.JLabel lblTotalVenta;
    private javax.swing.JScrollPane scrollBoleta;
    private javax.swing.JTable tablaBoleta;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCant;
    // End of variables declaration//GEN-END:variables
}
