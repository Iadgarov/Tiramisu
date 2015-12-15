package components;

import java.util.ArrayList;
import java.util.List;

/**
 * the MUL/DIV class. A processing unit for floating point multiplication and division. 
 * @author David
 *
 */
public class MultUnit{
	
	private static int executionDelay;
	private static List<ReservationStation> reservationStations;
	
	/**
	 * construct a MUL/DIV unit

	 */
	public MultUnit() {
		
		MultUnit.executionDelay = Processor.addUnitDelay;
		MultUnit.reservationStations = createStations();
		
	}

	/**
	 * construct reservation stations for all MUL/DIV units
	 * @return list of reservation stations for MUL/DIV unit
	 */
	private List<ReservationStation> createStations() {
		
		ArrayList<ReservationStation> returnMe = new ArrayList<ReservationStation>();
		int temp = Processor.multResevationStationNumber;
		for (int i = 0; i < temp; i++){
			returnMe.add(new ReservationStation(temp));
		}
		
		return returnMe;
	}

	/**
	 * Doing the actual calculation
	 * @param x	first number to do calculation with
	 * @param y	second number to do calculation with
	 * @param command if command = true its a MUL command else DIV
	 * @return floating point result
	 */
	public float execute(float x, float y, boolean command) {
		
		if (command)
			return x * y;
		return x / y;
	}

}
