/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package org.wkh.swarmscale.physics.invertedpendulum.gp;
import ec.gp.*;

public class ForceData extends GPData
    {
    public double force;    // return value

    public void copyTo(final GPData gpd)   // copy my stuff to another ForceData
        { ((ForceData)gpd).force = force; }
    }


