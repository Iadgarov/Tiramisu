package components;

import java.util.ArrayList;

import collections.RegisterCollection;
import components.Processor;
import support.Instruction;
import support.Tag;
/**
 * Reservation station class
 * @author David
 *
 */
public class ReservationStation {
	

	
	// tags representing which reservation station we want
	public final static int ADD_REPOSITORY = 0; 
	public final static int MUL_REPOSITORY = 1;
	public final static int LOAD_REPOSITORY = 2;
	public final static int STORE_REPOSITORY = 3;
	

	private int size;
	private Instruction[] instructions;
	
	int opCode[];
	Float Vj[], Vk[];
	Tag Qj[], Qk[];
	int immediate[];
	int thread[]; 			// which thread sent this instruction?
	int acceptedWhen[]; 	// CC that this got into the station
	private boolean inExecution[];	// did we already push this to unit? Don't want to push something twice..
	int type;				// ADD/SUB station or MULT/DIV station?
	
	private int entryNumber = 0;
	
	
	/**
	 * Constructor for a reservation station
	 * @param size number of lines/spots in the station
	 * @param type the type of station we want (what unit is this a station for?)
	 */
	public ReservationStation(int size, int type){
		this.size = size;
		this.type = type;
		
		this.setInstructions(new Instruction[size]);
		this.opCode = new int[size];
		
		// initialize all stations to empty
		for (int i = 0; i < this.opCode.length; i++)
			this.opCode[i] = Instruction.EMPTY;
		
		// Values will be NULL if waiting for update from Q's
		this.Vj = new Float[size];
		this.Vk = new Float[size];
		
		// Will be NULL if value is already in V's
		this.Qj = new Tag[size];
		this.Qk = new Tag[size];
		
		// The immediate value:
		this.immediate = new int[size];
		
		// The CC when command joined the station
		this.acceptedWhen = new int[size];
		
		// Executing or not?
		this.setInExecution(new boolean[size]);
		for (int i = 0; i < size; i++)
			this.getInExecution()[i] = false;	// set inital state
		
		// Which thread?
		this.thread = new int[size];
	}
	
	
	/**
	 * Add new instruction to station.<br>
	 * Should only be called if there is room in the station<br>
	 * Empty spot has an EMPTY instruction
	 * @param inst the new instruction object to be injected into the station
	 * @param thread the thread to whom the instruction belongs
	 */
	public void addInstruction (Instruction inst, int thread){
		
		int insertHere = this.emptySpotIndex();
		if (insertHere == -1){
			System.out.println("I told you not to force addition to a full station!!! Exiting\n");
			System.exit(0);
		}
		
		// was useful for testing at one point. I shall leave it as a memorial to the good times
		if (inst.getThread() != thread)
			System.out.println("SOMETHING IS WRONG! Instruction: " + inst.toString() + 
					" thinks thread is " + inst.getThread() + 
					" But thread is " + thread);
			
		this.opCode[insertHere] = inst.getOpCode();
		this.getInstructions()[insertHere] = inst;
		this.immediate[insertHere] = inst.getImmidiate();
		this.acceptedWhen[insertHere] = entryNumber++;
		
		RegisterCollection registers;
		// WHICH THREAD? 
		if (thread == Processor.THREAD_0)
			registers = Processor.registers_0;
		else
			registers = Processor.registers_1;
	
		
		// if status is null then take value from register, otherwise set Q to status 
		// For the J:
		if  (registers.getStatus()[inst.getSrc0()] == null){
			this.Vj[insertHere] = registers.getRegisters()[inst.getSrc0()];
			this.Qj[insertHere] = null;
		}
		else{
			this.Vj[insertHere] = (Float) null;
			this.Qj[insertHere] = registers.getStatus()[inst.getSrc0()];	
		}
		// For the K:
		if  (registers.getStatus()[inst.getSrc1()] == null){
			this.Vk[insertHere] = registers.getRegisters()[inst.getSrc1()];
			this.Qk[insertHere] = null;
		}
		else{
			this.Vk[insertHere] = (Float) null;
			this.Qk[insertHere] = registers.getStatus()[inst.getSrc1()];	
		}	
		
		// Update status for register being changed by this command, except for STORE command, no register update there
		if (inst.getOpCode() != Instruction.ST){
			
			registers.getStatus()[inst.getDst()] = new Tag(this.type, insertHere, thread, inst);
			System.out.print("");
		}
		

	}
	
