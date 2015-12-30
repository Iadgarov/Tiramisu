package components;


import java.util.Queue;

/**
 * Main processor class
 * @author David
 *
 */
public class Processor {
	
	
	// Constants:
	public final static int MEMORY_SIZE = 65536;
	public final static int THREAD_0	= 0;
	public final static int THREAD_1	= 1;
	
	// Attributes:
	public static InstructionQueue instructionQ;	// instructions
	
	public static Adders addUnits;
	
	public static Multers multUnits;
	
	public static LoadUnit loader;
	
	public static StoreUnit storer;

	
	public static RegisterCollection registers_0 = new RegisterCollection(THREAD_0);
	public static RegisterCollection registers_1 = new RegisterCollection(THREAD_1);
	
	public static int CC = 0; // start counter at 0
	
	// The main memory
	public static float[] memory;

	/**
	 * Constructor for processor class
	 * @param instructionQ_0	Instructions for first thread
	 * @param instructionQ_1	Instructions for second thread
	 * @param addUnitNumber		Number of ADD/SUB units
	 * @param addUnitDelay		ADD/SUB unit calculation time in CC's
	 * @param multUnitNumber	Number of MULT/DIV units
	 * @param multUnitDelay		MULT/DIV unit calculation time in CC's
	 * @param storeBufferNumber	Number of store buffers
	 * @param loadBufferNumber	Number of load buffers
	 * @param addReservationStationNumber	Number of reservation stations for ADD/SUB units
	 * @param multReservationStationNumber	Number of reservation stations for MULT/DIV units
	 */
	public Processor(float[] memory, Queue<Instruction> instructionQ_0, Queue<Instruction> instructionQ_1, 
			int addUnitNumber, int addUnitDelay,int multUnitNumber, 
			int multUnitDelay, int storeBufferNumber, int loadBufferNumber, 
			int addReservationStationNumber, int multReservationStationNumber) {
		
		//super();
		
		Processor.memory = memory;
		Processor.instructionQ = new InstructionQueue( instructionQ_0, instructionQ_1);

		LoadUnit.reservationStationNumber = storeBufferNumber;
		StoreUnit.reservationStationNumber = loadBufferNumber;
		
		loader = new LoadUnit();
		storer = new StoreUnit();

		
		AddUnit.executionDelay = addUnitDelay;
		AddUnit.reservationStationNumber = addReservationStationNumber;
		addUnits = new Adders(addUnitNumber);
		
		MultUnit.executionDelay = multUnitDelay;
		MultUnit.reservationStationNumber = multReservationStationNumber;
		multUnits = new Multers(multUnitNumber);
		
		
	}
	
}
