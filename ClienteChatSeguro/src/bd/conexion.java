/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author MANU
 */
public class conexion {
    
    private Logger log = Logger.getLogger(conexion.class);
    
    Connection cn = null;
    
    /* Parametros de Conexion a Base de Datos */
    final String dbdir = "beta.fiandeira.es";
    final String dbName = "ChatJava";
    final String dbUser = "ChatJava";
    final String dbPass = "ChatJava";
    
    /* Conexion MySQL */
    final String url = "jdbc:mysql://"+dbdir+"/"+dbName;

    public conexion() throws SQLException {
        
        cn = DriverManager.getConnection(url,dbUser,dbPass);
        
    }

    public Connection getCn() {
        return cn;
    }
    
    public boolean insert(String campos, String valor) {
        
        boolean error = false;
        
        try{
        
            conexion con= new conexion();

            /* Insert */
            PreparedStatement insert = con.getCn().prepareStatement("INSERT INTO Usuarios ("+campos+") VALUES ("+valor+")");

            insert.executeUpdate();
            
            log.info("Usuario creado con exito.");
            
            return false;
            
        }
        catch(SQLException ex){
            log.error("Error al Crear usuario en DB.");
            return true;
        }
    }
    
    public boolean delete(String condicion) {
        
        boolean error = false;
        
        try{
        
            conexion con= new conexion();

            /* Delete */
            PreparedStatement delete = con.getCn().prepareStatement("DELETE FROM Usuarios WHERE " + condicion);

            delete.executeUpdate();
            
            log.info("Usuario eliminado con exito.");
            
            return false;
            
        }
        catch(SQLException ex){
            log.error("Error al eliminar usuario en DB.");
            return true;
        }
    }
    
    public boolean update(String campos, String condicion) {
        
        boolean error = false;
        
        try{
        
            conexion con= new conexion();

            /* Update */
            PreparedStatement update = con.getCn().prepareStatement("UPDATE Usuarios SET "+campos+" WHERE "+condicion);

            update.executeUpdate();
            
            log.info("Usuario Modificado con exito.");
            
            return false;
            
        }
        catch(SQLException ex){
            log.error("Error al Modificar usuario en DB.");
            return true;
        }
    }
    
    public ResultSet select(String condicion) throws SQLException{
            
        conexion con= new conexion();
            
        /* Select */
        PreparedStatement consulta = con.getCn().prepareStatement("SELECT * FROM Usuarios where "+condicion);

        ResultSet rs = consulta.executeQuery();
            
        return rs;
        
    }
    
}
