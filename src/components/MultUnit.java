package components;

import support.Instruction;
import support.Tag;

/**
 * the MUL/DIV class. A processing unit for floating point multiplication and division. 
 * @author David
 *
 */
public class MultUnit{
	
	static int executionDelay;
	private boolean busy[];		// true if station is busy in a specific CC
	public static ReservationStation reservationStations;
	
	
	/**
	 * construct a MUL/DIV unit<br>
	 * Gives each unit a "busy" array so we know what CC's it's working 
	 */
	public MultUnit() {
		
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
		
		
		//int t = Processor.CC;
		for (int i = 0; i < executionDelay; i++)
			this.busy[Processor.CC + i] = true; // this unit will be working for these CC's
		
		
		float result = 0;
		if (command == Instruction.MULT)
			result = (float)((float)src0 * (float)src1);
		else if (command == Instruction.DIV)
			result = (float)((float)src0 / (float)src1);
		else {
			System.out.println("[MultUnit>execute] NONE MULT/DIV COMMAND SENT TO MULT/DIV UNIT!! EXITING");
			System.exit(0);
		}
		
		//write result to CDB, along with who calculated it
				CDB.writeToCDB( result, new Tag(ReservationStation.MUL_REPOSITORY, 
						stationNumber, thread,  
						getReservationStations().getInstructions()[stationNumber]), 
						Processor.CC + executionDelay);
				
		
		
	}
	
	/**
	 * attempt to accept new command into the reservation station, it there is room it succeeds
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
	 * is this station busy this CC?
	 * @param now the CC we are checking
	 * @return true if station is working, else false
	 */
	public boolean isBusy(int now){
		return this.busy[now];
	}



	public static ReservationStation getReservationStations() {
		return reservationStations;
	}



	public static void setReservationStations(ReservationStation reservationStations) {
		MultUnit.reservationStations = reservationStations;
	}
	
	
		
	

}
