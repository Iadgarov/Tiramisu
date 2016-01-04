package components;

import java.util.Queue;

/**
 * The instruction queue structure. responsible for issuing when possible and keeping track of instructions. 
 * @author David
 *
 */
public class InstructionQueue {
	
	public static Queue<Instruction> instructionQ_0;	// instructions for thread 0
	public static Queue<Instruction> instructionQ_1;	// instructions for thread 1
	
	// remember the hex string that encodes each instruction, will be useful 
	static String isntHexEncoding_0[] = new String[Processor.InstAmount];			// for thread 0
	static String isntHexEncoding_1[] = new String[Processor.InstAmount];			// for thread 1

	static int issueCC_0[] = new int[Processor.InstAmount];				// What CC was each command issued on for thread 0
	static int issueCC_1[] = new int[Processor.InstAmount];				// What CC was each command issued on for thread 1
	static int exeCC_0[] = new int[Processor.InstAmount];				// What CC was each command executed on for thread 0
	static int exeCC_1[] = new int[Processor.InstAmount];				// What CC was each command executed on for thread 1
	static int writeBackCC_0[] = new int[Processor.InstAmount];			// What CC was each command written back on for thread 0
	static int writeBackCC_1[] = new int[Processor.InstAmount];			// What CC was each command written back on for thread 1
	
	static int totalIssues = 0;	// total number of commands issued so far
	
	static boolean halt0 = false;	// has the first halt been reached?
	static boolean halt1 = false;	// has the second halt been reached?
	
	static int instCount_0 = 0;
	static int instCount_1 = 0;
	
	// how many instructions we've issued from each queue this CC
	static int issueCountPerCC_0 = 0;
	static int issueCountPerCC_1 = 0;
	
	public InstructionQueue( Queue<Instruction> one,  Queue<Instruction> two){
		
		instructionQ_0 = one;
		instructionQ_1 = two;
		

	}

	/**
	 * 
	 * @return True if both threads have finished all their instructions
	 */
	public boolean isEmpty(){
		return (instructionQ_0.isEmpty() && instructionQ_1.isEmpty());
	}

	
	
	/**
	 * Attempt to issue commands from each thread, if stations are full nothing changes and we keep waiting.
	 * Remember PC for each successful issue.
	 */
	public void attemptIssue() {
		
		// attempt issue
		Instruction inst = new Instruction(null);
		if (!instructionQ_0.isEmpty() && !instructionQ_1.isEmpty()){
			
			Instruction inst0 = instructionQ_0.peek();
			Instruction inst1 = instructionQ_1.peek();
			
			// choose the one that was there first (in the instruction queue)
			inst = ((inst0.getqLocation() * 2) <= (inst1.getqLocation() * 2 + 1)) ? inst0 : inst1;
			
			// unless he is a NOP.. Or we reached our issue limit for this queue
			if (inst0.getOpCode() == Instruction.NOP || issueCountPerCC_0 >= 2)
				inst = inst1;
			if (inst1.getOpCode() == Instruction.NOP || issueCountPerCC_1 >= 2)
				inst = inst0;
			
			
		}
		else if (!instructionQ_0.isEmpty()){
			inst = instructionQ_0.peek();
	
		}
			
		else if (!instructionQ_1.isEmpty()){
			inst = instructionQ_1.peek();	
		}
			
		
		int thread = inst.getThread();
		boolean result = false;
		
		//System.out.println(inst.toString() + " " + thread);
		
		switch (inst.getOpCode()){
		
			case Instruction.ADD:;
			case Instruction.SUB: result = AddUnit.acceptIntoStation(inst, thread); break;
			
			case Instruction.DIV:;
			case Instruction.MULT: result = MultUnit.acceptIntoStation(inst, thread); break;
			
			case Instruction.LD: result = LoadUnit.acceptIntoStation(inst, thread); break;
			case Instruction.ST: result = StoreUnit.acceptIntoStation(inst, thread); break;
			
			case Instruction.NOP:
				if (thread == Processor.THREAD_0)
					instructionQ_0.poll();	// remove NOP	
				else
					instructionQ_1.poll();	// remove NOP
				result = false; break;		// NOP means skip issue for this CC
				
			case Instruction.HALT: 
				if (thread == Processor.THREAD_0){ 
					halt0 = true;
					instructionQ_0.poll();	// remove HALT
				}
				else{
					halt1 = true;
					instructionQ_1.poll();	// remove HALT
				}; break;
			
			default:	System.out.println("[InstructionQueue>attemptIssue] INVALID INSTRUCTION: " + inst.toString() + " Exiting!");
						System.exit(0);	
		}
		
		// If successful issue then remove instruction from the queue and set the issue cycle for the instruction
		if (result){
			
			System.out.println("[CC = " + Processor.CC + "] Issue for thread " 
								+ inst.getThread() + " > " + inst.toString());
			
			totalIssues++;
			if (thread == Processor.THREAD_0){
				issueCountPerCC_0++;
				inst = instructionQ_0.poll();
				issueCC_0[inst.getqLocation()] = Processor.CC;
			}
			else{ 
				issueCountPerCC_1++;
				inst = instructionQ_1.poll();
				issueCC_1[inst.getqLocation()] = Processor.CC;
			}
		}
	
		
	
		
	}
}
