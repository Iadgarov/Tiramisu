package collections;

import java.util.ArrayList;
import java.util.List;

import components.AddUnit;
import components.InstructionQueue;
import components.Processor;
import components.ReservationStation;
import support.Instruction;

/**
 * Class for ADD/SUB unit collection belonging to the processor
 * @author David
 *
 *
 */
public class Adders {

	
	private static List<AddUnit> addUnits;
	public static int addUnitNumber;
	private static int reservationStationNumber;

	/**
	 * Constructor for ADD/SUB unit collection
	 * Initiate unit amount and creates the unit objects
	 * Initiate reservtion station creation for ADD/SUB units
	 * @param unitNumber	how many units?
	 */
	public Adders(int unitNumber){
		
		addUnitNumber = unitNumber;
		addUnits = createAddUnits();
		AddUnit.setReservationStations(createStations());
	}
	
	/**
	 * Checks if all ADD/SUB units are busy in this CC
	 * Uses freeUnitIndex method to do so (compares its result to -1)
	 * @return true if all units are busy
	 */
	public boolean isFullyBusy(){
		return (freeUnitIndex() == -1) ? true : false;
	}
	
	/**
	 * Go over all ADD/SUB units and return index of a free unit if it exists.
	 * @return index of free ADD/SUB unit or -1 is all are busy
	 */
	public static int freeUnitIndex(){
		
		for(int i =0; i< addUnitNumber; i++){
			if (!addUnits.get(i).isBusy(Processor.CC))
				return i;
		}
		return -1;
		
	}
	
	/**
	 * This method initializes all ADD/SUB units for the processor. 
	 * Called by the constructor
	 * 
	 * @param addUnitNumber	number of units to construct
	 * @param addUnitDelay	number of cycles the units takes to finish execution
	 * @return ArrayList of ADD/SUB units
	 */
	private List<AddUnit> createAddUnits() {
		
		ArrayList<AddUnit> returnMe = new ArrayList<AddUnit>();
		for (int i = 0; i < addUnitNumber; i++){
			returnMe.add(new AddUnit());
		}
		
		return returnMe;
	}
	
	/**
	 * construct reservation stations for all ADD/SUB units
	 * @return	reservation stations Object of correct size for ADD/SUB unit
	 */
	static ReservationStation createStations() {
		
		int temp = getReservationStationNumber();
		return new ReservationStation(temp, ReservationStation.ADD_REPOSITORY);
		
	}


	
	/**
	 * search reservation stations for commands that can begin execution
	 * search for available unit.
	 * If both found begin execution of the command in the unit
	 */
	public static void attemptPushToUnit() {
		
		// find instruction that is ready to be pushed
		int freeInstructionIndex;
		
		// so long s there are instructions willing to execute keep going (will quit if out of not busy units) 
		while ((freeInstructionIndex = AddUnit.getReservationStations().isReadyIndex()) != -1){	// index of reservation station that we want
			
			
			if (freeInstructionIndex == -1)
				return; // nothing to push, no commands are ready
			
			// find unit that is capable accepting new instruction for execution
			int freeUnitIndex = freeUnitIndex();
			if (freeUnitIndex == -1)
				return; // no free units available for execution work
			
			// We have an instruction and a unit willing to run it
			// Mark this CC as the CC that this command started execution
			Instruction inst = AddUnit.reservationStations.getInstructions()[freeInstructionIndex];
			if (inst.getThread() == Processor.THREAD_0){
				
				if (InstructionQueue.getIssueCC_0()[inst.getqLocation()] >= Processor.CC)
					return; // it was just issued, wait another cycle at least
				
				InstructionQueue.getExeCC_0()[inst.getqLocation()] = Processor.CC;
			}
			else if (inst.getThread() == Processor.THREAD_1){
				
				if (InstructionQueue.getIssueCC_1()[inst.getqLocation()] >= Processor.CC)
					return; // it was just issued, wait another cycle at least
				
				InstructionQueue.getExeCC_1()[inst.getqLocation()] = Processor.CC;
			}
			AddUnit.reservationStations.getInExecution()[freeInstructionIndex] = true;	// this instruction has started execution
			addUnits.get(freeUnitIndex).execute(freeInstructionIndex);
		}
		
		
		return;
	}

	public static int getReservationStationNumber() {
		return reservationStationNumber;
	}

	public static void setReservationStationNumber(int reservationStationNumber) {
		Adders.reservationStationNumber = reservationStationNumber;
	}
	
}
