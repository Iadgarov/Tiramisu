package components;

public class LoadUnit {
	
	static int executionDelay;
	static int reservationStationNumber;
	private static boolean busy[];
	static ReservationStation reservationStations;
	
	/**
	 * construct a LOAD unit
	 * Only one should be constructed. No support for more currently 
	 */
	public LoadUnit() {
		
		busy = new boolean[Processor.MAX_MEMORY_SIZE];
		for (int i = 0; i < Processor.MAX_MEMORY_SIZE; i++)
			busy[i] = false; // all stations start as available at first
		LoadUnit.reservationStations = createStations();
		
	}

	/**
	 * construct reservation stations for all LOAD units
	 * @return	reservation stations Object of correct size for LOAD unit
	 */
	private ReservationStation createStations() {
		
		int temp = reservationStationNumber;
		return new ReservationStation(temp, ReservationStation.LOAD_REPOSITORY);
		
	}

	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and writing to CDB
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 */
	public static void execute(int stationNumber) {
		
		int thread = reservationStations.instructions[stationNumber].getThread(); // thread this command belongs to
		int command = reservationStations.opCode[stationNumber];

		
		for (int i = 0; i < executionDelay; i++)
			busy[Processor.CC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (command == Instruction.LD)
			result = Processor.memory.get(reservationStations.immediate[stationNumber]);
		else {
			System.out.println("NONE LOAD COMMAND SENT TO LOAD UNIT!! EXITING");
			System.exit(0);
		}
		
		//write result to CDB, along with who calculated it
		CDB.writeToCDB( result, new Tag (ReservationStation.LOAD_REPOSITORY, 
				stationNumber, thread, reservationStations.instructions[stationNumber]),
				Processor.CC + executionDelay);
	}
	
	/**
	 * attempt to accept new command into the reservation station
	 * @param inst the instruction we want to our station
	 * @param thread the thread the instruction belongs to
	 * @return True if successful, else false
	 */
	public static boolean acceptIntoStation(Instruction inst, int thread){
		
		if(!reservationStations.isFull()){
			
			reservationStations.addInstruction(inst, thread);
			return true;
		}
		
		return false; // if you got here then there was no room in the station or you F'd up in some other way
	}
	
	/**
	 * is this unit busy?
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
		int freeInstructionIndex;	// index of reservation station that we want
		
		while ((freeInstructionIndex = reservationStations.isReadyIndex()) != -1){
			
			if (freeInstructionIndex == -1)
				return; // nothing to push, no commands are ready
			
	
		
			if (isBusy(Processor.CC))
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
			
			reservationStations.inExecution[freeInstructionIndex] = true;	// this instruction has started execution
			execute(freeInstructionIndex);	
		}
	}

}
