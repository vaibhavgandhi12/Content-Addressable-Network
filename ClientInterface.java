import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

/**
 * ClientInterface provides the functionality for new Clients (peers) to talk to
 * each other
 * 
 * @author vaibhavgandhi
 *
 */
public interface ClientInterface extends Remote {

	/**
	 * Declaration to get coordinate
	 * 
	 * @return coordinate of current node
	 * @throws RemoteException
	 */
	Coordinate getCoordinate() throws RemoteException;

	/**
	 * Declaration to set coordinate of current node
	 * 
	 * @param c
	 *            where c is a new coordinate
	 * @throws RemoteException
	 */
	void setCoordinate(Coordinate c) throws RemoteException;

	/**
	 * Declaration to get ip address
	 * 
	 * @return
	 * @throws RemoteException
	 */
	String getIPAddress() throws RemoteException;

	/**
	 * Declaration to get identifier
	 * 
	 * @return
	 * @throws RemoteException
	 */
	String getIdentifier() throws RemoteException;

	/**
	 * Declaration to get neighbours
	 * 
	 * @return neighbours
	 * @throws RemoteException
	 */
	HashSet<Neighbour> getNeighbours() throws RemoteException;

	/**
	 * Declaration to add neighbor
	 * 
	 * @param n
	 *            where n is a new neighbor
	 * @throws RemoteException
	 */
	void addNeighbour(Neighbour n) throws RemoteException;

	/**
	 * Declaration to update neighbor
	 * 
	 * @param n
	 *            where n is an existing neighbor
	 * @throws RemoteException
	 */
	void updateNeighbour(Neighbour n) throws RemoteException;

	/**
	 * Declaration to delete a neighbor
	 * 
	 * @param n
	 *            where n is a neighbor that has to be deleted
	 * @throws RemoteException
	 */
	void deleteNeighbour(Neighbour n) throws RemoteException;

	/**
	 * Declaration to route a new node to its destination
	 * 
	 * @param x
	 * @param y
	 *            where x and y are destination coordinates
	 * @param ip
	 *            where ip is the IP address of new node
	 * @throws RemoteException
	 */
	String routeNode(double x, double y, String ip) throws RemoteException;

	/**
	 * Declaration to route file to destination node
	 * 
	 * @param x
	 * @param y
	 *            where x and y are destination coordinates
	 * @param filename
	 *            where filename is the name of the file
	 * @param originatingIP
	 *            where originatingIP is the IP from where files originates
	 * @throws RemoteException
	 */
	void routeFile(double x, double y, String filename, String originatingIP)
			throws RemoteException;

	/**
	 * Declaration to find file from source node
	 * 
	 * @param x
	 * @param y
	 *            where x and y are destination coordinates
	 * @param filename
	 *            where filename is the name of the file
	 * @throws RemoteException
	 */
	String findFile(double x, double y, String filename) throws RemoteException;

	/**
	 * Declaration to get files
	 * 
	 * @return set of files associated with the current node
	 * @throws RemoteException
	 */
	HashSet<String> getFileList() throws RemoteException;

	/**
	 * Declaration to add file
	 * 
	 * @param file
	 *            name of the file
	 * @param originatingIP
	 *            ip of origin peer
	 * @throws RemoteException
	 */
	void addFile(String file, String originatingIP) throws RemoteException;

	/**
	 * Declaration to save file
	 * 
	 * @param fileName
	 *            file name
	 * @return byte form of file
	 * @throws RemoteException
	 */
	public byte[] saveFile(String fileName) throws RemoteException;

	/**
	 * Declaration to has joined
	 * 
	 * @return boolean value
	 * @throws RemoteException
	 */
	public boolean hasJoined() throws RemoteException;
}
