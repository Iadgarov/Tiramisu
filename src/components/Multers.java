package components;

import java.util.ArrayList;
import java.util.List;

public class Multers {

	private static List<MultUnit> multUnits;
	public static int multUnitNumber;

	/**
	 * Construct the list of MULT/DIV units. 
	 * @param multUnitNumber	number of said units
	 */
	public Multers(int multUnitNumber){
		
		Multers.multUnitNumber = multUnitNumber;
		multUnits = createMulUnits();
	}
	
	
	/**
	 * Checks if all MULT/DIV units are busy
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
			if (!multUnits.get(i).isBusy(Processor.CC))
				return i;
		}
		return -1;
		
	}
	

	/**
	 * search reservation stations for commands that can begin execution
	 */
	public static void attemptPushToUnit() {
		
		// find instruction that is ready to be pushed
		int freeInstructionIndex = AddUnit.reservationStations.isReadyIndex();	// index of reservation station that we want
		if (freeInstructionIndex == -1)
			return; // nothing to push, no commands are ready
		
		// find unit that is capable accepting new instruction for execution
		int freeUnitIndex = freeUnitIndex();
		if (freeUnitIndex == -1)
			return; // no free units available for execution work
		
		// We have an instruction and a unit willing to run it
		// Mark this CC as the CC that this command started execution
		Instruction inst = AddUnit.reservationStations.instructions[freeInstructionIndex];
		if (inst.getThread() == Processor.THREAD_0){
			
			if (InstructionQueue.issueCC_0[inst.getqLocation()] >= Processor.CC)
				return; // it was just issued, wait another cycle at least
			
			InstructionQueue.exeCC_0[inst.getqLocation()] = Processor.CC;
		}	
		else if (inst.getThread() == Processor.THREAD_1){
			
			if (InstructionQueue.issueCC_1[inst.getqLocation()] >= Processor.CC)
				return; // it was just issued, wait another cycle at least
			
			InstructionQueue.exeCC_1[inst.getqLocation()] = Processor.CC;
		}
		multUnits.get(freeUnitIndex).execute(freeInstructionIndex);

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
		for (int i = 0; i < multUnitNumber; i++){
			returnMe.add(new MultUnit());
		}
		
		return returnMe;
	}

}
