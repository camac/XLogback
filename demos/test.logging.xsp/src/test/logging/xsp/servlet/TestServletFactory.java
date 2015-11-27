package test.logging.xsp.servlet;

import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

public class TestServletFactory implements IServletFactory {

	private final static String SERVLET_URI = "/testlog";
	private static Logger logger = LoggerFactory.getLogger(TestServletFactory.class);
	
	private ComponentModule module;
	
	private Servlet testServlet;
	
	public void init(ComponentModule module) {
		this.module = module;
		logger.warn("Servlet Factory for /testlog initialized!");
	}

	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		logger.debug("ContextPath: '{}' Path: '{}'", contextPath, path);

		String servletPath = "";

		if(path!=null && path.toLowerCase(Locale.ENGLISH).contains(SERVLET_URI) ) { 
			String pathInfo = path;
			Servlet servlet = getTestServlet();
			return new ServletMatch(servlet, servletPath, pathInfo);
		}

		return null;
	}

	public Servlet getTestServlet() throws ServletException {
		if(testServlet == null) {
			testServlet = module.createServlet(TestServlet.class, "TestServlet", null);
		}
		
		return testServlet;
	}
	
}
