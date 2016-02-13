import sk.upjs.jpaz2.*;

public class Launcher {

	public static void main(String[] args) {
		// create an object inspector
		ObjectInspector oi = new ObjectInspector();
		
		// create a pane - a drawing surface (sand beach for turtles)
		WinPane win = new WinPane();
		
		// create a turtle named "john"
		Turtle john = new Turtle();
		
		// ask the pane "win" to accept the turtle "john" 
		win.add(john);
		
		// ask "john" to move to the center of its home pane 
		john.center();
		
		// ask the object inspector "oi" to inspect the turtle "john"
		oi.inspect(john);
	}
	
}
