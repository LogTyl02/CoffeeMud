package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2011 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

@SuppressWarnings("unchecked")
public class Bleeding extends StdAbility
{
    public String ID() { return "Bleeding"; }
    public String name(){ return "Bleeding";}
    public String displayText(){ return "(Bleeding)";}
    protected int canAffectCode(){return CAN_ITEMS|Ability.CAN_MOBS;}
    protected int canTargetCode(){return 0;}
    protected int hpToKeep=-1;
    protected int lastDir=-1;
    protected Room lastRoom=null;

    public double healthPct(MOB mob){ return CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints());}

    public void unInvoke()
    {
        if((affected instanceof MOB)
        &&(canBeUninvoked())
        &&(!((MOB)affected).amDead())
        &&(CMLib.flags().isInTheGame(affected,true)))
            ((MOB)affected).location().show((MOB)affected,null,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> stop(s) bleeding.");
        super.unInvoke();
    }

    public void  executeMsg(Environmental myHost, CMMsg msg)
    {
        super.executeMsg(myHost,msg);
        if((affected!=null)&&(msg.amITarget(affected))&&(affected instanceof MOB))
		{
	        if(msg.targetMinor()==CMMsg.TYP_HEALING)
	        {
	            hpToKeep=-1;
	            if(healthPct((MOB)affected)>0.50)
	            	unInvoke();
	        }
	        else
	        if((msg.targetMinor()==CMMsg.TYP_LOOK)
	        ||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
	        	msg.source().tell((MOB)msg.target(),null,null,"^R<S-NAME> <S-IS-ARE> still bleeding...");
		}
        else
        if((msg.source()==affected)
        &&(msg.target() instanceof Room)
        &&(msg.tool() instanceof Exit)
        &&(msg.targetMinor()==CMMsg.TYP_LEAVE))
        {
        	Room R=(Room)msg.target();
        	int dir=-1;
        	for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
        		if(msg.tool()==R.getReverseExit(d))
        			dir=d;
        	if((dir>=0)&&(R.findItem(null,"a trail of blood")==null))
        	{
        		Item I=CMClass.getItem("GenFatWallpaper");
        		I.setName("A trail of blood");
	        	if(lastDir>=0)
	        		I.setDisplayText("A faint trail of blood leads from "
        				+Directions.getDirectionName(lastDir)+" to "+Directions.getDirectionName(dir)+".");
	        	else
	        		I.setDisplayText("A faint trail of blood leads "+Directions.getDirectionName(dir)+".");
        		I.phyStats().setDisposition(I.phyStats().disposition()|PhyStats.IS_HIDDEN|PhyStats.IS_UNSAVABLE);
        		I.setSecretIdentity(msg.source().Name()+"`s blood.");
        		R.addItem(I,ItemPossessor.Expire.Monster_EQ);
        	}
        	lastDir=Directions.getOpDirectionCode(dir);
        	lastRoom=R;
        }
    }
    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
        	return false;
        if((ticking instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
        {
        	MOB mob=(MOB)ticking;
            if(hpToKeep<=0)
            {
                hpToKeep=mob.curState().getHitPoints();
                mob.recoverMaxState();
            }
            else
            {
	            if(mob.curState().getHitPoints()>hpToKeep)
	            	mob.curState().setHitPoints(hpToKeep);
	            int maxMana=(int)Math.round(CMath.mul(mob.maxState().getMana(),healthPct(mob)));
                if(mob.curState().getMana()>maxMana)
                	mob.curState().setMana(maxMana);
	            int maxMovement=(int)Math.round(CMath.mul(mob.maxState().getMovement(),healthPct(mob)));
                if(mob.curState().getMovement()>maxMovement)
                	mob.curState().setMovement(maxMovement);
            }
        }
        return true;
    }

    public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel)
    {
    	if(target==null) target=mob;
    	if(!(target instanceof MOB)) return false;
    	if(CMLib.flags().isGolem((MOB)target)) return false;
    	if(((MOB)target).phyStats().level()<CMProps.getIntVar(CMProps.SYSTEMI_INJBLEEDMINLEVEL)) return false;
    	if(((MOB)target).fetchEffect(ID())!=null) return false;
    	if(((MOB)target).location()==null) return false;
    	if(((MOB)target).location().show((MOB)target,null,this,CMMsg.MSG_OK_VISUAL,"^R<S-NAME> start(s) BLEEDING!^?"))
	    	beneficialAffect(mob,target,asLevel,0);
    	return true;
    }
}
