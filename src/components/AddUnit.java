package components;

import support.Instruction;
import support.Tag;

/**
 * Individual ADD/SUB unit class
 * @author David
 *
 */
public class AddUnit{
	
	static int executionDelay;
	private boolean busy[];
	public static ReservationStation reservationStations;
	
	/**
	 * construct a ADD/SUB unit
	 * Gives each unit a "busy" array so we know what CC's it's working 
	 */
	public AddUnit() {
		
		this.busy = new boolean[Processor.MAX_MEMORY_SIZE];
		for (int i = 0; i < Processor.MAX_MEMORY_SIZE; i++)
			this.busy[i] = false; // all stations start as available at first
		
		
	}



	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and writing to CDB
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 */
	public void execute(int stationNumber) {
		
		System.out.println("[CC = " + Processor.CC + "] Exe start for  " + getReservationStations().getInstructions()[stationNumber].toString() );

		
		int thread = getReservationStations().getInstructions()[stationNumber].getThread(); // thread this command belongs to
		int command = getReservationStations().opCode[stationNumber];
		float src0 = getReservationStations().Vj[stationNumber];
		float src1 = getReservationStations().Vk[stationNumber];
		//int destinationRegister = reservationStations.instructions[stationNumber].getDst();
		
		for (int i = 0; i < executionDelay; i++)
			this.busy[Processor.CC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (command == Instruction.ADD)
			result = src0 + src1;
		else if (command == Instruction.SUB)
			result = src0 - src1;
		else {
			System.out.println("[AddUnit>execute] NONE ADD/SUB COMMAND SENT TO ADD/SUB UNIT!! EXITING");
			System.exit(0);
		}
	
		//write result to CDB, along with who calculated it
		Tag tag = new Tag (ReservationStation.ADD_REPOSITORY, stationNumber, 
				thread, getReservationStations().getInstructions()[stationNumber]);
		CDB.writeToCDB( result, tag, Processor.CC + executionDelay);
		
	}
	
	/**
	 * attempt to accept new command into the reservation station. Accepts if there is room in station.
	 * @param inst the instruction we want to our station
	 * @param thread the thread the instruction belongs to
	 * @return True if successful, else false
	 */
	public static boolean acceptIntoStation(Instruction inst, int thread){
		
		if(!getReservationStations().isFull()){
			
			getReservationStations().addInstruction(inst, thread);
			return true;
		}
		
		return false; // if you got here then there was no room in the station or you F'd up in some other way
	}
	
	/**
	 * is this unit busy this CC?
	 * @return true if station is working, else false
	 */
	public boolean isBusy(int now){
		return this.busy[now];
	}



	public static ReservationStation getReservationStations() {
		return reservationStations;
	}



	public static void setReservationStations(ReservationStation reservationStations) {
		AddUnit.reservationStations = reservationStations;
	}


}
