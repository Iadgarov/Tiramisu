package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import collections.Adders;
import collections.Multers;
import collections.RegisterCollection;
import components.AddUnit;
import components.CDB;
import components.InstructionQueue;
import components.MemoryUnit;
import components.MultUnit;
import components.Processor;
import support.Instruction;

/**
 * The main initiator of the program. Input goes here and output get's pushed out here
 * @author David
 *
 */
public class Sim {
	
	// Not entirely sure why I'm bothering with these consts...
	// Apparently const usage in Java is frowned upon too
	private final static String ADD_UNIT_AMOUNT 	= "add_nr";
	private final static String MULT_UNIT_AMOUNT 	= "mul_nr";
	private final static String ADD_UNIT_DELAY 		= "add_delay";
	private final static String MULT_UNIT_DELAY		= "mul_delay";
	private final static String MEM_UNIT_DELAY 		= "mem_delay";
	private final static String ADD_RS_AMOUNT 		= "add_nr_reservation";
	private final static String MULT_RS_AMOUNT 		= "mul_nr_reservation";
	private final static String LOAD_BUFF_AMOUNT 	= "mem_nr_load_buffers";
	private final static String STORE_BUFF_AMOUNT	= "mem_nr_store_buffers";
	
	private  static int addUnitAmount;
	private  static int multUnitAmount;
	private  static int addDelay;
	private  static int multDelay;
	private  static int memDelay;
	private  static int addStationAmount;
	private  static int multStationAmount;
	private  static int loadBuffAmount;
	private  static int storeBuffAmount;
	
	private static ArrayList<Float> memory = new ArrayList<>();
	static ArrayList<String> memoryInString = new ArrayList<>();	// used to save an instructions encoding for later

	/**
	 * main, start everything here.
	 * Responsible for decoding input, creating instruction queues, initiating doWork method and committing output to files
	 * @param args - the arguments defined in the project files.
	 */
	public static void main(String[] args) {
	
		//System.out.println(Integer.toHexString(Float.floatToIntBits((float) 2.72)).toUpperCase());
		
		// get parameters from cfg
		receive(true, args[1]);
		// set up initial memory picture
		receive(false, args[2]);
		Processor.InstAmount = getMemory().size();
		
		// now pad the rest of the memory with zeros
		for (int i = getMemory().size(); i < Processor.MAX_MEMORY_SIZE; i++)
			memory.add((float) 0);
		
		Queue<Instruction> instQ_0 = new LinkedBlockingQueue<Instruction>();
		Queue<Instruction> instQ_1 = new LinkedBlockingQueue<Instruction>();
		
		// create the two queues of instructions
		
		boolean Q0Halt = false;	// has queue 0 gotten halt?
		boolean Q1Halt = false;	// has queue 1 gotten halt?
		

		System.out.println("The program in Assembly:\n");
		
		for (int i = 0 ; !(Q0Halt && Q1Halt); i++, Processor.InstAmount = i){
			
			Instruction inst = new Instruction(memoryInString.get(i));
			inst.setGlobalQLocation(i);
			
			
			System.out.println("\t" + inst.toString());
			
			
			if (i % 2 == 0 && !Q0Halt){
				instQ_0.add(inst);
				inst.setqLocation(i/2);
				inst.setThread(i%2);
				InstructionQueue.getIsntHexEncoding_0()[i/2] = memoryInString.get(i);
				if (inst.getOpCode() != Instruction.NOP)
					InstructionQueue.setInstCount_0(InstructionQueue.getInstCount_0() + 1);
				
				if (inst.getOpCode() == Instruction.HALT)
					Q0Halt = true;	
			}
			else if (i % 2 == 1 && !Q1Halt){ 
				instQ_1.add(inst);
				inst.setqLocation(i/2);
				inst.setThread(i%2);
				InstructionQueue.getIsntHexEncoding_1()[i/2] = memoryInString.get(i);
				if (inst.getOpCode() != Instruction.NOP)
					InstructionQueue.setInstCount_1(InstructionQueue.getInstCount_1() + 1);
				
				if (inst.getOpCode() == Instruction.HALT)
					Q1Halt = true;
			}
			
			
			
		}
		
		// remove the two halt commands from the command count
		Processor.InstAmount--;
		Processor.InstAmount--;

		System.out.println();
		
		// create the processor
		new Processor(instQ_0, instQ_1, addUnitAmount ,
				addDelay, multUnitAmount, multDelay, storeBuffAmount, loadBuffAmount, 
				addStationAmount, multStationAmount, memDelay);
		
		System.out.println("Here we go:\n");
		
		doWork();
		
		// write back all the data to the files
		memoryToFile(args[3]);
		registersToFile(args[4], Processor.THREAD_0);
		registersToFile(args[7], Processor.THREAD_1);
		traceToFile(args[5], Processor.THREAD_0);
		traceToFile(args[8], Processor.THREAD_1);
		CPIToFile(args[6], Processor.THREAD_0);
		CPIToFile(args[9], Processor.THREAD_1);
		
		
	}
	
