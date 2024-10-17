package infovk.l_schepp24;

public class Schlaganfall extends SimpleRobot {
	public Schlaganfall() {
		behavior=new SchlaganfallBehavior(this);
	}
}
