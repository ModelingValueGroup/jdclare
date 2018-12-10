package org.modelingvalue.jdclare.test;

import static org.junit.Assert.assertEquals;
import static org.modelingvalue.jdclare.DClare.of;

import org.junit.Assert;
import org.junit.Test;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.test.BirdUniverse.BlackCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.BlueCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.GreenCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.RedCondorUniverse;
import org.modelingvalue.jdclare.test.BirdUniverse.WhiteCondorUniverse;

public class BirdTest {
	
	    //@Test
	    public void ConcurrentModificationException1() {
		  try {
			  DClare<RedCondorUniverse> redCondor = of(RedCondorUniverse.class);
			  redCondor.run();
			  Assert.fail();
		  } catch (Throwable t) {
			  Throwable cause = getCause(t);
			  assertEquals(java.util.ConcurrentModificationException.class, cause.getClass());
		  }
	    }
	    
	    //@Test
	    public void ConcurrentModificationException2() {
		  try {
			  DClare<WhiteCondorUniverse> whiteCondor = of(WhiteCondorUniverse.class);
			  whiteCondor.run();
			  Assert.fail();
		  } catch (Throwable t) {
			  Throwable cause = getCause(t);
			  assertEquals(java.util.ConcurrentModificationException.class, cause.getClass());
		  }
	    }
	  
	    @Test
	    public void TooManyChangesException1() {
		  try {
			  DClare<GreenCondorUniverse> greenCondor = of(GreenCondorUniverse.class);
			  greenCondor.run();
			  Assert.fail();
		  } catch (Throwable t) {
			  Throwable cause = getCause(t);
			  assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
		  }
	    }
	    
	    @Test
	    public void TooManyChangesException2() {
		  try {
			  DClare<BlackCondorUniverse> blackCondor = of(BlackCondorUniverse.class);
			  blackCondor.run();
			  Assert.fail();
		  } catch (Throwable t) {
			  Throwable cause = getCause(t);
			  assertEquals(org.modelingvalue.transactions.TooManyChangesException.class, cause.getClass());
		  }
	    }
	  
	    //@Test
	    public void NonDeterministicException() {
		  try {
			  DClare<BlueCondorUniverse> bleuCondor = of(BlueCondorUniverse.class);
			  bleuCondor.run();
			  Assert.fail();
		  } catch (Throwable t) {
			  Throwable cause = getCause(t);
			  assertEquals(org.modelingvalue.transactions.NonDeterministicException.class, cause.getClass());
		  }
	    }
	    

	  
	  
	  private Throwable getCause(Throwable t) {
		  while (t.getCause() != null) {
			  t = t.getCause();
		  }
		  return t;
	  }
	  
}
