package com.planet_ink.coffee_mud.Behaviors;
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
public class FaithHelper extends StdBehavior
{
	public String ID(){return "FaithHelper";}

	protected boolean mobKiller=false;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
				((MOB)forMe).setWorshipCharID(parms.trim());
		}
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((msg.target()==null)||(!(msg.target() instanceof MOB))) return;
		MOB source=msg.source();
		MOB observer=(MOB)affecting;
		MOB target=(MOB)msg.target();

		if((target==null)||(observer==null)) return;
		if((source!=observer)
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(!observer.isInCombat())
		&&(target!=observer)
		&&(source!=target)
		&&(observer.getWorshipCharID().length()>0)
		&&(CMLib.flags().canBeSeenBy(source,observer))
		&&(CMLib.flags().canBeSeenBy(target,observer))
		&&(!BrotherHelper.isBrother(source,observer)))
		{
			if(observer.getWorshipCharID().equalsIgnoreCase(target.getWorshipCharID()))
			{
				boolean yep=Aggressive.startFight(observer,source,true,false);
				String reason="THAT`S MY FRIEND!! CHARGE!!";
				if((observer.getWorshipCharID().equals(target.getWorshipCharID()))
				&&(!observer.getWorshipCharID().equals(source.getWorshipCharID())))
					reason="BELIEVERS OF "+observer.getWorshipCharID().toUpperCase()+" UNITE! CHARGE!";
				if(yep)	CMLib.commands().postSay(observer,null,reason,false,false);
			}
		}
	}
}
