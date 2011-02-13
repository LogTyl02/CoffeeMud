package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultClan;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.AutoPromoteFlag;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Government;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Position;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2011 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
@SuppressWarnings("unchecked")
public class Clans extends StdLibrary implements ClanManager
{
	public SHashtable<String,Clan> all				  =new SHashtable<String,Clan>();
	public long	 		  		   lastGovernmentLoad = 0;
	
    public String ID(){return "Clans";}
	public boolean shutdown()
	{
		for(Enumeration e=all.elements();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
		}
		all.clear();
        return true;
	}

    public boolean isCommonClanRelations(String clanName1, String clanName2, int relation)
    {
        if((clanName1.length()==0)||(clanName2.length()==0)) return relation==Clan.REL_NEUTRAL;
        Clan C1=getClan(clanName1);
        Clan C2=getClan(clanName2);
        if((C1==null)||(C2==null)) return relation==Clan.REL_NEUTRAL;
        int i1=C1.getClanRelations(clanName2);
        int i2=C2.getClanRelations(clanName1);
        if((i1==i2)
        &&((i1==Clan.REL_WAR)
           ||(i1==Clan.REL_ALLY)))
           return i1==relation;
        for(Enumeration e=clans();e.hasMoreElements();)
        {
            Clan C=(Clan)e.nextElement();
            if((C!=C1)&&(C!=C2))
            {
                if((i1!=Clan.REL_WAR)
                &&(C1.getClanRelations(C.clanID())==Clan.REL_ALLY)
                &&(C.getClanRelations(C2.clanID())==Clan.REL_WAR))
                    i1=Clan.REL_WAR;
                if((i2!=Clan.REL_WAR)
                &&(C2.getClanRelations(C.clanID())==Clan.REL_ALLY)
                &&(C.getClanRelations(C1.clanID())==Clan.REL_WAR))
                    i2=Clan.REL_WAR;
            }
        }
        if(i1==i2) return relation==i1;
        
        if(Clan.REL_NEUTRALITYGAUGE[i1]<Clan.REL_NEUTRALITYGAUGE[i2]) return relation==i1;
        return relation==i2;
    }
    
