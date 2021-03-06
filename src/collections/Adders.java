package collections;

import java.util.ArrayList;
import java.util.List;

import components.AddUnit;
import components.InstructionQueue;
import components.Processor;
import components.ReservationStation;
import support.Instruction;

/**
 * Class for ADD/SUB unit collection belonging to the processor.<br>
 * Will hold a list of individual ADD/SUB calculation units. 
 * 
 * @author David
 *
 *
 */
public class Adders {

	
	private static List<AddUnit> addUnits;	// list of ADD/SUB units
	public static int addUnitNumber;
	private static int reservationStationNumber;

	/**
	 * Constructor for ADD/SUB unit collection<br>
	 * Initiate unit amount and creates the unit objects<br>
	 * Initiate reservation station creation for ADD/SUB units, same stations for all the units<br>
	 * @param unitNumber	how many units?
	 */
	public Adders(int unitNumber){
		
		addUnitNumber = unitNumber;
		setAddUnits(createAddUnits());
		AddUnit.setReservationStations(createStations());
	}
	
	/**
	 * Checks if all ADD/SUB units are busy in this CC<br>
	 * Uses freeUnitIndex method to do so (compares its result to -1)<br>
	 * @return true if all units are busy
	 */
	public boolean isFullyBusy(){
		return (freeUnitIndex() == -1) ? true : false;
	}
	
	/**
	 * Go over all ADD/SUB units and return index of a free ( = not currently calculating) unit if it exists.<br>
	 * @return index of free ADD/SUB unit or -1 is all are busy
	 */
	public static int freeUnitIndex(){
		
		for(int i =0; i< addUnitNumber; i++){
			if (!getAddUnits().get(i).isBusy())
				return i;
		}
		return -1;
		
	}
	
	/**
	 * This method initializes all ADD/SUB units for the processor. <br>
	 * Called by the constructor.
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
	 * construct reservation stations for all ADD/SUB units.
	 * Calls the ReservationStation class constructor a number of times. 
	 * @return	reservation stations Object of correct size for ADD/SUB unit
	 */
	static ReservationStation createStations() {
		
		int temp = getReservationStationNumber();
		return new ReservationStation(temp, ReservationStation.ADD_REPOSITORY);
		
	}


	
	/**
	 * search reservation stations for commands that can begin execution<br>
	 * search for available unit.<br>
	 * If both found begin execution of the command in the execution unit
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
			getAddUnits().get(freeUnitIndex).execute(freeInstructionIndex);
		}
		
		
		return;
	}

	/**
	 * Gets the number of reservation stations for ADD/SUB commands
	 * @return the number of reservation stations for ADD/SUB commands
	 */
	public static int getReservationStationNumber() {
		return reservationStationNumber;
	}

	/**
	 * Sets the number of reservation stations for ADD/SUB commands
	 * @param reservationStationNumber number of reservation stations for ADD/SUB commands
	 */
	public static void setReservationStationNumber(int reservationStationNumber) {
		Adders.reservationStationNumber = reservationStationNumber;
	}
	
	/**
	 * Gets the list of ADD/SUB execution units
	 * @return the list of ADD/SUB execution units
	 */
	public static List<AddUnit> getAddUnits() {
		return addUnits;
	}

	/**
	 * Sets the list of ADD/SUB units for our processor. A list of objects of the class AddUnit.
	 * @param addUnits A list of objects of the class AddUnit.
	 */
	public static void setAddUnits(List<AddUnit> addUnits) {
		Adders.addUnits = addUnits;
	}
	
}
