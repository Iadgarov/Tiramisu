package collections;

import support.Tag;

/**
 * The register set class. <br>
 * Basically a flat array for each thread.<br>
 * We shall create one of these for each thread
 * @author David
 *
 */
public class RegisterCollection {
	
	private float registers[];	// holds values of each register
	private Tag status[];	// Tag will tell us what reservation station and what line in it and what thread are responsible for this value
	private int thread;
	
	/**
	 * Constructor for a register collection. Get's called for each thread.
	 * creates the register float array and a register status array.
	 * @param thread	thread for whom this collection is for
	 */
	public RegisterCollection (int thread){
		this.thread = thread;
		this.registers = new float[16];
		this.status = new Tag[16];
		
		for (int i = 0; i < 16; i ++){
			this.status[i] = null; // NULL value means that the value is in the register array as it should be
			this.registers[i] = (float)i; // initial register values set
			
		}
	}

	/**
	 * Get registers for calling thread.
	 * @return registers for calling thread.
	 */
	public float[] getRegisters() {
		return registers;
	}

	/**
	 * Sets status for a register in a threads collection. Marks what reservation station command is resposible
	 * for this registers value.
	 * If NULL then register is updated in collection.
	 * @return array of Tag class instances of he who is responsible for each registers value.
	 */
	public Tag[] getStatus() {
		return status;
	}

	/**
	 * Get thread to whom this register colection belongs too
	 * @return thread to whom this register colection belongs too
	 */
	public int getThread() {
		return thread;
	}
	
	

}
