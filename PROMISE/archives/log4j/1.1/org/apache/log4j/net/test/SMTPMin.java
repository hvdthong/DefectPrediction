package org.apache.log4j.net.test;

import org.apache.log4j.*;
import java.io.IOException;
import java.io.InputStreamReader;

public class SMTPMin {
  
  static Category cat = Category.getInstance(SMTPMin.class);

  public 
  static 
  void main(String argv[]) {
    if(argv.length == 1) 
      init(argv[0]);
    else 
      usage("Wrong number of arguments.");     
    
    NDC.push("some context");
    test();
  }

  static
  void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + SMTPMin.class.getName()
		       + " configFile");
    System.exit(1);
  }

  static
  void init(String configFile) {
    PropertyConfigurator.configure(configFile);
  }


  static
  void test() {
    int i  = 0;
    cat.debug( "Message " + i++);
    cat.debug("Message " + i++,  new Exception("Just testing."));
    cat.info( "Message " + i++);
    cat.warn( "Message " + i++);
    cat.error( "Message " + i++);
    cat.log(Priority.FATAL, "Message " + i++);
    Category.shutdown();
    Thread.currentThread().getThreadGroup().list();
  }
  
}