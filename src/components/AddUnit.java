package components;


public class AddUnit{
	
	static int executionDelay;
	static int reservationStationNumber;
	private boolean busy[];
	static ReservationStation reservationStations;
	
	/**
	 * construct a ADD/SUB unit
	 */
	public AddUnit() {
		
		this.busy = new boolean[Processor.MEMORY_SIZE];
		for (int i = 0; i < Processor.MEMORY_SIZE; i++)
			this.busy[i] = false; // all stations start as available at first
		AddUnit.reservationStations = createStations();
		
	}

	/**
	 * construct reservation stations for all ADD/SUB units
	 * @return	reservation stations Object of correct size for ADD/SUB unit
	 */
	private ReservationStation createStations() {
		
		int temp = reservationStationNumber;
		return new ReservationStation(temp, ReservationStation.ADD_REPOSITORY);
		
	}

	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and writing to CDB
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 * @return floating point result
	 */
	public void execute(int stationNumber) {
		
		int thread = reservationStations.instructions[stationNumber].getThread(); // thread this command belongs to
		int command = reservationStations.opCode[stationNumber];
		float src0 = reservationStations.Vj[stationNumber];
		float src1 = reservationStations.Vk[stationNumber];
		int destinationRegister = reservationStations.instructions[stationNumber].getDst();
		
		for (int i = 0; i < executionDelay; i++)
			this.busy[Processor.PC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (command == Instruction.ADD)
			result = src0 + src1;
		else if (command == Instruction.SUB)
			result = src0 - src1;
		else {
			System.out.println("NONE ADD/SUB COMMAND SENT TO ADD/SUB UNIT!! EXITING");
			System.exit(0);
		}
		
		// remove instruction from the station, it has been taken care of
		reservationStations.opCode[stationNumber] = Instruction.EMPTY;
		reservationStations.Vj[stationNumber] = (Float)null;
		reservationStations.Vk[stationNumber] = (Float)null;
		
		//write result to CDB, along with who calculated it
		CDB.writeToCDB( result, new ReservationStation.Tag(ReservationStation.ADD_REPOSITORY, 
				stationNumber, thread));
	}
	
	/**
	 * attempt to accept new command into the reservation station
	 * @param inst the instruction we want to our station
	 * @param thread the thread the instruction belongs to
	 * @return True if successful, else false
	 */
	public static boolean acceptIssue(Instruction inst, int thread){
		
		if(!reservationStations.isFull()){
			
			reservationStations.addInstruction(inst, thread);
			return true;
		}
		
		return false; // if you got here then there was no room in the station or you F'd up in some other way
	}
	
	/**
	 * is this station busy?
	 * @return true if station is working, else false
	 */
	public boolean isBusy(int now){
		return this.busy[now];
	}


}
