
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author franc
 */
public class ConexionBD {
    
    // Parámetros de conexión - AJUSTA ESTOS VALORES SEGÚN TU CONFIGURACIÓN
    private static final String URL = "jdbc:mysql://localhost:3306/inventario_ventas";
    private static final String USUARIO = "root";
    private static final String PASSWORD = ""; // Cambia según tu configuración
    
    // Driver de MySQL
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    /**
     * Obtiene una conexión a la base de datos
     * @return Connection objeto de conexión
     * @throws SQLException si hay error al conectar
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Cargar el driver de MySQL
            Class.forName(DRIVER);
            
            // Establecer la conexión
            Connection conn = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado: " + e.getMessage());
        }
    }
    
    /**
     * Prueba la conexión a la base de datos
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public static boolean probarConexion() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error al probar conexión: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cierra una conexión de manera segura
     * @param conn la conexión a cerrar
     */
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}