/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientechatseguro;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 *
 * @author MANU
 */

public class ClienteChatSeguro extends JFrame{

    private Logger log = Logger.getLogger(ClienteChatSeguro.class);
    public static JTextArea mensajesChat;
    private Socket socket;
    
    private int puerto;
    private String host;
    private String usuario;
    private String usuarioC;
    
    public static utilidades.RSA rsaUser;
    public static utilidades.RSA rsaConectar;
    
    public ClienteChatSeguro() {
    
        super("Cliente Chat");
        
        // Elementos de la ventana
        mensajesChat = new JTextArea();
        mensajesChat.setEnabled(false); // El area de mensajes del chat no se debe de poder editar
        mensajesChat.setLineWrap(true); // Las lineas se parten al llegar al ancho del textArea
        mensajesChat.setWrapStyleWord(true); // Las lineas se parten entre palabras (por los espacios blancos)
        JScrollPane scrollMensajesChat = new JScrollPane(mensajesChat);
        JTextField tfMensaje = new JTextField("");
        JButton btEnviar = new JButton("Enviar");
        
        
        // Colocacion de los componentes en la ventana
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(20, 20, 20, 20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        c.add(scrollMensajesChat, gbc);
        // Restaura valores por defecto
        gbc.gridwidth = 1;        
        gbc.weighty = 0;
        
        gbc.fill = GridBagConstraints.HORIZONTAL;        
        gbc.insets = new Insets(0, 20, 20, 20);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        c.add(tfMensaje, gbc);
        // Restaura valores por defecto
        gbc.weightx = 0;
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        c.add(btEnviar, gbc);
        
        this.setBounds(400, 100, 400, 500);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        //Icono
        Image icono = new ImageIcon(getClass().getResource("/img/chat.png")).getImage();
        setIconImage(icono);
        
        // Ventana de configuracion inicial
        VentanaConfiguracion vc = new VentanaConfiguracion(this);
        host = vc.getHost();
        puerto = vc.getPuerto();
        usuario = vc.getUsuario();
        usuarioC = vc.getUsuarioC();
        
        // Creando Usuario en DB
        bd.conexion con = null;
        
        try{
            con = new bd.conexion();
            while(con.insert("usuario", "'"+usuario+"'") != false){
                
                // Ventana de configuracion inicial
                vc = new VentanaConfiguracion(this);
                host = vc.getHost();
                puerto = vc.getPuerto();
                usuario = vc.getUsuario();
                usuarioC = vc.getUsuarioC();
                
            }
            
        }
        catch(SQLException ex){
            log.error("Error al conectar con la DB.");
        }
        
        this.setTitle("Cliente Chat - " + usuarioC.toUpperCase());
        
        log.info("Quieres conectarte a " + host + " en el puerto " + puerto + " con el nombre de ususario: " + usuario + ".");
        log.info("Conectando al usuario " + usuarioC + " de forma segura.");
        
        String nombreFolder = "tmp-"+usuario;
        
        File folder = new File(nombreFolder);
        folder.mkdir();
        folder.setReadOnly();
        
        log.info("Creada carpeta temporal para "+usuario+".");
        
        // Nonmbres archivos de claves
        String nombreClavePublica = "/rsa-"+usuario+".pub";
        String nombreClavePrivada = "/rsa-"+usuario+".pri";
        
        // Crear par de Claves para Usuario
        try{
            
            rsaUser = new utilidades.RSA();
            
            rsaUser.genKeyPair(1024);
            
            rsaUser.saveToDiskPrivateKey(nombreFolder + nombreClavePrivada);
            rsaUser.saveToDiskPublicKey(nombreFolder + nombreClavePublica);
            
            log.info("Creada par de Claves para " + usuario + ".");
            
        }
        catch(IOException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex){
            log.error("Error al crear par de Claves para " + usuario + ".");
        }
        
        try{
            
            // Leer Ficheiro
            File archivo = new File (nombreFolder + nombreClavePublica);
            FileReader fr = new FileReader (archivo);
            BufferedReader br = new BufferedReader(fr);
            
            String linea = br.readLine();
            
            fr.close();
            br.close();
            
            con.update("clavePublica = '"+linea+"'", "usuario = '"+usuario+"'");
            
        }
        catch(IOException ex){
            log.error("Error al leer archivo de claves.");
        }
        
        // Descarga fichero de usuario a conectar
        String nombreClavePublicaUsuarioConectar = "/rsa-"+usuarioC+".pub";
        
        // Buscar usuario a conectar
        try{            
            int cont = 0;
            ResultSet rs;
            String pruebaClave = "";
            
            while(cont < 20){
                
                log.info("Buscando el Usuario a Conectar.");
                
                Thread.sleep(1500);
                rs = con.select("usuario = '"+usuarioC+"'");
                
                while(rs.next()){
                    
                    cont = 20;
                    
                    ResultSet clave = con.select("usuario = '"+usuarioC+"'");
                    while(clave.next()){
                        String claveUsuario = clave.getString("clavePublica");
                        
                        // Guardar archivo
                        try{
                            FileWriter fichero = new FileWriter(nombreFolder + nombreClavePublicaUsuarioConectar);
                            PrintWriter pw = new PrintWriter(fichero);
                            
                            pw.print(claveUsuario);
                            
                            fichero.close();
                            
                        }
                        catch(IOException ex){
                            log.error("Error al guardar clave Publica de "+usuarioC+".");
                        }
                        
                    }
                    
                    log.info("Usuario encontrando.");
                    log.info("Rescatar ClavePublica de "+usuarioC+".");
                    
                }
                
                cont++;
                
            }
            
            // Clave Usuario a Conectar
            rsaConectar = new utilidades.RSA();
            
            try{
            
                rsaConectar.openFromDiskPublicKey(nombreFolder + nombreClavePublicaUsuarioConectar);
            
            }
            catch(IOException ex){
                log.error("Error al abrir archivo de clave publica.");
            }
            catch(Exception ex){
                log.error("Error al recuperar Clave publica.");
            }
            
        }
        catch(SQLException ex){
            log.error("Error al conectar con la DB.");
        } catch (InterruptedException ex) {
        }
        
        // Se crea el socket para conectar con el Sevidor del Chat
        try {
            socket = new Socket(host, puerto);
        } catch (UnknownHostException ex) {
            log.error("No se ha podido conectar con el servidor (" + ex.getMessage() + ").");
        } catch (IOException ex) {
            log.error("No se ha podido conectar con el servidor (" + ex.getMessage() + ").");
        }
        
        // Accion para el boton enviar
        btEnviar.addActionListener(new ConexionServidor(socket, tfMensaje, usuario, usuarioC));
        
    }
    
    @Override
    public void dispose() {
        
        try{
            
            bd.conexion con = new bd.conexion();
            
            con.delete("usuario = '" + usuario + "'");
            
        }
        catch(SQLException ex){
            log.error("Error al eliminar usuario.");
        }
        
        String sDirectorio = "tmp-"+usuario;
        File f = new File(sDirectorio);
        
        if (f.delete())
            log.info("El fichero " + sDirectorio + " ha sido borrado correctamente");
        else
            log.error("El fichero " + sDirectorio + " no se ha podido borrar");
        
        log.info("Cerrando conexion");
        
        // Cerrar ventana
        super.dispose();
        System.exit(0);
        
     }
    
    public void recibirMensajesServidor(){
        // Obtiene el flujo de entrada del socket
        DataInputStream entradaDatos = null;
        String mensaje;
        try {
            entradaDatos = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            log.error("Error al crear el stream de entrada: " + ex.getMessage());
        } catch (NullPointerException ex) {
            log.error("El socket no se creo correctamente. ");
        }
        
        // Bucle infinito que recibe mensajes del servidor
        boolean conectado = true;
        while (conectado) {
            try {
                mensaje = entradaDatos.readUTF();
                
                String[] partes = mensaje.split(":");
                String concatenar;
                
                if(partes[0].compareTo(usuario) == 0 && partes[1].compareTo(usuarioC) == 0){
                    concatenar = partes[1]+": "+ClienteChatSeguro.rsaUser.Decrypt(partes[2].trim());
                    mensajesChat.append(concatenar + System.lineSeparator());
                }
                
            } catch (IOException ex) {
                log.error("Error al leer del stream de entrada: " + ex.getMessage());
                conectado = false;
            } catch (NullPointerException ex) {
                log.error("El socket no se creo correctamente. ");
                conectado = false;
            }
            catch(Exception ex){
                log.error("Error al Desencriptar.");
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Carga el archivo de configuracion de log4J
        PropertyConfigurator.configure("log4j.properties");        
        
        ClienteChatSeguro c = new ClienteChatSeguro();
        c.recibirMensajesServidor();
    }
    
}