	public int getClanRelations(String clanName1, String clanName2)
	{
		if((clanName1.length()==0)||(clanName2.length()==0)) return Clan.REL_NEUTRAL;
		Clan C1=getClan(clanName1);
		Clan C2=getClan(clanName2);
		if((C1==null)||(C2==null)) return Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(clanName2);
		int i2=C2.getClanRelations(clanName1);
		int rel=Clan.RELATIONSHIP_VECTOR[i1][i2];
		if(rel==Clan.REL_WAR) return Clan.REL_WAR;
		if(rel==Clan.REL_ALLY) return Clan.REL_ALLY;
		for(Enumeration e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			if((C!=C1)
			&&(C!=C2)
			&&(((C1.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C2.clanID())==Clan.REL_WAR)))
				||((C2.getClanRelations(C.clanID())==Clan.REL_ALLY)&&(C.getClanRelations(C1.clanID())==Clan.REL_WAR)))
					return Clan.REL_WAR;
		}
		return rel;
	}

	public Clan getClan(String id)
	{
		if(id.length()==0) return null;
		Clan C=(Clan)all.get(id.toUpperCase());
        if(C!=null) return C;
        for(Enumeration e=all.elements();e.hasMoreElements();)
        {
            C=(Clan)e.nextElement();
            if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
                return C;
        }
        return null;
	}
    public Clan findClan(String id)
    {
        Clan C=getClan(id);
        if(C!=null) return C;
        for(Enumeration e=all.elements();e.hasMoreElements();)
        {
            C=(Clan)e.nextElement();
            if(CMLib.english().containsString(CMStrings.removeColors(C.name()),id))
                return C;
        }
        return null;
    }

    public boolean isFamilyOfMembership(MOB M, List<MemberRecord> members) {
        if(M == null)
            return false;
        if(members.contains(M.Name()))
            return true;
        if((M.getLiegeID().length()>0)
        &&(M.isMarriedToLiege())
        &&(members.contains(M.getLiegeID())))
            return true;
        for(Enumeration<MOB.Tattoo> e=M.tattoos();e.hasMoreElements();)
        {
            MOB.Tattoo T=e.nextElement();
            if(T.tattooName.startsWith("PARENT:"))
            {
                String name=T.tattooName.substring("PARENT:".length());
                MOB M2=CMLib.players().getLoadPlayer(name.toLowerCase());
                if((M2 != null)&&isFamilyOfMembership(M2,members))
                    return true;
            }
        }
        return false;
    }
    
	public Enumeration<Clan> clans()
	{
		return all.elements();
	}
	public int numClans()
	{
		return all.size();
	}
	public void addClan(Clan C)
	{
		if(!CMSecurity.isDisabled("CLANTICKS"))
			CMLib.threads().startTickDown(C,Tickable.TICKID_CLAN,(int)CMProps.getTicksPerDay());
		all.put(C.clanID().toUpperCase(),C);
		CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT, 
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "+"+C.name()));
	}
	public void removeClan(Clan C)
	{
		CMLib.threads().deleteTick(C,Tickable.TICKID_CLAN);
		all.remove(C.clanID().toUpperCase());
		CMLib.map().sendGlobalMessage(CMLib.map().deity(), CMMsg.TYP_CLANEVENT, 
				CMClass.getMsg(CMLib.map().deity(), CMMsg.MSG_CLANEVENT, "-"+C.name()));
	}

	public void tickAllClans()
	{
		for(Enumeration e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			C.tick(C,Tickable.TICKID_CLAN);
		}
	}
	
	public void clanAnnounceAll(String msg)
	{
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel((String)channels.get(i),"ALL",msg,true);
	}

    public Enumeration<String> clansNames(){return all.keys();}
	public String translatePrize(int trophy)
	{
	    String prizeStr="";
	    switch(trophy)
	    {
	    	case Clan.TROPHY_AREA: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA); break;
	    	case Clan.TROPHY_CONTROL: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPCP); break;
	    	case Clan.TROPHY_EXP: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP); break;
	    	case Clan.TROPHY_PK: prizeStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPPK); break;
	    }
	    if(prizeStr.length()==0) return "None";
        if(prizeStr.length()>0)
        {
            Vector<String> V=CMParms.parse(prizeStr);
            if(V.size()>=2)
            {
                String type=((String)V.lastElement()).toUpperCase();
                String amt=(String)V.firstElement();
                if("EXPERIENCE".startsWith(type))
                    return amt+" experience point bonus.";
            }
        }
	    return prizeStr;
	}
	public boolean trophySystemActive()
	{
	    return (CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
	        || (CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0)
	        || (CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP).length()>0)
	        || (CMProps.getVar(CMProps.SYSTEM_CLANTROPPK).length()>0);
	    
	}
    
    public boolean goForward(MOB mob, Clan C, Vector commands, Clan.Function function, boolean voteIfNecessary)
    {
        if((mob==null)||(C==null)) return false;
        Clan.Authority allowed=C.getAuthority(mob.getClanRole(),function);
        if(allowed==Clan.Authority.CAN_DO) return true;
        if(allowed==Clan.Authority.CAN_NOT_DO) return false;
        if(function==Clan.Function.ASSIGN)
        {
            if(C.getAuthority(mob.getClanRole(),Clan.Function.VOTE_ASSIGN)!=Clan.Authority.CAN_DO)
               return false;
        }
        else
        if(C.getAuthority(mob.getClanRole(),Clan.Function.VOTE_OTHER)!=Clan.Authority.CAN_DO)
           return false;
        if(!voteIfNecessary) return true;
        String matter=CMParms.combine(commands,0);
        for(Enumeration e=C.votes();e.hasMoreElements();)
        {
            Clan.ClanVote CV=(Clan.ClanVote)e.nextElement();
            if((CV.voteStarter.equalsIgnoreCase(mob.Name()))
            &&(CV.voteStatus==Clan.VSTAT_STARTED))
            {
                mob.tell("This matter must be voted upon, but you already have a vote underway.");
                return false;
            }
            if(CV.matter.equalsIgnoreCase(matter))
            {
                mob.tell("This matter must be voted upon, and is already BEING voted upon.  Use CLANVOTE to see.");
                return false;
            }
        }
        if(mob.session()==null) return false;
        try{
            int numVotes=C.getNumVoters(function);
            if(numVotes==1) return true;

            if(mob.session().confirm("This matter must be voted upon.  Would you like to start the vote now (y/N)?","N"))
            {
                Clan.ClanVote CV=new Clan.ClanVote();
                CV.matter=matter;
                CV.voteStarter=mob.Name();
                CV.function=function.ordinal();
                CV.voteStarted=System.currentTimeMillis();
                CV.votes=new DVector(2);
                CV.voteStatus=Clan.VSTAT_STARTED;
                C.addVote(CV);
                C.updateVotes();
                final Clan.Function voteFunctionType = (function == Clan.Function.ASSIGN) ? Clan.Function.VOTE_ASSIGN : Clan.Function.VOTE_OTHER;
                final List<Integer> votingRoles = new LinkedList<Integer>();
                for(int i=0;i<C.getRolesList().length;i++)
                	if(C.getAuthority(i, voteFunctionType)==Clan.Authority.CAN_DO)
                		votingRoles.add(Integer.valueOf(i));
                if(votingRoles.size()>0)
                {
                	final String firstRoleName = C.getRoleName(votingRoles.iterator().next().intValue(), true, true);
                	final String rest = " "+firstRoleName+" should use CLANVOTE to participate.";
	                if(votingRoles.size() >= (C.getRolesList().length-2))
	                {
	                	if(function == Clan.Function.ASSIGN)
	                        clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new election to vote upon. "+rest);
	                	else
	                        clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
	                }
	                else
	                if(votingRoles.size()==1)
                        clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "+rest);
	                else
	                {
	                	final String[] roleNames = new String[votingRoles.size()];
	                	for(int i=0;i<votingRoles.size();i++)
	                	{
	                		Integer roleID=votingRoles.get(i);
	                		roleNames[i]=C.getRoleName(roleID.intValue(), true, true);
	                	}
	                	String list = CMLib.english().toEnglishStringList(roleNames);
                        clanAnnounce(mob,"The "+C.getGovernmentName()+" "+C.clanID()+" has a new matter to vote upon. "
                        		+list+" should use CLANVOTE to participate.");
	                }
                }
                mob.tell("Your vote has started.  Use CLANVOTE to cast your vote.");
                return false;
            }
        }
        catch(java.io.IOException e){}
        mob.tell("Without a vote, this command can not be executed.");
        return false;
    }

    protected String indt(int x)
    {
    	return CMStrings.SPACES.substring(0,x*3);
    }

    public long getLastGovernmentLoad()
    {
    	return lastGovernmentLoad;
    }

    public String getGovernmentHelp(MOB mob, String named, boolean exact)
    {
    	Clan.Government helpG=null;
    	for(Clan.Government G : getStockGovernments())
    		if(G.name.equalsIgnoreCase(named))
    			helpG=G;
    	if((helpG==null)&&(exact)) return null;
    	if(helpG==null)
        	for(Clan.Government G : getStockGovernments())
        		if(G.name.toUpperCase().startsWith(named.toUpperCase()))
        			helpG=G;
    	if(helpG==null)
    	{
    		List<Clan.Government> gtypes=new LinkedList<Clan.Government>();
    		String name=null;
        	for(Clan.Government G : getStockGovernments())
        		for(Position P : G.positions)
        			if(P.name.equalsIgnoreCase(named)||P.pluralName.equalsIgnoreCase(named))
        			{
        				gtypes.add(G);
        				name=P.name;
        			}
        	if(gtypes.size()==0)
        	{
        		if(exact) return null;
            	for(Clan.Government G : getStockGovernments())
            		for(Position P : G.positions)
            			if(P.name.toUpperCase().startsWith(named.toUpperCase())
            				||P.pluralName.toUpperCase().startsWith(named.toUpperCase()))
            			{
            				gtypes.add(G);
            				name=P.name;
            			}
        	}
        	if(gtypes.size()==0)
        		return null;
        	String[] typeNames=new String[gtypes.size()];
        	for(int g=0;g<gtypes.size();g++)
        		typeNames[g]=gtypes.get(g).name;
        	return "The "+name+" is a rank or position within the following clan types: "
        		   +CMLib.english().toEnglishStringList(typeNames)
        		   +".  Please see help on CLAN or on one of the listed clan types for more information. ";
    	}
    	if(helpG.longDesc.length()==0)
    		return null;
    	if(helpG.helpStr==null)
    	{
	    	StringBuilder str=new StringBuilder("\n\rOrganization type: "+helpG.name+"\n\r\n\r");
	    	str.append(helpG.longDesc).append("\n\r").append("\n\rAuthority Chart:\n\r\n\r");
	    	final List<Position> showablePositions=new Vector<Position>();
    		for(Position P : helpG.positions)
    		{
    			boolean showMe=false;
    			for(Clan.Authority a : P.functionChart)
    				if(a==Authority.CAN_DO)
    					showMe=true;
    			if(showMe)
    				showablePositions.add(P);
    		}
	    	final List<Position> sortedPositions=new Vector<Position>();
	    	while(sortedPositions.size() < showablePositions.size())
	    	{
	    		Position highPos=null;
	    		for(Position P : showablePositions)
	    			if((!sortedPositions.contains(P))
	    			&&((highPos==null)||(highPos.rank<P.rank)))
	    				highPos=P;
	    		sortedPositions.add(highPos);
	    	}
	    	final int[] posses=new int[sortedPositions.size()];
	    	int posTotalLen=0;
	    	for(int p=0;p<sortedPositions.size();p++)
	    	{
	    		posses[p]=sortedPositions.get(p).name.length()+2;
	    		posTotalLen+=posses[p];
	    	}
	    	int funcMaxLen=0;
	    	int funcTotal=0;
	    	String[] functionNames=new String[Clan.Function.values().length];
	    	for(int f=0;f<Clan.Function.values().length;f++)
	    	{
	    		Clan.Function func=Clan.Function.values()[f];
				funcTotal+=func.name().length()+1;
	    		if(func.name().length() > funcMaxLen)
	    			funcMaxLen=func.name().length()+1;
	    		functionNames[f]=func.name();
	    	}
	    	int funcAvg = funcTotal / Clan.Function.values().length;
	    	int funcMaxAvg = (int)CMath.round((double)funcAvg * 1.3);
	    	while((funcMaxLen > funcMaxAvg)&&((funcMaxAvg + posTotalLen)>78))
	    		funcMaxLen--;
	    	if(posses.length>0)
		    	while((funcMaxLen + posTotalLen) > 78)
		    	{
		    		int highPos=0;
		        	for(int p=1;p<sortedPositions.size();p++)
		        		if(posses[p]>posses[highPos])
		        			highPos=p;
		        	posTotalLen--;
		        	posses[highPos]--;
		    	}
	    	
	    	final int commandColLen = funcMaxLen;
	    	str.append(CMStrings.padRight("Command",commandColLen-1)).append("!");
	    	for(int p=0;p<posses.length;p++)
	    	{
	    		Clan.Position pos = sortedPositions.get(p);
	    		String name=CMStrings.capitalizeAndLower(pos.name.replace('_',' '));
		    	str.append(CMStrings.padRight(name,posses[p]-1));
		    	if(p<posses.length-1)
		    		str.append("!");
	    	}
	    	str.append("\n\r");
	    	Object lineDraw = new Object(){
	    		private static final String line = "----------------------------------------------------------------------------"; 
	    		public String toString() {
	    			StringBuilder s=new StringBuilder("");
	    	    	s.append(line.substring(0,commandColLen-1)).append("+");
	    	    	for(int p=0;p<posses.length;p++)
	    	    	{
	    		    	s.append(CMStrings.padRight(line,posses[p]-1));
	    		    	if(p<posses.length-1)
	    		    		s.append("+");
	    	    	}
	    	    	return s.toString();
	    		}
	    	};
	    	str.append(lineDraw.toString()).append("\n\r");
	    	for(Clan.Function func : Clan.Function.values())
	    	{
	    		String fname=CMStrings.capitalizeAndLower(func.toString().replace('_', ' '));
		    	str.append(CMStrings.padRight(fname,commandColLen-1)).append("!");
		    	for(int p=0;p<sortedPositions.size();p++)
		    	{
		    		Clan.Position pos = sortedPositions.get(p);
		    		Authority auth = pos.functionChart[func.ordinal()];
		    		String x = "";
		    		if(auth==Authority.CAN_DO)
		    			x="X";
		    		else
		    		if(auth==Authority.MUST_VOTE_ON)
		    			x="v";
    		    	str.append(CMStrings.padCenter(x,posses[p]-1));
    		    	if(p<posses.length-1)
    		    		str.append("!");
		    	}
		    	str.append("\n\r").append(lineDraw.toString()).append("\n\r");
	    	}
	    	helpG.helpStr=str.toString();
    	}
    	return helpG.helpStr;
    }
    
    public Clan.Government createAnarchy()
    {
		Authority[] pows=new Authority[Function.values().length];
		for(int i=0;i<pows.length;i++) pows[i]=Authority.CAN_DO;
		Position pos=new Position("FREEMAN", 0, 0, "Freeman", "Freemen", Integer.MAX_VALUE, "", pows, false);
		Government gvt=new Government(0,"Anarchy",new Position[]{pos}, 
				0,0,"",AutoPromoteFlag.NONE,true,false,Integer.valueOf(1),true,true,false,"I wanna be...","",0,0);
		gvt.isDefault=true;
		return gvt;
    }
    
    
    public void reSaveGovernmentsXML()
    {
    	Clan.Government[] govt = getStockGovernments();
    	String xml = makeGovernmentXML(govt);
    	if(!Resources.updateFileResource("clangovernments.xml", xml))
    	{
    		Log.errOut("Clans","Can't save clangovernments.xml");
    	}
    	Resources.removeResource("parsed_clangovernments");
    	getStockGovernments();
    }
    
    public Clan.Government createGovernment(String name)
    {
    	Clan.Government[] gvts=getStockGovernments();
    	List<Clan.Government> govts = new SLinkedList<Clan.Government>(gvts);
    	for(Clan.Government G : gvts)
    		if(G.name.equalsIgnoreCase(name))
    			return null;
    	Clan.Government newG=createAnarchy();
    	Set<Integer> takenIDs=new HashSet<Integer>();
    	for(Clan.Government g : gvts)
    		takenIDs.add(Integer.valueOf(g.ID));
    	int newID=CMLib.dice().roll(1, Integer.MAX_VALUE, 0);
    	for(int i=0;i<gvts.length+1;i++)
    		if(!takenIDs.contains(Integer.valueOf(i)))
    			newID=i;
    	newG.ID=newID;
    	newG.name=name;
    	govts.add(newG);
    	Resources.submitResource("parsed_clangovernments", govts.toArray(new Clan.Government[0]));
    	return newG;
    }
    
    public boolean removeGovernment(Clan.Government government)
    {
    	Clan.Government[] gvts=getStockGovernments();
    	if(gvts.length==1) return false;
    	List<Clan.Government> govts = new SLinkedList<Clan.Government>(gvts);
    	govts.remove(government);
    	if(govts.size()==gvts.length) return false;
    	Resources.submitResource("parsed_clangovernments", govts.toArray(new Clan.Government[0]));
    	return false;
    }
    
    public Clan.Government[] getStockGovernments()
    {
    	Clan.Government[] gvts=(Clan.Government[])Resources.getResource("parsed_clangovernments");
    	if(gvts==null)
    	{
    		synchronized(this)
    		{
    			if(gvts==null)
    			{
    				StringBuffer str=Resources.getFileResource("clangovernments.xml", true);
    				if(str==null)
    					gvts=new Clan.Government[0];
    				else
    				{
    					gvts=parseGovernmentXML(str);
    				}
    				if((gvts==null)||(gvts.length==0))
    				{
	    				Government gvt=createAnarchy();
	    				gvt.isDefault=true;
	    				gvts=new Government[]{gvt};
    				}
    				lastGovernmentLoad=System.currentTimeMillis();
    				Resources.submitResource("parsed_clangovernments",gvts);
    			}
    		}
    	}
    	return gvts;
    }
    
    public Clan.Government getDefaultGovernment()
    {
    	final Clan.Government[] gvts=getStockGovernments();
    	for(Clan.Government gvt : gvts)
    	{
    		if(gvt.isDefault)
    			return gvt;
    	}
		return gvts[0];
    }
    
    public Clan.Government getStockGovernment(int typeid)
    {
    	final Clan.Government[] gvts=getStockGovernments();
    	if(gvts.length <= typeid)
    	{
    		Log.errOut("Clans","Someone mistakenly requested stock government typeid "+typeid);
    		return gvts[0];
    	}
    	return gvts[typeid];
    }
    
    public String makeGovernmentXML(Clan.Government gvt)
    {
    	final StringBuilder str=new StringBuilder("");
    	str.append("<CLANTYPE ").append("TYPEID="+gvt.ID+" ").append("NAME="+gvt.name+" ").append(">\n");
    	if(gvt.isDefault)
    		str.append(indt(1)).append("<ISDEFAULT>true</ISDEFAULT>\n");
    	str.append(indt(1)).append("<SHORTDESC>").append(CMLib.xml().parseOutAngleBrackets(gvt.shortDesc)).append("</SHORTDESC>\n");
    	str.append(indt(1)).append("<LONGDESC>").append(CMLib.xml().parseOutAngleBrackets(gvt.longDesc)).append("</LONGDESC>\n");
    	str.append(indt(1)).append("<POSITIONS>\n");
    	Set<Clan.Authority> voteSet = new HashSet<Clan.Authority>(); 
    	for(Clan.Position pos : gvt.positions)
    	{
        	str.append(indt(2)).append("<POSITION ").append("ID=\""+pos.ID+"\" ").append("ROLEID="+pos.roleID+" ")
        						.append("RANK="+pos.rank+" ").append("NAME=\""+pos.name+"\" ").append("PLURAL=\""+pos.pluralName+"\" ")
        						.append("MAX="+pos.max+" ").append("INNERMASK=\""+CMLib.xml().parseOutAngleBrackets(pos.innerMaskStr)+"\" ")
        						.append("PUBLIC=\""+pos.isPublic+"\" ")
        						.append(">\n");
        	for(Clan.Authority pow : pos.functionChart)
        		if(pow==Clan.Authority.CAN_DO)
	            	str.append(indt(3)).append("<POWER>").append(pow.toString()).append("</POWER>\n");
        		else
        		if(pow==Clan.Authority.MUST_VOTE_ON)
        			voteSet.add(pow);
        	str.append(indt(2)).append("</POSITION>\n");
    	}
    	str.append(indt(1)).append("</POSITIONS>\n");
    	if(voteSet.size()==0)
	    	str.append(indt(1)).append("<VOTING/>\n");
    	else
    	{
	    	str.append(indt(1)).append("<VOTING>\n");
	    	for(Clan.Authority pow : voteSet)
            	str.append(indt(3)).append("<POWER>").append(pow.toString()).append("</POWER>\n");
	    	str.append(indt(1)).append("</VOTING>\n");
    	}
    	str.append(indt(1)).append("<AUTOPOSITION>").append(gvt.positions[gvt.autoRole].ID).append("</AUTOPOSITION>\n");
    	str.append(indt(1)).append("<ACCEPTPOSITION>").append(gvt.positions[gvt.acceptPos].ID).append("</ACCEPTPOSITION>\n");
    	str.append(indt(1)).append("<REQUIREDMASK>").append(CMLib.xml().parseOutAngleBrackets(gvt.requiredMaskStr)).append("</REQUIREDMASK>\n");
    	str.append(indt(1)).append("<AUTOPROMOTEBY>").append(gvt.autoPromoteBy.toString()).append("</AUTOPROMOTEBY>\n");
    	str.append(indt(1)).append("<PUBLIC>").append(gvt.isPublic).append("</PUBLIC>\n");
    	str.append(indt(1)).append("<FAMILYONLY>").append(gvt.isFamilyOnly).append("</FAMILYONLY>\n");
    	if(gvt.overrideMinMembers == null)
	    	str.append(indt(1)).append("<OVERRIDEMINMEMBERS />\n");
    	else
	    	str.append(indt(1)).append("<OVERRIDEMINMEMBERS>").append(gvt.overrideMinMembers.toString()).append("</OVERRIDEMINMEMBERS>\n");
    	str.append(indt(1)).append("<CONQUEST>\n");
    	{
        	str.append(indt(2)).append("<ENABLED>").append(gvt.conquestEnabled).append("</ENABLED>\n");
        	str.append(indt(2)).append("<ITEMLOYALTY>").append(gvt.conquestItemLoyalty).append("</ITEMLOYALTY>\n");
        	str.append(indt(2)).append("<DEITYBASIS>").append(gvt.conquestDeityBasis).append("</DEITYBASIS>\n");
    	}
    	str.append(indt(1)).append("</CONQUEST>\n");
    	str.append("</CLANTYPE>\n");
    	return str.toString();
    }
    
    public String makeGovernmentXML(Clan.Government gvts[])
    {
    	final StringBuilder str=new StringBuilder("");
    	str.append("<CLANTYPES>\n");
    	for(Clan.Government gvt : gvts)
    		str.append(makeGovernmentXML(gvt));
    	str.append("</CLANTYPES>\n");
    	return str.toString();
    }
    
    public Clan.Government[] parseGovernmentXML(StringBuffer xml)
    {
    	List<XMLLibrary.XMLpiece> xmlV = CMLib.xml().parseAllXML(xml);
    	XMLLibrary.XMLpiece clanTypesTag = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPES");
    	List<XMLLibrary.XMLpiece> clanTypes = null;
    	if(clanTypesTag != null)
    		clanTypes = clanTypesTag.contents;
    	else
    	{
        	XMLLibrary.XMLpiece clanType = CMLib.xml().getPieceFromPieces(xmlV, "CLANTYPE");
        	if(clanType != null)
        	{
        		clanTypes = new LinkedList<XMLLibrary.XMLpiece>();
        		clanTypes.add(clanType);
        	}
        	else
        	{
	    		Log.errOut("Clans","No CLANTYPES found in xml"); 
	        	return null;
        	}
    	}
    	 
    	List<Government> governments=new LinkedList<Government>();
    	for(XMLLibrary.XMLpiece clanTypePieceTag : clanTypes)
    	{
			final String typeName=clanTypePieceTag.parms.get("NAME");
			final int typeID=CMath.s_int(clanTypePieceTag.parms.get("TYPEID"));
			final boolean isDefault=CMath.s_bool(clanTypePieceTag.parms.get("ISDEFAULT"));
	    	
			Authority[]	baseFunctionChart = new Authority[Function.values().length];
			for(int i=0;i<Function.values().length;i++)
				baseFunctionChart[i]=Authority.CAN_NOT_DO;
	    	XMLLibrary.XMLpiece votingTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "VOTING");
	    	int maxVotingDays = CMath.s_int(votingTag.parms.get("MAXDAYS"));
	    	int minVotingPct = CMath.s_int(votingTag.parms.get("QUORUMPCT"));
	    	for(XMLLibrary.XMLpiece piece : votingTag.contents)
	    	{
	    		if(piece.tag.equalsIgnoreCase("POWER"))
	    		{
	    			Function power = Function.valueOf(piece.value);
	    			if(power == null)
	    	    		Log.errOut("Clans","Illegal power found in xml: "+piece.value);
	    			else
	    				baseFunctionChart[power.ordinal()] = Authority.MUST_VOTE_ON;
	    		}
	    	}
	    	
	    	final List<Position> positions=new LinkedList<Position>();
	    	XMLLibrary.XMLpiece positionsTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "POSITIONS");
	    	for(XMLLibrary.XMLpiece posPiece : positionsTag.contents)
	    	{
	    		if(posPiece.tag.equalsIgnoreCase("POSITION"))
	    		{
	    			Authority[]	functionChart = baseFunctionChart.clone();
	    			final String ID=posPiece.parms.get("ID");
	    			final int roleID=CMath.s_int(posPiece.parms.get("ROLEID"));
	    			final int rank=CMath.s_int(posPiece.parms.get("RANK"));
	    			final String name=posPiece.parms.get("NAME");
	    			final String pluralName=posPiece.parms.get("PLURAL");
	    			final int max=CMath.s_int(posPiece.parms.get("MAX"));
	    			final boolean isPublic=CMath.s_bool(posPiece.parms.get("PUBLIC"));
	    			final String innerMaskStr=CMLib.xml().restoreAngleBrackets(posPiece.parms.get("INNERMASK"));
	    	    	for(XMLLibrary.XMLpiece powerPiece : posPiece.contents)
	    	    	{
	    	    		if(powerPiece.tag.equalsIgnoreCase("POWER"))
	    	    		{
	    	    			Function power = Function.valueOf(powerPiece.value);
	    	    			if(power == null)
	    	    	    		Log.errOut("Clans","Illegal power found in xml: "+powerPiece.value);
	    	    			else
	    	    				functionChart[power.ordinal()] = Authority.CAN_DO;
	    	    		}
	    	    	}
	    			Position pos=new Position(ID,roleID,rank,name,pluralName,max,innerMaskStr,functionChart,isPublic);
	    			positions.add(pos);
	    		}
	    	}
    		Position[] posArray = new Position[positions.size()];
    		for(Position pos : positions)
    			if((pos.roleID>=0)&&(pos.roleID<positions.size()))
    				if(posArray[pos.roleID]!=null)
    				{
        	    		Log.errOut("Clans","Bad ROLEID "+pos.roleID+" in positions list in "+typeName);
        	    		posArray=new Position[0];
        	    		break;
    				}
    				else
	    			{
	    				posArray[pos.roleID]=pos;
	    			}
    		if(posArray.length==0)
			{
	    		Log.errOut("Clans","Missing positions in "+typeName);
	    		continue;
			}
			String	autoRoleStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "AUTOPOSITION");
			Position autoRole=null;
			for(Position pos : positions)
				if(pos.ID.equalsIgnoreCase(autoRoleStr) )
					autoRole=pos;
			if(autoRole==null)
			{
	    		Log.errOut("Clans","Illegal role found in xml: "+autoRoleStr);
	    		continue;
			}
			String	acceptRoleStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "ACCEPTPOSITION");
			Position acceptRole=null;
			for(Position pos : positions)
				if(pos.ID.equalsIgnoreCase(acceptRoleStr) )
					acceptRole=pos;
			if(acceptRole==null)
			{
	    		Log.errOut("Clans","Illegal acceptRole found in xml: "+acceptRoleStr);
	    		continue;
			}
			String requiredMaskStr=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "REQUIREDMASK"));
			String shortDesc=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "SHORTDESC"));
			String longDesc=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "LONGDESC"));
			String autoPromoteStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "AUTOPROMOTEBY");
			Clan.AutoPromoteFlag autoPromote = AutoPromoteFlag.valueOf(autoPromoteStr);
			if(autoPromote==null)
			{
	    		Log.errOut("Clans","Illegal AUTOPROMOTEBY found in xml: "+autoPromoteStr);
	    		continue;
			}
			boolean isPublic=CMath.s_bool(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "PUBLIC"));
			boolean isFamilyOnly=CMath.s_bool(CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "FAMILYONLY"));
			String	overrideMinMembersStr=CMLib.xml().getValFromPieces(clanTypePieceTag.contents, "OVERRIDEMINMEMBERS");
			Integer overrideMinMembers = null;
			if((overrideMinMembersStr!=null)&&CMath.isInteger(overrideMinMembersStr))
				overrideMinMembers=Integer.valueOf(CMath.s_int(overrideMinMembersStr));
			XMLLibrary.XMLpiece conquestTag = CMLib.xml().getPieceFromPieces(clanTypePieceTag.contents, "CONQUEST");
			boolean conquestEnabled=true;
			boolean conquestItemLoyalty=true;
			boolean conquestDeityBasis=false;
			if(conquestTag!=null)
			{
				conquestEnabled=CMath.s_bool(CMLib.xml().getValFromPieces(conquestTag.contents, "ENABLED"));
				conquestItemLoyalty=CMath.s_bool(CMLib.xml().getValFromPieces(conquestTag.contents, "ITEMLOYALTY"));
				conquestDeityBasis=CMath.s_bool(CMLib.xml().getValFromPieces(conquestTag.contents, "DEITYBASIS"));
			}
			Government gvt=new Government(typeID,typeName,posArray, 
						   autoRole.roleID,acceptRole.roleID,requiredMaskStr,autoPromote,isPublic,isFamilyOnly,
						   overrideMinMembers, conquestEnabled,conquestItemLoyalty,conquestDeityBasis,shortDesc,
						   longDesc,maxVotingDays,minVotingPct);
			gvt.isDefault=isDefault;
			governments.add(gvt);
    	}
    	Government[] govts=new Government[governments.size()];
    	for(Government govt : governments)
    		if((govt.ID < 0)||(govt.ID >=governments.size()) || (govts[govt.ID]!=null))
    		{
	    		Log.errOut("Clans","Bad TYPEID "+govt.ID);
	    		return new Government[0];
    		}
    		else
    			govts[govt.ID]=govt;
    	
    	if(governments.size()>0)
    	{
	    	for(int i=0;i<govts.length;i++)
	    		if(govts[i]==null)
	    			govts[i]=governments.get(0);
	    	return govts;
    	}
    	else
    	{
    		return null;
    	}
    }

    public void clanAnnounce(MOB mob, String msg)
    {
    	List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel(mob,(String)channels.get(i),msg,true);
    }
    
	public boolean authCheck(String clanID, int roleID, Function function) 
	{
		if((clanID==null)||(clanID.length()==0)) return false;
		Clan C=(Clan)all.get(clanID.toUpperCase());
        if(C==null) return false;
        return C.getAuthority(roleID, function)!=Clan.Authority.CAN_NOT_DO;
	}
}
