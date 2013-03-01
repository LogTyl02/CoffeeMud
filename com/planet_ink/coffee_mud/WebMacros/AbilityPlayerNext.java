package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.miniweb.interfaces.*;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class AbilityPlayerNext extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(HTTPRequest httpReq, String parm)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getUrlParameter("ABILITY");
		if(parms.containsKey("RESET"))
		{	
			if(last!=null) httpReq.removeUrlParameter("ABILITY");
			return "";
		}
		String ableType=httpReq.getUrlParameter("ABILITYTYPE");
		if((ableType!=null)&&(ableType.length()>0))
			parms.put(ableType,ableType);
		String domainType=httpReq.getUrlParameter("DOMAIN");
		if((domainType!=null)&&(domainType.length()>0))
			parms.put("DOMAIN",domainType);
		
		String lastID="";
		String playerName=httpReq.getUrlParameter("PLAYER");
		MOB M=null;
		if((playerName!=null)&&(playerName.length()>0))
			M=CMLib.players().getLoadPlayer(playerName);
		if(M==null)
		{
			if(parms.containsKey("EMPTYOK"))
				return "<!--EMPTY-->";
			return " @break@";
		}

		Vector abilities=new Vector();
		HashSet foundIDs=new HashSet();
		for(Enumeration<Ability> a=M.allAbilities();a.hasMoreElements();)
		{
			Ability A=a.nextElement();
			if((A!=null)&&(!foundIDs.contains(A.ID())))
			{
				foundIDs.add(A.ID());
				abilities.addElement(A);
			}
		}
		foundIDs.clear();
		foundIDs=null;
		for(int a=0;a<abilities.size();a++)
		{
			Ability A=(Ability)abilities.elementAt(a);
			boolean okToShow=true;
			int classType=A.classificationCode()&Ability.ALL_ACODES;
			String className=httpReq.getUrlParameter("CLASS");
			
			if((className!=null)&&(className.length()>0))
			{
				int level=CMLib.ableMapper().getQualifyingLevel(className,true,A.ID());
				if(level<0)
					okToShow=false;
				else
				{
					String levelName=httpReq.getUrlParameter("LEVEL");
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			else
			{
				int level=CMLib.ableMapper().getQualifyingLevel("Archon",true,A.ID());
				if(level<0)
					okToShow=false;
				else
				{
					String levelName=httpReq.getUrlParameter("LEVEL");
					if((levelName!=null)&&(levelName.length()>0)&&(CMath.s_int(levelName)!=level))
						okToShow=false;
				}
			}
			if(okToShow)
			{
				if(parms.containsKey("DOMAIN")&&(classType==Ability.ACODE_SPELL))
				{
					String domain=parms.get("DOMAIN");
					if(!domain.equalsIgnoreCase(Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5]))
					   okToShow=false;
				}
				else
				{
					boolean containsOne=false;
					for(int i=0;i<Ability.ACODE_DESCS.length;i++)
						if(parms.containsKey(Ability.ACODE_DESCS[i]))
						{ containsOne=true; break;}
					if(containsOne&&(!parms.containsKey(Ability.ACODE_DESCS[classType])))
						okToShow=false;
				}
			}
			if(parms.containsKey("NOT")) 
				okToShow=!okToShow;
			if(okToShow)
			{
				if((last==null)||((last.length()>0)&&(last.equals(lastID))&&(!A.ID().equals(lastID))))
				{
					httpReq.addFakeUrlParameter("ABILITY",A.ID());
					return "";
				}
				lastID=A.ID();
			}
		}
		httpReq.addFakeUrlParameter("ABILITY","");
		if(parms.containsKey("EMPTYOK"))
			return "<!--EMPTY-->";
		return " @break@";
	}
}
