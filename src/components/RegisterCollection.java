package components;

import components.ReservationStation.Tag;

public class RegisterCollection {
	
	private float registers[];
	private Tag status[];	// Tag will tell us what reservation station and what line in it and what thread are responsible for this value
	private int thread;
	
	public RegisterCollection (int thread){
		this.thread = thread;
		this.registers = new float[16];
		this.status = new Tag[16];
		
		for (int i = 0; i < 16; i ++){
			this.status[i] = null; // NULL value means that the value is in the register array as it should be
			this.registers[i] = (float)i; // initial register values set
			
		}
	}

	public float[] getRegisters() {
		return registers;
	}

	public Tag[] getStatus() {
		return status;
	}

	public int getThread() {
		return thread;
	}
	
	

}
