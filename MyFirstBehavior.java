package infovk.l_schepp24;

import static infovk.l_schepp24.Wrappers.*;
import static infovk.l_schepp24.Utils.*;
import static java.awt.Color.*;

public class MyFirstBehavior extends SimpleRobotBehavior {
	//Ged�chtnissektion

	public void debug(String str) {
		System.out.println(str);
	}

	public MyFirstBehavior(SimpleRobot  robot) {
		super(robot);
	}

	Point enemyPosition;
	double distance;
	double gunHeat;


	
	@Override
	public void start() {
		turnRadar(720);
	}

	@Override
	void execute() {
		gunHeat = getGunHeat();
		if (hasScannedRobot()) {
			ScannedRobotEvent ev = getScannedRobotEvent();
			distance = getDistance(ev);
			double deg = getBearing(ev) + getHeading();
			double radarTurn = normalRelativeAngle(deg - getRadarHeading());
			double gunTurn = normalRelativeAngle(deg - getGunHeading());
			turnRadar(radarTurn * 1.10);
			turnGun(gunTurn);

			enemyPosition = add(pointFromPolarCoordinates(deg, distance), getPoint());
		} else {
			turnRadar(-360);
		}
		cicle(distance);
		debug(String.valueOf(getHeading()));
		if (distance < 200) {
			double vel = 20 - 3 * 3;
			fireBullet(3);
		} else if (distance > 200 && distance < 500) {
			fireBullet(2);
		} else {
			fireBullet(1);
		}
	}

	void cicle(double distance) {
		int dist = 20;
		int deg = 50;
		boolean rand = randomBoolean();
		if (hasHitWall()) {
			HitWallEvent hit = getHitWallEvent();
			ahead(-200);
		}
		if (rand) {
			ahead(dist);
		} else {
			ahead(-dist);
		}
		turn(deg);
	}
}
