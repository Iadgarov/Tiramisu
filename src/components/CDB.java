package components;

import java.util.ArrayList;
import java.util.List;

import collections.RegisterCollection;
import support.Instruction;
import support.Tag;

/**
 * The CDB
 * @author David
 *
 */
public class CDB {

	static List<Integer> commitWhen = new ArrayList<>();	// is it time to commit this data yet?
	static List<Tag> commitWho = new ArrayList<>();			// who needs to get this data?
	static List<Float> commitWhat = new ArrayList<>();		// what is the data?
	static List<Integer> commitWhere = new ArrayList<>(); 	// used to find location in memory for a store command
	
	private static int totalCommits = 0;	// how many instructions have commited so far
	
	
	/**
	 * This method commits results from our calculations to the stations and registers once the time is right.<br>
	 * Execution result is sent to the CDB right away. <br>
	 * The CDB is responsible to only broadcast it once the execution delay has passed. <br>
	 * But why write it this way? meh.. made sense at the time.<br>
	 * Once a result has been broadcasted this means we can remove the relevant command from the relevant station and make
	 * room for a new one. <br>
	 * Tells all reservation stations what value is being broadcasted and by whom should they want it<br>
	 * Commits all instructions that are ready for it per CC.
	 * Saves the CDB broadcast CC to be used in the trace file later. Store commands get the storing CC *(-1) so we can calculate
	 * CPI on a program that ends with a STORE. When printing to trace a negative number is translated to -1. 
	 * 
	 */
	public static void commit(){
		
		/* flags: false if we have yet to put this type of command result onto the line
		boolean addCommit = false;
		boolean multCommit = false;
		boolean memCommit = false;
		*/
		
		// if it's your time (execute would have finished by now) get committed
		for (int i = 0; i < commitWhen.size(); i++){
			
			
			
			// commit a cycle after execution is done
			if (commitWhen.get(i) <= Processor.CC){
				
				float result = commitWhat.get(i);
				Tag tag = commitWho.get(i);
				int stationNumber = tag.getStationLine();
				
				
				Instruction inst = null;
						
				if (tag.getStation() == ReservationStation.ADD_REPOSITORY){
				
					inst = tag.getInstruction();
					
					// remove instruction from the station, it has been taken care of
					AddUnit.getReservationStations().getInstructions()[stationNumber] = null;
					AddUnit.getReservationStations().opCode[stationNumber] = Instruction.EMPTY;
					AddUnit.getReservationStations().Vj[stationNumber] = (Float)null;
					AddUnit.getReservationStations().Vk[stationNumber] = (Float)null;
					AddUnit.getReservationStations().getInExecution()[stationNumber] = false;	// no longer executing this stations command
				}
				else if (tag.getStation() == ReservationStation.MUL_REPOSITORY){
			
					inst = tag.getInstruction();
					
					// remove instruction from the station, it has been taken care of
					MultUnit.getReservationStations().getInstructions()[stationNumber] = null;
					MultUnit.getReservationStations().opCode[stationNumber] = Instruction.EMPTY;
					MultUnit.getReservationStations().Vj[stationNumber] = (Float)null;
					MultUnit.getReservationStations().Vk[stationNumber] = (Float)null;
					MultUnit.getReservationStations().getInExecution()[stationNumber] = false;	// no longer executing this stations command
				}
				else if (tag.getStation() == ReservationStation.LOAD_REPOSITORY ||
						tag.getStation() == ReservationStation.STORE_REPOSITORY){
				
					inst = tag.getInstruction();
					int type = inst.getOpCode();
					
					// remove instruction from the station, it has been taken care of
					MemoryUnit.getReservationStations(type).getInstructions()[stationNumber] = null;
					MemoryUnit.getReservationStations(type).opCode[stationNumber] = Instruction.EMPTY;
					MemoryUnit.getReservationStations(type).Vj[stationNumber] = (Float)null;
					MemoryUnit.getReservationStations(type).Vk[stationNumber] = (Float)null;
					MemoryUnit.getReservationStations(type).getInExecution()[stationNumber] = false;	// no longer executing this stations command
					
					
				}
				else{
					System.out.println("Unknown station writing ot CDB! EXITING!");
					System.exit(0);
				}
				
				
				setTotalCommits(getTotalCommits() + 1);
				System.out.println("Thread = "+ tag.getThread() +" [CC = " + Processor.CC +
						"]\tCommit for:\t" + inst.toString() );
				
				//System.out.println("Adders all busy? " + Processor.addUnits.isFullyBusy());
				//System.out.println("Multers all busy? " + Processor.multUnits.isFullyBusy());
				
			
				// if NOT a STORE command, update the others (STORE does not return a result)
				if (tag.getStation() != ReservationStation.STORE_REPOSITORY){
					
					// First the reservation stations:
					updateStation(result, tag, ReservationStation.ADD_REPOSITORY);		// ADD/SUB stations
					updateStation(result, tag, ReservationStation.MUL_REPOSITORY);		// MULT/DIV stations
					updateStation(result, tag, ReservationStation.LOAD_REPOSITORY);		// MULT/DIV stations
					updateStation(result, tag, ReservationStation.STORE_REPOSITORY);	// MULT/DIV stations
				
					
					// Next update the register file
					updateRegisters(result, tag);
				
					// remember when this instruction was committed
					if (tag.getThread() == Processor.THREAD_0)
						InstructionQueue.getWriteBackCC_0()[inst.getqLocation()] = Processor.CC;
					else if (tag.getThread() == Processor.THREAD_1)
						InstructionQueue.getWriteBackCC_1()[inst.getqLocation()] = Processor.CC;
					
				}
				// STORE command, update the memory
				else if(tag.getStation() == ReservationStation.STORE_REPOSITORY){
					int address = commitWhere.get(i);
					updateMemory(result, tag, address, i);	// STORE updating the memory
					
					// remember when this instruction was committed
					// for a STORE command save the CC*(-1) so we know to print -1 in the trace file instead
					if (tag.getThread() == Processor.THREAD_0)
						InstructionQueue.getWriteBackCC_0()[inst.getqLocation()] = -1 * Processor.CC;
					else if (tag.getThread() == Processor.THREAD_1)
						InstructionQueue.getWriteBackCC_1()[inst.getqLocation()] = -1 * Processor.CC;
					
					
				}
				else{
					System.out.println("Unkown command type attempting to write on CDB. EXITING!");
					System.exit(0);
				}
				
				
			
				// this command has been committed, get rid of it
				commitWhen.remove(i);
				commitWho.remove(i);
				commitWhat.remove(i);
				commitWhere.remove(i);
			
			}
		
		}
		
		
	}
	
	
	/**
	 * gets calculation result and who sent it. Places commit request into the list <br>
	 * Once the correct CC comes the data will be committed. See commit method documentation
	 * @param result data that was calculated
	 * @param tag	who sent the data, if from STORE station write to MEM instead
	 * @param when	what CC should we actually commit data on?
	 */
	public static void writeToCDB(float result, Tag tag, int when) {
		
		//System.out.println("Waiting for commit: result = " + result + " When = " + when);
		//System.out.println("Who: station = " + tag.getStation() + " line = " + tag.getStationLine() + " thread = " + tag.getThread());
		
		commitWhere.add(-1);	// add dummy value to fix stupid bug
		commitWhen.add(when);
		commitWhat.add(result);
		commitWho.add(tag);
		
	
	}
	
	
	/**
	 * Extension of other constructor adding memory address option as input for STORE commands to use
	 * @param result see writeToCDB
	 * @param tag	see writeToCDB
	 * @param when	see writeToCDB
	 * @param address	the IMM value of the STORE instruction. We will place that in memory at this location.
	 */
	public static void writeToCDB(float result, Tag tag, int when, int address){
		
		commitWhere.add(address);
		commitWhen.add(when);
		commitWhat.add(result);
		commitWho.add(tag);
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
			rs = AddUnit.getReservationStations();
		else if (station == ReservationStation.MUL_REPOSITORY)
			// MULT/DIV stations:
			rs = MultUnit.getReservationStations();
		else if (station == ReservationStation.LOAD_REPOSITORY)
			// MEM (LOAD) stations:
			rs = MemoryUnit.getReservationStations(Instruction.LD);
		else if (station == ReservationStation.STORE_REPOSITORY)
			// MEM (STOREstations:
			rs = MemoryUnit.getReservationStations(Instruction.ST);
		else{
			System.out.println("Unknown reservation station accessing CDB");
			System.exit(0);
		}
		
		for (int i = 0; i < rs.Qj.length; i++) {
			
			// Qj:
			if (rs.Qj[i] != null && rs.Qj[i].equals(tag)){
				rs.Vj[i] = result;
				rs.Qj[i] = null;
			}
			
			// Qk:
			if (rs.Qk[i] != null && rs.Qk[i].equals(tag)){
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
			if (regs.getStatus()[i] != null && regs.getStatus()[i].equals(tag)){
				
				regs.getRegisters()[i] = result;
				regs.getStatus()[i] = null;

			}
			
		}
	}
	
	/**
	 * Place data in the memory, happens as a result of a STORE command.
	 * @param result the data that will go into the memory
	 * @param tag	the tag representing who sent this data
	 * @param address the location in memory that we write to
	 * @param i	represents which instruction we are committing
	 */
	private static void updateMemory(float result, Tag tag, int address, int i){
		
		Processor.memory.set(address, result);
		

		
	}


	public static int getTotalCommits() {
		return totalCommits;
	}


	public static void setTotalCommits(int totalCommits) {
		CDB.totalCommits = totalCommits;
	}
	
	

}
