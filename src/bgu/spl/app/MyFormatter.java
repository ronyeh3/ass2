package bgu.spl.app;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Formatter for the project logger, extends {@link SimpleFormatter}
 */
public class MyFormatter extends SimpleFormatter {

	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		buf.append("\n");

		if (rec.getLevel().intValue()>=Level.WARNING.intValue()) {
			buf.append(" ! "+rec.getLevel()+" ! "+rec.getLevel()+" ! /n");
		} 
		else {
			buf.append(rec.getLevel()+" :	");
		}
		buf.append(formatMessage(rec));
		return buf.toString();
	}
	
	
	private String calcDate(long millisecs) {	//avoiding the built-in handler use of ugly date- just in case
		return "";
	}


	public String getHead(Handler h) {
		calcDate(0);
		return "\t  "+(new Date())+"\n"
				+"\t\tSTORE SIMULATION\t\n";
	}
	
	public String getTail(Handler h) {
		return "\n\t\tEND OF SIMULATION";
	}

}
