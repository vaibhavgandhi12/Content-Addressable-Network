import java.io.Serializable;

/**
 * Class Neighbour is a model class that stores a neighbour for a peer
 * 
 * @author vaibhavgandhi
 *
 */
public class Neighbour implements Serializable {

	/**
	 * Global shared variables
	 */
	Coordinate c;
	String ip;
	String identifier;

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public Neighbour() {

	}

	/**
	 * Constructor
	 * 
	 * @param ip
	 * @param c
	 */
	public Neighbour(String ip, Coordinate c) {
		this.ip = ip;
		this.c = c;
	}

	/**
	 * Constructor
	 * 
	 * @param ip
	 * @param identifier
	 * @param c
	 *
	 */
	public Neighbour(String ip, String identifier, Coordinate c) {
		this.ip = ip;
		this.identifier = identifier;
		this.c = c;
	}

	@Override
	public String toString() {
		return ("IP: " + ip + "\nIdentifier: " + identifier + "\nCoordinate: " + c);
	}

	@Override
	public boolean equals(Object n) {
		if (n instanceof Neighbour)
			return (ip.equals(((Neighbour) n).ip));
		return false;
	}

	@Override
	public int hashCode() {
		return ip.hashCode();
	}

}