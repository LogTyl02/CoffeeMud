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
public class Prayer_InfuseUnholiness extends Prayer
{
	@Override public String ID() { return "Prayer_InfuseUnholiness"; }
	private final static String localizedName = CMLib.lang()._("Infuse Unholiness");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Infused Unholiness)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	@Override public long flags(){return Ability.FLAG_UNHOLY;}
	@Override public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	protected int serviceRunning=0;
	@Override public int abilityCode(){return serviceRunning;}
	@Override public void setAbilityCode(int newCode){serviceRunning=newCode;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_EVIL);
		if(CMath.bset(affectableStats.disposition(),PhyStats.IS_GOOD))
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_GOOD);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(canBeUninvoked())
			if(affected instanceof MOB)
				((MOB)affected).tell(_("Your infused unholiness fades."));

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(serviceRunning==0)
			return super.okMessage(myHost, msg);
		if(((msg.targetMajor() & CMMsg.MASK_MALICIOUS)==CMMsg.MASK_MALICIOUS)
		&&(msg.target() instanceof MOB))
		{
			if(msg.source().getWorshipCharID().equalsIgnoreCase(((MOB)msg.target()).getWorshipCharID()))
			{
				msg.source().tell(_("Not right now -- you're in a service."));
				msg.source().makePeace();
				((MOB)msg.target()).makePeace();
				return false;
			}
		}
		if((msg.sourceMinor() == CMMsg.TYP_LEAVE)&&(msg.source().isMonster()))
		{
			msg.source().tell(_("Not right now -- you're in a service."));
			return false;
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_ANY);
		if(target==null)
		{
			if((CMLib.law().doesOwnThisProperty(mob,mob.location()))
			&&(CMParms.combine(commands,0).equalsIgnoreCase("room")
				||CMParms.combine(commands,0).equalsIgnoreCase("here")))
				target=mob.location();
			else
				return false;
		}

		Deity D=null;
		if(CMLib.law().getClericInfusion(target)!=null)
		{

			if(target instanceof Room) D=CMLib.law().getClericInfused((Room)target);
			if(D!=null)
				mob.tell(_("There is already an infused aura of @x1 around @x2.",D.Name(),target.name(mob)));
			else
				mob.tell(_("There is already an infused aura around @x1.",target.name(mob)));
			return false;
		}

		D=mob.getMyDeity();
		if(target instanceof Room)
		{
			if(D==null)
			{
				mob.tell(_("The faithless may not infuse unholiness in a room."));
				return false;
			}
			final Area A=mob.location().getArea();
			Room R=null;
			for(final Enumeration e=A.getMetroMap();e.hasMoreElements();)
			{
				R=(Room)e.nextElement();
				if(CMLib.law().getClericInfused((Room)target)==D)
				{
					mob.tell(_("There is already an unholy place of @x1 in this area at @x2.",D.Name(),R.displayText(mob)));
					return false;
				}
			}
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?_("An unholy aura appears around <T-NAME>."):_("^S<S-NAME> @x1 to infuse an unholy aura around <T-NAMESELF>.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(D!=null) setMiscText(D.Name());
				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,((Room)target))))
				{
					target.addNonUninvokableEffect((Ability)this.copyOf());
					CMLib.database().DBUpdateRoom((Room)target);
				}
				else
					beneficialAffect(mob,target,asLevel,0);
				target.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,_("<S-NAME> @x1 to infuse an unholy aura in <T-NAMESELF>, but fail(s).",prayForWord(mob)));

		return success;
	}
}
