package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2000-2010 Bo Zimmerman

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
public class Chant_SensePoison extends Chant
{
	public String ID() { return "Chant_SensePoison"; }
	public String name(){ return "Sense Poison";}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PRESERVING;}

	public List<Ability> returnOffensiveAffects(Physical fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
				offenders.addElement(A);
		}
		if(fromMe instanceof MOB)
		{
			MOB mob=(MOB)fromMe;
			for(int a=0;a<mob.numAllEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)&&(!offenders.contains(A)))
					offenders.addElement(A);
			}
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability A=mob.fetchAbility(a);
				if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
					offenders.addElement(A);
			}
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		List<Ability> offensiveAffects=returnOffensiveAffects(target);

		if((success)&&((offensiveAffects.size()>0)
					   ||((target instanceof Drink)&&(((Drink)target).liquidType()==RawMaterial.RESOURCE_POISON))))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) over <T-NAME>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer buf=new StringBuffer(target.name()+" contains: ");
				if(offensiveAffects.size()==0)
					buf.append("weak impurities, ");
				else
				for(int i=0;i<offensiveAffects.size();i++)
					buf.append(((Ability)offensiveAffects.get(i)).name()+", ");
				mob.tell(buf.toString().substring(0,buf.length()-2));
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> chant(s) over <T-NAME>, but receives no insight.");


		// return whether it worked
		return success;
	}
}
