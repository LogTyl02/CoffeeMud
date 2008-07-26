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
public class Song_Rebirth extends Song
{
	public String ID() { return "Song_Rebirth"; }
	public String name(){ return "Rebirth";}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	protected boolean skipStandardSongInvoke(){return true;}
	protected boolean HAS_QUANTITATIVE_ASPECT(){return false;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        steadyDown=-1;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		unsing(mob,mob,true);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String str=auto?"The "+songOf()+" begins to play!":"^S<S-NAME> begin(s) to sing the "+songOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+songOf()+" over again.^?";

			for(int v=0;v<commonRoomSet.size();v++)
			{
				Room R=(Room)commonRoomSet.elementAt(v);
				String msgStr=getCorrectMsgString(R,str,v);
				CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					if(R==originRoom)
						R.send(mob,msg);
					else
						R.sendOthers(mob, msg);
					boolean foundOne=false;
					int i=0;
					while(i<R.numItems())
					{
						Item body=R.fetchItem(i);
						if((body!=null)
						&&(body instanceof DeadBody)
						&&(((DeadBody)body).playerCorpse())
						&&(((DeadBody)body).mobName().length()>0))
						{
							MOB rejuvedMOB=CMLib.players().getPlayer(((DeadBody)body).mobName());
							if(rejuvedMOB!=null)
							{
								rejuvedMOB.tell("You are being resusitated.");
								if(rejuvedMOB.location()!=R)
								{
									rejuvedMOB.location().showOthers(rejuvedMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> disappear(s)!");
									R.bringMobHere(rejuvedMOB,false);
								}
	
								Ability A=rejuvedMOB.fetchAbility("Prop_AstralSpirit");
								if(A!=null) rejuvedMOB.delAbility(A);
								A=rejuvedMOB.fetchEffect("Prop_AstralSpirit");
								if(A!=null) rejuvedMOB.delEffect(A);
	
								int it=0;
								while(it<rejuvedMOB.location().numItems())
								{
									Item item=rejuvedMOB.location().fetchItem(it);
									if((item!=null)&&(item.container()==body))
									{
										CMMsg msg2=CMClass.getMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
										rejuvedMOB.location().send(rejuvedMOB,msg2);
										CMMsg msg3=CMClass.getMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
										rejuvedMOB.location().send(rejuvedMOB,msg3);
										it=0;
									}
									else
										it++;
								}
								body.destroy();
								R.recoverRoomStats();
								foundOne=true;
								rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
								i=0;
							}
							else
								i++;
						}
						else
							i++;
					}
					if(!foundOne)
						mob.tell("Nothing seems to happen.");
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
