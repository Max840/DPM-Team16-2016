package dpmCompetition;

import lejos.hardware.Sound;

/**
 * Provides robot the capabilities of traveling to different destinations
 * 
 */
public class Driver {
	
	/** Reference to the odometer thread used by the robot */ 
	private Odometer odometer;
	/** Reference to the navigation used by the robot */
	private Navigation navigation;
	/** Reference to the Competition's data */
	private CompetitionData competitionData;
	/** Reference to the Scanner */
	private AreaScanner areaScanner;

	/** The acceptable distance for the robot to determine if it is close enough to the target */
	private static final double acceptableError = 2;
	/** The time interval before continuing the next loop */
	private final int TIMEOUT_PERIOD = 20;
    /** The offset for the robot to travel to the center of a square  */
	private final double squareOffset = 15.0;

	
	/**
	 * Constructor 
	 * 
	 * @param odometer a reference to the odometer thread used by the robot.
	 * @param navigation a reference to the navigation use by the robot
	 * @param competitionData a reference to the competition's data
	 * @param areaScanner a reference to the scanner
	 */
	Driver(Odometer odometer, Navigation navigation, CompetitionData competitionData, AreaScanner areaScanner) {

		this.odometer = odometer;
		this.navigation = navigation;
		this.competitionData = competitionData;
		this.areaScanner = areaScanner;

	}

	/**
	 * The robot travels to the closest blue block.
	 */
	public void travelToBlueBlock() {
		Coordinate[] blueBlocks = areaScanner.findCloseObjects();
		Coordinate[] destinations = new Coordinate[4];
		destinations[0] = new Coordinate();
		destinations[0].x = 0;
		destinations[0].y = 0;
		destinations[1] = new Coordinate();
		destinations[1].x = 0;
		destinations[1].y = 30;
		destinations[2] = new Coordinate();
		destinations[2].x = 30;
		destinations[2].y = 30;
		destinations[3] = new Coordinate();
		destinations[3].x = 30;
		destinations[3].y = 0;
		int j = 1;
		/*
		while (blueBlocks.length == 0){
			Sound.beep();
			navigation.turn(-90);
			//navigation.turn(Math.random()*(-90));
			//navigation.goForward(30);
			blueBlocks = areaScanner.findCloseObjects();
		}*/
		while (blueBlocks.length == 0){
			
			travelTo(destinations[j%4].x, destinations[j%4].y);
			navigation.turnTo(0);
			blueBlocks = areaScanner.findCloseObjects();
			j++;
			
		}
		int index = 0;
		double odometerX = odometer.getX();
		double odometerY = odometer.getY();
		double minDist = Math
				.sqrt(Math.pow(blueBlocks[0].x - odometerX, 2) + Math.pow(blueBlocks[0].y - odometerY, 2));
		for (int i = 1; i < blueBlocks.length; i++) {
			double dist = Math
					.sqrt(Math.pow(blueBlocks[i].x - odometerX, 2) + Math.pow(blueBlocks[i].y - odometerY, 2));
			if (dist < minDist){
				minDist = dist;
				index = i;
			}
		}
		Display.print(blueBlocks[index].x, 4);
		Display.print(blueBlocks[index].y, 5);
		travelTo(blueBlocks[index].x, blueBlocks[index].y);
	}

	// only consider the case that the robot will only go to the green zone once
	/**
	 * The robot travels to the Green zone, which its coordinates are fetched from the 
	 * competition's data
	 */
	public void travelToGreenZone() {
		double x = (competitionData.greenZone.lowerLeft.x + competitionData.greenZone.upperRight.x)/2;
		double y = (competitionData.greenZone.lowerLeft.y + competitionData.greenZone.upperRight.y)/2;

		travelTo(x, y);
	}

	/**
	 * The robot will travel to the x and y coordinates
	 * 
	 * @param x 
	 * @param y 
	 */
	public void travelTo(double x, double y) {

		boolean traveling = true;

		while (traveling) {
			double distX = x - odometer.getX();
			double distY = y - odometer.getY();
			double distanceFromTarget = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));

			if (distanceFromTarget < acceptableError) {
				navigation.stopMoving();
				traveling = false;
				continue;
			}

			// Calculate the angle the plant needs to face in order to get to
			// the target
			double angle = Math.toDegrees(Math.atan2(distY, distX));

			// Turn only if the minimal angle to turn is larger than 50 degrees
			// (in any direction)
			// Prevents the plant from doing a lot of small turns that could
			// induce more error in the odometer.
			if (Navigation.minimalAngleDifference(odometer.getTheta(), angle) > acceptableError
					|| Navigation.minimalAngleDifference(odometer.getTheta(), angle) < -acceptableError) {
				navigation.turnTo(angle);
			}

			// After turning, go forward in the new direction.
			navigation.goForward();

			try {
				Thread.sleep(TIMEOUT_PERIOD);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
