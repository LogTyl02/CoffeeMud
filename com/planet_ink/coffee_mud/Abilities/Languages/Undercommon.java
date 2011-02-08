package com.planet_ink.coffee_mud.Abilities.Languages;
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
   Copyright 2000-2011 Lee Fox

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
// just a mix of all the underworld languages
public class Undercommon extends StdLanguage
{
	public String ID() { return "Undercommon"; }
	public String name(){ return "Undercommon";}
	public static List<String[]> wordLists=null;
	private static Drowish drowish = new Drowish();
	public Undercommon()
	{
		super();
	}
	public List<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={"y","a","e","i","o","�","�","�","�","i","klpt","ih","g"};
			String[] two={"te","it","at","to","`ai","`oi","`ul","os","vi","ne","vo","li","eh","no","ai","by","et","ce","un","il","te","il","ag","go"};
			String[] three={"nep","tem","tit","nip","pop","pon","upo","wip","pin","aya","dum","mim","oyo","tum","�na","cil","sar","tan","hel","loa","si'r","hep","yur","nol","hol","qua","�th","nik","rem","tit","nip","pop","pon","ipi","wip","pec"};
			String[] four={"peep","meep","neep","pein","nopo","popo","woop","weep","teep","teet","menu","bund","ibun","khim","nala","rukhs","dumu","zirik","gunud","gabil","gamil","perp","merp","nerp","pein","noog","gobo","koer","werp","terp","tert","grlt","Jrl","gran","kert","s�ya","qual","quel","lara","uqua","sana","yava","mas'se","yan'na","quettaparma","manna","manan","merme","carma","harno","harne","varno","essar","saira","cilta","veuma","norta","turme","saita"};
			String[] five={"whemp","thwam","nippo","punno","upoon","teepe","tunno","ponno","twano","ywhap","kibil","celeb","mahal","narag","zaram","sigin","tarag","uzbad","zigil","zirak","aglab","baraz","baruk","bizar","felak","whamb","thwam","nipgo","pungo","upoin","krepe","tungo","pongo","twang","hrgap","splt","krnch","baam","poww","cuiva","cuina","nonwa","imire","nauta","cilta","entuc","norta","latin","l�tea","veuya","veuro","apama","hampa","nurta","firta","saira","holle","herwa","uquen","arcoa","calte","cemma","hanta","tan'en"};
			String[] six={"tawhag","ponsol","paleep","ponpopol","niptittle","minwap","tinmipmip","niptemtem","wipwippoo","azanul","bundushathur","morthond","felagund","gabilan","ganthol","khazad","kheled","khuzud","mazarbul","khuzdul","tawthak","krsplt","palpep","poopoo","dungdung","owwie","greepnak","tengak","grnoc","pisspiss","phlyyytt","plllb","hrangnok","ticktick","nurang","mahtale","porisal'que","hairie","tararan","amba'rwa","lati'na","ol�tie","amawil","apacen","yavinqua","apalume","lin'quil'ea","menelwa","alassea","nurmea","parmasse","ceniril","heldasse","imirin","eari'na","calaten'gew","lapselunga","ria'nna","eneques"};
			wordLists=new Vector();
			wordLists.add(one);
			wordLists.add(two);
			wordLists.add(three);
			wordLists.add(four);
			wordLists.add(five);
			wordLists.add(six);
		}
		return wordLists;
	}
    public boolean translatesLanguage(String language) 
    { 
    	return ID().equalsIgnoreCase(language) 
    			|| "Dwarven".equalsIgnoreCase(language)
    			|| "Goblinese".equalsIgnoreCase(language)
    			|| "Drowish".equalsIgnoreCase(language)
    			|| "Gnomish".equalsIgnoreCase(language);
    }
    public int getProficiency(String language) { 
        if(ID().equalsIgnoreCase(language))
            return proficiency();
        if(translatesLanguage(language))
        	return proficiency() / 5;
        return 0;
    }
	public Map<String, String> translationHash(String language)
	{
		return drowish.translationHash(drowish.ID());
	}
}