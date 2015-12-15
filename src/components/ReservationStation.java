package components;

/**
 * Reservation station class
 * @author David
 *
 */
public class ReservationStation {
	
	// consts to represent type of command
	private final static int ADD = 0;
	private final static int SUB = 1;
	private final static int MUL = 2;
	private final static int DIV = 3;
	
	// tags representing which reservation station we want
	private final static int ADD_REPOSITORY = 1; 
	private final static int MUL_REPOSITORY = -1;

	private int size;
	private Instruction[] instructions;
	// INSERT FUNCTION TO SET ALL SPOTS TO EMPTY
	private int opCode[];
	private float Vj[], Vk[];
	private Tag Qj[], Qk[];
	
	
	/**
	 * Constructor for a reservation station
	 * @param size number of lines/spots in the station
	 */
	public ReservationStation(int size){
		this.size = size;
		this.instructions = new Instruction[size];
		this.opCode = new int[size];
		this.Vj = new float[size];
		this.Vk = new float[size];
		this.Qj = new Tag[size];
		this.Qk = new Tag[size];
	}
	
	
	/**
	 * Add new instruction to station.
	 * Should only be called if there is room in the station
	 * Empty spot has an EMPTY instruction
	 * @param inst the new instruction object to be injected into the sation
	 */
	public void addInstruction (Instruction inst){
		// FILL IN
	}
	
	public boolean isFull (){
		
		int temp = emptySpotIndex();
		return ( temp == -1) ? true : false;
	}
	
	public int emptySpotIndex(){
		//INTERATE OVER INTRUCTIONS
		//RETURN INDEX OF EMPTY INSTRUCTION
		//IF NO EMPTY INSTRUCTION RETURN -1
		return 0;
	}
	
	
	/**
	 * Tag class, append this to data that is written to CDB so we know where it came from
	 * (1,j) = ADD/SUB reservation station line j
	 * (-1,j) = MUL/FIV reservation station line j
	 * @author David
	 *
	 */
	private class Tag {
		
		private int station;
		private int stationLine;
		
		public int getStation() {
			return station;
		}
		
		public void setStation(int station) {
			this.station = station;
		}
		
		public int getStationLine() {
			return stationLine;
		}
		
		public void setStationLine(int stationLine) {
			this.stationLine = stationLine;
		}
		
		
	}
}
