package org.apache.log4j.xml.examples;


import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.CategoryFactory;

import org.apache.log4j.xml.examples.XPriority;

/**
   A simple example showing Category sub-classing. It shows the
   minimum steps necessary to implement one's {@link CategoryFactory}.
   Note that sub-classes follow the hiearchy even if its categories
   belong to different classes.

   See <b><a href="doc-files/XCategory.java">source
   code</a></b> for more details.
   
 */
public class XCategory extends Category implements OptionHandler {

  private static XFactory factory = new XFactory();


  static String instanceFQCN = XCategory.class.getName();
  
  public static final String SUFFIX_OPTION = "Suffix";

  String suffix;

  /**
     Just calls the parent constuctor.
   */
  public XCategory(String name) {
    super(name);
  }

  public
  void activateOptions() {
  }

  /**
     Overrides the standard debug method by appending " world" to each
     message.  */
  public 
  void debug(String message) {
    log(instanceFQCN, Priority.DEBUG, message + suffix, null);
  }

  public
  void fatal(String message) { 
    if(disable <=  XPriority.FATAL_INT) return;   
    if(XPriority.FATAL.isGreaterOrEqual(this.getChainedPriority()))
      callAppenders(new LoggingEvent(instanceFQCN, this, XPriority.FATAL, 
				     message, null));
  }
  

  /**
     This method overrides {@link Category#getInstance} by supplying
     its own factory type as a parameter.

   */
  public 
  static
  Category getInstance(String name) {
    return Category.getInstance(name, factory); 
  }

  public
  String[] getOptionStrings() {
    return (new String[] {SUFFIX_OPTION});
  }

  public
  void setOption(String option, String value) {
    System.out.println(option+"="+value);
    if(option == null) {
      return;
    }
    if(option.equalsIgnoreCase(SUFFIX_OPTION)) {
      this.suffix = value;
      System.out.println("Setting suffix to"+suffix);

    }
  }

  public
  void trace(String message) { 
    if(disable <=  XPriority.TRACE_INT) return;   
    if(XPriority.TRACE.isGreaterOrEqual(this.getChainedPriority()))
      callAppenders(new LoggingEvent(instanceFQCN, this, XPriority.TRACE, message, 
				     null));
  }



  private static class XFactory implements CategoryFactory {
    XFactory() {
    }

    public
    Category makeNewCategoryInstance(String name) {
      return new XCategory(name);
    }
  }


}


