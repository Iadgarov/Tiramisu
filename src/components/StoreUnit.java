package components;

import support.Instruction;
import support.Tag;

public class StoreUnit {
	
	static int executionDelay;
	static int reservationStationNumber;
	private static boolean busy[];
	private static ReservationStation reservationStations;
	
	/**
	 * construct a LOAD unit<br>
	 * initiate its reservation station (buffer) creation
	 */
	public StoreUnit() {
		
		busy = new boolean[Processor.MAX_MEMORY_SIZE];
		for (int i = 0; i < Processor.MAX_MEMORY_SIZE; i++)
			busy[i] = false; // all stations start as available at first
		StoreUnit.setReservationStations(createStations());
		
	}

	/**
	 * construct reservation stations for all STORE units
	 * @return	reservation stations Object of correct size for STORE unit
	 */
	private ReservationStation createStations() {
		
		int temp = reservationStationNumber;
		return new ReservationStation(temp, ReservationStation.STORE_REPOSITORY);
		
	}

	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and writing to CDB
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 */
	public static void execute(int stationNumber) {
		
		int thread = getReservationStations().getInstructions()[stationNumber].getThread(); // thread this command belongs to
		int command = getReservationStations().opCode[stationNumber];
		int dst = getReservationStations().getInstructions()[stationNumber].getDst();
		
		for (int i = 0; i < executionDelay; i++)
			busy[Processor.CC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (thread == Processor.THREAD_0)
			if (command == Instruction.ST)
				result = Processor.registers_0.getRegisters()[dst];
			else {
				System.out.println("NONE STORE COMMAND SENT TO STORE UNIT!! : " 
						+ getReservationStations().getInstructions()[stationNumber] + "EXITING");
				System.exit(0);
			}
		
		
		//write result to CDB, along with who calculated it and address since this is a LOAD command
		Tag tag =  new Tag (ReservationStation.STORE_REPOSITORY, 
				stationNumber, thread, getReservationStations().getInstructions()[stationNumber]);
		CDB.writeToCDB( result, tag, Processor.CC + executionDelay, getReservationStations().immediate[stationNumber]);


	}
	
	/**
	 * attempt to accept new command into the reservation station. If there is room we will succeed. 
	 * @param inst the instruction we want to our station
	 * @param thread the thread the instruction belongs to
	 * @return True if successful, else false
	 */
	public static boolean acceptIntoStation(Instruction inst, int thread){
		
		if(!getReservationStations().isFull()){
			
			getReservationStations().addInstruction(inst, thread);
			return true;
		}
		
		return false; // if you got here then there was no room in the station or I F****d up in some other way
	}
	
	/**
	 * is this unit busy during this CC?
	 * @param now	the CC we are checking
	 * @return true if station is working, else false
	 */
	public static boolean isBusy(int now){
		return busy[now];
	}
	
	/**
	 * search reservation stations for commands that can begin execution
	 */
	public static void attemptPushToUnit() {
		
		// find instruction that is ready to be pushed
		
		//ReservationStation t = reservationStations;
		
		int freeInstructionIndex;;	// index of reservation station that we want
		
		while ((freeInstructionIndex = getReservationStations().isReadyIndex()) != -1){
				
			
			if (freeInstructionIndex == -1)
				return; // nothing to push, no commands are ready
			
	
		
			if (isBusy(Processor.CC))
				return; // no free units available for execution work
			
			// We have an instruction and a unit willing to run it
			// Mark this CC as the CC that this command started execution
			Instruction inst = getReservationStations().getInstructions()[freeInstructionIndex];
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
			
			getReservationStations().getInExecution()[freeInstructionIndex] = true;	// this instruction has started execution
			execute(freeInstructionIndex);	
		}
	}

	public static ReservationStation getReservationStations() {
		return reservationStations;
	}

	public static void setReservationStations(ReservationStation reservationStations) {
		StoreUnit.reservationStations = reservationStations;
	}

}
