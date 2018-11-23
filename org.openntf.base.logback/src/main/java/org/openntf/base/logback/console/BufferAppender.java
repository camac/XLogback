package org.openntf.base.logback.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class BufferAppender<E> extends AppenderBase<E> {

	static int DEFAULT_LIMIT = 100;
	int counter = 0;
	int limit = DEFAULT_LIMIT;

	private Queue<String> logs =  new LinkedList<String>();
	
	private Layout<E> layout;

	PatternLayoutEncoder encoder;

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void clearBuffer() {
		synchronized (logs) {
			logs.clear();
		}		
	}
	
	@Override
	public void start() {

		try {

			super.start();

			if (encoder == null) {
				encoder = new PatternLayoutEncoder();
			}

			if (this.encoder == null) {
				addError("No encoder set for the appender named [" + name + "].");
				return;
			}

			try {
				encoder.init(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void append(E event) {

		if (!isStarted()) {
			return;
		}

		try {
			
			String message;
			if(null==layout) {
				message = event.toString();
			} else {
				message = layout.doLayout(event);
			}

			logs.add(message);
						
			while(logs.size() > limit) {
				logs.remove();
			}
			
			// prepare for next event
			counter++;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Layout<E> getLayout() {
		return layout;
	}

	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}
	
	public List<String> dumpLogs() {
		
		return new ArrayList<String>(logs);
		
	}

}
