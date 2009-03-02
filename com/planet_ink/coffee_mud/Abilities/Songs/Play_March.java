package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2000-2009 Bo Zimmerman

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
public class Play_March extends Play
{
	public String ID() { return "Play_March"; }
	public String name(){ return "March";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected String songOf(){return "a "+name();}

	public void affectEnvStats(Environmental affected, EnvStats stats)
	{
		super.affectEnvStats(affected,stats);
		if((affected instanceof MOB)&&(!((MOB)affected).isMonster()))
		{
			
		}
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(((MOB)target).curState().getMovement()>=((MOB)target).maxState().getMovement()/2)
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected instanceof MOB)&&(invoker()!=null))
		{
			MOB mob=(MOB)affected;
			mob.curState().adjMovement(adjustedLevel(invoker(),0)/4,mob.maxState());
		}
		return true;
	}
}
