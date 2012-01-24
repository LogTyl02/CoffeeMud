package com.planet_ink.coffee_mud.CharClasses;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/* 
   Copyright 2000-2012 Bo Zimmerman

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
public class Prancer extends StdCharClass
{
	public String ID(){return "Prancer";}
	public String name(){return "Dancer";}
	public String baseClass(){return "Bard";}
	public int getMovementMultiplier(){return 18;}
	public int getBonusPracLevel(){return 1;}
	public int getBonusAttackLevel(){return 0;}
	public int getAttackAttribute(){return CharStats.STAT_CHARISMA;}
	public int getLevelsPerBonusDamage(){ return 10;}
	public int getHPDivisor(){return 3;}
	public int getHPDice(){return 2;}
	public int getHPDie(){return 6;}
	public int getManaDivisor(){return 6;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 2;}
	protected String armorFailMessage(){return "<S-NAME> armor makes <S-HIM-HER> mess up <S-HIS-HER> <SKILL>!";}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_THIEFLIKE;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}

	public Prancer()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
		maxStatAdj[CharStats.STAT_STRENGTH]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Befriend",50,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Dance_Stop",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Dance_CanCan",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Thief_Lore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Dance_Foxtrot",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Fighter_Kick",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Dance_Tarantella",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Thief_Appraise",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Dance_Waltz",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Dance_Salsa",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Dance_Grass",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Dance_Clog",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Distract",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Dance_Capoeira",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Dance_Tap",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Dance_Swing",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Disarm",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Dance_Basse",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_BodyFlip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Dance_Tango",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_Spring",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Dance_Polka",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Dance_RagsSharqi",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Dance_Manipuri",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_Trip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Dance_Cotillon",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_TwoWeaponFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Dance_Ballet",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Tumble",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Dance_Jitterbug",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Dance_Butoh",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Attack2",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Dance_Courante",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Dance_Musette",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_Endurance",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_Cartwheel",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Dance_Swords",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Dance_Flamenco",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_Roll",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Dance_Jingledress",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Dance_Morris",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_BlindFighting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Dance_Butterfly",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Dance_Macabre",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_CircleTrip",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Dance_War",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Dance_Square",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

    public void executeMsg(Environmental host, CMMsg msg)
    {
        super.executeMsg(host,msg);
        Bard.visitationBonusMessage(host,msg);
    }
    
	public String getStatQualDesc(){return "Charisma 9+, Strength 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_CHARISMA) <= 8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Charisma to become a Dancer.");
				return false;
			}
			if(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH) <= 8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Strength to become a Dancer.");
				return false;
			}
			Race R=mob.baseCharStats().getMyRace();
			if((!(R.racialCategory().equals("Human")))
			&&(!(R.racialCategory().equals("Humanoid")))
			&&(!(R.racialCategory().equals("Elf")))
			&&(!(R.racialCategory().equals("Halfling"))))
			{
				if(!quiet)
					mob.tell("You must be Human, Elf, Halfling, or Half Elf to be a Dancer");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
	public String getOtherLimitsDesc(){return "";}
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.add(w);
		}
		return outfitChoices;
	}
	

	
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
												mob.charStats().getClassLevel(ID()),
												false,
												false);
			for(AbilityMapper.AbilityMapping able : V)
			{
				Ability A=CMClass.getAbility(able.abilityID);
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			if(CMLib.flags().isStanding((MOB)affected))
			{
				MOB mob=(MOB)affected;
				int attArmor=(((int)Math.round(CMath.div(mob.charStats().getStat(CharStats.STAT_DEXTERITY),9.0)))+1)*(mob.charStats().getClassLevel(this)-1);
				affectableStats.setArmor(affectableStats.armor()-attArmor);
			}
		}
	}

	public void unLevel(MOB mob)
	{
		if(mob.phyStats().level()<2)
			return;
		super.unLevel(mob);
	    if(((mob.basePhyStats().level()+1) % 3)==0)
	    {
			int dexStat=mob.charStats().getStat(CharStats.STAT_DEXTERITY);
			int maxDexStat=(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)
						 +mob.charStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
			if(dexStat>maxDexStat) dexStat=maxDexStat;
			int attArmor=(int)Math.round(CMath.div(dexStat,10.0));
			if(dexStat>=25)attArmor+=2;
			else
			if(dexStat>=22)attArmor+=1;
			attArmor=attArmor*-1;
			mob.basePhyStats().setArmor(mob.basePhyStats().armor()-attArmor);
			mob.phyStats().setArmor(mob.phyStats().armor()-attArmor);
	    }

		mob.recoverPhyStats();
		mob.recoverCharStats();
		mob.recoverMaxState();
	}

    public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount){ return Bard.bardAdjustExperienceGain(host,mob,victim,amount,5.0);}
    
	public String getOtherBonusDesc(){return "Receives defensive bonus for high dexterity.  Receives group bonus combat experience when in an intelligent group, and more for a group of players.  Receives exploration and pub-finding experience based on danger level.";}

	public void level(MOB mob, List<String> newAbilityIDs)
	{
	    if(CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))  return;
	    if((mob.basePhyStats().level() % 3)==0)
	    {
			int dexStat=mob.charStats().getStat(CharStats.STAT_DEXTERITY);
			int maxDexStat=(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)
						 +mob.charStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
			if(dexStat>maxDexStat) dexStat=maxDexStat;
			int attArmor=((int)Math.round(CMath.div(dexStat,10.0)))+1;
			if(dexStat>=25)attArmor+=2;
			else
			if(dexStat>=22)attArmor+=1;
			
			mob.basePhyStats().setArmor(mob.basePhyStats().armor()-attArmor);
			mob.phyStats().setArmor(mob.phyStats().armor()-attArmor);
			mob.tell("^NYour grace grants you a defensive bonus of ^H"+attArmor+"^?.^N");
	    }
	}
}

