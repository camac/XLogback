package test.logging.dots;

import lotus.domino.NotesException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.dots.task.AbstractServerTaskExt;
import com.ibm.dots.task.RunWhen;

public class Startup extends AbstractServerTaskExt {

	private static Logger logger = LoggerFactory.getLogger(Startup.class);
	
	@Override
	public void dispose() throws NotesException {
	}
	
	@Override
	protected void doRun(RunWhen runWhen, IProgressMonitor monitor) throws NotesException {
		logger.trace("Trace Test");
		logger.debug("Debug Test");
		logger.info("Info Test");
		logger.warn("Warn Test");
		logger.error("Error Test", new Throwable("error message"));
	}

}
