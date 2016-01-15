package components;

import support.Instruction;
import support.Tag;

/**
 * Individual ADD/SUB unit class.
 * Processor can have several of these. They are responsible for ADD/SUB calculations. 
 * @author David
 *
 */
public class AddUnit{
	
	private static int executionDelay;	
	private boolean busy;
	public static ReservationStation reservationStations;	// reservation stations that hold commands for this type of unit
	private int exeStart; // when execution started so we can free unit when done (done = delay has passed since the start)

	
	/**
	 * construct a ADD/SUB unit<br>
	 * Gives each unit a boolean "busy"  so we know what CC's it's working. Sets it to false. 
	 */
	public AddUnit() {
	
		setBusy(false);
		
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
		//int destinationRegister = reservationStations.instructions[stationNumber].getDst();
		
		setBusy(true);
		
		
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
		CDB.writeToCDB( result, tag, Processor.CC + getExecutionDelay());
		
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
	public boolean isBusy(){
		return this.busy;
	}


	/**
	 * Get reservation stations for ADD/SUB unit
	 * @return reservation stations object for ADD/SUB unit
	 */
	public static ReservationStation getReservationStations() {
		return reservationStations;
	}


	/**
	 * Set reservationStation object for ADD/SUB units.
	 * @param reservationStations reservationStation object for ADD/SUB units.
	 */
	public static void setReservationStations(ReservationStation reservationStations) {
		AddUnit.reservationStations = reservationStations;
	}



	public static int getExecutionDelay() {
		return executionDelay;
	}



	public static void setExecutionDelay(int executionDelay) {
		AddUnit.executionDelay = executionDelay;
	}


	/**
	 * Get's CC of when this unit began the current calculation.
	 * @return CC of when this unit began the current calculation.
	 */
	public  int getExeStart() {
		return exeStart;
	}


	/**
	 * Set CC when this unit begins a calculation.
	 * @param exeStart CC when this unit begins a calculation.
	 */
	public  void setExeStart(int exeStart) {
		this.exeStart = exeStart;
	}


	/**
	 * Set this unit to busy/not busy. When calculating busy = true, when idle busy = flase.
	 * @param b boolean, is the unit busy now?
	 */
	public void setBusy(boolean b) {
		this.busy = b;
		
	}
	



}
