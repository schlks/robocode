package infovk.l_schepp24;

import java.awt.*;
import java.util.List;

import static infovk.l_schepp24.Wrappers.*;
import static infovk.l_schepp24.Utils.*;
import static java.awt.Color.*;

public class SchlaganfallBehavior extends SimpleRobotBehavior {
	//Ged�chtnissektion

	public void debug(String str) {
		System.out.println(str);
	}

	public SchlaganfallBehavior(SimpleRobot  robot) {
		super(robot);
	}

	Point enemyPosition;
	Point relativeEnemyPosition;
	Point lastdistance;
	double distance;
	double gunHeat;
	double height;
	double width;
	double gunTurn;
	List <Point> lastandcurrentpos = createEmptyList();
	//java.awt.Graphics2D canvas = getGraphics();
	boolean neg = false;
	double cooldown = 0;
	double velocity = 0;
	double bearing = 0;
	int direction = 1;
	int turnDirection = 1;
	ScannedRobotEvent ev = null;

	@Override
	public void start() {
		java.awt.Graphics2D canvas = getGraphics();
		for (int i = 1; i <= 2; i++){
			lastandcurrentpos = insertBack(lastandcurrentpos, getPoint());
		}
		height = getBattleFieldHeight();
		width = getBattleFieldWidth();
		debug("-------------------");
		debug("Height: " + String.valueOf(height));
		debug("Width:  " + String.valueOf(width));
		debug("-------------------");
		turnRadar(720);
		Color color = MAGENTA;
		setColors(color, color, color, color, color);
	}

	@Override
	void execute() {
		gunHeat = getGunHeat();
		if (hasScannedRobot()) {
			ev = getScannedRobotEvent();
			distance = getDistance(ev);
			velocity = Wrappers.getVelocity(ev);
			bearing = getBearing(ev);
			double deg = bearing + getHeading();
			double radarTurn = normalRelativeAngle(deg - getRadarHeading());
			gunTurn = normalRelativeAngle(deg - getGunHeading());
			turnRadar(radarTurn * 1.10);
			turnGun(gunTurn);
			relativeEnemyPosition = pointFromPolarCoordinates(deg, distance);
			enemyPosition = add(relativeEnemyPosition, getPoint());
			if (length(lastandcurrentpos) == 2) {
				lastandcurrentpos = insertBack(lastandcurrentpos, enemyPosition);
				lastandcurrentpos = removeFront(lastandcurrentpos);
			} else {
				lastandcurrentpos = insertBack(lastandcurrentpos, enemyPosition);
			}
		} else {
			turnRadar(-360);
		}
		double power = 0;
		//turnGun(adjust);
		movement(distance);
		if (distance <= 200) {
			power = 3;
		} else if (distance > 200 && distance <= 400) {
			power = 2;
		}else if (distance > 400 && distance <= 600) {
			power = 1;
		} else {
			power = 0.5;
		}
		if (getVelocity() != 0) {
			aimbot(power, getLast(lastandcurrentpos), getFirst(lastandcurrentpos));
		}
		fireBullet(power);
	}

	double preaimAngle(Point current, Point distance) {
		double distancelength = length(distance);
		//Point absolute = add(current, distance);
		Point absolute = distance;
		paintLine(getPoint(), current, RED);
		paintLine(getPoint(), absolute, YELLOW);
		paintLine(current, absolute, BLUE);
		double angle = angleBetween(current, getPoint());
		angle = angle - getGunHeading();
		if (angle >= -360 && angle < 0) {
			angle += 360;
		} else if (angle <= 360 && angle > 0) {
			angle -= 360;
		}
		angle = normalRelativeAngle(angle);
		return angle;
	}

	void aimbot(double power, Point posEnemy, Point lastPosEnemy) {
		Point enemyDistanceTravl = subtract(posEnemy, lastPosEnemy);
		double velEnemy = length(enemyDistanceTravl);
		double velBullet = 20 - 3 * power;
		double limit = distance / velBullet;
		//debug(String.valueOf((int)limit));
		//   Point ownPos = getPoint();
		Point newPosEnemy = pointFromCoordinates(0, 0);
		double angle = 0;
		for (int t=1; t<=(int)limit; t++) {
			newPosEnemy = add(posEnemy, enemyDistanceTravl);/* Richtung beachten */
			//   Point vecToEnemy = subtract(ownPos, newPosEnemy);
			//   Point newPosBullet = add(ownPos, multiply(normalize(vecToEnemy), velBullet*t)); /* Richtung beachten */
			// hitbox nachgucken
			posEnemy = newPosEnemy;

			//if ((distance(newPosBullet, newPosEnemy) < 26 )) {
			//	turnGun();
			//	fireBullet(power);
			//}
		}
		double newDistance = distance(newPosEnemy, getPoint());
		double newTicks = newDistance / velBullet;
		double newLimit = newTicks - limit;

		for (int t = 0; t <= (int)newLimit; t++) {
			newPosEnemy = add(posEnemy, enemyDistanceTravl);
			posEnemy = newPosEnemy;
		}
		angle = preaimAngle(posEnemy, newPosEnemy);
		turnGun(angle);
	}

	void movement(double distance) {
		Point middle = new Point(width/2, height/2);
		int dist = 20;
		int deg = 50;
		double limit = 100;
		double X = getX();
		double Y = getY();
		double b = 1;
		double c = 10;
		//debug(String.valueOf(X));
		//debug(String.valueOf(Y));

		if (height - Y <= limit || Y <= limit) {
			cooldown = getTime() + c;
			ahead(direction * -20);
			neg = true;
		} else if (width - X <= limit || X <= limit) {
			cooldown = getTime() + c;
			ahead(direction * -20);
			neg = true;
		}
		if (neg && getTime() <= cooldown || hasHitWall()) {
			goTo(middle);
		} else if (neg && cooldown == getTime()) {
			direction *= -1;
			neg = false;
		} else {
			if (distance > 200) {
				b = 50;
			} else if (distance < 200) {
				b = -50;
			}
			if (hasHitWall()) {
				direction *= -1;
			}
			turn(normalRelativeAngle(bearing - 90) * turnDirection + b);
			ahead(direction * 20);
		}
		if (velocity == 0) {
			turn(bearing + 90);
			if (getTime() % 40 == 0) {
				direction *= -1;
			}
		}
	}

	void goTo(Point gotoPoint) {
		Point us = getPoint();
		Point toPoint = subtract(gotoPoint, us);
		double angle = angle(toPoint);
		turn(normalRelativeAngle(angle - getHeading()));
		ahead(20);
	}
}
