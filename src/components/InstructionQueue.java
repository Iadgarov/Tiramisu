package components;

import java.util.Queue;

import support.Instruction;

/**
 * The instruction queue structure. responsible for issuing when possible and keeping track of instructions. 
 * Keeps track of when each instruction was issued/executed/written back
 * Keeps all sorts of data in parameters for convenience
 * @author David
 *
 */
public class InstructionQueue {
	
	public static Queue<Instruction> instructionQ_0;	// instructions for thread 0
	public static Queue<Instruction> instructionQ_1;	// instructions for thread 1
	
	// remember the hex string that encodes each instruction, will be useful 
	private static String isntHexEncoding_0[] = new String[Processor.InstAmount];			// for thread 0
	private static String isntHexEncoding_1[] = new String[Processor.InstAmount];			// for thread 1

	private static int issueCC_0[] = new int[Processor.InstAmount];				// What CC was each command issued on for thread 0
	private static int issueCC_1[] = new int[Processor.InstAmount];				// What CC was each command issued on for thread 1
	private static int exeCC_0[] = new int[Processor.InstAmount];				// What CC was each command executed on for thread 0
	private static int exeCC_1[] = new int[Processor.InstAmount];				// What CC was each command executed on for thread 1
	private static int writeBackCC_0[] = new int[Processor.InstAmount];			// What CC was each command written back on for thread 0
	private static int writeBackCC_1[] = new int[Processor.InstAmount];			// What CC was each command written back on for thread 1
	
	private static int totalIssues = 0;	// total number of commands issued so far
	
	private static boolean halt0 = false;	// has the first halt been reached?
	private static boolean halt1 = false;	// has the second halt been reached?
	
	private static int instCount_0 = 0;
	private static int instCount_1 = 0;
	
	// how many instructions we've issued from each queue this CC
	static int issueCountPerCC_0 = 0;
	static int issueCountPerCC_1 = 0;
	
	/**
	 * Instruction constructor
	 * @param one	first instruction queue that main sent us, for thread 0
	 * @param two	second instruction queue that main sent us, for thread 1
	 */
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
	 * Issues commands in order. That is if there is a command in thread 1's queue that showed up first in
	 * the memory compared to the one in thread 0's queue it will be issued first.
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
					setHalt0(true);
					instructionQ_0.poll();	// remove HALT
				}
				else{
					setHalt1(true);
					instructionQ_1.poll();	// remove HALT
				}; break;
			
			default:	System.out.println("[InstructionQueue>attemptIssue] INVALID INSTRUCTION: " + inst.toString() + " Exiting!");
						System.exit(0);	
		}
		
		// If successful issue then remove instruction from the queue and set the issue cycle for the instruction
		if (result){
			
			System.out.println("[CC = " + Processor.CC + "] Issue for thread " 
								+ inst.getThread() + " > " + inst.toString());
			
			setTotalIssues(getTotalIssues() + 1);
			if (thread == Processor.THREAD_0){
				issueCountPerCC_0++;
				inst = instructionQ_0.poll();
				getIssueCC_0()[inst.getqLocation()] = Processor.CC;
			}
			else{ 
				issueCountPerCC_1++;
				inst = instructionQ_1.poll();
				getIssueCC_1()[inst.getqLocation()] = Processor.CC;
			}
		}
	
		
	
		
	}

	public static int[] getIssueCC_0() {
		return issueCC_0;
	}

	public static void setIssueCC_0(int issueCC_0[]) {
		InstructionQueue.issueCC_0 = issueCC_0;
	}

	public static int[] getExeCC_0() {
		return exeCC_0;
	}

	public static void setExeCC_0(int exeCC_0[]) {
		InstructionQueue.exeCC_0 = exeCC_0;
	}

	public static int[] getIssueCC_1() {
		return issueCC_1;
	}

	public static void setIssueCC_1(int issueCC_1[]) {
		InstructionQueue.issueCC_1 = issueCC_1;
	}

	public static int[] getExeCC_1() {
		return exeCC_1;
	}

	public static void setExeCC_1(int exeCC_1[]) {
		InstructionQueue.exeCC_1 = exeCC_1;
	}

	public static int getInstCount_0() {
		return instCount_0;
	}

	public static void setInstCount_0(int instCount_0) {
		InstructionQueue.instCount_0 = instCount_0;
	}

	public static int[] getWriteBackCC_0() {
		return writeBackCC_0;
	}

	public static void setWriteBackCC_0(int writeBackCC_0[]) {
		InstructionQueue.writeBackCC_0 = writeBackCC_0;
	}

	public static int getInstCount_1() {
		return instCount_1;
	}

	public static void setInstCount_1(int instCount_1) {
		InstructionQueue.instCount_1 = instCount_1;
	}

	public static int[] getWriteBackCC_1() {
		return writeBackCC_1;
	}

	public static void setWriteBackCC_1(int writeBackCC_1[]) {
		InstructionQueue.writeBackCC_1 = writeBackCC_1;
	}

	public static String[] getIsntHexEncoding_1() {
		return isntHexEncoding_1;
	}

	public static void setIsntHexEncoding_1(String isntHexEncoding_1[]) {
		InstructionQueue.isntHexEncoding_1 = isntHexEncoding_1;
	}

	public static String[] getIsntHexEncoding_0() {
		return isntHexEncoding_0;
	}

	public static void setIsntHexEncoding_0(String isntHexEncoding_0[]) {
		InstructionQueue.isntHexEncoding_0 = isntHexEncoding_0;
	}

	public static int getTotalIssues() {
		return totalIssues;
	}

	public static void setTotalIssues(int totalIssues) {
		InstructionQueue.totalIssues = totalIssues;
	}

	public static boolean isHalt0() {
		return halt0;
	}

	public static void setHalt0(boolean halt0) {
		InstructionQueue.halt0 = halt0;
	}

	public static boolean isHalt1() {
		return halt1;
	}

	public static void setHalt1(boolean halt1) {
		InstructionQueue.halt1 = halt1;
	}
}
