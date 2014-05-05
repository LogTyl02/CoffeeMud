package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Prayer_Heal extends Prayer implements MendingSkill
{
	@Override public String ID() { return "Prayer_Heal"; }
	public final static String localizedName = CMLib.lang()._("Heal");
	@Override public String name() { return localizedName; }
	@Override public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HEALING;}
	@Override public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALINGMAGIC;}
	@Override protected long minCastWaitTime(){return CMProps.getTickMillis()/2;}

	@Override
	public boolean supportsMending(Physical item)
	{
		return (item instanceof MOB)
				&&((((MOB)item).curState()).getHitPoints()<(((MOB)item).maxState()).getHitPoints());
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if(!supportsMending(target))
					return Ability.QUALITY_INDIFFERENT;
				if(((MOB)target).charStats().getMyRace().racialCategory().equals("Undead"))
					return Ability.QUALITY_MALICIOUS;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		final boolean undead=target.charStats().getMyRace().racialCategory().equals("Undead");

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			final CMMsg msg=CMClass.getMsg(mob,target,this,(!undead?0:CMMsg.MASK_MALICIOUS)|verbalCastCode(mob,target,auto),auto?_("<T-NAME> become(s) surrounded by a white light."):_("^S<S-NAME> @x1 for tremendous healing power over <T-NAMESELF>.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int healing=CMLib.dice().roll(5,adjustedLevel(mob,asLevel),10);
				final int oldHP=target.curState().getHitPoints();
				CMLib.combat().postHealing(mob,target,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,healing,null);
				if(target.curState().getHitPoints()>oldHP)
					target.tell(_("You feel tons better!"));
				lastCastHelp=System.currentTimeMillis();
			}
		}
		else
			beneficialWordsFizzle(mob,target,_("<S-NAME> @x1 for <T-NAMESELF>, but nothing happens.",prayWord(mob)));


		// return whether it worked
		return success;
	}
}
