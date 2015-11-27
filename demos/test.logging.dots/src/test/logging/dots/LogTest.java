package test.logging.dots;

import lotus.domino.NotesException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.dots.task.AbstractServerTaskExt;
import com.ibm.dots.task.RunWhen;

public class LogTest extends AbstractServerTaskExt {

	private static Logger logger = LoggerFactory.getLogger(LogTest.class);
	
	@Override
	public void dispose() throws NotesException {
	}

	@Override
	protected void doRun(RunWhen arg0, IProgressMonitor arg1) throws NotesException {
		logger.warn("Warn Test with exception", new Throwable());
		for(int i = 0; i<100; i++) {
			logger.warn("Test warning {}", i);
		}
	}

}
