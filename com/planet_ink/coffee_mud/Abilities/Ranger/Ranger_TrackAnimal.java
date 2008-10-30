package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2000-2008 Bo Zimmerman

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
public class Ranger_TrackAnimal extends StdAbility
{
	public String ID() { return "Ranger_TrackAnimal"; }
	public String name(){ return "Track Animal";}
	protected String displayText="(tracking an animal)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"TRACKANIMAL"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_COMBATLORE;}
	public long flags(){return Ability.FLAG_TRACKING;}
	public int usageType(){return USAGE_MOVEMENT;}

	protected Vector theTrail=null;
	public int nextDirection=-2;


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell("The trail seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The trail dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The trail seems to continue "+Directions.getDirectionName(nextDirection)+".");
				if(mob.isMonster())
				{
					Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
					{
						int dir=nextDirection;
						nextDirection=-2;
						CMLib.tracking().move(mob,dir,false,false);
					}
					else
						unInvoke();
				}
				else
					nextDirection=-2;
			}

		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public MOB animalHere(Room room)
	{
		if(room==null) return null;

		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(CMLib.flags().isAnimalIntelligence(mob))
				return mob;
		}
		return null;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!CMLib.flags().aliveAwakeMobile(mob,false))
			return false;

		if(!CMLib.flags().canBeSeenBy(mob.location(),mob))
		{
			mob.tell("You can't see anything to track!");
			return false;
		}

		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();
		if(V.size()>0)
		{
			mob.tell("You stop tracking.");
			if(commands.size()==0) return true;
		}

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(animalHere(mob.location())!=null)
		{
			mob.tell("Try 'look'.");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),true,false,true,true,true,75+(2*getXLEVELLevel(mob)));
		for(Enumeration r=checkSet.elements();r.hasMoreElements();)
		{
			Room R=CMLib.map().getRoom((Room)r.nextElement());
			if(animalHere(R)!=null)
				rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,true,false,true,true,true,75+(2*getXLEVELLevel(mob)));

		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=animalHere((Room)theTrail.firstElement());

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.addElement(mob.location());
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> begin(s) to track <T-NAMESELF>.",null,"<S-NAME> begin(s) to track <T-NAMESELF>.");
			if((mob.location().okMessage(mob,msg))&&(target.okMessage(target,msg)))
			{
				mob.location().send(mob,msg);
				target.executeMsg(target,msg);
				invoker=mob;
				displayText="(tracking "+target.name()+")";
				Ranger_TrackAnimal newOne=(Ranger_TrackAnimal)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to track an animal, but can't find the trail.");


		// return whether it worked
		return success;
	}

}
