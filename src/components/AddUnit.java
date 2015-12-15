package components;

import java.util.ArrayList;
import java.util.List;

public class AddUnit{
	
	private static int executionDelay;
	private static List<ReservationStation> reservationStations;
	
	/**
	 * construct a ADD/SUB unit
	 */
	public AddUnit() {
		
		AddUnit.executionDelay = Processor.addUnitDelay;
		AddUnit.reservationStations = createStations();
		
	}

	/**
	 * construct reservation stations for all ADD/SUB units
	 * @return	list of reservation stations for ADD/SUB unit
	 */
	private List<ReservationStation> createStations() {
		
		ArrayList<ReservationStation> returnMe = new ArrayList<ReservationStation>();
		int temp = Processor.addReservationStationNumber;
		for (int i = 0; i < temp; i++){
			returnMe.add(new ReservationStation(temp));
		}
		
		return returnMe;
	}

	/**
	 * Doing the actual calculation
	 * @param x	first number to do calculation with
	 * @param y	second number to do calculation with
	 * @param command if command = true its an ADD command else SUB
	 * @return floating point result
	 */
	public float execute(float x, float y, boolean command) {
		
		if (command)
			return x + y;
		return x - y;
	}

}
