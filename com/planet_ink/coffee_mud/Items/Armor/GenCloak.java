package com.planet_ink.coffee_mud.Items.Armor;
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
public class GenCloak extends GenArmor
{
	public String ID(){	return "GenCloak";}
	public GenCloak()
	{
		super();

		setName("a hooded cloak");
		setDisplayText("a hooded cloak is here");
		setDescription("");
		properWornBitmap=Item.WORN_ABOUT_BODY;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		recoverEnvStats();
		material=RawMaterial.RESOURCE_COTTON;
		readableText="a hooded figure";
	}

	public void affectEnvStats(Environmental host, EnvStats stats)
	{
		if(!amWearingAt(Item.IN_INVENTORY))
			stats.setName(readableText());
	}
}
