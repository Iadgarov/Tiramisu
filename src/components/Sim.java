package components;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;

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
	private final static String STORE_BUFF_AMOUNT	= "mem_nr_load_buffers";
	
	private  static int addUnitAmount;
	private  static int multUnitAmount;
	private  static int addDelay;
	private  static int multDelay;
	private  static int memDelay;
	private  static int addStationAmount;
	private  static int multStationAmount;
	private  static int loadBuffAmount;
	private  static int storeBuffAmount;
	
	//static String cfg;
	private static float memory[] = new float[Processor.MEMORY_SIZE];

	public static void main(String[] args) {
	
		// The memory, we will populate this from the mem file
		
		
		// get parameters from cfg
		receive(true, args[1]);
		// set up initial memory picture
		receive(false, args[2]);
		
		Queue<Instruction> instQ_0 = new PriorityQueue<Instruction>();
		Queue<Instruction> instQ_1 = new PriorityQueue<Instruction>();
		
		// create the two queues of instructions
		
		boolean Q0Halt = false;	// has queue 0 reached halt?
		boolean Q1Halt = false;	// has queue 1 reached halt?
		for (int i = 0 ;; i++){
			
			if ((i % 2 == 0) && !Q0Halt){
				instQ_0.add(new Instruction((Float.toHexString(memory[i]))));
				if (instQ_0.peek().getOpCode() == Instruction.HALT)
					Q0Halt = true;	
			}
			else if (!Q1Halt){ 
				instQ_1.add(new Instruction((Float.toHexString(memory[i]))));
				if (instQ_1.peek().getOpCode() == Instruction.HALT)
					Q1Halt = true;
			}
			else
				break;
			
		}
		
		// create the processor
		Processor CPU = new Processor(memory, instQ_0, instQ_1, addUnitAmount,
				addDelay, multUnitAmount, multDelay, storeBuffAmount, loadBuffAmount, 
				addStationAmount, multStationAmount);
		
		
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
		
		while (NOT DONE){
			
			Processor.instructionQ.attemptIssue();	// issue any new commands if possible
			// Pass any commands that we can to the units
			Adders.attemptPushToUnit(); 
			Multers.attemptPushToUnit();
			LoadUnit.attemptPushToUnit();
			StoreUnit.attemptPushToUnit();
			
			CDB.commit(); // write back whatever is ready to be written back
			
			Processor.CC++; // onto the next cycle
			
		}
		
		// write back all the data to the files
		
		
	}
	

	
	
	/**
	 * Get parameter data from cfg.txt and save it in variables for later use
	 * @param whatDo if true then read data for parameters from cfg, if false read memory file
	 */
	private static void receive(boolean whatDo, String file){
		
		String line;
		int i = 0;
		
        try {
        
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	
            	if (whatDo)
            		getCfgData(line);
            	else
            		memory[i++] = hexStringToFloat(line);
      
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
		
    	if (line.contains(ADD_UNIT_AMOUNT))
    		addUnitAmount = getDataFromSring(line);
    	else if (line.contains(MULT_UNIT_AMOUNT))
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
		
		st.replaceAll("\\s+", "");	// remove white spaces
		int i = st.indexOf('='); 	// find equation sign, data is after this
		String data = st.substring(i + 1, st.length());
		return Integer.parseInt(data);
	}

}
