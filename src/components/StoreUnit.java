package components;

public class StoreUnit {
	
	static int executionDelay;
	static int reservationStationNumber;
	private static boolean busy[];
	static ReservationStation reservationStations;
	
	/**
	 * construct a LOAD unit
	 */
	public StoreUnit() {
		
		this.busy = new boolean[Processor.MEMORY_SIZE];
		for (int i = 0; i < Processor.MEMORY_SIZE; i++)
			this.busy[i] = false; // all stations start as available at first
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
		int dst = reservationStations.instructions[stationNumber].getDst();
		
		for (int i = 0; i < executionDelay; i++)
			busy[Processor.CC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (thread == Processor.THREAD_0)
			if (command == Instruction.LD)
				result = Processor.registers_0.getRegisters()[dst];
			else {
				System.out.println("NONE LOAD COMMAND SENT TO LOAD UNIT!! EXITING");
				System.exit(0);
			}
		
		
		//write result to CDB, along with who calculated it
		CDB.writeToCDB( result, null, Processor.CC + executionDelay, reservationStations.immediate[stationNumber]);
		
		
		// remove instruction from the station, it has been taken care of
		reservationStations.opCode[stationNumber] = Instruction.EMPTY;
		reservationStations.Vj[stationNumber] = (Float)null;
		reservationStations.Vk[stationNumber] = (Float)null;
		

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
		int freeInstructionIndex = reservationStations.isReadyIndex();	// index of reservation station that we want
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
		execute(freeInstructionIndex);	
	}

}