	/**
	 * Convert string of a hex number into a float value
	 * @param myString	the input string, convert this
	 * @return	float value of the hex string
	 */
	public static float hexStringToFloat(String myString){
        
        Long i = Long.parseLong(myString.substring(0,8), 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return f;
		
	}
	
	/**
	 * Simulates the processors run one CC at a time. <br> 
	 * Runs till all stations are empty and all instruction have been executed and committed to the CDB. <br>
	 * calls attempt to issue the instructions, up to 4 per cycle.<br>
	 * calls attempt to being execution<br>
	 * calls any possible i on the CDB (see CDB documentation) to happen<br>
	 * increment the CC and keeps going
	 */
	private static void doWork(){
		
		while (!((CDB.getTotalCommits() == InstructionQueue.getTotalIssues()) &&	// committed everything that was issued
				 (AddUnit.getReservationStations().isEmpty()) &&				// stations/buffers are empty of commands
				 (MultUnit.getReservationStations().isEmpty()) &&
				 (MemoryUnit.getReservationStations(Instruction.LD).isEmpty()) &&
				 (MemoryUnit.getReservationStations(Instruction.ST).isEmpty()) && 
				 (InstructionQueue.isHalt0()) &&							// reached first halt
				 (InstructionQueue.isHalt1()))){							// reached second halt
			
			// issue any new commands if possible
			for (int i = 0; i < 2; i++ ){
				Processor.instructionQ.attemptIssue((Processor.CC %2 == 0) ? Processor.THREAD_0 : Processor.THREAD_1);	
				Processor.instructionQ.attemptIssue((Processor.CC %2 == 0) ? Processor.THREAD_1 : Processor.THREAD_0);
			}
			
			// ready for next CC issuing
			InstructionQueue.setIssueCountPerCC_0(0);
			InstructionQueue.setIssueCountPerCC_1(0);
			
			// Pass any commands that we can to the units
			Adders.attemptPushToUnit(); 
			Multers.attemptPushToUnit();
			MemoryUnit.attemptPushToUnit(Instruction.ST);
			MemoryUnit.attemptPushToUnit(Instruction.LD);
			
			
			unitClean();	// release units that are done with execution
			
			CDB.commit(); // write back whatever is ready to be written back
			
			Processor.CC++; // onto the next cycle
			
			//System.out.println("ADD STATION: \n" + AddUnit.reservationStations.toString());
			//System.out.println("MULT STATION: \n" + MultUnit.reservationStations.toString());
			
		}
		
	}
	
	/**
	 * Go through all the execution units and free them if enough cycles have passed to complete
	 * the execution delay<br>
	 * Should be called after execution for this CC has been initiated. 
	 */
	private static void unitClean(){
		
		// ADD/SUB
		for (int i = 0; i < Adders.addUnitNumber; i++){
			AddUnit x = Adders.getAddUnits().get(i);
			if (x.isBusy() && x.getExeStart() + AddUnit.getExecutionDelay() <= Processor.CC )
				x.setBusy(false);
		}
		
		// MULT/DIV
		for (int i = 0; i < Multers.multUnitNumber; i++){
			MultUnit x = Multers.getMultUnits().get(i);
			if (x.isBusy() && x.getExeStart() + MultUnit.getExecutionDelay() <= Processor.CC)
				x.setBusy(false);
		}
		
		// MEM
	
		int temp = (MemoryUnit.getExeStart() < 0) ? MemoryUnit.getExeStart() * (-1) : MemoryUnit.getExeStart();
		if (MemoryUnit.isBusy() && (temp + 
				MemoryUnit.getExecutionDelay()) <= Processor.CC){
			MemoryUnit.setBusy(false);
		}
	}
	
	

	
	
	/**
	 * An input parser.<br>
	 * Get parameter data from cfg.txt and save it in variables for later use<br>
	 * Or reads the memory file and saves that for later use
	 * @param whatDo if true then read data for parameters from cfg, if false read memory file
	 */
	private static void receive(boolean whatDo, String file){
		
		String line;
		//int i = 0;
		
        try {
        
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	
            	if (whatDo)
            		getCfgData(line);
            	else{
            		getMemory().add(hexStringToFloat(line));
            		memoryInString.add(line.substring(0, 8));
            	}
      
            }   

         
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "' EXITING!");    
            System.exit(0);
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + file + "' EXITING!");                  
            System.exit(0);
        }
		
	}
	
	/**
	 * Tells apart the various different types of lines in cfg file and places it in relevant variables
	 * @param line current line from cfg
	 */
	private static void getCfgData(String line){
		
		//System.out.println(line);
    	if (line.contains(ADD_UNIT_AMOUNT + " "))			// add spaces to prevent prefix issues with contain
    		addUnitAmount = getDataFromSring(line);
    	else if (line.contains(MULT_UNIT_AMOUNT + " "))		// add spaces to prevent prefix issues with contain
    		multUnitAmount = getDataFromSring(line);
    	else if (line.contains(ADD_UNIT_DELAY))
    		addDelay = getDataFromSring(line);
    	else if (line.contains(MULT_UNIT_DELAY))
    		multDelay = getDataFromSring(line);
    	else if (line.contains(MEM_UNIT_DELAY))
    		memDelay = getDataFromSring(line);
    	else if (line.contains(ADD_RS_AMOUNT))
    		addStationAmount = getDataFromSring(line);
    	else if (line.contains(MULT_RS_AMOUNT))
    		multStationAmount = getDataFromSring(line);
    	else if (line.contains(LOAD_BUFF_AMOUNT))
    		loadBuffAmount = getDataFromSring(line);
    	else if (line.contains(STORE_BUFF_AMOUNT))
    		storeBuffAmount = getDataFromSring(line);
    	
	}
	/**
	 * Get string of the form a = b. Return b.
	 * @param st the input string of the form a = b
	 * @return for a string of form a = b return b.
	 */
	private static int getDataFromSring(String st){
		
		//st.replaceAll("\\s+", "");	// remove white spaces
		int i = st.indexOf('='); 	// find equation sign, data is after this
		String data = st.substring(i + 2, st.length());
		return Integer.parseInt(data);
	}

	/**
	 * Writes the memory state into a file, each line in file is a location in memory
	 * @param file	the path to the file we want to write in
	 */
	private static void memoryToFile(String file){
			
		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			for (float i : Processor.memory){
				String temp = Integer.toHexString(Float.floatToIntBits(i)).toUpperCase();
				
				// Pad with zeros if HEX string is not the full 32 bits
				while (temp.length() < 8)
					temp = "0" + temp;
				
				writer.println(temp);
			}
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
		
		
	}
	
	/**
	 * Writes the register data into a file
	 * @param file	the path to the file we want to write in
	 * @param thread the thread we are currently working on
	 */
	private static void registersToFile(String file, int thread){
		
		
		RegisterCollection rg = null;
		
		if (thread == Processor.THREAD_0){
			rg = Processor.registers_0;
			System.out.println("\nRegisters for thread 0:");
		}
		else if (thread == Processor.THREAD_1){
			rg = Processor.registers_1;
			System.out.println("\nRegisters for thread 1:");
		}
		else{
			System.out.println("Attempted access to register of an undefined thread! EXITING!");
			System.exit(0);
		}
	
		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			int count = 0;
			for (int i = 0; i < rg.getRegisters().length; i++){
				writer.println(rg.getRegisters()[i]);
				String temp = (count == 15) ? "" : ", ";
				System.out.print("F" + (count++) + " = " + rg.getRegisters()[i] + temp);
				if (i % 5 == 0 && i > 0)
					System.out.println();
				
			}
			
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
	}
	
	/**
	 * Writes the trace of a thread into a file, each line in file is a location in memory<br>
	 * Line consists of operation Hex encoding, issue CC, exe start CC, writeback to CDB CC
	 * @param file	the path to the file we want to write in
	 * @param thread the thread we are currently working on
	 */
	private static void traceToFile(String file, int thread){
		
		

		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			//for (int i = 0; i < Processor.InstAmount; i++){
				
			if (thread == Processor.THREAD_0){
				for (int i = 0; i < InstructionQueue.getInstCount_0(); i++){
					writer.print(InstructionQueue.getIsntHexEncoding_0()[i] + "\t");
					writer.print(Integer.toString(InstructionQueue.getIssueCC_0()[i]) + "\t");
					writer.print(Integer.toString(InstructionQueue.getExeCC_0()[i]) + "\t");
					writer.println(((InstructionQueue.getWriteBackCC_0()[i] < 0) ?
							-1 : Integer.toString(InstructionQueue.getWriteBackCC_0()[i])) );
				}
			
			}
			else if (thread == Processor.THREAD_1){
				for (int i = 0; i < InstructionQueue.getInstCount_1(); i++){
					writer.print(InstructionQueue.getIsntHexEncoding_1()[i] + "\t");
					writer.print(Integer.toString(InstructionQueue.getIssueCC_1()[i]) + "\t");
					writer.print(Integer.toString(InstructionQueue.getExeCC_1()[i]) + "\t");
					writer.println(((InstructionQueue.getWriteBackCC_1()[i] < 0) ?
							-1 : Integer.toString(InstructionQueue.getWriteBackCC_1()[i])) );
				}
			}
			else{
				System.out.println("Attempted access instructions of an undefined thread! EXITING!");
				System.exit(0);
			}
				
			
				
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
	}
	
	/**
	 * Writes the CPI to a file. CPI = (total CC's)/(total command #) 
	 * @param file	the path to the file we want to write in
	 * @param thread the thread we are currently working on
	 */
	private static void CPIToFile(String file, int thread){
		
		int totalInstructionCount = 0;
		int totalCycleCount = 0;
		

		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");

			if (thread == Processor.THREAD_0){
				totalInstructionCount = InstructionQueue.getInstCount_0();
				for (int i : InstructionQueue.getWriteBackCC_0()){
					
					if (i < 0)
						i *= -1;
					totalCycleCount = Math.max(totalCycleCount, i);
				}
				System.out.print("\nCPI for thread 0: ");
			}
			else if (thread == Processor.THREAD_1){
				totalInstructionCount = InstructionQueue.getInstCount_1();
				for (int i : InstructionQueue.getWriteBackCC_1()){
					
					if (i < 0)
						i *= -1;
					totalCycleCount = Math.max(totalCycleCount, i);
				}
				System.out.print("\nCPI for thread 1: ");
			}
			else{
				System.out.println("Attempted access instructions of an undefined thread! EXITING!");
				System.exit(0);
			}
			writer.println(Float.toString((float)((float)totalCycleCount/(float)totalInstructionCount)));
			System.out.println(totalCycleCount + "/" + totalInstructionCount
					+ " = " + Float.toString((float)((float)totalCycleCount/(float)totalInstructionCount)));
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
	}

	public static ArrayList<Float> getMemory() {
		return memory;
	}

	public static void setMemory(ArrayList<Float> memory) {
		Sim.memory = memory;
	}
}
