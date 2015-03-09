import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Class Client implements functionality for peers in a CAN system. It has
 * functions to join, leave, view peers, add, search and list files.
 * 
 * @author vaibhavgandhi
 *
 */
public class Client extends UnicastRemoteObject implements ClientInterface,
		Serializable {

	/**
	 * Global shared variables
	 */
	HashSet<Neighbour> neighbours;
	boolean joinFlag;
	Coordinate myCoordinate;
	HashSet<String> files;
	Registry myPeer;
	String BSIPAddress;
	String identifier;
	String myIPAddress;
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 * 
	 * @throws RemoteException
	 * @throws UnknownHostException
	 */
	protected Client() throws RemoteException, UnknownHostException {
		super();
		BSIPAddress = null;
		files = new HashSet<String>();
		identifier = null;
		joinFlag = false;
		myCoordinate = null;
		myIPAddress = InetAddress.getLocalHost().getHostAddress();
		myPeer = null;
		neighbours = new HashSet<Neighbour>();
	}

	@Override
	public String toString() {
		String s = "";
		s += "My IP: " + myIPAddress + "\n";
		s += "My indentifier: " + identifier + "\n";
		if (joinFlag)
			s += "Node is connected\n";
		s += "\nMy Coordinates: " + myCoordinate;
		s += "Files list: \n";
		if (!files.isEmpty()) {
			Iterator<String> fileIterator = files.iterator();
			while (fileIterator.hasNext()) {
				String file = fileIterator.next();
				s += file + "\n";
			}
		} else {
			s += "Empty";
		}
		s += "\nNeighbour list: \n";
		if (!neighbours.isEmpty()) {
			Iterator<Neighbour> neighbourIterator = neighbours.iterator();
			while (neighbourIterator.hasNext()) {
				Neighbour n = neighbourIterator.next();
				s += n;
			}
		} else {
			s += "Empty";
		}
		return s;
	}

	/**
	 * Hash Function to generate x coordinate
	 * 
	 * @param keyword
	 * @return double value
	 */
	private double CharAtOdd(String keyword) {
		double sum = 0;
		if (keyword.length() == 1) {
			return 0;
		} else {
			for (int i = 0; i < keyword.toCharArray().length;) {
				sum = sum + keyword.charAt(i);
				i = i + 2;
			}
			return sum % 10;
		}
	}

	/**
	 * Hash Function to generate y coordinate
	 * 
	 * @param keyword
	 * @return double value
	 */
	private double CharAtEven(String keyword) {
		double sum = 0;
		if (keyword.length() == 1) {
			return 1;
		} else {
			for (int i = 1; i < keyword.toCharArray().length;) {
				sum = sum + keyword.charAt(i);
				i = i + 2;
			}
			return sum % 10;
		}
	}

	/**
	 * join method adds this node to the CAN network
	 */
	private void join() {
		try {
			Registry bsServer = LocateRegistry.getRegistry(BSIPAddress, 21291);
			BootstrapInterface obj = (BootstrapInterface) bsServer
					.lookup("Bootstrap Server");
			String bsIP = obj.getIPAddress(myIPAddress);
			myPeer = LocateRegistry.createRegistry(21291);
			myPeer.bind("peer", this);
			identifier = new SimpleDateFormat("HHmmss").format(Calendar
					.getInstance().getTime());
			if (bsIP == null) {
				System.out.println("First Node in CAN");
				myCoordinate = new Coordinate(0, 0, 10, 10);
				obj.setIPAddress(myIPAddress);
			} else {
				Registry peer_ = LocateRegistry.getRegistry(bsIP, 21291);
				ClientInterface peer = (ClientInterface) peer_.lookup("peer");

				double randomint_x = obj.getRandomCoordinate();
				double randomint_y = obj.getRandomCoordinate();
				String s = peer
						.routeNode(randomint_x, randomint_y, myIPAddress);
				System.out.println("Added " + s);
			}
			joinFlag = true;
			System.out.println("Joining Successful\n");
		} catch (RemoteException e) {
			System.out.println("Error in join on " + myIPAddress);
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Error in join on " + myIPAddress);
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			System.out.println("Error in join on " + myIPAddress);
			e.printStackTrace();
		}
	}

	/**
	 * splitCurrentSpace splits the current space to make room for new peers
	 * 
	 * @param incomingIP
	 *            ip address of the incoming node
	 */
	private void splitCurrentSpace(String incomingIP) {
		System.out.println("Split space for: " + incomingIP);
		try {
			Registry incomingRegistry = LocateRegistry.getRegistry(incomingIP,
					21291);
			ClientInterface incomingPeer = (ClientInterface) incomingRegistry
					.lookup("peer");

			// Perform split and assign new coordinates
			if (myCoordinate.splitVertically()) {
				double x = myCoordinate.upperX;
				myCoordinate.performSplitVertically();
				incomingPeer.setCoordinate(new Coordinate(myCoordinate.upperX,
						myCoordinate.lowerY, x, myCoordinate.upperY));
			} else {
				double y = myCoordinate.upperY;
				myCoordinate.performSplitHorizontally();
				incomingPeer.setCoordinate(new Coordinate(myCoordinate.lowerX,
						myCoordinate.upperY, myCoordinate.upperX, y));
			}

			// Reassigning the files based on coordinates
			Iterator<String> iterator = files.iterator();
			while (iterator.hasNext()) {
				String file = iterator.next();
				double fileX = CharAtOdd(file), fileY = CharAtEven(file);
				if (!myCoordinate.contains(fileX, fileY)) {
					System.out.println("File " + file + " moved to "
							+ incomingPeer.getIdentifier());
					iterator.remove();
					files.remove(file);
					incomingPeer.addFile(file, myIPAddress);
				}
			}

			// Updating the neighbours
			Neighbour incomingAsNeighbour = new Neighbour(
					incomingPeer.getIPAddress(), incomingPeer.getIdentifier(),
					incomingPeer.getCoordinate());
			addNeighbour(incomingAsNeighbour);

			Neighbour selfAsNeighbour = new Neighbour(myIPAddress, identifier,
					myCoordinate);
			incomingPeer.addNeighbour(selfAsNeighbour);

			Neighbour temp = new Neighbour();
			Registry neighbourRegistry;

			if (neighbours.size() > 1) {
				System.out.println("Reassigning Neighbours");
				Iterator<Neighbour> iter = neighbours.iterator();
				while (iter.hasNext()) {
					temp = iter.next();

					Coordinate c = incomingPeer.getCoordinate();
					neighbourRegistry = LocateRegistry.getRegistry(temp.ip,
							21291);
					ClientInterface tempNode = (ClientInterface) neighbourRegistry
							.lookup("peer");

					if (((c.isAdjacentX(temp.c) && c.isSubsetY(temp.c)) || (c
							.isAdjacentY(temp.c) && c.isSubsetX(temp.c)))
							&& (!temp.ip.equals(incomingIP))) {
						System.out.println("Adding node "
								+ incomingPeer.getIdentifier()
								+ " to neighbour " + temp.identifier);
						incomingPeer.addNeighbour(temp);
						tempNode.addNeighbour(incomingAsNeighbour);
					}

					if ((myCoordinate.isAdjacentX(temp.c) && myCoordinate
							.isSubsetY(temp.c))
							|| (myCoordinate.isAdjacentY(temp.c) && myCoordinate
									.isSubsetX(temp.c))) {
						tempNode.updateNeighbour(selfAsNeighbour);
					} else {
						System.out.println("Removing neighbour "
								+ temp.identifier);
						iter.remove();
						neighbours.remove(temp);
						tempNode.deleteNeighbour(selfAsNeighbour);
					}
				}
			}
		} catch (NotBoundException e) {
			System.out.println("Error in splitCurrentSpace on " + myIPAddress
					+ " for ip " + incomingIP);
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Error in splitCurrentSpace on " + myIPAddress
					+ " for ip " + incomingIP);
			e.printStackTrace();
		}
	}

	/**
	 * checkNeighbour finds the neighbour with the best match in terms of size
	 * 
	 * @return the neighbour with the best match
	 */
	private Neighbour checkNeighbour() {
		Neighbour myNeighbour;
		Iterator<Neighbour> iter = neighbours.iterator();
		while (iter.hasNext()) {
			myNeighbour = iter.next();
			if (myCoordinate.isSameSize(myNeighbour.c)) {
				return myNeighbour;
			}
		}
		return null;
	}

	/**
	 * leave enables this node to leave and transfer its files to other
	 * neighbours
	 */
	private void leave() {
		if (neighbours.isEmpty()) {
			Registry bsServer;
			try {
				bsServer = LocateRegistry.getRegistry(BSIPAddress, 21291);
				BootstrapInterface obj = (BootstrapInterface) bsServer
						.lookup("Bootstrap Server");
				files.clear();
				obj.setIPAddress(null);
				UnicastRemoteObject.unexportObject(myPeer, true);
				joinFlag = false;
				return;
			} catch (RemoteException e) {
				System.out.println("Error in leave on " + myIPAddress);
				e.printStackTrace();
			} catch (NotBoundException e) {
				System.out.println("Error in leave on " + myIPAddress);
				e.printStackTrace();
			}
		}

		// Find the best neighbour to merge with
		Neighbour myNeighbour = checkNeighbour();
		if (myNeighbour != null) {
			try {
				Registry neighbourRegistry = LocateRegistry.getRegistry(
						myNeighbour.ip, 21291);
				ClientInterface peer = (ClientInterface) neighbourRegistry
						.lookup("peer");

				Registry bsServer = LocateRegistry.getRegistry(BSIPAddress,
						21291);
				BootstrapInterface obj = (BootstrapInterface) bsServer
						.lookup("Bootstrap Server");

				// Update bootstrap ip if the leaving node was the bootstrap
				// peer
				if (myIPAddress.equals(obj.getIPAddress(myIPAddress))) {
					System.out.println("Update Bootstrp peer");
					obj.setIPAddress(myNeighbour.ip);
				}

				// Reassign the files directly since the coordinates will be
				// merged
				Iterator<String> iterator = files.iterator();
				while (iterator.hasNext()) {
					String filename = iterator.next();
					peer.addFile(filename, myIPAddress);
					iterator.remove();
					files.remove(filename);
				}

				// Update the coordinates of the neighbour
				Coordinate c = peer.getCoordinate();
				// System.out.println("old c: " + c);
				c = myCoordinate.updateCoordinate(c);
				// System.out.println("new c: " + c);
				peer.setCoordinate(c);
				myNeighbour.c = c;

				// Update the neighbours to reflect the change in coordinates
				// and add my neighbours to the merged zone
				Neighbour tempNeighbour = new Neighbour();
				Registry newNeighbourRegistry;
				Iterator<Neighbour> iter = neighbours.iterator();
				while (iter.hasNext()) {
					tempNeighbour = iter.next();
					newNeighbourRegistry = LocateRegistry.getRegistry(
							tempNeighbour.ip, 21291);
					ClientInterface tempNode = (ClientInterface) newNeighbourRegistry
							.lookup("peer");
					if ((c.isAdjacentX(tempNeighbour.c) && c
							.isSubsetY(tempNeighbour.c))
							|| (c.isAdjacentY(tempNeighbour.c) && c
									.isSubsetX(tempNeighbour.c))) {
						tempNode.addNeighbour(myNeighbour);
						tempNode.deleteNeighbour(new Neighbour(myIPAddress,
								identifier, myCoordinate));
					} else {
						System.out.println("Removing neighbour "
								+ tempNeighbour.identifier);
						tempNode.deleteNeighbour(myNeighbour);
						tempNode.deleteNeighbour(new Neighbour(myIPAddress,
								identifier, myCoordinate));
					}
					// Remove neighbours from self
					iter.remove();
					neighbours.remove(tempNeighbour);
				}

				// Update the coordinates for myNeighbour's neighbours
				HashSet<Neighbour> myNeighbourNeighbours = peer.getNeighbours();
				iter = myNeighbourNeighbours.iterator();
				while (iter.hasNext()) {
					tempNeighbour = iter.next();
					newNeighbourRegistry = LocateRegistry.getRegistry(
							tempNeighbour.ip, 21291);
					ClientInterface tempNode = (ClientInterface) newNeighbourRegistry
							.lookup("peer");
					tempNode.updateNeighbour(myNeighbour);
				}

				// Export object to end RMI connection and reset join value
				UnicastRemoteObject.unexportObject(myPeer, true);
				joinFlag = false;
			} catch (RemoteException e) {
				System.out.println("Error in leave on " + myIPAddress);
				e.printStackTrace();
			} catch (NotBoundException e) {
				System.out.println("Error in leave on " + myIPAddress);
				e.printStackTrace();
			}
		} else {
			System.out.println("Failure");
		}
	}

	/**
	 * Stores a file from a particular node by figuring out it's coordinates
	 * 
	 * @param filename
	 *            of the file
	 */
	private void storeFile(String filename) {
		// System.out.println("Saving file on " + identifier);
		double x = CharAtOdd(filename), y = CharAtEven(filename);
		File f = new File(filename);

		// Checks if the file actually present
		if (f.exists() && !f.isDirectory()) {
			if (myCoordinate.contains(x, y)) {
				addFile(filename, myIPAddress);
			} else {
				try {
					routeFile(x, y, filename, myIPAddress);
				} catch (RemoteException e) {
					System.out.println("Error in saveFile on " + myIPAddress);
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("File does not exist, Failure");
		}
	}

	/**
	 * Search for a file
	 * 
	 * @param filename
	 *            name of the file
	 */
	private void searchFile(String filename) {
		double x = CharAtOdd(filename), y = CharAtEven(filename);
		String ip;
		try {
			ip = findFile(x, y, filename);
			if (ip != null && !ip.contains("Failure"))
				System.out.println("File found on " + ip);
			else
				System.out.println("File Not Found, Failure");

		} catch (RemoteException e) {
			System.out.println("Eror in searchFile on " + myIPAddress);
			e.printStackTrace();
		}
	}

	/**
	 * getClosestNeighbour finds the closest neighbour for given coordinates
	 * 
	 * @param x
	 * @param y
	 *            given set of coordinates
	 * @return a neighbour close to the input coordinates
	 */
	private Neighbour getClosestNeighbour(double x, double y) {
		Neighbour neighbouringPeer = new Neighbour();
		double distance = Double.MAX_VALUE;
		Iterator<Neighbour> iterator = neighbours.iterator();
		Neighbour temp = new Neighbour();
		while (iterator.hasNext()) {
			temp = iterator.next();
			if (temp.c.distance(x, y) <= distance) {
				distance = temp.c.distance(x, y);
				neighbouringPeer = temp;
			}
		}
		return neighbouringPeer;
	}

	/**
	 * display method displays a peer's information
	 * 
	 * @param ip
	 */
	private void display(String ip) {
		try {
			Registry connection = LocateRegistry.getRegistry(ip, 21291);
			ClientInterface peer = (ClientInterface) connection.lookup("peer");
			String s = "\nIP " + peer.getIPAddress() + "\n";
			if (peer.hasJoined())
				s += "Node is connected\n";
			s += "Coordinates: " + peer.getCoordinate();
			s += "Files list: \n";
			if (!files.isEmpty()) {
				Iterator<String> fileIterator = peer.getFileList().iterator();
				while (fileIterator.hasNext()) {
					String file = fileIterator.next();
					s += file + "\n";
				}
			} else {
				s += "Empty";
			}
			s += "\nNeighbour list: \n";
			if (!neighbours.isEmpty()) {
				Iterator<Neighbour> neighbourIterator = peer.getNeighbours()
						.iterator();
				while (neighbourIterator.hasNext()) {
					Neighbour n = neighbourIterator.next();
					s += n;
				}
			} else {
				s += "Empty";
			}
			System.out.println("Node info: " + s);
		} catch (NotBoundException e) {
			System.out.println("Error in display of " + ip);
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Error in display of " + ip);
			e.printStackTrace();
		}
	}

	/**
	 * getIPs collects the details for all the peers in the CAN system
	 * 
	 * @param id
	 *            id is the string to that represents the node to be displayed
	 *            or all nodes
	 */
	private void getIPs(String id) throws RemoteException {
		HashSet<Neighbour> globalPeers = new HashSet<Neighbour>();
		Queue<Neighbour> q = new LinkedList<Neighbour>();
		// Add self
		globalPeers.add(new Neighbour(myIPAddress, identifier, myCoordinate));
		for (Neighbour n : neighbours) {
			q.add(n);
			globalPeers.add(n);
		}
		Neighbour tempNeighbour = new Neighbour();
		// Add all the other nodes
		while (!q.isEmpty()) {
			tempNeighbour = q.remove();
			try {
				Registry connection = LocateRegistry.getRegistry(
						tempNeighbour.ip, 21291);
				ClientInterface peer = (ClientInterface) connection
						.lookup("peer");
				HashSet<Neighbour> current = peer.getNeighbours();
				for (Neighbour currentNeighbour : current) {
					if (!globalPeers.contains(tempNeighbour)) {
						q.add(currentNeighbour);
					}
				}
			} catch (NotBoundException e) {
				System.out.println("Error in getIPs of " + myIPAddress);
				e.printStackTrace();
			}
		}

		// Provide different views
		if (id.equals("view")) {
			for (Neighbour each : globalPeers) {
				display(each.ip);
			}
		} else {
			for (Neighbour each : globalPeers) {
				if (each.identifier.equals(id)) {
					display(each.ip);
					break;
				}
			}
		}
	}

	/**
	 * getCoordinate
	 * 
	 * @return my coordinates
	 */
	public Coordinate getCoordinate() throws RemoteException {
		// System.out.println("Getting coordinate on " + myIPAddress);
		return myCoordinate;
	}

	/**
	 * setCoordinate
	 * 
	 * @param c
	 *            coordinate to be set
	 */
	public void setCoordinate(Coordinate c) throws RemoteException {
		// System.out.println("Setting coordinate on " + myIPAddress);
		myCoordinate = c;
	}

	/**
	 * getIPAddress
	 * 
	 * @return string containing the IP address
	 */
	public String getIPAddress() throws RemoteException {
		// System.out.println("Getting ip on " + myIPAddress);
		return myIPAddress;
	}

	/**
	 * getIdentifier
	 * 
	 * @return string containing the IP address
	 */
	public String getIdentifier() throws RemoteException {
		// System.out.println("Getting identifier on " + identifier);
		return identifier;
	}

	/**
	 * getNeighbours
	 * 
	 * @return set of neighbours
	 */
	public HashSet<Neighbour> getNeighbours() throws RemoteException {
		// System.out.println("Getting neighbours on " + myIPAddress);
		return neighbours;
	}

	/**
	 * addNeighbour if not already present else update it
	 * 
	 * @param n
	 *            neighbour to be added
	 */
	public void addNeighbour(Neighbour n) throws RemoteException {
		System.out.println("Adding neighbour " + n.identifier + " on "
				+ identifier);
		if (neighbours.contains(n)) {
			System.out.println("Neighbour already present. Updating");
			updateNeighbour(n);
			return;
		}
		neighbours.add(n);
	}

	/**
	 * updateNeighbour updates the coordinates of given neighbour
	 * 
	 * @param n
	 *            neighbour to be updated
	 */
	public void updateNeighbour(Neighbour n) throws RemoteException {
		Iterator<Neighbour> iterator = neighbours.iterator();
		Neighbour temp = new Neighbour();
		while (iterator.hasNext()) {
			temp = iterator.next();
			if (temp.ip.equals(n.ip)) {
				// System.out.println("Found " + temp.ip);
				System.out.println("Update neighbour " + n.identifier + " on "
						+ identifier);
				temp.c = n.c;
				return;
			}
		}
	}

	/**
	 * deleteNeighbour deletes a neighbour
	 * 
	 * @param n
	 *            neighbour to be deleted
	 */
	public void deleteNeighbour(Neighbour n) throws RemoteException {
		neighbours.remove(n);
	}

	/**
	 * getFileList gets set of files
	 * 
	 * @return set of files
	 */
	public HashSet<String> getFileList() throws RemoteException {
		// System.out.println("Getting file list on " + myIPAddress);
		return files;
	}

	/**
	 * addFile adds the file to a node
	 * 
	 * @param filename
	 *            name of the file
	 * @param originatingIP
	 *            ip of the originating node
	 */
	public void addFile(String filename, String originatingIP) {
		if (originatingIP.equals(myIPAddress)) {
			files.add(filename);
			System.out.println("Adding file " + filename + " on " + identifier);
		} else {
			try {
				Registry fileRegistry = LocateRegistry.getRegistry(
						originatingIP, 21291);
				ClientInterface peer = (ClientInterface) fileRegistry
						.lookup("peer");
				byte[] filedata = peer.saveFile(filename);
				File file = new File(filename);
				BufferedOutputStream output = new BufferedOutputStream(
						new FileOutputStream(file.getName()));
				output.write(filedata, 0, filedata.length);
				output.flush();
				output.close();
				files.add(filename);
				System.out.println("Adding file " + filename + " on "
						+ identifier);
			} catch (Exception e) {
				System.err.println("Error in addFile on " + myIPAddress);
				e.printStackTrace();
			}
		}
	}

	/**
	 * saveFile gets the file from the originating ip
	 * 
	 * @param filename
	 *            name of the file
	 * @return byte array containing the contents of the file
	 */
	public byte[] saveFile(String fileName) {
		// System.out.println("Saving file " + fileName + " from " +
		// myIPAddress);
		try {
			File file = new File(fileName);
			byte buffer[] = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(fileName));
			input.read(buffer, 0, buffer.length);
			input.close();
			return (buffer);
		} catch (Exception e) {
			System.out.println("saveFile on + " + myIPAddress + ": "
					+ e.getMessage());
			e.printStackTrace();
			return (null);
		}
	}

	/**
	 * routeFile routes the file from current node to end node
	 * 
	 * @param x
	 * @param y
	 *            x and y are the coordinates for the file
	 * @param filename
	 *            name of the file
	 * @param originatingIP
	 *            ip of the originating node
	 */
	public void routeFile(double x, double y, String filename,
			String originatingIP) throws RemoteException {
		// System.out.println("Routing file on " + myIPAddress);
		if (myCoordinate.contains(x, y)) {
			addFile(filename, originatingIP);
		} else {
			Neighbour neighbouringClient = getClosestNeighbour(x, y);
			String neighbrIP = neighbouringClient.ip;
			try {
				Registry connection = LocateRegistry.getRegistry(neighbrIP,
						21291);
				ClientInterface peer = (ClientInterface) connection
						.lookup("peer");
				peer.routeFile(x, y, filename, originatingIP);
			} catch (NotBoundException e) {
				System.out.println("Error in routeFile on " + myIPAddress);
				e.printStackTrace();
			}
		}
	}

	/**
	 * findFile finds the file from current node to end node
	 * 
	 * @param x
	 * @param y
	 *            x and y are the coordinates for the file
	 * @param filename
	 *            name of the file
	 */
	public String findFile(double x, double y, String filename)
			throws RemoteException {
		// System.out.println("Searching for " + filename + " on " +
		// myIPAddress);
		if (myCoordinate.contains(x, y)) {
			if (files.contains(filename)) {
				return myIPAddress + " with route: " + identifier;
			} else
				return "Failure";
		} else {
			Neighbour neighbouringClient = getClosestNeighbour(x, y);
			String neighbrIP = neighbouringClient.ip;
			try {
				Registry connection = LocateRegistry.getRegistry(neighbrIP,
						21291);
				ClientInterface peer = (ClientInterface) connection
						.lookup("peer");
				return peer.findFile(x, y, filename) + "<--" + identifier;
			} catch (NotBoundException e) {
				System.out.println("Error in findFile on " + myIPAddress);
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * routeNode routes the node from current node to end node
	 * 
	 * @param x
	 * @param y
	 *            x and y are the coordinates for the file
	 * @param incomingIP
	 *            ip of the incoming node
	 */
	public String routeNode(double x, double y, String incomingIP)
			throws RemoteException {
		System.out.println("Incoming to route node " + incomingIP + " with x: "
				+ x + " y: " + y);
		if (myCoordinate.contains(x, y)) {
			splitCurrentSpace(incomingIP);
			return myIPAddress + " with route: " + identifier;
		} else {
			// System.out.println("Forwarding");
			Neighbour neighbouringPeer = getClosestNeighbour(x, y);
			try {
				Registry connection = LocateRegistry.getRegistry(
						neighbouringPeer.ip, 21291);
				ClientInterface peer = (ClientInterface) connection
						.lookup("peer");
				return peer.routeNode(x, y, incomingIP) + "<--" + identifier;
			} catch (NotBoundException e) {
				System.out.println("Error in routeNode on " + myIPAddress);
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * has joined
	 * 
	 * @return true if joined
	 */
	public boolean hasJoined() {
		return joinFlag;
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Client me = new Client();
			me.BSIPAddress = args[0];
			@SuppressWarnings("resource")
			Scanner sc = new Scanner(System.in);
			int choice;
			while (true) {
				System.out.println("1. Join CAN");
				System.out.println("2. Leave CAN");
				System.out.println("3. View Neighbours");
				System.out.println("4. View information of peer by ip");
				System.out.println("5. Insert file");
				System.out.println("6. Search for file");
				System.out.println("7. View files in current peer");
				System.out.println("8. Exit");
				choice = sc.nextInt();

				switch (choice) {
				case 1:
					System.out.println("Trying to join");
					if (!me.joinFlag) {
						me.join();
						System.out.println("Node info: " + me);
					} else {
						System.out.println("Already Connected to CAN");
					}
					break;
				case 2:
					if (me.joinFlag) {
						System.out.println("Leaving CAN");
						me.leave();
					} else {
						System.out.println("Can't leave without joining");
					}
					break;
				case 3:
					if (me.joinFlag) {
						System.out.println("Obtaining neighbours");
						Iterator<Neighbour> iterator = me.neighbours.iterator();
						Neighbour temp = new Neighbour();
						while (iterator.hasNext()) {
							temp = iterator.next();
							System.out.println(temp);
						}
					} else {
						System.out
								.println("Can't obtain neighbours without joining");
					}
					break;
				case 4:
					if (me.joinFlag) {
						System.out.println("Enter Node identifier: ");
						String id = sc.next();
						if (id.equals(me.identifier)) {
							System.out.println("Node info: " + me);
						} else {
							me.getIPs(id);
						}
					} else {
						System.out.println("Can't view nodes without joining");
					}
					break;
				case 5:
					if (me.joinFlag) {
						System.out.println("Enter filename to be inserted: ");
						me.storeFile(sc.next());
					} else {
						System.out.println("Can't add file without joining");
					}
					break;
				case 6:
					if (me.joinFlag) {
						System.out.println("Enter filename to be searched: ");
						me.searchFile(sc.next());
					} else {
						System.out
								.println("Can't search for file without joining");
					}
					break;
				case 7:
					if (me.joinFlag) {
						System.out.println("File list: ");
						Iterator<String> iterator = me.files.iterator();
						String temp = "";
						while (iterator.hasNext()) {
							temp = iterator.next();
							System.out.println(temp);
						}
					} else {
						System.out
								.println("Can't see file list without joining");
					}
					break;
				case 8:
					if (!me.joinFlag) {
						System.out.println("Exit");
						System.exit(0);
					} else {
						System.out.println("Can't exit without leaving");
					}
					break;
				default:
					System.out.println("Oops wrong choice, try again!");
					break;
				}
			}
		} catch (RemoteException e) {
			System.out.println("Remote Exception in Client");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host Exception in Client");
			e.printStackTrace();
		}
	}
}