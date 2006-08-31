package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class Prop_Doppleganger extends Property
{
	public String ID() { return "Prop_Doppleganger"; }
	public String name(){ return "Doppleganger";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected boolean lastLevelChangers=true;
    private int maxLevel=Integer.MAX_VALUE;
    private int minLevel=Integer.MIN_VALUE;
    protected Environmental lastOwner=null;
    
	public String accountForYourself()
	{ return "Level Changer";	}
	
    public void setMiscText(String text)
    {
        super.setMiscText(text);
        maxLevel=CMParms.getParmInt(text,"MAX",Integer.MAX_VALUE);
        minLevel=CMParms.getParmInt(text,"MIN",Integer.MIN_VALUE);
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
    	if((affected instanceof Item)
    	&&(((Item)affected).owner()!=lastOwner)
    	&&(((Item)affected).owner() instanceof MOB))
    	{
    		lastOwner=((Item)affected).owner();
			int level=((MOB)lastOwner).envStats().level()+CMath.s_int(text());
			if(text().endsWith("%")) level=(int)Math.round(CMath.mul(level,CMath.s_pct(text())));
			if(level<minLevel) level=minLevel;
            if(level>maxLevel) level=maxLevel;
			((Item)affected).baseEnvStats().setLevel(level);
			((Item)affected).envStats().setLevel(level);
			CMLib.itemBuilder().balanceItemByLevel((Item)affected);
			((Item)affected).baseEnvStats().setLevel(((MOB)lastOwner).envStats().level());
			((Item)affected).envStats().setLevel(((MOB)lastOwner).envStats().level());
			lastOwner.recoverEnvStats();
			Room R=((MOB)lastOwner).location();
			if(R!=null) R.recoverRoomStats();
    	}
    	super.executeMsg(myHost,msg);
    }
    
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(msg.target() instanceof Room)
		&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
		&&(lastLevelChangers))
		{
			lastLevelChangers=false;
			MOB mob=(MOB)affected;

			if((mob.location()!=null)
			&&(CMLib.flags().aliveAwakeMobile(mob,true))
			&&(mob.curState().getHitPoints()>=mob.maxState().getHitPoints())
			&&(mob.location().numInhabitants()>1))
			{
				int total=0;
				int num=0;
				MOB victim=mob.getVictim();
				if(victim!=null)
				{
					total+=victim.envStats().level();
					num++;
				}
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB M=mob.location().fetchInhabitant(i);
					if((M!=mob)
					&&((M.getVictim()==mob)||(victim==null))
					&&(M.fetchEffect(ID())==null)
			        &&(!CMSecurity.isAllowed(M,mob.location(),"CMDMOBS"))
			        &&(!CMSecurity.isAllowed(M,mob.location(),"CMDROOMS")))
					{
						total+=M.envStats().level();
						num++;
					}
				}
				if(num>0)
				{
					int level=(int)Math.round(CMath.div(total,num))+CMath.s_int(text());
					if(text().endsWith("%")) level=(int)Math.round(CMath.mul(CMath.div(total,num),CMath.s_pct(text())));
					if(level<minLevel) level=minLevel;
                    if(level>maxLevel) level=maxLevel;
					if(level!=mob.baseEnvStats().level())
					{
						CharClass C=mob.charStats().getCurrentClass();
						mob.baseEnvStats().setLevel(level);
						mob.baseEnvStats().setArmor(C.getLevelArmor(mob));
						mob.baseEnvStats().setAttackAdjustment(C.getLevelAttack(mob));
						mob.baseEnvStats().setDamage(C.getLevelDamage(mob));
						mob.baseEnvStats().setSpeed(1.0+(CMath.div(level,100)*4.0));
						mob.baseState().setHitPoints(CMLib.dice().rollHP(level,11));
						mob.baseState().setMana(C.getLevelMana(mob));
						mob.baseState().setMovement(C.getLevelMove(mob));
						mob.recoverEnvStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
						mob.resetToMaxState();
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking==affected)
		&&(tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB))
			lastLevelChangers=true;
		return super.tick(ticking,tickID);
	}
}
