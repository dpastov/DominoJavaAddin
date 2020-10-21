package org.demo;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;
import lotus.notes.addins.JavaServerAddin;

public class DemoAddin extends JavaServerAddin {
	final String			JADDIN_NAME				= "DemoAddin";
	final String			JADDIN_VERSION			= "2";
	final String			JADDIN_DATE				= "2020-10-19 11:00 CET";

	// Instance variables
	String[] 				args 					= null;
	private int				secondsElapsed			= 30;
	private Session 		session					= null;
	private Database 		ab						= null;

	// we expect our first parameter is dedicated for secondsElapsed
	public DemoAddin(String[] args) {
		this.secondsElapsed = Integer.parseInt(args[0]);
	}

	// constructor if no parameters
	public DemoAddin() {}

	@SuppressWarnings("deprecation")
	public void runNotes() {
		displayLoadInfo();

		try {
			session = NotesFactory.createSession();
			String server = session.getServerName();

			while (this.addInRunning()) {
				/* gives control to other task in non preemptive os*/
				OSPreemptOccasionally();
				if (this.AddInHasSecondsElapsed(secondsElapsed)) {
					ab = session.getDatabase(server, "names.nsf");
					long count = ab.getView("People").getAllEntries().getCount();
					logMessage("Count of persons: " + Long.toString(count));
					ab.recycle();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		terminate();
	}

	/**
	 * Display run configuration
	 */
	private void displayLoadInfo() {
		String jvmVersion = System.getProperty("java.specification.version", "0");

		logMessage("version		" + this.JADDIN_VERSION);
		logMessage("build date		" + this.JADDIN_DATE);
		logMessage("java		" + jvmVersion);
		logMessage("seconds elapsed	" + secondsElapsed);
	}

	/**
	 * Write a log message to the Domino console. The message string will be prefixed with the add-in name
	 * followed by a column, e.g. <code>"AddinName: xxxxxxxx"</code>
	 * 
	 * @param	message		Message to be displayed
	 */
	private final void logMessage(String message) {
		AddInLogMessageText(this.JADDIN_NAME + ": " + message, 0);
	}

	/**
	 * This method is called by the Java runtime during garbage collection.
	 */
	public void finalize() {
		terminate();

		super.finalize();
	}

	/**
	 * Terminate all variables
	 */
	private void terminate() {
		try {
			if (this.ab != null) {
				this.ab.recycle();
			}
			if (this.session != null) {
				this.session.recycle();
			}

			logMessage("UNLOADED (OK)");
		} catch (NotesException e) {
			logMessage("UNLOADED (**FAILED**)");
		}
	}

}
