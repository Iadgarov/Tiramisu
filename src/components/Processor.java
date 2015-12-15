package components;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Main processor class
 * @author David
 *
 */
public class Processor {
	
	
	// Constants:
	public final static int MEMORY_SIZE = 65536;
	
	
	// Attributes:
	private static Queue<Instruction> instructionQ;
	
	private static List<AddUnit> addUnits;
	public static int addUnitNumber;
	public static int addReservationStationNumber;
	public static int addUnitDelay;
	
	private static List<MultUnit> multUnits;
	public static int multUnitNumber;
	public static int multResevationStationNumber;
	public static int multUnitDelay;
	
	private static List<StoreBuffer> storeBuffers;
	public static int storeBufferNumber;
	
	private static List<LoadBuffer> loadBuffer;
	public static int loadBufferNumber;
	
	private static Register registers[] = new Register[16];
	
	private static int PC = 0; // start counter at 0

	/**
	 * 
	 * @param instructionQ	the program we want to run, broken up into indivigual instructions.
	 * @param addUnitNumber	number of ADD/SUB processing units
	 * @param multUnitNumber	number of MUL/DIV processing units
	 * @param storeBufferNumber	number of Store Buffers
	 * @param loadBufferNumber	number of Load Buffers
	 */
	public Processor(Queue<Instruction> instructionQ, int addUnitNumber, int addUnitDelay, int multUnitNumber,
			int multUnitDelay, int storeBufferNumber, int loadBufferNumber, int addReservationStationNumber,
			int multReservationStationNumber) {
		
		super();
		
		Processor.instructionQ = instructionQ;
		Processor.addUnitNumber = addUnitNumber;
		Processor.multUnitNumber = multUnitNumber;
		Processor.storeBufferNumber = storeBufferNumber;
		Processor.loadBufferNumber = loadBufferNumber;
		Processor.addReservationStationNumber = addReservationStationNumber;
		Processor.multResevationStationNumber = multReservationStationNumber;
		Processor.addUnitDelay = addUnitDelay;
		Processor.multUnitDelay = multUnitDelay;
		
		addUnits = createAddUnits();
		multUnits = createMulUnits();
		
		
	}

	/**
	 * This method initializes all the MUL/DIV units for the processor. 
	 * 
	 * @param multUnitNumber	number of units to construct
	 * @param multUnitDelay		number of cycles it takes for this unit to complete execution
	 * @return	ArrayList of MUL/DIV units
	 */
	private List<MultUnit> createMulUnits() {


		ArrayList<MultUnit> returnMe = new ArrayList<MultUnit>();
		for (int i = 0; i < Processor.addUnitNumber; i++){
			returnMe.add(new MultUnit());
		}
		
		return returnMe;
	}

	/**
	 * This method initializes all ADD/SUB units for the processor. 
	 * 
	 * @param addUnitNumber	number of units to construct
	 * @param addUnitDelay	number of cycles the units takes to finish execution
	 * @return ArrayList of ADD/SUB units
	 */
	private List<AddUnit> createAddUnits() {
		
		ArrayList<AddUnit> returnMe = new ArrayList<AddUnit>();
		for (int i = 0; i < Processor.addUnitNumber; i++){
			returnMe.add(new AddUnit());
		}
		
		return returnMe;
	}
	
	
	
}
