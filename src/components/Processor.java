package components;


import java.util.ArrayList;
import java.util.Queue;

import collections.Adders;
import collections.Multers;
import collections.RegisterCollection;
import main.Sim;
import support.Instruction;

/**
 * Main processor class. Holds many variables and not much more.<br>
 * Honestly don't really need it.. but hey why not?
 * @author David
 *
 */
public class Processor {
	
	
	// Constants:
	public final static int MAX_MEMORY_SIZE = 65536;
	public final static int THREAD_0	= 0;
	public final static int THREAD_1	= 1;
	
	// Attributes:
	public static int InstAmount;
	
	public static InstructionQueue instructionQ;	// instructions
	
	public static Adders addUnits;
	
	public static Multers multUnits;
	
	public static MemoryUnit memory_er;

	
	public static RegisterCollection registers_0 = new RegisterCollection(THREAD_0);
	public static RegisterCollection registers_1 = new RegisterCollection(THREAD_1);
	
	public static int CC = 0; // start counter at 0
	
	// The main memory
	public static ArrayList<Float> memory;

	/**
	 * Constructor for processor class. 
	 * This is a glorified constant holder
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
	 * @param memDelay	The amount of CC's it takes to do a memory action (LOAD/STORE)
	 */
	public Processor( Queue<Instruction> instructionQ_0, Queue<Instruction> instructionQ_1, 
			int addUnitNumber, int addUnitDelay,int multUnitNumber, 
			int multUnitDelay, int storeBufferNumber, int loadBufferNumber, 
			int addReservationStationNumber, int multReservationStationNumber, int memDelay) {
		
		//super();
		
		Processor.memory = Sim.getMemory();
		Processor.instructionQ = new InstructionQueue( instructionQ_0, instructionQ_1);

		MemoryUnit.reservationStationNumberSTORE = storeBufferNumber;
		MemoryUnit.reservationStationNumberLOAD = loadBufferNumber;
		MemoryUnit.executionDelay = memDelay;
		memory_er = new MemoryUnit();
		
		
		AddUnit.setExecutionDelay(addUnitDelay);
		Adders.setReservationStationNumber(addReservationStationNumber);
		addUnits = new Adders(addUnitNumber);
		
		MultUnit.executionDelay = multUnitDelay;
		Multers.setReservationStationNumber(multReservationStationNumber);
		multUnits = new Multers(multUnitNumber);
		
		
	}
	
}
