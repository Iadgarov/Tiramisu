package components;

public class Instruction {
	
	// consts to represent type of command
	final static int EMPTY = -1; // empty reservation station line
	final static int NOP = 0;
	final static int LD = 1;
	final static int ST = 2;
	final static int ADD = 3;
	final static int SUB = 4;
	final static int MULT = 5;
	final static int DIV = 6;
	final static int HALT = 7;
	

	
	private int opCode;
	private int dst;
	private int src0;
	private int src1;
	private int immidiate;
	private int qLocation; 	// where is it in the instruction Queue
	private int thread;		// which thread does it belong to
	
	/**
	 * Takes command line string from memory array and converts it into an instruction
	 * @param commandLine a line in binary, 32 bits, parse this into a command
	 */
	public Instruction(String commandLine){
		
		this.opCode		= Integer.parseInt(commandLine.substring(0, 1), 16);
		this.dst 		= Integer.parseInt(commandLine.substring(1, 2), 16);
		this.src0 		= Integer.parseInt(commandLine.substring(2, 3), 16);
		this.src1 		= Integer.parseInt(commandLine.substring(3, 4), 16);
		this.immidiate 	= Integer.parseInt(commandLine.substring(4, 8), 16);

	}
	

	/**
	 * 
	 * @return True if this instruction is of the ADD/SUB type
	 */
	public boolean isADD_SUB (){
		return (this.opCode == ADD || this.opCode == SUB);
	}
	
	/**
	 * 
	 * @return True if this instruction is of the MULT/DIV type
	 */
	public boolean isMULT_DIV (){
		return (this.opCode == MULT || this.opCode == DIV);
	}
	
	/**
	 * 
	 * @return True if this instruction is of the LOAD type
	 */
	public boolean isLOAD (){
		return (this.opCode == LD);
	}
	
	/**
	 * 
	 * @return True if this instruction is of the STORE type
	 */
	public boolean isSTORE (){
		return (this.opCode == ST);
	}

	
	public int getqLocation() {
		return qLocation;
	}

	public void setqLocation(int qLocation) {
		this.qLocation = qLocation;
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		this.thread = thread;
	}
	
	public int getOpCode() {
		return opCode;
	}

	public int getDst() {
		return dst;
	}

	public int getSrc0() {
		return src0;
	}

	public int getSrc1() {
		return src1;
	}

	public int getImmidiate() {
		return immidiate;
	}
	
	public String toString(){
		
		String returnMe = "";
		switch( this.opCode){
		
			case EMPTY: returnMe += "NO-COMMAND "; break;
			case NOP: 	returnMe += "NOP "; break;
			case LD:	returnMe += "LOAD F" + this.dst + " " + this.immidiate + "(" + this.src0 + ")\n"; break;
			case ST:	returnMe += "STORE F" + this.src0 + " " + this.immidiate + "(" + this.dst + ")\n"; break;
			case ADD:	returnMe += "ADD F" + this.dst + " F" + this.src0 + " F" + this.src1 + "\n"; break;
			case SUB:	returnMe +=	"SUB F" + this.dst + " F" + this.src0 + " F" + this.src1 + "\n"; break;
			case MULT:	returnMe += "MULT F" + this.dst + " F" + this.src0 + " F" + this.src1 + "\n"; break;
			case DIV:	returnMe += "DIV F" + this.dst + " F" + this.src0 + " F" + this.src1 + "\n"; break;
			case HALT:	returnMe += "HALT"; break;
		
		}
		
		return returnMe;
	}
	
	
}












