import java.io.IOException;

import javax.swing.JOptionPane;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;


public class StartRestServer {
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		HttpServer server = HttpServerFactory.create("http://localhost:8080/rest");
		server.start();
		JOptionPane.showMessageDialog(null, "Done.");
		//server.stop(0);
	}

}
