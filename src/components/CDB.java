package components;

import components.ReservationStation.Tag;

public class CDB {

	/**
	 * gets calculation result and who sent it. Updates anyone that is waiting for this data
	 * @param result data that was calculated
	 * @param tag	who sent the data
	 */
	public static void writeToCDB(float result, Tag tag) {
		
		// First the reservation stations:
		// ADD/SUB stations:
		ReservationStation rs = AddUnit.reservationStations;
		
	}
	
	

}
