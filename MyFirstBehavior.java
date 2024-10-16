package infovk.l_schepp24;

import java.util.List;
import java.util.Timer;

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
	Point relativeEnemyPosition;
	Point lastdistance;
	double distance;
	double gunHeat;
	double height;
	double width;
	double gunTurn;
	List <Point> lastandcurrentpos = createEmptyList();
	//List count = createEmptyList();
	//java.awt.Graphics2D canvas = getGraphics();
	boolean neg = false;

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
	}

	@Override
	void execute() {
		gunHeat = getGunHeat();
		if (hasScannedRobot()) {
			ScannedRobotEvent ev = getScannedRobotEvent();
			distance = getDistance(ev);
			double deg = getBearing(ev) + getHeading();
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
		/*
		lastdistance = subtract(getLast(lastandcurrentpos), getFirst(lastandcurrentpos));
		double lastdistancelength = length(lastdistance); //geschw. des gegners
		Point absolute = add(getLast(lastandcurrentpos), lastdistance);
		Point us = pointFromCoordinates(getX(), getY());
		Point relative = subtract(absolute, us);
		//paintDot(relative, RED);
		//paintDot(absolute, YELLOW);
		//paintDot(getLast(lastandcurrentpos), BLUE);
		paintLine(getPoint(), getLast(lastandcurrentpos), RED);
		paintLine(getPoint(), absolute, YELLOW);
		paintLine(getLast(lastandcurrentpos), absolute, BLUE);
		double adjust = angleBetween(getLast(lastandcurrentpos), us);
		adjust = adjust - getGunHeading();
		if (adjust >= -360 && adjust < 0) {
			adjust += 360;
		} else if (adjust <= 360 && adjust > 0 ) {
			adjust -= 360;
		}
		adjust = normalRelativeAngle(adjust);
		 */
		//Point lastdistance = subtract(getLast(lastandcurrentpos), getFirst(lastandcurrentpos));
		//double power = 2;
		//aimbot(power, getLast(lastandcurrentpos), getFirst(lastandcurrentpos));
		//debug("-------preaim--------");
		//debug(String.valueOf(adjust));
		//debug("---------------------");
		double power = 0;
		//turnGun(adjust);
		circle(distance);
		if (distance <= 200) {
			power = 3;
		} else if (distance > 200 && distance <= 400) {
			power = 2;
		}else if (distance > 400 && distance > 600) {
			power = 1;
		} else {
			power = 0.5;
		}
		aimbot(power, getLast(lastandcurrentpos), getFirst(lastandcurrentpos));
		//dreieck(power, getLast(lastandcurrentpos), getFirst(lastandcurrentpos));
		fireBullet(power);
		//debug("["+getFirst(lastandcurrentpos)+", "+getLast(lastandcurrentpos)+"]");
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

	double dreieck(double power, Point posEnemy, Point lastPosEnemy) {
		Point enemyDistanceTravl = subtract(posEnemy, lastPosEnemy);
		double velBullet = 20 - 3 * power;
		double ticksToEnemy = distance / velBullet;
		Point newPosEnemy = add(posEnemy, multiply(enemyDistanceTravl, ticksToEnemy));
		double angle = preaimAngle(posEnemy, newPosEnemy);
		turnGun(angle);
		debug(String.valueOf(angle));
		return angle;
	}

	// noch absolut gar nicht richtig aber wir haben schon einen weg dahin
	void aimbot(double power, Point posEnemy, Point lastPosEnemy) {
		Point enemyDistanceTravl = subtract(posEnemy, lastPosEnemy);
		double velEnemy = length(enemyDistanceTravl);
		double velBullet = 20 - 3 * power;
		double limit = distance / velBullet;
		debug(String.valueOf((int)limit));
		Point newPosEnemy = pointFromCoordinates(0, 0);
		double angle = 0;
		for (int t=1; t<=(int)limit; t++) {
			newPosEnemy = add(posEnemy, enemyDistanceTravl);/* Richtung beachten */
			//Point posBullet = multiply(add(getPoint(), velBullet), t); /* Richtung beachten */
			// hitbox nachgucken
			angle = preaimAngle(posEnemy, newPosEnemy);
			posEnemy = newPosEnemy;

			/*
			if ((newPosEnemy - radius_hitbox) <= newPosBullet && newPosBullet <= (newPosEnemy + radius_hitbox)) {
				fireBullet(power);
			}
			 */
		}
		Point relative = subtract(newPosEnemy, getPoint());
		double length = length(relative);
		double newTicks = length / velBullet;
		double newLimit = newTicks - limit;

		for (int t = 0; t <= (int)newLimit; t++) {
			newPosEnemy = add(posEnemy, enemyDistanceTravl);
			angle = preaimAngle(posEnemy, newPosEnemy);
			posEnemy = newPosEnemy;
		}
		//debug(String.valueOf(length));
		//debug(String.valueOf(newLimit));
		turnGun(angle);
	}

	void circle(double distance) {
		int dist = 20;
		int deg = 50;
		double limit = 100;
		double X = getX();
		double Y = getY();
		ahead(dist);
		turn(deg);
		//debug(String.valueOf(X));
		//debug(String.valueOf(Y));
		/*
		if (height - Y <= limit || Y <= limit) {
			ahead(-200);
			neg = true;
		} else if (width - X <= limit || X <= limit) {
			ahead(-200);
			neg = true;
		}
		if (neg) {
			turn((deg+20)*-1);
			ahead(-dist);
			neg = false;
		} else {
			turn(deg);
			ahead(dist);
		}

		 */
	}
}
