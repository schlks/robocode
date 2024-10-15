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
	List count = createEmptyList();
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
		debug("-------preaim--------");
		debug(String.valueOf(adjust));
		debug("---------------------");

		turnGun(adjust);
		circle(distance);
		if (distance < 200) {
			double vel = 20 - 3 * 3;
			fireBullet(3);
		} else if (distance > 200 && distance < 500) {
			fireBullet(2);
		} else {
			fireBullet(1);
		}
		debug("["+getFirst(lastandcurrentpos)+", "+getLast(lastandcurrentpos)+"]");
	}

	void circle(double distance) {
		int dist = 20;
		int deg = 50;
		double limit = 100;
		double X = getX();
		double Y = getY();
		debug(String.valueOf(X));
		debug(String.valueOf(Y));
		debug(String.valueOf(height-Y));
		debug(String.valueOf(width-X));
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
	}

	// noch absolut gar nicht richtig aber wir haben schon eien weg dahin
	void aimbot(double v_enemy, double v_bullet, Point pos_enemy) {
		for (int t=1; t<=15; t++) {
			Point new_pos_enemy = multiply(add(pos_enemy, vel_enemy), t);/* Richtung beachten */
			double angle = magic_calc_angle_func; // von oben
			Point pos_bullet = multiply(add(getPoint(), vel_bullet), t); /* Richtung beachten */
			// hitbox nachgucken
			if ((new_pos_enemy - radius_hitbox) <= new_pos_bullet && new_pos_bullet <= (new_pos_enemy + radius_hitbox)) {
				fireBullet(3);
			}
		}
	}
}