	/**
	 * Tells us if there is room in this station for more commands<br>
	 * Uses emptySpotIndex method, compared its result to -1
	 * @return true if full, false otherwise
	 */
	public boolean isFull (){
		
		int temp = emptySpotIndex();
		return ( temp == -1) ? true : false;
	}
	
	/**
	 * Iterate over all lines in station, if one is empty return its location
	 * @return location of empty line or -1 if station is full
	 */
	public int emptySpotIndex(){
		
		for(int i = 0; i < this.size; i++){
			if (this.opCode[i] == Instruction.EMPTY)
				return i;
		}
		return -1;
	}
	
	/**
	 * gets the index of a command that is in the station and ready to be executed<br>
	 * If there are several, choose the one with the smallest issue CC!!!<br>
	 * Why? because for some reason I thought it was smart at the time. meh..
	 * @return index of instruction that is ready to be executed, -1 if no such instruction exists
	 */
	public int isReadyIndex(){
		
		ArrayList<Integer> temp = new ArrayList<>();
			
		
		// gather all ready commands
		for(int i = 0; i < this.size; i++){
			
			// if the values are ready and the pointers are not pointing then command is ready
			if (this.Vj[i] != null && this.Vk[i] != null
					&& this.Qj[i] == null && this.Qk[i] == null && !this.getInExecution()[i])
				temp.add(i);
			
			// a Load command is always ready in our case
			if (this.opCode[i] == Instruction.LD && !this.getInExecution()[i])
				temp.add(i);
			
			// a Store command only needs src1
			if (this.Vj[i] != (Float)null && this.Qj[i] == null && 
					this.opCode[i] == Instruction.ST && !this.getInExecution()[i])
				temp.add(i);
		}
		
		if (temp.isEmpty())
			return -1;
		
		// choose oldest ready command
		int returnMe = temp.get(0);
		for (int i : temp){
			if (this.acceptedWhen[i] < this.acceptedWhen[returnMe])
				returnMe = i;
		}
		
		return returnMe;
	}
	
	
	/**
	 * checks if this station has any commands in it 
	 * @return	true if the station is empty , false otherwise
	 */
	public boolean isEmpty(){
		
		for(int i =0; i< this.size; i++){
			if (this.opCode[i] != Instruction.EMPTY)
				return false;
		}
		return true;
		
	}
	
	/**
	 * Converts a reservation station to a string
	 * Used for debugging
	 */
	public String toString(){
		
		String returnMe = "";
		
		String a = "",b = ""; 
		
		for (int i = 0; i < this.size; i++){
			returnMe += "[Line " + i + "]: ";
			if (this.getInstructions()[i] == null)
				returnMe += "NULL\n";
			else{
				
				if ( this.Qj[i] != null){
					a = this.Qj[i].toString();
				}
				if ( this.Qk[i] != null){
					b = this.Qk[i].toString();
				}
				returnMe += this.getInstructions()[i].toString() + " Needs: \n" + a + "\n" + b ;
				
			}
		}
		return returnMe;
		
	}


	public Instruction[] getInstructions() {
		return instructions;
	}


	public void setInstructions(Instruction[] instructions) {
		this.instructions = instructions;
	}


	public boolean[] getInExecution() {
		return inExecution;
	}


	public void setInExecution(boolean inExecution[]) {
		this.inExecution = inExecution;
	}
	

	
}
