
package clientechatseguro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JTextField;
import org.apache.log4j.Logger;

/**
 *
 * @author MANU
 */

public class ConexionServidor implements ActionListener {
    
    private Logger log = Logger.getLogger(ConexionServidor.class);
    private Socket socket; 
    private JTextField tfMensaje;
    private String usuario;
    private String usuarioC;
    private DataOutputStream salidaDatos;
    
    public ConexionServidor(Socket socket, JTextField tfMensaje, String usuario, String usuarioC) {
        this.socket = socket;
        this.tfMensaje = tfMensaje;
        this.usuario = usuario;
        this.usuarioC = usuarioC;
        
        try {
            this.salidaDatos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            log.error("Error al crear el stream de salida : " + ex.getMessage());
        } catch (NullPointerException ex) {
            log.error("El socket no se creo correctamente. ");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            salidaDatos.writeUTF(usuarioC + ":" + usuario + ": " + ClienteChatSeguro.rsaConectar.Encrypt(tfMensaje.getText()));
            ClienteChatSeguro.mensajesChat.append("Yo: " + tfMensaje.getText() + System.lineSeparator());
            tfMensaje.setText("");
        } catch (IOException ex) {
            log.error("Error al intentar enviar un mensaje: " + ex.getMessage());
        }
        catch(Exception ex){
            log.error("Error al cifrar mensaje.");
        }
        
    }

}