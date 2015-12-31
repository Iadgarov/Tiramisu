package components;

import components.Processor;
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
	Instruction[] instructions;
	// INSERT FUNCTION TO SET ALL SPOTS TO EMPTY
	int opCode[];
	float Vj[], Vk[];
	Tag Qj[], Qk[];
	int immediate[];
	int thread[]; 	// which thread sent this instruction?
	int type;		// ADD/SUB station or MULT/DIV station?
	
	
	/**
	 * Constructor for a reservation station
	 * @param size number of lines/spots in the station
	 */
	public ReservationStation(int size, int type){
		this.size = size;
		this.type = type;
		
		this.instructions = new Instruction[size];
		this.opCode = new int[size];
		
		// Values will be NULL if waiting for update from Q's
		this.Vj = new float[size];
		this.Vk = new float[size];
		
		// Will be NULL if value is already in V's
		this.Qj = new Tag[size];
		this.Qk = new Tag[size];
		
		// The immediate value:
		this.immediate = new int[size];
		
		// Which thread?
		this.thread = new int[size];
	}
	
	
	/**
	 * Add new instruction to station.
	 * Should only be called if there is room in the station
	 * Empty spot has an EMPTY instruction
	 * @param inst the new instruction object to be injected into the station
	 */
	public void addInstruction (Instruction inst, int thread){
		
		int insertHere = this.emptySpotIndex();
		if (insertHere == -1){
			System.out.println("I told you not to force addition to a full station!!! Exiting\n");
			System.exit(0);
		}
			
		this.opCode[insertHere] = inst.getOpCode();
		this.instructions[insertHere] = inst;
		this.immediate[insertHere] = inst.getImmidiate();
		
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
			this.Vj[insertHere] = registers.getRegisters()[inst.getSrc1()];
			this.Qj[insertHere] = null;
		}
		else{
			this.Vj[insertHere] = (Float) null;
			this.Qj[insertHere] = registers.getStatus()[inst.getSrc1()];	
		}	
		
		// Update status for register being changed by this command, except for STORE command, no register update there
		if (inst.getOpCode() != Instruction.ST){
			
			registers.getStatus()[inst.getDst()] = new Tag(this.type, insertHere, thread);
		}
		

	}
	
	/**
	 * Tells us if there is room in this station for more commands
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
		
		for(int i =0; i< this.size; i++){
			if (this.opCode[i] == Instruction.EMPTY)
				return i;
		}
		return -1;
	}
	
	/**
	 * gets the index of a command that is in the station and ready to be executed
	 * @return index of instruction that is ready to be executed, -1 if no such instruction exists
	 */
	public int isReadyIndex(){
		
		for(int i =0; i< this.size; i++){
			
			// if the values are ready and the pointers are not pointing then command is ready
			if (this.Vj[i] != (Float)null && this.Vk[i] != (Float)null
					&& this.Qj[i] == null && this.Qk[i] == null)
				return i;
			
			// a Load command is always ready in our case
			if (this.opCode[i] == Instruction.LD)
				return i;
			
			// a Store command only needs src1
			if (this.Vj[i] != (Float)null && this.Qj[i] == null && this.opCode[i] == Instruction.ST)
				return i;
		}
		return -1;
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
	

	
}
