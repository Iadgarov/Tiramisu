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
	static String isntHexEncoding_0[];			// for thread 0
	static String isntHexEncoding_1[];			// for thread 1

	static int issueCC_0[];						// What CC was each command issued on for thread 0
	static int issueCC_1[];						// What CC was each command issued on for thread 1
	static int exeCC_0[];						// What CC was each command executed on for thread 0
	static int exeCC_1[];						// What CC was each command executed on for thread 1
	static int writeBackCC_0[];					// What CC was each command written back on for thread 0
	static int writeBackCC_1[];					// What CC was each command written back on for thread 1
	
	static int totalIssues = 0;	// total number of commands issued so far
	
	static boolean halt0 = false;	// has the first halt been reached?
	static boolean halt1 = false;	// has the second halt been reached?
	
	static int instCount_0 = 0;
	static int instCount_1 = 0;
	
	
	public InstructionQueue( Queue<Instruction> one,  Queue<Instruction> two){
		
		instructionQ_0 = one;
		instructionQ_1 = two;
		
		// CONSIDER MOVING THIS TO THE THE AREA WHERE ONE AND TWO ARE CREATED
		// Tell the instructions where they are in the queue
		for (int i = 0; i < instructionQ_0.size(); i++)
			instructionQ_0.peek().setqLocation(i);
		
		for (int i = 0; i < instructionQ_1.size(); i++)
			instructionQ_1.peek().setqLocation(i);
		
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
		
		// attempt issue for thread 0
		if (!instructionQ_0.isEmpty()){
			
			Instruction inst = instructionQ_0.peek();
			boolean result = false;
			
			switch (inst.getOpCode()){
			
				case Instruction.ADD:;
				case Instruction.SUB: result = AddUnit.acceptIntoStation(inst, Processor.THREAD_0); break;
				
				case Instruction.DIV:;
				case Instruction.MULT: result = MultUnit.acceptIntoStation(inst, Processor.THREAD_0); break;
				
				case Instruction.LD: result = LoadUnit.acceptIntoStation(inst, Processor.THREAD_0); break;
				case Instruction.ST: result = LoadUnit.acceptIntoStation(inst, Processor.THREAD_0); break;
				
				case Instruction.HALT: halt0 = true; break;

				default:	System.out.println("INVALID INSTRUCTION: " + inst.toString() + " Exiting!");
							System.exit(0);	
			}
			
			// If successful issue then remove instruction from the queue and set the issue cycle for the instruction
			if (result){
				totalIssues++;
				inst = instructionQ_0.poll();
				issueCC_0[inst.getqLocation()] = Processor.CC;
			}
	
		}
		
		// attempt issue for thread 1
		if (!instructionQ_1.isEmpty()){
			
			Instruction inst = instructionQ_1.peek();
			boolean result = false;
			
			switch (inst.getOpCode()){
			
				case Instruction.ADD:;
				case Instruction.SUB: result = AddUnit.acceptIntoStation(inst, Processor.THREAD_1); break;
				
				case Instruction.DIV:;
				case Instruction.MULT: result = MultUnit.acceptIntoStation(inst, Processor.THREAD_1); break;
				
				case Instruction.LD: result = LoadUnit.acceptIntoStation(inst, Processor.THREAD_1); break;
				case Instruction.ST: result = LoadUnit.acceptIntoStation(inst, Processor.THREAD_1); break;
				
				case Instruction.HALT: halt1 = true; break;
				
				default:	System.out.println("INVALID INSTRUCTION: " + inst.toString() + " Exiting!");
							System.exit(0);
					
			}
			
			// If successful issue then remove instruction from the queue and set the issue cycle for the instruction
			if (result){
				inst = instructionQ_1.poll();
				issueCC_1[inst.getqLocation()] = Processor.CC;	
			}
	
		}
		
		
		
	}
}
