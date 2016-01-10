package components;

import support.Instruction;
import support.Tag;

/**
 * Class responsible for memory related instructions, Store and Load.<br>
 * Represents the single MEM unit we have.
 * @author David
 *
 */
public class MemoryUnit {

	static int executionDelay;
	static int reservationStationNumberLOAD;		// load buffer amount
	static int reservationStationNumberSTORE;		// store buffer amounts
	private static boolean busy;
	private static int exeStart; // when execution started so we can free unit when done
	private static ReservationStation reservationStationsLOAD;
	private static ReservationStation reservationStationsSTORE;
	
	/**
	 * construct a MEM unit<br>
	 * initiate its reservation stations (buffers) creation
	 */
	public MemoryUnit() {
		
		busy = false;
		MemoryUnit.setReservationStations(createStations(Instruction.LD), Instruction.LD);
		MemoryUnit.setReservationStations(createStations(Instruction.ST), Instruction.ST);

	}

	/**
	 * construct reservation stations for all STORE units
	 * @param type tells us if we want a load buffer or a store buffer
	 * @return	reservation stations Object of correct size for STORE unit
	 */
	private ReservationStation createStations(int type) {
		
		
		int temp = (type == Instruction.ST) ? reservationStationNumberSTORE : reservationStationNumberLOAD;
		return new ReservationStation(temp, 
				(type == Instruction.ST) ? ReservationStation.STORE_REPOSITORY : ReservationStation.LOAD_REPOSITORY);
		
	}

	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and writing to CDB<br>
	 * For a STORE command the saving to memory happens in the CDB class to simulate the unit delay and save the
	 * commands completion CC.
	 * @param type LOAD or STORE?
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 */
	public static void execute(int stationNumber, int type) {
		
		System.out.println("Thread = "+ getReservationStations(type).thread[stationNumber] +
               " [CC = " + Processor.CC + "] Exe start for  " + getReservationStations(type).getInstructions()[stationNumber].toString() );

		
		exeStart = Processor.CC;
		
		int thread = getReservationStations(type).getInstructions()[stationNumber].getThread(); // thread this command belongs to
		int command = getReservationStations(type).opCode[stationNumber];
		//int dst = getReservationStations(type).getInstructions()[stationNumber].getDst();
		
		
		busy = true; // this unit will be working for these CC's
		
		if (command != type){
			System.out.println("Something went wrong, type and opcode are not the same! EXITING!");
			System.exit(0);
		}
			
		
		float result = 0;

		
		if (command == Instruction.LD){
			
		
			result = Processor.memory.get(getReservationStations(type).immediate[stationNumber]);
			//write result to CDB, along with who calculated it
			CDB.writeToCDB( result, new Tag (ReservationStation.LOAD_REPOSITORY, 
					stationNumber, thread, getReservationStations(type).getInstructions()[stationNumber]),
					Processor.CC + executionDelay);
			
		}
		
		else if (command == Instruction.ST){
		
			/*
			if (thread == Processor.THREAD_0)
				result = Processor.registers_0.getRegisters()[dst];
			else if (thread == Processor.THREAD_1)
				result = Processor.registers_1.getRegisters()[dst];
			*/
			
			result = getReservationStations(type).Vk[stationNumber];
			
			//write result to CDB, along with who calculated it and address since this is a LOAD command
			Tag tag =  new Tag (ReservationStation.STORE_REPOSITORY, 
					stationNumber, thread, getReservationStations(type).getInstructions()[stationNumber]);
		
			int address = getReservationStations(type).immediate[stationNumber];
			CDB.writeToCDB( result, tag, Processor.CC + executionDelay, address);

		}
		else {
				System.out.println("NONE MEMORY COMMAND SENT TO STORE UNIT!! : " 
						+ getReservationStations(type).getInstructions()[stationNumber] + "EXITING");
				System.exit(0);
			}
		
	}
	
	/**
	 * attempt to accept new command into the reservation station. If there is room we will succeed. 
	 * @param inst the instruction we want to our station
	 * @param thread the thread the instruction belongs to
	 * @return True if successful, else false
	 */
	public static boolean acceptIntoStation(Instruction inst, int thread){
		
		if(!getReservationStations(inst.getOpCode()).isFull()){
			
			getReservationStations(inst.getOpCode()).addInstruction(inst, thread);
			return true;
		}
		
		return false; // if you got here then there was no room in the station or I F****d up in some other way
	}
	
	/**
	 * is this unit busy during this CC?
	 * @return true if station is working, else false
	 */
	public static boolean isBusy(){
		return busy;
	}
	
	/**
	 * search reservation stations for commands that can begin execution
	 * @param type LOAD or STORE?
	 */
	public static void attemptPushToUnit( int type) {
		
		// find instruction that is ready to be pushed
		
		ReservationStation rs =  getReservationStations(type);
		
		int freeInstructionIndex;	// index of reservation station that we want
		
		while ((freeInstructionIndex = rs.isReadyIndex()) != -1){
				
			Instruction inst = getReservationStations(type).getInstructions()[freeInstructionIndex];
			
			if (freeInstructionIndex == -1)
				return; // nothing to push, no commands are ready
			
			// test to see if this memory command is dependent on previous ones.
			if (type == Instruction.LD){
				if (isAddressInUse(Instruction.ST, inst.getqLocation(), inst.getImmidiate()))
					return;
			}
			else if (type == Instruction.ST){
				if (isAddressInUse(Instruction.ST, inst.getqLocation(), inst.getImmidiate()) &&
						isAddressInUse(Instruction.LD, inst.getqLocation(), inst.getImmidiate()))
					return;
			}
				
		
			if (isBusy())
				return; // no free units available for execution work
			
			// We have an instruction and a unit willing to run it
			// Mark this CC as the CC that this command started execution
			
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
			
			getReservationStations(inst.getOpCode()).getInExecution()[freeInstructionIndex] = true;	// this instruction has started execution
			execute(freeInstructionIndex, inst.getOpCode());	
		}
	}
	
	
	/**
	 * Goes over the buffer (load or store, based on type) and checks if there is a command using the address
	 * that has also been issued before our own command.
	 * @param type the type of buffer we wants to check (Load or Store)
	 * @param myInstructionQueueLocation the location of the calling command in the instruction Queue. 
	 * Only a command that uses the same address and shows up earlier than us will cause a true return value.
	 * @address the address in question.
	 * @return True if there exists a command in the buffer that needs said address before the calling command does, else false;
	 */
	private static boolean isAddressInUse(int type, int myInstructionQueueLocation, int address){
		
		ReservationStation rs = getReservationStations(type);
		// go over station spots that are full and compare addresses
		for (int i = 0; i < rs.immediate.length; i++){
			
			if (rs.getInstructions()[i] == null)
				continue;
			
			if (rs.immediate[i] == address)
				if (rs.getInstructions()[i].getqLocation() < myInstructionQueueLocation)
					return true;
			
					
		}
		return false;
	}

	public static ReservationStation getReservationStations(int type) {
		return (type == Instruction.ST) ? reservationStationsSTORE : reservationStationsLOAD;
	}

	public static void setReservationStations(ReservationStation reservationStations, int type) {
		if (type == Instruction.ST)
			MemoryUnit.reservationStationsSTORE = reservationStations;
		else
			MemoryUnit.reservationStationsLOAD = reservationStations;
		
	}

	public static int getExeStart() {
		return exeStart;
	}

	public static void setBusy(boolean b) {
		busy = b;
	}

	public static int getExecutionDelay() {
		
		return executionDelay;
	}
}
