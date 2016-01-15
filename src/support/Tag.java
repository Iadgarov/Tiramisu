package support;


/**
 * Tag class, append this to data that is written to CDB so we know where it came from.<br>
 * 
 * (k,j,i) =  reservation k station line j thread i	<br>
 * k = 0 = ADD/SUB
 * k = 1 = MULT/DIV
 * k = 2 = LOAD
 * k = 3 = STORE
 * 
 * @author David
 *
 */
public class Tag {
	
	private int station;		// what station or buffer are we talking about?
	private int stationLine;	// what part of the buffer/station?
	private int thread;			// which thread?
	private Instruction inst;	// what's the instruction?
	
	public Tag(int station, int stationLine, int thread, Instruction inst){
		this.station = station;
		this.stationLine = stationLine;
		this.thread = thread;
		this.inst = inst;
	}
	
	@Override
	/**
	 * Compares two tags, so we know if what is on the CDB is what we want
	 * @param other object to compare to the relevant Tag
	 * @return true is Tags are the same, false otherwise
	 */
	public boolean equals(Object other){
		if(!(other instanceof Tag))
			return false;
		
		Tag that = (Tag)other;
		
		if (that.getStation() != this.getStation())
			return false;
		if (that.getStationLine() != this.getStationLine())
			return false;
		if (that.getThread() != this.getThread())
			return false;
		
		return true;
			
	}
	
	public int getStation() {
		return station;
	}
	
	public void setStation(int station) {
		this.station = station;
	}
	
	public Instruction getInstruction() {
		return inst;
	}
	
	public void setInstruction(Instruction inst) {
		this.inst = inst;
	}
	
	public int getStationLine() {
		return stationLine;
	}
	
	public void setStationLine(int stationLine) {
		this.stationLine = stationLine;
	}
	
	public int getThread() {
		return thread;
	}
	
	public void setThreadn(int thread) {
		this.thread = thread;
	}
	
	/**
	 * Turns tag object into a string, used for debugging
	 */
	public String toString(){
		
		return "Who: station = " + this.getStation() + " line = " + this.getStationLine() + " thread = " + this.getThread();
	}
}