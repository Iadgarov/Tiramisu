package components;

import java.util.List;

/**
 * The CDB
 * @author David
 *
 */
public class CDB {

	static List<Integer> commitWhen;	// is it time to commit this data yet?
	static List<Tag> commitWho;	// who needs to get this data?
	static List<Float> commitWhat;	// what is the data?
	static List<Integer> commitWhere; // used to find location in memory for a store command
	
	static int totalCommits = 0;	// how many instructions have commited so far
	
	
	/**
	 * This method commits results from our calculations to the stations and registers once the time is right.
	 * Commits the first instruction that matches this CC for each unit type
	 */
	public static void commit(){
		
		// flags: false if we have yet to put this type of command result onto the line
		boolean addCommit = false;
		boolean multCommit = false;
		boolean memCommit = false;
		
		// if it's your time (execute would have finished by now) get committed
		for (int i = 0; i < commitWhen.size(); i++){
			
			// commit a cycle after execution is done
			if (commitWhen.get(i) + 1 >= Processor.CC){
				
				float result = commitWhat.get(i);
				Tag tag = commitWho.get(i);
				
				if (tag.getStation() == ReservationStation.ADD_REPOSITORY)
					if (addCommit)
						continue; // already did an ADD/SUB commit in this CC
					else 
						addCommit = true;
				else if (tag.getStation() == ReservationStation.MUL_REPOSITORY)
					if (multCommit)
						continue; // already did an MULT/DIV commit in this CC
					else 
						multCommit = true;
				else if (tag.getStation() == ReservationStation.LOAD_REPOSITORY)
					if (memCommit)
						continue; // already did an ADD/SUB commit in this CC
					else 
						memCommit = true;
				else if (tag.getStation() == ReservationStation.STORE_REPOSITORY)
					if (memCommit)
						continue; // already did an ADD/SUB commit in this CC
					else 
						memCommit = true;
				
				
				totalCommits++;
				
			
				// if not a STORE command, update the others
				if (tag.getStation() != ReservationStation.STORE_REPOSITORY){
					
					// First the reservation stations:
					updateStation(result, tag, ReservationStation.ADD_REPOSITORY);	// ADD/SUB stations
					updateStation(result, tag, ReservationStation.MUL_REPOSITORY);	// MULT/DIV stations
					updateStation(result, tag, ReservationStation.LOAD_REPOSITORY);	// MULT/DIV stations
					updateStation(result, tag, ReservationStation.STORE_REPOSITORY);	// MULT/DIV stations
				
					
					// Next update the register file
					updateRegisters(result, tag);
				
					// remember when this instruction was committed
					if (tag.getThread() == Processor.THREAD_0)
						InstructionQueue.writeBackCC_0[i] = Processor.CC;
					else if (tag.getThread() == Processor.THREAD_1)
						InstructionQueue.writeBackCC_1[i] = Processor.CC;
					
				}
				// STORE command, update the memory
				else if(tag.getStation() == ReservationStation.LOAD_REPOSITORY){
					int address = commitWhere.get(i);
					updateMemory(result, tag, address, i);	// STORE updating the memory
					
					// remember when this instruction was committed
					if (tag.getThread() == Processor.THREAD_0)
						InstructionQueue.writeBackCC_0[i] = -1;
					else if (tag.getThread() == Processor.THREAD_1)
						InstructionQueue.writeBackCC_1[i] = -1;
				}
				else{
					System.out.println("Unkown command type attempting to write on CDB. EXITING!");
					System.exit(0);
				}
			
				// this command has been committed, get rid of it
				commitWhen.remove(i);
				commitWho.remove(i);
				commitWhat.remove(i);
			
			}
		
		}
		
		
	}
	
	
	/**
	 * gets calculation result and who sent it. Places commit request into the list
	 * Once the correct CC comes the data will be committed
	 * @param result data that was calculated
	 * @param tag	who sent the data, if from STORE station write to MEM instead
	 * @param when	what CC should we actually commit data on?
	 */
	public static void writeToCDB(float result, Tag tag, int when) {
		
		commitWhen.add(when);
		commitWhat.add(result);
		commitWho.add(tag);
		
	
	}
	
	
	/**
	 * Extension of other constructor adding memory address option as input
	 * @param result see writeToCDB
	 * @param tag	see writeToCDB
	 * @param when	see writeToCDB
	 * @param address	the IMM value of the STORE instruction. We will place that in memory at this location.
	 */
	public static void writeToCDB(float result, Tag tag, int when, int address){
		
		commitWhere.add(address);
		writeToCDB(result, tag, when);
	}
	
	
	/**
	 * Update commands in the reservation stations/buffer with the data on the CDB
	 * @param result	the data on the CDB
	 * @param tag	who sent the data
	 * @param station	type of station to update
	 */
	private static void updateStation(float result, Tag tag, int station){
		
		ReservationStation rs = null;
		if (station == ReservationStation.ADD_REPOSITORY)
			// ADD/SUB stations:
			rs = AddUnit.reservationStations;
		else if (station == ReservationStation.MUL_REPOSITORY)
			// MULT/DIV stations:
			rs = MultUnit.reservationStations;
		else if (station == ReservationStation.LOAD_REPOSITORY)
			// MULT/DIV stations:
			rs = LoadUnit.reservationStations;
		else if (station == ReservationStation.STORE_REPOSITORY)
			// MULT/DIV stations:
			rs = StoreUnit.reservationStations;
		else{
			System.out.println("Unknown reservation station accessing CDB");
			System.exit(0);
		}
		
		for (int i = 0; i < rs.Qj.length; i++) {
			
			// Qj:
			if (rs.Qj[i].equals(tag)){
				rs.Vj[i] = result;
				rs.Qj[i] = null;
			}
			
			// Qk:
			if (rs.Qk[i].equals(tag)){
				rs.Vk[i] = result;
				rs.Qk[i] = null;
			}
		}
			
	}
		
	/**
	 * Update register from data on CDB
	 * @param result	the data on the CDB
	 * @param tag	who sent the data on the CDB
	 */
	private static void updateRegisters(float result, Tag tag){
		
		
		RegisterCollection regs = null;
		if (tag.getThread() == Processor.THREAD_0)
			regs = Processor.registers_0;
		else if (tag.getThread() == Processor.THREAD_1)
			regs = Processor.registers_1;
		else{
			System.out.println("Undefined thread writing to CDB. Exiting program!");
			System.exit(0);
		}
		

		for (int i = 0; i < regs.getStatus().length; i++) {
			
			// Checking which registers can be updated in value:
			//make sure to update register class and not a local copy
			if (regs.getStatus()[i].equals(tag)){
				
				regs.getRegisters()[i] = result;
				regs.getStatus()[i] = null;

			}
			
		}
	}
	
	/**
	 * Place data in the memory, happens as a result of a STORE command.
	 * @param result the data that will go into the memory
	 * @param tag	the tag representin who sent this data
	 * @param address the location in memory that we write to
	 * @param i	represents which instruction we are committing
	 */
	private static void updateMemory(float result, Tag tag, int address, int i){
		
		Processor.memory[address] = result;
		
		// remember when this instruction was committed
		if (tag.getThread() == Processor.THREAD_0)
			InstructionQueue.writeBackCC_0[i] = Processor.CC;
		else if (tag.getThread() == Processor.THREAD_1)
			InstructionQueue.writeBackCC_1[i] = Processor.CC;
		
	}
	
	

}
