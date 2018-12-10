package org.modelingvalue.jdclare.test;


import static org.modelingvalue.jdclare.DClare.dclare;
import static org.modelingvalue.jdclare.DClare.set;
import static org.modelingvalue.jdclare.PropertyQualifier.containment;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;


public interface BirdUniverse extends DUniverse {
	
	
	// Conflicting Rules Error
    interface Bird extends DStruct1<String>, DNamed {
        @Property(key = 0)
        String type();

        @Property
        String color();
        
        @Property(containment)
        Set<Wing> wings();
     }
    
    interface Wing extends DStruct2<Bird, String>, DNamed {
        @Property(key = 0)
        Bird bird();
    	
        @Property(key = 1)
        String id();
        
        @Property
        default int span() {
        	return 1;
        }
     }
    
    
    @Rule
    default void setCondorColor() {	
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("white".equals(condor.color())) {
  	  		set(condor, Bird::color, "orange");
  	  		set(condor, Bird::color, "purple");
  	  	}
    }
    
    @Rule
    default void setCondorColor1() {	
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("red".equals(condor.color())) {
  	  		set(condor, Bird::color, "orange");
  	  	}
    }
    
    @Rule
    default void setCondorColor2() {	
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("red".equals(condor.color())) {
  	  		set(condor, Bird::color, "purple");
  	  	}
    }
    
    
    @Rule
    default void addWing() {
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("green".equals(condor.color())) {
    		Wing wing = dclare(Wing.class, condor, "Left");
    		set(condor, Bird::wings, Set::add, wing);
    	}
    }
    
    @Rule
    default void removeWing() {
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("green".equals(condor.color())) {
    		Wing wing = dclare(Wing.class, condor, "Left");
    		set(condor, Bird::wings, Set::remove, wing);
    	}
    }
    
    @Rule
    default void addAndRemoveWing() {
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("blue".equals(condor.color())) {
    		Wing wing = dclare(Wing.class, condor, "Left");
    		set(condor, Bird::wings, Set::remove, wing);
    		set(condor, Bird::wings, Set::add, wing);
    	}
    }
    
    @Rule
    default void increaseSpan() {
    	Bird condor = dclare(Bird.class, "Condor");
    	if ("black".equals(condor.color())) {
    		Wing wing = dclare(Wing.class, condor, "Left");
    		int span = wing.span();
    		set(wing, Wing::span, span +1);
    	}
    }
    
    @Override
    default void init() {
        DUniverse.super.init();
    }
    
    static void main(String[] args) {
        DClare.run(BirdUniverse.class);
    }
    

    
    public interface RedCondorUniverse extends BirdUniverse {
    	
  	   @Override
  	    default void init() {
  		   BirdUniverse.super.init();
  	        Bird condor = dclare(Bird.class, "Condor");
  	        set(condor, Bird::color, "red");
  	    }

     }
    
    public interface GreenCondorUniverse extends BirdUniverse {
    	
  	   @Override
  	    default void init() {
  		   BirdUniverse.super.init();
  	        Bird condor = dclare(Bird.class, "Condor");
  	        set(condor, Bird::color, "green");
  	    }
     }
    
    public interface BlueCondorUniverse extends BirdUniverse {
    	
  	   @Override
  	    default void init() {
  		   BirdUniverse.super.init();
  	        Bird condor = dclare(Bird.class, "Condor");
  	        set(condor, Bird::color, "blue");
  	    }
     }
    
    public interface WhiteCondorUniverse extends BirdUniverse {
    	
   	   @Override
   	    default void init() {
   		   BirdUniverse.super.init();
   	        Bird condor = dclare(Bird.class, "Condor");
   	        set(condor, Bird::color, "white");
   	    }
      }
    
    public interface BlackCondorUniverse extends BirdUniverse {
    	
   	   @Override
   	    default void init() {
   		   BirdUniverse.super.init();
   	        Bird condor = dclare(Bird.class, "Condor");
   	        set(condor, Bird::color, "black");
   	    }
      }
}


