package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/*
   Copyright 2000-2007 Bo Zimmerman

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
public class StdAuctioneer extends StdShopKeeper implements Auctioneer
{
    public String ID(){return "StdAuctioneer";}

    public StdAuctioneer()
    {
        super();
        Username="an auctioneer";
        setDescription("He talks faster than you!");
        setDisplayText("The local auctioneer is here calling prices.");
        CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);
        setMoney(0);
        whatISell=ShopKeeper.DEAL_AUCTIONEER;
        baseEnvStats.setWeight(150);
        setWimpHitPoint(0);

        baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
        baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

        baseEnvStats().setArmor(0);

        baseState.setHitPoints(1000);

        recoverMaxState();
        resetToMaxState();
        recoverEnvStats();
        recoverCharStats();
    }


    public String auctionHouse(){return text();}
    public void setAuctionHouse(String name){setMiscText(name);}
    
    protected double liveListingPrice=-1.0;
    public double liveListingPrice(){return liveListingPrice;}
    public void setLiveListingPrice(double d){liveListingPrice=d;}

    protected double timedListingPrice=-1.0;
    public double timedListingPrice(){return timedListingPrice;}
    public void setTimedListingPrice(double d){timedListingPrice=d;}

    protected double timedListingPct=-1.0;
    public double timedListingPct(){return timedListingPct;}
    public void setTimedListingPct(double d){timedListingPct=d;}

    protected double liveFinalCutPct=-1.0;
    public double liveFinalCutPct(){return liveFinalCutPct;}
    public void setLiveFinalCutPct(double d){liveFinalCutPct=d;}

    protected double timedFinalCutPct=-1.0;
    public double timedFinalCutPct(){return timedFinalCutPct;}
    public void setTimedFinalCutPct(double d){timedFinalCutPct=d;}

    protected int maxTimedAuctionDays=-1;
    public int maxTimedAuctionDays(){return maxTimedAuctionDays;}
    public void setMaxTimedAuctionDays(int d){maxTimedAuctionDays=d;}

    protected int minTimedAuctionDays=-1;
    public int minTimedAuctionDays(){return minTimedAuctionDays;}
    public void setMinTimedAuctionDays(int d){minTimedAuctionDays=d;}

    public void destroy()
    {
        super.destroy();
        CMLib.map().delAuctionHouse(this);
    }
    public void bringToLife(Room newLocation, boolean resetStats)
    {
        super.bringToLife(newLocation,resetStats);
        CMLib.map().addAuctionHouse(this);
    }

    public int whatIsSold(){return ShopKeeper.DEAL_AUCTIONEER;}
    public void setWhatIsSold(int newSellCode){ }
    
    public Vector getAuctions(Object ofLike)
    {
    	Vector auctions=new Vector();
    	String house="SYSTEM_AUCTIONS_"+auctionHouse().toUpperCase().trim();
	    Vector otherAuctions=CMLib.database().DBReadJournal(house);
	    for(int o=0;o<otherAuctions.size();o++)
	    {
	        Vector auctionData=(Vector)otherAuctions.elementAt(o);
            String from=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_FROM);
	        String to=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_TO);
            String key=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_KEY);
	        if((ofLike instanceof MOB)&&(!((MOB)ofLike).Name().equals(to)))
	        	continue;
	        if((ofLike instanceof String)&&(!((String)ofLike).equals(key)))
	        	continue;
            AuctionData data=new AuctionData();
            String start=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_DATE);
            data.start=CMath.s_long(start);
            data.tickDown=CMath.s_long(to);
            String xml=(String)auctionData.elementAt(DatabaseEngine.JOURNAL_MSG);
            Vector xmlV=CMLib.xml().parseAllXML(xml);
            xmlV=CMLib.xml().getContentsFromPieces(xmlV,"AUCTION");
            String bid=CMLib.xml().getValFromPieces(xmlV,"PRICE");
            double oldBid=CMath.s_double(bid);
            data.bid=oldBid;
            String highBidder=CMLib.xml().getValFromPieces(xmlV,"BIDDER");
            if(highBidder.length()>0)
                data.highBidderM=CMLib.map().getLoadPlayer(highBidder);
            String maxBid=CMLib.xml().getValFromPieces(xmlV,"MAXBID");
            double oldMaxBid=CMath.s_double(maxBid);
            data.highBid=oldMaxBid;
            String buyOutPrice=CMLib.xml().getValFromPieces(xmlV,"BUYOUT");
            data.buyOutPrice=CMath.s_double(buyOutPrice);
            data.auctioningM=CMLib.map().getLoadPlayer(from);
            data.currency=CMLib.beanCounter().getCurrency(data.auctioningM);
            for(int v=0;v<xmlV.size();v++)
            {
                XMLLibrary.XMLpiece X=(XMLLibrary.XMLpiece)xmlV.elementAt(v);
                if(X.tag.equalsIgnoreCase("AUCTIONITEM"))
                {
                    data.auctioningI=CMLib.coffeeMaker().getItemFromXML(X.value);
                    break;
                }
            }
            if((ofLike instanceof Item)&&(!((Item)ofLike).sameAs(data.auctioningI)))
                continue;
            auctions.addElement(data);
	    }
	    return auctions;
    }

    

    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) return true;

        return true;
    }

    public void autoGive(MOB src, MOB tgt, Item I)
    {
        CMMsg msg2=CMClass.getMsg(src,I,null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
        location().send(this,msg2);
        msg2=CMClass.getMsg(tgt,I,null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
        location().send(this,msg2);
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_SELL:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return;
            case CMMsg.TYP_BUY:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return;
            case CMMsg.TYP_VALUE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
	            {
	                CMLib.commands().postSay(this,mob,"That's for the people to decide.",true,false);
	            }
                return;
            case CMMsg.TYP_VIEW:
                super.executeMsg(myHost,msg);
                return;
            case CMMsg.TYP_LIST:
            {
                super.executeMsg(myHost,msg);
    			if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
    			{
    			}
                return;
            }
            default:
                break;
            }
        }
        super.executeMsg(myHost,msg);
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
        &&(msg.target()==location())
        &&(CMLib.flags().isInTheGame(this,true)))
        	return false;
        else
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_SELL:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
					if(!(msg.tool() instanceof Item))
					{
	                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
		                return false;
					}
					if(msg.source().isMonster())
					{
	                    CMLib.commands().postSay(this,mob,"You can't sell anything.",true,false);
		                return false;
					}
                    Item I=(Item)msg.tool();
                    if((I instanceof Container)&&(((Container)I).getContents().size()>0))
                    {
                        CMLib.commands().postSay(this,mob,I.name()+" will have to be emptied first.",true,false);
                        return false;
                    }
                    if(!(I.amWearingAt(Item.IN_INVENTORY)))
                    {
                        CMLib.commands().postSay(this,mob,I.name()+" will have to be removed first.",true,false);
                        return false;
                    }
                    AuctionRates aRates=new AuctionRates(this);
                    CMLib.commands().postSay(this,mob,"Ok, so how many local days will your auction run for ("+aRates.minDays+"-"+aRates.maxDays+")?",true,false);
                    int days=0;
                    try{days=CMath.s_int(mob.session().prompt(":","",10000));}catch(Exception e){return false;}
                    if(days==0) return false;
                    if(days<aRates.minDays)
                    {
                        CMLib.commands().postSay(this,mob,"Minimum number of local days on an auction is "+aRates.minDays+".",true,false);
                        return false;
                    }
                    if(days>aRates.maxDays)
                    {
                        CMLib.commands().postSay(this,mob,"Maximum number of local days on an auction is "+aRates.maxDays+".",true,false);
                        return false;
                    }
                    double deposit=aRates.timeListPrice;
                    deposit+=(aRates.timeListPct*new Integer(CMath.mul(days,I.baseGoldValue())).doubleValue());
                    String depositAmt=CMLib.beanCounter().nameCurrencyLong(mob, deposit);
                    if(CMLib.beanCounter().getTotalAbsoluteValue(mob,CMLib.beanCounter().getCurrency(mob))<deposit)
                    {
                        CMLib.commands().postSay(this,mob,"You don't have enough to cover the listing fee of "+depositAmt+".  Sell a cheaper item, use fewer days, or come back later.",true,false);
                        return false;
                    }
                    CMLib.commands().postSay(this,mob,"Auctioning "+I.name()+" will cost a listing fee of "+depositAmt+", proceed?",true,false);
                    try{if(!mob.session().confirm("(Y/N):","Y",10000)) return false;}catch(Exception e){return false;}
                }
                return true;
            case CMMsg.TYP_BID:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                        return false;
                    if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                        return false;
					if(msg.value()<0)
					{
	                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to do business with you.",true,false);
	                    return false;
					}
                }
                return false;
            case CMMsg.TYP_BUY:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                        return false;
                    if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                        return false;
                    CMLib.commands().postSay(this,mob,"Ugh, I can't seem to auction "+msg.tool().name()+".",true,false);
                }
                return false;
            case CMMsg.TYP_VIEW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
                {
                    if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                        return false;
                }
                return false;
            default:
                break;
            }
        }
        return super.okMessage(myHost,msg);
    }
}