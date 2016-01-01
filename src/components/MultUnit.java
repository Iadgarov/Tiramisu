package components;


/**
 * the MUL/DIV class. A processing unit for floating point multiplication and division. 
 * @author David
 *
 */
public class MultUnit{
	
	static int executionDelay;
	static int reservationStationNumber;
	private boolean busy[];		// true if station is busy in a specific CC
	static ReservationStation reservationStations;
	
	
	/**
	 * construct a MUL/DIV unit
	 */
	public MultUnit() {
		
		this.busy = new boolean[Processor.MAX_MEMORY_SIZE];
		for (int i = 0; i < Processor.MAX_MEMORY_SIZE; i++)
			this.busy[i] = false; // all stations start as available at first
		MultUnit.reservationStations = createStations();
		
	}

	/**
	 * construct reservation stations for all MUL/DIV units
	 * @return list of reservation stations for MUL/DIV unit
	 */
	private ReservationStation createStations() {
		
		int temp = reservationStationNumber;
		return new ReservationStation(temp, ReservationStation.MUL_REPOSITORY);
	}

	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and writing to CDB
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 */
	public void execute(int stationNumber) {
		
		System.out.println("[CC = " + Processor.CC + "] Exe start for  " + reservationStations.instructions[stationNumber].toString() );

		
		int thread = reservationStations.instructions[stationNumber].getThread(); // thread this command belongs to
		int command = reservationStations.opCode[stationNumber];
		float src0 = reservationStations.Vj[stationNumber];
		float src1 = reservationStations.Vk[stationNumber];
		
		
		//int t = Processor.CC;
		for (int i = 0; i < executionDelay; i++)
			this.busy[Processor.CC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (command == Instruction.MULT)
			result = src0 * src1;
		else if (command == Instruction.DIV)
			result = src0 / src1;
		else {
			System.out.println("[MultUnit>execute] NONE MULT/DIV COMMAND SENT TO MULT/DIV UNIT!! EXITING");
			System.exit(0);
		}
		
		//write result to CDB, along with who calculated it
				CDB.writeToCDB( result, new Tag(ReservationStation.MUL_REPOSITORY, 
						stationNumber, thread,  
						reservationStations.instructions[stationNumber]), 
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
	 * is this station busy?
	 * @return true if station is working, else false
	 */
	public boolean isBusy(int now){
		return this.busy[now];
	}
	
	
		
	

}
