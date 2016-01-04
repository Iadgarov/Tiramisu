package components;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sim {
	
	// Not entirely sure why I'm bothering with these consts...
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
	
	static ArrayList<Float> memory = new ArrayList<>();
	static ArrayList<String> memoryInString = new ArrayList<>();

	public static void main(String[] args) {
	
		// The memory, we will populate this from the mem file
		
		
		// get parameters from cfg
		receive(true, args[1]);
		// set up initial memory picture
		receive(false, args[2]);
		Processor.InstAmount = memory.size();
		
		Queue<Instruction> instQ_0 = new LinkedBlockingQueue<Instruction>();
		Queue<Instruction> instQ_1 = new LinkedBlockingQueue<Instruction>();
		
		// create the two queues of instructions
		
		boolean Q0Halt = false;	// has queue 0 gotten halt?
		boolean Q1Halt = false;	// has queue 1 gotten halt?
		

		
		for (int i = 0 ; !(Q0Halt && Q1Halt); i++){
			
			Instruction inst = new Instruction(memoryInString.get(i));
			
			
			if (i % 2 == 0 && !Q0Halt){
				instQ_0.add(inst);
				inst.setqLocation(i/2);
				inst.setThread(i%2);
				InstructionQueue.isntHexEncoding_0[i/2] = memoryInString.get(i);
				InstructionQueue.instCount_0++;
				
				if (inst.getOpCode() == Instruction.HALT)
					Q0Halt = true;	
			}
			else if (i % 2 == 1 && !Q1Halt){ 
				instQ_1.add(inst);
				inst.setqLocation(i/2);
				inst.setThread(i%2);
				InstructionQueue.isntHexEncoding_1[i/2] = memoryInString.get(i);
				InstructionQueue.instCount_1++;
				
				if (inst.getOpCode() == Instruction.HALT)
					Q1Halt = true;
			}
			
			
			
		}

		
		// create the processor
		new Processor(instQ_0, instQ_1, addUnitAmount ,
				addDelay, multUnitAmount, multDelay, storeBuffAmount, loadBuffAmount, 
				addStationAmount, multStationAmount, memDelay);
		
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
        
        Long i = Long.parseLong(myString, 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return f;
		
	}
	
	private static void doWork(){
		
		while (!((CDB.totalCommits == InstructionQueue.totalIssues) &&	// committed everything that was issued
				 (AddUnit.reservationStations.isEmpty()) &&				// stations/buffers are empty of commands
				 (MultUnit.reservationStations.isEmpty()) &&
				 (LoadUnit.reservationStations.isEmpty()) &&
				 (StoreUnit.reservationStations.isEmpty()) && 
				 (InstructionQueue.halt0) &&							// reached first halt
				 (InstructionQueue.halt1))){							// reached second halt
			
			
			for (int i = 0; i < 4; i++ )
				Processor.instructionQ.attemptIssue();	// issue any new commands if possible
			
			// Pass any commands that we can to the units
			Adders.attemptPushToUnit(); 
			Multers.attemptPushToUnit();
			LoadUnit.attemptPushToUnit();
			StoreUnit.attemptPushToUnit();
			
			CDB.commit(); // write back whatever is ready to be written back
			
			Processor.CC++; // onto the next cycle
			
			//System.out.println("ADD STATION: \n" + AddUnit.reservationStations.toString());
			//System.out.println("MULT STATION: \n" + MultUnit.reservationStations.toString());
			
		}
		
	}
	

	
	
	/**
	 * Get parameter data from cfg.txt and save it in variables for later use
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
            		memory.add(hexStringToFloat(line));
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
	 * Tells apart the various different types of lines in cfg file
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
				if (temp.equals("0"))
					temp = "00000000";
				writer.println(temp);
			}
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
		
		
	}
	
	/**
	 * Writes the register data into a file, each line in file is a location in memory
	 * @param file	the path to the file we want to write in
	 * @param thread the thread we are currently working on
	 */
	private static void registersToFile(String file, int thread){
		
		
		RegisterCollection rg = null;
		
		if (thread == Processor.THREAD_0){
			rg = Processor.registers_0;
			System.out.println("Registers for thread 0:");
		}
		else if (thread == Processor.THREAD_1){
			rg = Processor.registers_1;
			System.out.println("Registers for thread 1:");
		}
		else{
			System.out.println("Attempted access to register of an undefined thread! EXITING!");
			System.exit(0);
		}
	
		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			System.out.print("[");
			int count = 0;
			for (float i : rg.getRegisters()){
				writer.println(i);
				String temp = (count == 15) ? "" : ", ";
				System.out.print("F" + (count++) + "=" + i + temp);
				
			}
			System.out.println("]");
			writer.close();
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		
	}
	
	/**
	 * Writes the trace of a thread into a file, each line in file is a location in memory
	 * @param file	the path to the file we want to write in
	 * @param thread the thread we are currently working on
	 */
	private static void traceToFile(String file, int thread){
		
		

		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			//for (int i = 0; i < Processor.InstAmount; i++){
				
			if (thread == Processor.THREAD_0){
				for (int i = 0; i < InstructionQueue.instCount_0; i++){
					writer.print(InstructionQueue.isntHexEncoding_0[i] + "\t");
					writer.print(Integer.toString(InstructionQueue.issueCC_0[i]) + "\t");
					writer.print(Integer.toString(InstructionQueue.exeCC_0[i]) + "\t");
					writer.println(Integer.toString(InstructionQueue.writeBackCC_0[i]));
				}
			
			}
			else if (thread == Processor.THREAD_1){
				for (int i = 0; i < InstructionQueue.instCount_1; i++){
					writer.print(InstructionQueue.isntHexEncoding_1[i] + "\t");
					writer.print(Integer.toString(InstructionQueue.issueCC_1[i]) + "\t");
					writer.print(Integer.toString(InstructionQueue.exeCC_1[i]) + "\t");
					writer.println(Integer.toString(InstructionQueue.writeBackCC_1[i]));
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
	 * Writes the register data into a file, each line in file is a location in memory
	 * @param file	the path to the file we want to write in
	 * @param thread the thread we are currently working on
	 */
	private static void CPIToFile(String file, int thread){
		
		int totalInstructionCount = 0;
		int totalCycleCount = 0;
		

		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");

			if (thread == Processor.THREAD_0){
				totalInstructionCount = InstructionQueue.instCount_0;
				for (int i : InstructionQueue.writeBackCC_0){
					totalCycleCount = Math.max(totalCycleCount, i);
				}
				System.out.print("CPI for thread 0: ");
			}
			else if (thread == Processor.THREAD_1){
				totalInstructionCount = InstructionQueue.instCount_1;
				for (int i : InstructionQueue.writeBackCC_1){
					totalCycleCount = Math.max(totalCycleCount, i);
				}
				System.out.print("CPI for thread 1: ");
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
}
