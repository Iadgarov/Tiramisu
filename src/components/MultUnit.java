package components;

import support.Instruction;
import support.Tag;

/**
 * the MUL/DIV class. An individual processing unit for floating point multiplication and division. 
 * @author David
 *
 */
public class MultUnit{
	
	static int executionDelay;
	private boolean busy;		// true if station is busy in a specific CC
	public static ReservationStation reservationStations;	// reservation stations for MUL/DIV units.
	private int exeStart; // when execution started so we can free unit when done (free unit upon exeStart+executionDelay)

	
	/**
	 * construct a MUL/DIV unit<br>
	 * Gives each unit a "busy" array so we know what CC's it's working 
	 */
	public MultUnit() {
		
		this.busy = true;
	}



	/**
	 * Doing the actual calculation (what happens once a command enters a unit from the station)
	 * and sending to CDB
	 * @param stationNumber the line in the reservation station table that holds the to be executed instruction
	 */
	public void execute(int stationNumber) {
		
		System.out.print("Thread = "+ getReservationStations().thread[stationNumber] +
				" [CC = " + Processor.CC + "]\tExe start for:\t" + getReservationStations().getInstructions()[stationNumber].toString() );

		if (getReservationStations().getInstructions()[stationNumber].getDependentOn().toString() != "Nothing")
			System.out.println("\t" 
               + getReservationStations().getInstructions()[stationNumber].getDependentOn().toString() 
               + " must have finished");
		else
			System.out.println();
		
		this.exeStart = Processor.CC;
		
		int thread = getReservationStations().getInstructions()[stationNumber].getThread(); // thread this command belongs to
		int command = getReservationStations().opCode[stationNumber];
		float src0 = getReservationStations().Vj[stationNumber];
		float src1 = getReservationStations().Vk[stationNumber];
		
		
		setBusy(true);
		
		
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
	 * set unit to busy or not busy when it starts/ends a job
	 * @param b true is setting to busy, false if setting to idle
	 */
	public void setBusy(boolean b) {
		this.busy = b;
		
	}



	/**
	 * attempt to accept new command into the reservation station, it there is room it succeeds.<br>
	 * Calls addInstruction method from ReservationStation class
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
	 * @return true if station is working, else false
	 */
	public boolean isBusy(){
		return this.busy;
	}



	public static ReservationStation getReservationStations() {
		return reservationStations;
	}



	public static void setReservationStations(ReservationStation reservationStations) {
		MultUnit.reservationStations = reservationStations;
	}



	/**
	 * get CC when this unit began execution of current commands
	 * @return CC when this unit began execution of current commands
	 */
	public int getExeStart() {
		return this.exeStart;
	}



	public static int getExecutionDelay() {
		return executionDelay;
	}
	
	
		
	

}
