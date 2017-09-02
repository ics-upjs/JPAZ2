import sk.upjs.jpaz2.*;

public class Launcher {

	public static void main(String[] args) {
		// create an object inspector
		ObjectInspector oi = new ObjectInspector();

		// create a pane - a drawing surface (sand beach for turtles)
		AnimatedWinPane beach = new AnimatedWinPane();

		// create a turtle named "john"
		Turtle john = new Turtle();

		// ask the pane "beach" to accept the turtle "john"
		beach.add(john);

		// ask the object inspector "oi" to inspect the turtle "john"
		oi.inspect(john);
	}

}
