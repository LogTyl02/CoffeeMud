package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_Hibernation extends Chant
{
	public String ID() { return "Chant_Hibernation"; }
	public String name(){ return "Hibernation";}
	public String displayText(){return "(Hibernating)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	private CharState oldState=null;
	protected int roundsHibernating=0;


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> end(s) <S-HIS-HER> hibernation.");
				else
					mob.tell("Your hibernation ends.");
			}
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
		&&((CMath.bset(msg.sourceCode(),CMMsg.MASK_MOVE))||(CMath.bset(msg.sourceCode(),CMMsg.MASK_HANDS))||(CMath.bset(msg.sourceCode(),CMMsg.MASK_MOUTH))))
			unInvoke();
		return;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);
		MOB mob=(MOB)affected;

		if((msg.amISource(mob)
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
		&&(msg.sourceMajor()>0)))
		{
			if(roundsHibernating<10)
			{
				mob.tell("You can't withdraw from hibernation just yet.");
				return false;
			}
			unInvoke();
		}
		return super.okMessage(myHost,msg);
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;

		if(tickID!=Tickable.TICKID_MOB) return true;
		if(!proficiencyCheck(null,0,false)) return true;

		if((!mob.isInCombat())
		&&(CMLib.flags().isSleeping(mob)))
		{
			roundsHibernating++;
			double man = (double)( ( mob.charStats().getStat( CharStats.STAT_INTELLIGENCE ) + mob.charStats().getStat( CharStats.STAT_WISDOM ) ) );
			mob.curState().adjMana( (int)Math.round( ( man * .1 ) + ( ( mob.envStats().level() + ( 2.0 * super.getXLEVELLevel( invoker() ) )  ) / 2.0 ) ),
			                        mob.maxState() );
			mob.curState().setHunger(oldState.getHunger());
			mob.curState().setThirst(oldState.getThirst());
			double move = (double)mob.charStats().getStat( CharStats.STAT_STRENGTH );
			mob.curState().adjMovement( (int)Math.round( ( move * .1 ) + ( ( mob.envStats().level() + ( 2.0 * super.getXLEVELLevel( invoker() ) )  ) / 2.0 ) ),
			                            mob.maxState() );
			if(!CMLib.flags().isGolem(mob))
			{
				double hp=(double)mob.charStats().getStat( CharStats.STAT_CONSTITUTION );
				if(!CMLib.combat().postHealing( mob,
				                                mob,
				                                this,
				                                CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,
				                                (int)Math.round( ( hp * .1 ) + ( ( mob.envStats().level() + ( 2.0 * super.getXLEVELLevel( invoker() ) ) ) / 2.0 ) ),
				                                null ) )
					unInvoke();
			}
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector<Object> commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell("You can't hibernate while in combat!");
			return false;
		}
		if(!CMLib.flags().isSitting(mob))
		{
			mob.tell("You must be in a sitting, restful position to hibernate.");
			return false;
		}
		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_SLEEP|CMMsg.MASK_MAGIC,"<S-NAME> begin(s) to hibernate...");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				oldState=mob.curState();
				beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE-1000);
				helpProficiency(mob);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to hibernate, but lose(s) concentration.");

		// return whether it worked
		return success;
	}
}
