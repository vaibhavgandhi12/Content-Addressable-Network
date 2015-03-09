import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class Bootstrap is used to connect a new peer to an existing peer in the
 * system
 * 
 * @author vaibhavgandhi
 *
 */
public class Bootstrap extends UnicastRemoteObject implements
		BootstrapInterface {

	/**
	 * Global variables
	 */
	private static final long serialVersionUID = 1L;
	String bootstrapNode_ip;

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	protected Bootstrap() throws RemoteException {
		super();
		bootstrapNode_ip = null;
	}

	/**
	 * Set the bootstrap IP address
	 * 
	 * @param ip
	 *            ip address
	 */
	public void setIPAddress(String ip) {
		bootstrapNode_ip = ip;
		System.out.println("IP of new Bootstrap Node: " + bootstrapNode_ip);
	}

	/**
	 * Get the bootstrap IP address
	 * 
	 * @param incoming
	 *            ip of node that tried to access bootstrap ip
	 * @return bootstrap ip
	 */
	public String getIPAddress(String incoming) {
		System.out.println("Attempting to connect by " + incoming);
		return bootstrapNode_ip;
	}

	/**
	 * Get random coordinate
	 * 
	 * @return a coordinate
	 */
	public double getRandomCoordinate() {
		int min = 1;
		int max = 9;
		return (double) (min + (Math.random() * ((max - min) + 1)));
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Bootstrap bs = new Bootstrap();
			Registry r = LocateRegistry.createRegistry(21291);
			r.bind("Bootstrap Server", bs);
			System.out.println("Server alive!");
			System.out.println("Server ip: "
					+ InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e) {
			System.out.println("Error while creating Bootstrap Server");
			e.printStackTrace();
		}

	}

}
