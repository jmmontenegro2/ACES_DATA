import java.util.ArrayList;

public class globals {
	ArrayList<String> message = new ArrayList<String>();

	// this is the one array of Strings that will be printed to the GUI.
	// Change it to your liking!
	public void addToMessage(String str) {
		this.message.add(str);
	}
}
