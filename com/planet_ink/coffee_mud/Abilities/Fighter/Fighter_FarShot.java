package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_FarShot extends FighterSkill
{
	public String ID() { return "Fighter_FarShot"; }
	public String name(){ return "Far Shot";}
	public String displayText(){ return "";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public int checkDown=4;

	protected Vector qualifiedWeapons=new Vector();

    protected void cloneFix(Ability E)
    {
        super.cloneFix(E);
        qualifiedWeapons=((Vector)((Fighter_FarShot)E).qualifiedWeapons.clone());
    }

    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        qualifiedWeapons=new Vector();
    }
    
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source()==affected)
		&&(msg.target() instanceof Weapon)
		&&(((Weapon)msg.target()).weaponClassification()==Weapon.CLASS_RANGED)
		&&(((Weapon)msg.target()).ammunitionType().length()>0))
		{
			if(((msg.targetMinor()==CMMsg.TYP_WEAR)
			   ||(msg.targetMinor()==CMMsg.TYP_WIELD)
			   ||(msg.targetMinor()==CMMsg.TYP_HOLD))
			&&(!qualifiedWeapons.contains(msg.target()))
			&&((msg.source().fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
			{
				qualifiedWeapons.addElement(msg.target());
				Ability A=(Ability)this.copyOf();
				A.setSavable(false);
				msg.target().addEffect(A);
			}
			else
			if(((msg.targetMinor()==CMMsg.TYP_REMOVE)
				||(msg.targetMinor()==CMMsg.TYP_DROP))
			&&(qualifiedWeapons.contains(msg.target())))
			{
				qualifiedWeapons.removeElement(msg.target());
				msg.target().delEffect(msg.target().fetchEffect(ID()));
			}
		}
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof Item)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.SENSE_ITEMNOMAXRANGE);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(--checkDown<=0)
		{
			checkDown=5;
			Item w=mob.fetchWieldedItem();
			if((w!=null)
			&&(w instanceof Weapon)
			&&(((Weapon)w).weaponClassification()==Weapon.CLASS_RANGED)
			&&(((Weapon)w).ammunitionType().length()>0)
			&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
			{
				if((CMLib.dice().rollPercentage()==1)&&(CMLib.dice().rollPercentage()<10))
					helpProficiency(mob);
				if(!qualifiedWeapons.contains(w))
				{
					qualifiedWeapons.addElement(w);
					Ability A=(Ability)this.copyOf();
					A.setSavable(false);
					w.addEffect(A);
				}
			}
			for(int i=qualifiedWeapons.size()-1;i>=0;i--)
			{
				Item I=(Item)qualifiedWeapons.elementAt(i);
				if((I.amWearingAt(Item.IN_INVENTORY))
				||(I.owner()!=affected))
				{
					qualifiedWeapons.removeElement(I);
					I.delEffect(I.fetchEffect(ID()));
				}
			}
		}
		return true;
	}
}
