package collections;

import java.util.ArrayList;
import java.util.List;

import components.InstructionQueue;
import components.MultUnit;
import components.Processor;
import components.ReservationStation;
import support.Instruction;

/**
 * Class responsible for creating collection of MULt/DIV units for the processor.
 * @author David
 *
 */
public class Multers {

	private static List<MultUnit> multUnits;	// list of MULT/DIV execution units
	public static int multUnitNumber;
	private static int reservationStationNumber;

	/**
	 * Class constructor<br>
	 * Set MULT/DIV unit amount<br>
	 * Construct the list of MULT/DIV units. <br>
	 * initiate reservation station construction for units<br>
	 * @param multUnitNumber	number of MULT/DIV units
	 */
	public Multers(int multUnitNumber){
		
		Multers.multUnitNumber = multUnitNumber;
		setMultUnits(createMulUnits());
		MultUnit.setReservationStations(createStations());

	}
	
	/**
	 * construct reservation stations for all MUL/DIV units, same stations for all units.<br>
	 * Calls ReservationStation class constructor a number of times. 
	 * @return list of reservation stations for MUL/DIV unit
	 */
	private ReservationStation createStations() {
		
		int temp = getReservationStationNumber();
		return new ReservationStation(temp, ReservationStation.MUL_REPOSITORY);
	}
	
	
	/**
	 * Checks if all MULT/DIV units are busy<br>
	 * Uses freeUnitIndex method to do so (compares its result to -1)<br>
	 * @return true if all units are busy
	 */
	public boolean isFullyBusy(){
		return (freeUnitIndex() == -1) ? true : false;
	}
	
	/**
	 * Go over all MULT/DIV units and return index of a free unit if it exists.
	 * @return index of free MULT/DIV unit or -1 is all are busy
	 */
	public static int freeUnitIndex(){
		
		for(int i =0; i< multUnitNumber; i++){
			if (!getMultUnits().get(i).isBusy())
				return i;
		}
		return -1;
		
	}
	

	/**
	 * search reservation stations for commands that can begin execution<br>
	 * search for unit that can accept command (not busy)<br>
	 * start execution of command in the unit if both are ready 
	 */
	public static void attemptPushToUnit() {
		
		// find instruction that is ready to be pushed
		int freeInstructionIndex;	// index of reservation station that we want
		
		while ((freeInstructionIndex = MultUnit.getReservationStations().isReadyIndex()) != -1){
			
			if (freeInstructionIndex == -1)
				return; // nothing to push, no commands are ready
			
			// find unit that is capable accepting new instruction for execution
			int freeUnitIndex = freeUnitIndex();
			if (freeUnitIndex == -1)
				return; // no free units available for execution work
			
			// We have an instruction and a unit willing to run it
			// Mark this CC as the CC that this command started execution
			Instruction inst = MultUnit.reservationStations.getInstructions()[freeInstructionIndex];
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
			
			MultUnit.reservationStations.getInExecution()[freeInstructionIndex] = true;	// this instruction has started execution
			getMultUnits().get(freeUnitIndex).execute(freeInstructionIndex);
		}
		return;
	}
	
	
	/**
	 * This method initializes all the MUL/DIV units for the processor. 
	 * Calls MultUnit constructor a number of times. 
	 * @param multUnitNumber	number of units to construct
	 * @param multUnitDelay		number of cycles it takes for this unit to complete execution
	 * @return	ArrayList of MUL/DIV units
	 */
	private List<MultUnit> createMulUnits() {


		ArrayList<MultUnit> returnMe = new ArrayList<MultUnit>();
		for (int i = 0; i < multUnitNumber; i++){
			returnMe.add(new MultUnit());
		}
		
		return returnMe;
	}

	/**
	 * Get's amount of reservation stations for MULT/DIV execution units
	 * @return amount of reservation stations for MULT/DIV execution units
	 */
	public static int getReservationStationNumber() {
		return reservationStationNumber;
	}

	/**
	 * amount of reservation stations for MULT/DIV execution units
	 * @param reservationStationNumber number of reservation stations for MULT/DIV units
	 */
	public static void setReservationStationNumber(int reservationStationNumber) {
		Multers.reservationStationNumber = reservationStationNumber;
	}

	/**
	 * gets list of all MULT/DIV execution units
	 * @return list of all MULT/DIV execution units
	 */
	public static List<MultUnit> getMultUnits() {
		return multUnits;
	}

	/**
	 * Set list of MULT/DIV execution units
	 * @param multUnits list of MULT/DIV execution units
	 */
	public static void setMultUnits(List<MultUnit> multUnits) {
		Multers.multUnits = multUnits;
	}

}
