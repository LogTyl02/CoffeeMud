package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
/*
   Copyright 2000-2011 Bo Zimmerman

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
public class CMMap extends StdLibrary implements WorldMap
{
    public String ID(){return "CMMap";}
	public final int 		QUADRANT_WIDTH	 = 10;
    public static MOB 		deityStandIn	 = null;
    public long 			lastVReset		 = 0;
	public List<Area>		areasList 		 = new SVector<Area>();
    public List<Area>		sortedAreas	     = null;
	public List<Deity>		deitiesList 	 = new SVector<Deity>();
    public List<PostOffice>	postOfficeList	 = new SVector<PostOffice>();
    public List<Auctioneer>	auctionHouseList = new SVector<Auctioneer>();
    public List<Banker> 	bankList		 = new SVector<Banker>();
	public List<SpaceObject>space			 = new SVector<SpaceObject>();
	
    public Map<Integer,List<WeakReference<MsgListener>>> 
    						globalHandlers	 = new SHashtable<Integer,List<WeakReference<MsgListener>>>();
	public Map<String,SLinkedList<LocatedPair>>
							scriptHostMap	 = new STreeMap<String,SLinkedList<LocatedPair>>();
    
    private ThreadEngine.SupportThread  thread	 = null;
    public ThreadEngine.SupportThread getSupportThread() { return thread;}
    
    protected int getGlobalIndex(Vector list, String name)
    {
        if(list.size()==0) return -1;
        int start=0;
        int end=list.size()-1;
        while(start<=end)
        {
            int mid=(end+start)/2;
            int comp=((Environmental)list.elementAt(mid)).Name().compareToIgnoreCase(name);
            if(comp==0)
                return mid;
            else
            if(comp>0)
                end=mid-1;
            else
                start=mid+1;

        }
        return -1;
    }

	// areas
	public int numAreas() { return areasList.size(); }
	public void addArea(Area newOne)
	{
		sortedAreas=null;
		areasList.add(newOne);
	}

	public void delArea(Area oneToDel)
	{
		sortedAreas=null;
		areasList.remove(oneToDel);
	}

	public Enumeration<Area> sortedAreas()
	{
		if(sortedAreas==null)
		{
			SVector V=new SVector<Area>();
			Area A=null;
			for(Enumeration e=areas();e.hasMoreElements();)
			{
				A=(Area)e.nextElement();
				String upperName=A.Name().toUpperCase();
				for(int v=0;v<=V.size();v++)
					if(v==V.size())
					{ V.addElement(A); break;}
					else
					if(upperName.compareTo(((Area)V.elementAt(v)).Name().toUpperCase())<=0)
					{ V.insertElementAt(A,v); break;}
			}
			sortedAreas=V;
		}
		return (sortedAreas==null)?sortedAreas():new IteratorEnumeration<Area>(sortedAreas.iterator());
	}

	public Area getArea(String calledThis)
	{
		for(Enumeration a=areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(A.Name().equalsIgnoreCase(calledThis))
				return A;
		}
		return null;
	}
    public Area findAreaStartsWith(String calledThis)
    {
        Area A=getArea(calledThis);
        if(A!=null) return A;
        for(Enumeration a=areas();a.hasMoreElements();)
        {
            A=(Area)a.nextElement();
            if(A.Name().toUpperCase().startsWith(calledThis))
                return A;
        }
        return null;
    }

    public Area findArea(String calledThis)
    {
        Area A=findAreaStartsWith(calledThis);
        if(A!=null) return A;
        for(Enumeration a=areas();a.hasMoreElements();)
        {
            A=(Area)a.nextElement();
            if(CMLib.english().containsString(A.Name(),calledThis))
                return A;
        }
        return null;
    }
	public Enumeration<Area> areas() { return new IteratorEnumeration<Area>(areasList.iterator()); }
	public Enumeration<String> roomIDs(){ return new WorldMap.CompleteRoomIDEnumerator(this);}
	public Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return (Area) areas().nextElement();
		return null;
	}
	public Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try{
				A=(Area)areasList.get(CMLib.dice().roll(1,numAreas(),-1));
            }catch(ArrayIndexOutOfBoundsException e){}
		}
		return A;
	}

    public void addGlobalHandler(MsgListener E, int category)
    {
    	if(E==null) return;
        List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
        if(V==null)
        {
            V=new SLinkedList<WeakReference<MsgListener>>();
            globalHandlers.put(Integer.valueOf(category),V);
        }
        synchronized(V)
        {
        	for(WeakReference<MsgListener> l : V)
        		if(l.get()==E)
        			return;
            V.add(new WeakReference<MsgListener>(E));
        }
    }

    public void delGlobalHandler(MsgListener E, int category)
    {
    	List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
        if((E==null)||(V==null)) 
        	return;
        synchronized(V)
        {
        	for(WeakReference<MsgListener> W : V)
        		if(W.get()==E)
		        	V.remove(W);
        }
    }
    public MOB deity() 
    {
    	if(deities().hasMoreElements())
    		return (MOB)deities().nextElement();
    	if((deityStandIn==null)
    	||(deityStandIn.amDestroyed())
    	||(deityStandIn.amDead())
    	||(deityStandIn.location()==null)
    	||(deityStandIn.location().isInhabitant(deityStandIn)))
    	{
    		if(deityStandIn!=null) deityStandIn.destroy();
            MOB everywhereMOB=CMClass.getMOB("StdMOB");
            everywhereMOB.setName("god");
            everywhereMOB.setLocation(this.getRandomRoom());
            deityStandIn=everywhereMOB;
    	}
    	return deityStandIn;
    }
    public MOB mobCreated() { return mobCreated(this.getRandomRoom());}
    public MOB mobCreated(Room R){
        MOB everywhereMOB=CMClass.getMOB("StdMOB");
        everywhereMOB.setName("somebody");
        everywhereMOB.setLocation(R);
        return everywhereMOB;
    }

	public boolean isObjectInSpace(SpaceObject O){ return space.contains(O); }
	public void delObjectInSpace(SpaceObject O){ space.remove(O); }
	public void addObjectToSpace(SpaceObject O){ space.add(O); }

	public long getDistanceFrom(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(CMath.mul((O1.coordinates()[0]-O2.coordinates()[0]),(O1.coordinates()[0]-O2.coordinates()[0]))
									+CMath.mul((O1.coordinates()[1]-O2.coordinates()[1]),(O1.coordinates()[1]-O2.coordinates()[1]))
									+CMath.mul((O1.coordinates()[2]-O2.coordinates()[2]),(O1.coordinates()[2]-O2.coordinates()[2]))));
	}
	public double[] getDirection(SpaceObject FROM, SpaceObject TO)
	{
		double[] dir=new double[2];
		double x=(double)(TO.coordinates()[0]-FROM.coordinates()[0]);
		double y=(double)(TO.coordinates()[1]-FROM.coordinates()[1]);
		double z=(double)(TO.coordinates()[2]-FROM.coordinates()[2]);
		dir[0]=Math.toDegrees(Math.acos(x/Math.sqrt((x*x)+(y*y))));
		dir[1]=Math.toDegrees(Math.acos(z/Math.sqrt((z*z)+(y*y))));
		return dir;
	}

	public void moveSpaceObject(SpaceObject O)
	{
		double x1=Math.cos(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double y1=Math.sin(Math.toRadians(O.direction()[0]))*Math.sin(Math.toRadians(O.direction()[1]));
		double z1=Math.cos(O.direction()[1]);
		O.coordinates()[0]=O.coordinates()[0]+Math.round(CMath.mul(O.velocity(),x1));
		O.coordinates()[1]=O.coordinates()[1]+Math.round(CMath.mul(O.velocity(),y1));
		O.coordinates()[2]=O.coordinates()[2]+Math.round(CMath.mul(O.velocity(),z1));
	}

	public long getRelativeVelocity(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt((double)(((O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0])*(O1.velocity()*O1.coordinates()[0])-(O2.velocity()*O2.coordinates()[0]))
									+((O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1])*(O1.velocity()*O1.coordinates()[1])-(O2.velocity()*O2.coordinates()[1]))
									+((O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2])*(O1.velocity()*O1.coordinates()[2])-(O2.velocity()*O2.coordinates()[2])))));
	}

	public String createNewExit(Room from, Room room, int direction)
	{
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.  One-way link created.";

		Exit thisExit=null;
		synchronized(("SYNC"+from.roomID()).intern())
		{
			from=getRoom(from);
			if(opRoom!=null)
				from.rawDoors()[direction]=null;

			from.rawDoors()[direction]=room;
			thisExit=from.getRawExit(direction);
			if(thisExit==null)
			{
				thisExit=CMClass.getExit("StdOpenDoorway");
				from.setRawExit(direction,thisExit);
			}
			CMLib.database().DBUpdateExits(from);
		}
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
			if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
			{
				room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
				room.setRawExit(Directions.getOpDirectionCode(direction),thisExit);
				CMLib.database().DBUpdateExits(room);
			}
		}
		return "";
	}

	public int numRooms()
    {
        int total=0;
        for(Enumeration e=areas();e.hasMoreElements();)
            total+=((Area)e.nextElement()).properSize();
        return total;
    }

    public boolean sendGlobalMessage(MOB host, int category, CMMsg msg)
    {
        List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
        if(V==null) 
        	return true;
        synchronized(V)
        {
	        try
	        {
	        	MsgListener O=null;
	            Environmental E=null;
	            for(WeakReference<MsgListener> W : V)
	            {
	            	O=W.get();
	            	if(O instanceof Environmental)
	            	{
		                E=(Environmental)O;
		                if(!CMLib.flags().isInTheGame(E,true))
		                {
		                    if(!CMLib.flags().isInTheGame(E,false))
		                        delGlobalHandler(E,category);
		                }
		                else
		                if(!E.okMessage(host,msg))
		                    return false;
	            	}
	            	else
	            	if(O instanceof MsgListener)
	            	{
	            		if(!((MsgListener)O).okMessage(host, msg))
	            			return false;
	            	}
	            	else
	            	if(O==null)
	            		V.remove(W);
	            }
	            for(WeakReference<MsgListener> W : V)
	            {
	            	O=W.get();
	            	if(O instanceof MsgListener)
		                ((MsgListener)O).executeMsg(host,msg);
	            }
	        }
	        catch(java.lang.ArrayIndexOutOfBoundsException xx){}
	        catch(Exception x){Log.errOut("CMMap",x);}
        }
        return true;
    }

	public String getExtendedRoomID(Room R)
	{
		if(R==null) return "";
		if(R.roomID().length()>0) return R.roomID();
		Area A=R.getArea();
		if(A==null) return "";
		GridLocale GR=R.getGridParent();
		if(GR!=null) return GR.getGridChildCode(R);
		return R.roomID();
	}

    public Room getRoom(Enumeration<Room> roomSet, String calledThis)
    {
        try
        {
            if(calledThis==null) return null;
            if(calledThis.endsWith(")"))
            {
                int child=calledThis.lastIndexOf("#(");
                if(child>1)
                {
                    Room R=getRoom(roomSet,calledThis.substring(0,child));
                    if((R!=null)&&(R instanceof GridLocale))
                    {
                        R=((GridLocale)R).getGridChild(calledThis);
                        if(R!=null) return R;
                    }
                }
            }
            Room R=null;
            if(roomSet==null)
            {
                int x=calledThis.indexOf('#');
                if(x>=0)
                {
                    Area A=getArea(calledThis.substring(0,x));
                    if(A!=null) R=A.getRoom(calledThis);
                    if(R!=null) return R;
                }
                for(Enumeration e=areas();e.hasMoreElements();)
                {
                    R = ((Area)e.nextElement()).getRoom(calledThis);
                    if(R!=null) return R;
                }
            }
            else
            for(Enumeration e=roomSet;e.hasMoreElements();)
            {
                R=(Room)e.nextElement();
                if(R.roomID().equalsIgnoreCase(calledThis))
                    return R;
            }
        }
        catch(java.util.NoSuchElementException x){}
        return null;
    }
    
    public Vector<Room> findRooms(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, int timePct)
    { 
    	Vector<Room> roomsV=new Vector<Room>();
		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,getRoom(mob.location().getArea().Name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,getRoom(srchStr));
		addWorldRoomsLiberally(roomsV,findRooms(rooms,mob,srchStr,displayOnly,false,timePct));
		return roomsV;
    }
    
    public Room findFirstRoom(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, int timePct)
    { 
    	Vector roomsV=new Vector();
		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,getRoom(mob.location().getArea().Name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,getRoom(srchStr));
    	if(roomsV.size()>0) return (Room)roomsV.firstElement();
		addWorldRoomsLiberally(roomsV,findRooms(rooms,mob,srchStr,displayOnly,true,timePct));
    	if(roomsV.size()>0) return (Room)roomsV.firstElement();
    	return null;
    }
    
    public Vector<Room> findRooms(Enumeration rooms, MOB mob, String srchStr, boolean displayOnly, boolean returnFirst, int timePct)
    {
    	Vector<Room> foundRooms=new Vector<Room>();
    	Vector completeRooms=new Vector();
		try { completeRooms=new XVector(rooms); }catch(NoSuchElementException nse){}
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		
		Enumeration enumSet;
		enumSet=completeRooms.elements();
		while(enumSet.hasMoreElements())
		{
	    	findRoomsByDisplay(mob,enumSet,foundRooms,srchStr,returnFirst,delay);
	    	if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
	    	if(enumSet.hasMoreElements()) try{Thread.sleep(1000 - delay);}catch(Exception e){}
		}
		if(!displayOnly)
		{
			enumSet=completeRooms.elements();
			while(enumSet.hasMoreElements())
			{
				findRoomsByDesc(mob,enumSet,foundRooms,srchStr,returnFirst,delay);
		    	if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
		    	if(enumSet.hasMoreElements()) try{Thread.sleep(1000 - delay);}catch(Exception e){}
			}
		}
    	return foundRooms;
    }
    
    protected void findRoomsByDisplay(MOB mob, Enumeration rooms, Vector foundRooms, String srchStr, boolean returnFirst, long maxTime)
    {
    	long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			boolean useTimer=maxTime>1;
			for(;rooms.hasMoreElements();)
			{
				Room room=(Room)rooms.nextElement();
				if((CMLib.english().containsString(CMStrings.removeColors(room.displayText()),srchStr))
				&&((mob==null)||CMLib.flags().canAccess(mob,room)))
					foundRooms.addElement(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
	    }catch(NoSuchElementException nse){}
    }

    protected void findRoomsByDesc(MOB mob, Enumeration rooms, Vector foundRooms, String srchStr, boolean returnFirst, long maxTime)
    {
    	long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			boolean useTimer=maxTime>1;
			for(;rooms.hasMoreElements();)
			{
				Room room=(Room)rooms.nextElement();
				if((CMLib.english().containsString(CMStrings.removeColors(room.description()),srchStr))
				&&((mob==null)||CMLib.flags().canAccess(mob,room)))
					foundRooms.addElement(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
	    }catch(NoSuchElementException nse){}
    }

    public Vector<MOB> findInhabitants(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { return findInhabitants(rooms,mob,srchStr,false,timePct);}
    public MOB findFirstInhabitant(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { 
    	Vector found=findInhabitants(rooms,mob,srchStr,true,timePct);
    	if(found.size()>0) return (MOB)found.firstElement();
    	return null;
    }
    public Vector<MOB> findInhabitants(Enumeration rooms, MOB mob, String srchStr, boolean returnFirst, int timePct)
    {
    	Vector<MOB> found=new Vector<MOB>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		for(;rooms.hasMoreElements();)
		{
			Room room=(Room)rooms.nextElement();
			if((room != null) && ((mob==null)||CMLib.flags().canAccess(mob,room)))
			{
				found.addAll(room.fetchInhabitants(srchStr));
		    	if((returnFirst)&&(found.size()>0)) return found;
			}
	    	if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
	    		try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
    	return found;
    }
    
    public Vector findInventory(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { return findInventory(rooms,mob,srchStr,false,timePct);}
    public Item findFirstInventory(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { 
    	Vector found=findInventory(rooms,mob,srchStr,true,timePct);
    	if(found.size()>0) return (Item)found.firstElement();
    	return null;
    }
    public Vector<Item> findInventory(Enumeration rooms, MOB mob, String srchStr, boolean returnFirst, int timePct)
    {
    	Vector<Item> found=new Vector<Item>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		MOB M=null;
		if(rooms==null)
		{
			for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
			{
				M=(MOB)e.nextElement();
				if(M!=null)
					found.addAll(M.findItems(srchStr));
		    	if((returnFirst)&&(found.size()>0)) return found;
			}
		}
		else
		for(;rooms.hasMoreElements();)
		{
			Room room=(Room)rooms.nextElement();
			if((room != null) && ((mob==null)||CMLib.flags().canAccess(mob,room)))
			{
				for(int m=0;m<room.numInhabitants();m++)
				{
					M=room.fetchInhabitant(m);
					if(M!=null)
						found.addAll(M.findItems(srchStr));
				}
		    	if((returnFirst)&&(found.size()>0)) return found;
			}
	    	if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
	    		try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
    	return found;
    }
    public Vector<Environmental> findShopStock(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { return findShopStock(rooms,mob,srchStr,false,false,timePct);}
    public Environmental findFirstShopStock(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { 
    	Vector found=findShopStock(rooms,mob,srchStr,true,false,timePct);
    	if(found.size()>0) return (Environmental)found.firstElement();
    	return null;
    }
    public Vector<Environmental> findShopStockers(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { return findShopStock(rooms,mob,srchStr,false,true,timePct);}
    public Environmental findFirstShopStocker(Enumeration rooms, MOB mob, String srchStr, int timePct)
    { 
    	Vector<Environmental> found=findShopStock(rooms,mob,srchStr,true,true,timePct);
    	if(found.size()>0) return (Environmental)found.firstElement();
    	return null;
    }
    public Vector<Environmental> findShopStock(Enumeration rooms, MOB mob, String srchStr, boolean returnFirst, boolean returnStockers, int timePct)
    {
    	XVector<Environmental> found=new XVector<Environmental>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		MOB M=null;
		Item I=null;
		HashSet stocks=new HashSet(1);
		HashSet areas=new HashSet();
		ShopKeeper SK=null;
		if(rooms==null)
		{
			for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
			{
				M=(MOB)e.nextElement();
				if(M!=null)
				{
					SK=CMLib.coffeeShops().getShopKeeper(M);
					if((SK!=null)&&(!stocks.contains(SK)))
					{
						stocks.add(SK);
						Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
						if(ei.hasNext()) 
						{
							if(returnFirst)
								return (returnStockers)?new XVector(M):new XVector(ei);
							if(returnStockers)
								found.add(M);
							else
								found.addAll(ei);
						}
					}
					for(int i=0;i<M.numItems();i++)
					{
						I=M.getItem(i);
						if(I!=null)
						{
							SK=CMLib.coffeeShops().getShopKeeper(I);
							if((SK!=null)&&(!stocks.contains(SK))){
								stocks.add(SK);
								Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
								if(ei.hasNext()) 
								{
									if(returnFirst)
										return (returnStockers)?new XVector(I):new XVector(ei);
									if(returnStockers)
										found.add(I);
									else
										found.addAll(ei);
								}
							}
						}
					}
				}
		    	if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
		    		try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception ex){}
			}
		}
		else
		for(;rooms.hasMoreElements();)
		{
			Room room=(Room)rooms.nextElement();
			if((room != null) && ((mob==null)||CMLib.flags().canAccess(mob,room)))
			{
				if(!areas.contains(room.getArea()))
					areas.add(room.getArea());
				SK=CMLib.coffeeShops().getShopKeeper(room);
				if((SK!=null)&&(!stocks.contains(SK))) {
					stocks.add(SK);
					Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
					if(ei.hasNext()) 
					{
						if(returnFirst)
							return (returnStockers)?new XVector(room):new XVector(ei);
						if(returnStockers)
							found.add(room);
						else
							found.addAll(ei);
					}
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					M=room.fetchInhabitant(m);
					if(M!=null)
					{
						SK=CMLib.coffeeShops().getShopKeeper(M);
						if((SK!=null)&&(!stocks.contains(SK))){
							stocks.add(SK);
							Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
							if(ei.hasNext()) 
							{
								if(returnFirst)
									return (returnStockers)?new XVector(M):new XVector(ei);
								if(returnStockers)
									found.add(M);
								else
									found.addAll(ei);
							}
						}
					}
				}
				for(int i=0;i<room.numItems();i++)
				{
					I=room.getItem(i);
					if(I!=null)
					{
						SK=CMLib.coffeeShops().getShopKeeper(I);
						if((SK!=null)&&(!stocks.contains(SK))){
							stocks.add(SK);
							Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
							if(ei.hasNext()) 
							{
								if(returnFirst)
									return (returnStockers)?new XVector(I):new XVector(ei);
								if(returnStockers)
									found.add(I);
								else
									found.addAll(ei);
							}
						}
					}
				}
			}
	    	if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
	    		try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		for(Iterator i=areas.iterator();i.hasNext();)
		{
			Area A=(Area)i.next();
			SK=CMLib.coffeeShops().getShopKeeper(A);
			if((SK!=null)&&(!stocks.contains(SK)))
			{
				stocks.add(SK);
				Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
				if(ei.hasNext()) 
				{
					if(returnFirst)
						return (returnStockers)?new XVector(A):new XVector(ei);
					if(returnStockers)
						found.add(A);
					else
						found.addAll(ei);
				}
			}
		}
    	return found;
    }
    
    public Vector findRoomItems(Enumeration rooms, MOB mob, String srchStr, boolean anyItems, int timePct)
    { return findRoomItems(rooms,mob,srchStr,anyItems,false,timePct);}
    public Item findFirstRoomItem(Enumeration rooms, MOB mob, String srchStr, boolean anyItems, int timePct)
    { 
    	Vector<Item> found=findRoomItems(rooms,mob,srchStr,anyItems,true,timePct);
    	if(found.size()>0) return (Item)found.firstElement();
    	return null;
    }
    public Vector<Item> findRoomItems(Enumeration rooms, MOB mob, String srchStr, boolean anyItems, boolean returnFirst, int timePct)
    {
    	Vector<Item> found=new Vector<Item>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		for(;rooms.hasMoreElements();)
		{
			Room room=(Room)rooms.nextElement();
			if((room != null) && ((mob==null)||CMLib.flags().canAccess(mob,room)))
			{
				found.addAll(anyItems?room.findItems(srchStr):room.findItems(null,srchStr));
		    	if((returnFirst)&&(found.size()>0)) return found;
			}
	    	if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
	    		try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
    	return found;
    }
    
	public Room getRoom(java.util.Map<String, Room> hashedRoomSet, String areaName, String calledThis)
	{
		if(calledThis.startsWith("#"))
		{
			if(hashedRoomSet.containsKey(calledThis.substring(1)))
				return (Room)hashedRoomSet.get(calledThis.substring(1));
		}
		else
		if(calledThis.startsWith(areaName+"#"))
		{
			if(hashedRoomSet.containsKey(calledThis.substring(areaName.length()+1)))
				return (Room)hashedRoomSet.get(calledThis.substring(areaName.length()+1));
		}
		else
		{
			if(hashedRoomSet.containsKey(calledThis))
				return (Room)hashedRoomSet.get(calledThis);
		}
		Room R=getRoom(calledThis);
		if(R!=null) return R;
		return getRoom(areaName+"#"+calledThis);
	}

    public Room getRoom(Room room)
    {
    	if(room==null)
    		return null;
    	if(room.amDestroyed())
    		return getRoom(getExtendedRoomID(room));
    	return room;
    }

	public Room getRoom(String calledThis){ return getRoom(null,calledThis); }
	public Enumeration<Room> rooms(){ return new AreaEnumerator(false); }
    public Enumeration<Room> roomsFilled(){ return new AreaEnumerator(true); }
	public Room getRandomRoom()
	{
		Room R=null;
        int numRooms=-1;
		while((R==null)&&((numRooms=numRooms())>0))
		{
			try
            {
				int which=CMLib.dice().roll(1,numRooms,-1);
                int total=0;
                for(Enumeration e=areas();e.hasMoreElements();)
                {
                    Area A=(Area)e.nextElement();
                    if(which<(total+A.properSize()))
                    { R=A.getRandomProperRoom(); break;}
                    total+=A.properSize();
                }
            }catch(NoSuchElementException e){}
		}
		return R;
	}

	public int numDeities() { return deitiesList.size(); }
	
	protected void addDeity(Deity newOne)
	{
		if (!deitiesList.contains(newOne))
			deitiesList.add(newOne);
	}
	
	protected void delDeity(Deity oneToDel)
	{
		deitiesList.remove(oneToDel);
	}
	
	public Deity getDeity(String calledThis)
	{
		for (Deity D : deitiesList)
			if (D.Name().equalsIgnoreCase(calledThis))
				return D;
		return null;
	}
	public Enumeration<Deity> deities() { return new IteratorEnumeration<Deity>(deitiesList.iterator()); }

    public int numPostOffices() { return postOfficeList.size(); }
    protected void addPostOffice(PostOffice newOne)
    {
        if(!postOfficeList.contains(newOne))
			postOfficeList.add(newOne);
    }
    protected void delPostOffice(PostOffice oneToDel)
    {
		postOfficeList.remove(oneToDel);
    }
    public PostOffice getPostOffice(String chain, String areaNameOrBranch)
    {
        for (PostOffice P : postOfficeList)
            if((P.postalChain().equalsIgnoreCase(chain))
            &&(P.postalBranch().equalsIgnoreCase(areaNameOrBranch)))
                return P;
        
        Area A=findArea(areaNameOrBranch);
        if(A==null) 
        	return null;
        
        for (PostOffice P : postOfficeList)
            if((P.postalChain().equalsIgnoreCase(chain))
            &&(getStartArea(P)==A))
                return P;
        return null;
    }
    public Enumeration<PostOffice> postOffices() { return new IteratorEnumeration<PostOffice>(postOfficeList.iterator()); }

    public Enumeration<Auctioneer> auctionHouses() { return new IteratorEnumeration<Auctioneer>(auctionHouseList.iterator()); }
    
    public int numAuctionHouses() { return auctionHouseList.size(); }
    
    protected void addAuctionHouse(Auctioneer newOne)
    {
        if (!auctionHouseList.contains(newOne))
        {
    		auctionHouseList.add(newOne);
        }
    }
    protected void delAuctionHouse(Auctioneer oneToDel)
    {
		auctionHouseList.remove(oneToDel);
    }
    public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch)
    {
        for (Auctioneer C : auctionHouseList)
            if((C.auctionHouse().equalsIgnoreCase(chain))
            &&(C.auctionHouse().equalsIgnoreCase(areaNameOrBranch)))
                return C;
        
        Area A=findArea(areaNameOrBranch);
        if(A==null) return null;
        
        for (Auctioneer C : auctionHouseList)
            if((C.auctionHouse().equalsIgnoreCase(chain))
            &&(getStartArea(C)==A))
                return C;
        
        return null;
    }

    public int numBanks() { return bankList.size(); }
    protected void addBank(Banker newOne)
    {
        if (!bankList.contains(newOne))
    		bankList.add(newOne);
    }
    protected void delBank(Banker oneToDel)
    {
    	bankList.remove(oneToDel);
    }
    public Banker getBank(String chain, String areaNameOrBranch)
    {
        for (Banker B : bankList)
            if((B.bankChain().equalsIgnoreCase(chain))
            &&(B.bankChain().equalsIgnoreCase(areaNameOrBranch)))
                return B;
        
        Area A=findArea(areaNameOrBranch);
        if(A==null) 
        	return null;
        
        for (Banker B : bankList)
            if((B.bankChain().equalsIgnoreCase(chain))
            &&(getStartArea(B)==A))
                return B;
        return null;
    }
    
    public Enumeration<Banker> banks() { return new IteratorEnumeration<Banker>(bankList.iterator());}
    
	public Iterator<String> bankChains(Area AreaOrNull)
	{
		HashSet<String> H=new HashSet<String>();
        for (Banker B : bankList)
			if((!H.contains(B.bankChain()))
			&&((AreaOrNull==null)
				||(getStartArea(B)==AreaOrNull)
				||(AreaOrNull.isChild(getStartArea(B)))))
					H.add(B.bankChain());
		return H.iterator();
	}

	public void renameRooms(Area A, String oldName, Vector allMyDamnRooms)
	{
		Vector onesToRenumber=new Vector();
		for(int r=0;r<allMyDamnRooms.size();r++)
		{
			Room R=(Room)allMyDamnRooms.elementAt(r);
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=getRoom(R);
				R.setArea(A);
				if(oldName!=null)
				{
					if(R.roomID().toUpperCase().startsWith(oldName.toUpperCase()+"#"))
					{
						Room R2=getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						if((R2==null)||(!R2.roomID().startsWith(A.Name()+"#")))
						{
							String oldID=R.roomID();
							R.setRoomID(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							onesToRenumber.addElement(R);
					}
					else
						CMLib.database().DBUpdateRoom(R);
				}
			}
		}
		if(oldName!=null)
		{
			for(int r=0;r<onesToRenumber.size();r++)
			{
				Room R=(Room)onesToRenumber.elementAt(r);
				String oldID=R.roomID();
				R.setRoomID(A.getNewRoomID(R,-1));
				CMLib.database().DBReCreate(R,oldID);
			}
		}
	}

    public int getRoomDir(Room from, Room to)
    {
    	if((from==null)||(to==null)) return -1;
    	for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
    		if(from.getRoomInDir(d)==to)
    			return d;
    	return -1;
    }

    public Room findConnectingRoom(Room room)
    {
    	if(room==null) return null;
    	Room R=null;
    	Vector otherChoices=new Vector();
    	for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
    	{
    		R=room.getRoomInDir(d);
    		if(R!=null)
    	    	for(int d1=Directions.NUM_DIRECTIONS()-1;d1>=0;d1--)
    	    		if(R.getRoomInDir(d1)==room)
    	    		{
    	    			if(R.getArea()==room.getArea())
    	    				return R;
    	    			otherChoices.addElement(R);
    	    		}
    	}
    	for(Enumeration e=rooms();e.hasMoreElements();)
    	{
    		R=(Room)e.nextElement();
    		if(R==room) continue;
            for(int d1=Directions.NUM_DIRECTIONS()-1;d1>=0;d1--)
	    		if(R.getRoomInDir(d1)==room)
	    		{
	    			if(R.getArea()==room.getArea())
	    				return R;
	    			otherChoices.addElement(R);
	    		}
    	}
    	if(otherChoices.size()>0)
    		return (Room)otherChoices.firstElement();
    	return null;
    }

	public boolean isClearableRoom(Room R)
	{
		if((R==null)||(R.amDestroyed())) return true;
		MOB M=null;
		Room sR=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if(M==null) continue;
			sR=M.getStartRoom();
			if((sR!=null)
			&&(!sR.roomID().equals(R.roomID())))
				return false;
			if(M.session()!=null)
				return false;
		}
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if((I!=null)&&(I.expirationDate()!=0))
				return false;
		}
		for(int a=0;a<R.numEffects();a++)
		{
			Ability A=R.fetchEffect(a);
			if((A!=null)&&(!A.isSavable()))
				return false;
		}
		return true;
	}
    
    public boolean explored(Room R)
    {
        if((R==null)
        ||(CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE))
        ||(R.getArea()==null))
            return false;
        return false;
    }

    public static class AreaEnumerator implements Enumeration
    {
        private Enumeration<Area> curAreaEnumeration=null;
        private Enumeration<Room> curRoomEnumeration=null;
        private boolean addSkys = false;
        public AreaEnumerator(boolean includeSkys) {
            addSkys = includeSkys;
        }
        public boolean hasMoreElements()
        {
            if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
            while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
            {
                if(!curAreaEnumeration.hasMoreElements()) return false;
                if(addSkys)
                    curRoomEnumeration=((Area)curAreaEnumeration.nextElement()).getFilledProperMap();
                else
                    curRoomEnumeration=((Area)curAreaEnumeration.nextElement()).getProperMap();
            }
            return curRoomEnumeration.hasMoreElements();
        }
        public Room nextElement()
        {
            if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
            while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
            {
                if(!curAreaEnumeration.hasMoreElements()) return null;
                if(addSkys)
                    curRoomEnumeration=((Area)curAreaEnumeration.nextElement()).getFilledProperMap();
                else
                    curRoomEnumeration=((Area)curAreaEnumeration.nextElement()).getProperMap();
            }
            return (Room)curRoomEnumeration.nextElement();
        }
    }

	public void obliterateRoom(Room deadRoom)
	{
		for(int a=deadRoom.numEffects()-1;a>=0;a--)
		{
			Ability A=deadRoom.fetchEffect(a);
			if(A!=null)
			{
				A.unInvoke();
				deadRoom.delEffect(A);
			}
		}
		try
		{
			for(Enumeration r=rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=getRoom(R);
					if(R==null) continue;
					boolean changes=false;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room thatRoom=R.rawDoors()[d];
						if(thatRoom==deadRoom)
						{
							R.rawDoors()[d]=null;
							changes=true;
							if((R.getRawExit(d)!=null)&&(R.getRawExit(d).isGeneric()))
							{
								Exit GE=R.getRawExit(d);
								GE.setTemporaryDoorLink(deadRoom.roomID());
							}
						}
					}
					if(changes)
						CMLib.database().DBUpdateExits(R);
				}
			}
	    }catch(NoSuchElementException e){}
	    for(int m=deadRoom.numInhabitants()-1;m>=0;m--)
	    {
	    	MOB M=deadRoom.fetchInhabitant(m);
	    	if((M!=null)&&(M.playerStats()!=null))
				M.getStartRoom().bringMobHere(M,true);
	    }
		emptyRoom(deadRoom,null);
		deadRoom.destroy();
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid(null);
		CMLib.database().DBDeleteRoom(deadRoom);
	}

	public void emptyArea(Area A)
	{
		for(int a=A.numEffects()-1;a>=0;a--)
		{
			Ability A1=A.fetchEffect(a);
			if(A1!=null)
			{
				A1.unInvoke();
				A.delEffect(A1);
			}
		}
		for(Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			emptyRoom(R,null);
			R.destroy();
		}
	}
	
	public Room roomLocation(Environmental E)
	{
		if(E==null) return null;
		if(E instanceof Area)
			return ((Area)E).getRandomProperRoom();
		else
		if(E instanceof Room)
			return (Room)E;
		else
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return ((MOB)((Item)E).owner()).location();
        else
        if(E instanceof Ability)
            return roomLocation(((Ability)E).affecting());
		return null;
	}
	public Area getStartArea(Environmental E)
	{
		if(E instanceof Area) return (Area)E;
		Room R=getStartRoom(E);
		if(R==null) return null;
		return R.getArea();
	}

    public Room getStartRoom(Environmental E)
    {
        if(E ==null) return null;
        if(E instanceof MOB)
        	return ((MOB)E).getStartRoom();
        if(E instanceof Item)
        {
            if(((Item)E).owner() instanceof MOB)
            	return getStartRoom(((Item)E).owner());
            if(CMLib.flags().isGettable((Item)E))
                return null;
        }
        if(E instanceof Ability)
            return getStartRoom(((Ability)E).affecting());
        if(E instanceof Area) return ((Area)E).getRandomProperRoom();
        if(E instanceof Room) return (Room)E;
        return roomLocation(E);
    }

    public Area areaLocation(CMObject E)
    {
        if(E==null) return null;
        if(E instanceof Area)
            return (Area)E;
        else
        if(E instanceof Room)
            return ((Room)E).getArea();
        else
        if(E instanceof MOB)
            return ((MOB)E).location().getArea();
        else
        if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
            return ((Room)((Item)E).owner()).getArea();
        else
        if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
           return (((MOB)((Item)E).owner()).location()).getArea();
        return null;
    }

	public void emptyRoom(Room room, Room bringBackHere)
	{
		if(room==null) return;
		Vector inhabs=new Vector();
		MOB M=null;
		for(int m=0;m<room.numInhabitants();m++)
		{
		    M=room.fetchInhabitant(m);
		    if(M!=null) inhabs.addElement(M);
		}
		for(int m=0;m<inhabs.size();m++)
		{
			M=(MOB)inhabs.elementAt(m);
			if(bringBackHere!=null)
				bringBackHere.bringMobHere(M,false);
			else
			if(!M.isSavable())
				continue;
			else
			if((M.getStartRoom()==null)
			||(M.getStartRoom()==room)
			||(M.getStartRoom().ID().length()==0))
				M.destroy();
			else
				M.getStartRoom().bringMobHere(M,false);
		}
		Item I=null;
		inhabs = null;
		
		Vector contents = new Vector();
		
		for(int i=0;i<room.numItems();i++)
		{
		    I=room.getItem(i);
		    if(I!=null) contents.addElement(I);
		}
		for(int i=0;i<contents.size();i++)
		{
			I=(Item)contents.elementAt(i);
			if(bringBackHere!=null)
				bringBackHere.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
			else
				I.destroy();
		}
		room.clearSky();
		CMLib.threads().clearDebri(room,0);
		if(room instanceof GridLocale)
			for(Iterator<Room> r=((GridLocale)room).getExistingRooms();r.hasNext();)
				emptyRoom(r.next(), bringBackHere);
	}


	public void obliterateArea(String areaName)
	{
		Area A=getArea(areaName);
		if(A==null) return;
		Vector rooms=new Vector(100);
		Room R=null;
        Enumeration e=A.getCompleteMap();
		while(e.hasMoreElements())
		{
			for(int i=0;(i<100)&&e.hasMoreElements();i++)
			{
				R=(Room)e.nextElement();
				if((R!=null)&&(R.roomID()!=null))
					rooms.addElement(R);
			}
			if(rooms.size()==0) break;
			for(Enumeration e2=rooms.elements();e2.hasMoreElements();)
			{
				R=(Room)e2.nextElement();
				if((R!=null)&&(R.roomID().length()>0))
					obliterateRoom(R);
			}
			e=A.getCompleteMap();
		}
		CMLib.database().DBDeleteArea(A);
		delArea(A);
	}

	public CMMsg resetMsg=null;
	public void resetRoom(Room room)
	{
		resetRoom(room,false);
	}
	
	public void resetRoom(Room room, boolean rebuildGrids)
	{
		if(room==null) return;
		if(room.roomID().length()==0) return;
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
			if((rebuildGrids)&&(room instanceof GridLocale))
				((GridLocale)room).clearGrid(null);
			boolean mobile=room.getMobility();
			room.toggleMobility(false);
			if(resetMsg==null) resetMsg=CMClass.getMsg(CMClass.sampleMOB(),room,CMMsg.MSG_ROOMRESET,null);
			resetMsg.setTarget(room);
			room.executeMsg(room,resetMsg);
			emptyRoom(room,null);
	        Ability A=null;
	        for(int a=room.numEffects()-1;a>=0;a--)
	        {
	            A=room.fetchEffect(a);
	            if((A!=null)&&(A.canBeUninvoked()))
	                A.unInvoke();
	        }
			CMLib.database().DBReadContent(room,null,true);
			room.toggleMobility(mobile);
		}
	}

	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds)
	{
		Vector rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,true,timePct, maxSeconds);
		if((rooms!=null)&&(rooms.size()!=0)) return (Room)rooms.firstElement();
		return null;
	}
	
	public Vector findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, int maxSeconds)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,false,timePct,maxSeconds); }
	
	public Room findAreaRoomLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int timePct)
	{
		Vector rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,true,timePct,120);
		if((rooms!=null)&&(rooms.size()!=0)) return (Room)rooms.firstElement();
		return null;
	}
	
	public Vector findAreaRoomsLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int timePct)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,false,timePct,120); }
	
	protected Room addWorldRoomsLiberally(Vector rooms, Vector choicesV)
	{
		if(choicesV==null) return null;
		if(rooms!=null)
		{
			for(Enumeration choices=choicesV.elements();choices.hasMoreElements();)
				addWorldRoomsLiberally(rooms,roomLocation((Environmental)choices.nextElement()));
			return null;
		}
		else
		{
			Room room=null;
			int tries=0;
            while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
                room=roomLocation((Environmental)choicesV.elementAt(CMLib.dice().roll(1,choicesV.size(),-1)));
            return room;
		}
	}
	
	protected Room addWorldRoomsLiberally(Vector rooms, Room room)
	{
		if(room==null) return null;
		if(rooms!=null)
		{ 
			if(!rooms.contains(room))
				rooms.addElement(room);
			return null;
		}
		return room;
	}
	
	protected Room addWorldRoomsLiberally(Vector rooms, Area area)
	{ return addWorldRoomsLiberally(rooms,area.getRandomProperRoom()); }
	
	protected Enumeration<Room> rightLiberalMap(Area A) {
		if(A==null) return roomsFilled();
		return A.getProperMap();
	}

	protected Vector<Room> returnResponse(Vector<Room> rooms, Room room)
	{
		if(rooms!=null) return rooms;
		if(room==null) return new Vector<Room>(1);
		return (Vector<Room>)new XVector(room);
	}
	
	protected boolean enforceTimeLimit(long startTime, int maxSeconds)
	{
		if(maxSeconds<=0) return false;
		return ((System.currentTimeMillis() - startTime) / 1000) > maxSeconds;
	}
	
	protected Vector<Room> findWorldRoomsLiberally(MOB mob, 
												   String cmd, 
												   String srchWhatAERIPMVK, 
												   Area A, 
												   boolean returnFirst, 
												   int timePct, 
												   int maxSeconds)
	{
		Room room=null;
		Vector<Room> rooms=(returnFirst)?null:new Vector<Room>();
		
		Room curRoom=(mob!=null)?mob.location():null;
		
		boolean searchWeakAreas=false;
		boolean searchStrictAreas=false;
		boolean searchRooms=false;
		boolean searchPlayers=false;
		boolean searchItems=false;
		boolean searchInhabs=false;
		boolean searchInventories=false;
		boolean searchStocks=false;
		char[] flags = srchWhatAERIPMVK.toUpperCase().toCharArray();
		for(int c=0;c<flags.length;c++)
			switch(flags[c])
			{
				case 'E': searchWeakAreas=true;   break;
				case 'A': searchStrictAreas=true; break;
				case 'R': searchRooms=true;       break;
				case 'P': searchPlayers=true;     break;
				case 'I': searchItems=true;       break;
				case 'M': searchInhabs=true;      break;
				case 'V': searchInventories=true; break;
				case 'K': searchStocks=true;      break;
			}
		long startTime = System.currentTimeMillis();
		if(searchRooms)
		{
			int dirCode=Directions.getGoodDirectionCode(cmd);
			if((dirCode>=0)&&(curRoom!=null))
				room=addWorldRoomsLiberally(rooms,curRoom.rawDoors()[dirCode]);
			if(room==null)
				room=addWorldRoomsLiberally(rooms,getRoom(cmd));
		}

		if(room==null)
		{
		    // first get room ids
			if((cmd.charAt(0)=='#')&&(curRoom!=null)&&(searchRooms))
				room=addWorldRoomsLiberally(rooms,getRoom(curRoom.getArea().Name()+cmd));
			else
			{
                String srchStr=cmd;
                
                if(searchPlayers)
                {
	                // then look for players
	                MOB M=CMLib.sessions().findPlayerOnline(srchStr,false);
	                if(M!=null)
	                	room=addWorldRoomsLiberally(rooms,M.location());
                }
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
                
                // search areas strictly
                if(searchStrictAreas && room==null && (A==null))
                {
                	A=getArea(srchStr);
                	if((A!=null) &&(A.properSize()>0) &&(A.getProperRoomnumbers().roomCountAllAreas()>0))
                		room=addWorldRoomsLiberally(rooms,A);
                	A=null;
                }
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
                
				// no good, so look for room inhabitants
				if(searchInhabs && room==null)
				{
					Vector candidates=findInhabitants(rightLiberalMap(A), mob, srchStr,returnFirst, timePct);
					if(candidates.size()>0)
                		room=addWorldRoomsLiberally(rooms,candidates);
				}
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// now check room text
				if(searchRooms && room==null)
				{
					Vector candidates=findRooms(rightLiberalMap(A), mob, srchStr, false,returnFirst, timePct);
					if(candidates.size()>0)
                		room=addWorldRoomsLiberally(rooms,candidates);
				}
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check floor items
				if(searchItems && room==null)
				{
					Vector candidates=findRoomItems(rightLiberalMap(A), mob, srchStr, false,returnFirst,timePct);
					if(candidates.size()>0)
                		room=addWorldRoomsLiberally(rooms,candidates);
				}
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check inventories
				if(searchInventories && room==null)
				{
					Vector candidates=findInventory(rightLiberalMap(A), mob, srchStr, returnFirst,timePct);
					if(candidates.size()>0)
                		room=addWorldRoomsLiberally(rooms,candidates);
				}
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// check stocks
				if(searchStocks && room==null)
				{
					Vector candidates=findShopStock(rightLiberalMap(A), mob, srchStr, returnFirst,false,timePct);
					if(candidates.size()>0)
                		room=addWorldRoomsLiberally(rooms,candidates);
				}
                if(enforceTimeLimit(startTime,maxSeconds)) return returnResponse(rooms,room);
				
				// search areas weakly
                if(searchWeakAreas && room==null && (A==null))
                {
                	A=findArea(srchStr);
                	if((A!=null) &&(A.properSize()>0) &&(A.getProperRoomnumbers().roomCountAllAreas()>0))
                		room=addWorldRoomsLiberally(rooms,A);
                	A=null;
                }
			}
		}
		return returnResponse(rooms,room);
	}

    protected DVector getAllPlayersHere(Area area, boolean includeLocalFollowers)
    {
        DVector playersHere=new DVector(2);
        MOB M=null;
        Room R=null;
		for(Session S : CMLib.sessions().localOnlineIterable())
        {
            M=S.mob();
            R=(M!=null)?M.location():null;
            if((R!=null)&&(R.getArea()==area)&&(M!=null))
            {
                playersHere.addElement(M,getExtendedRoomID(R));
                if(includeLocalFollowers)
                {
                    MOB M2=null;
                    Set<MOB> H=M.getGroupMembers(new HashSet<MOB>());
                    for(Iterator i=H.iterator();i.hasNext();)
                    {
                        M2=(MOB)i.next();
                        if((M2!=M)&&(M2.location()==R))
                            playersHere.addElement(M2,getExtendedRoomID(R));
                    }
                }
            }
        }
        return playersHere;

    }
    
    public void resetArea(Area area)
    {
        int oldFlag=area.getAreaState();
        area.setAreaState(Area.STATE_FROZEN);
        DVector playersHere=getAllPlayersHere(area,true);
        for(int p=0;p<playersHere.size();p++)
        {
            MOB M=(MOB)playersHere.elementAt(p,1);
            Room R=M.location();
            R.delInhabitant(M);
        }
        for(Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
            resetRoom((Room)r.nextElement());
        area.fillInAreaRooms();
        for(int p=0;p<playersHere.size();p++)
        {
            MOB M=(MOB)playersHere.elementAt(p,1);
            Room R=getRoom((String)playersHere.elementAt(p,2));
            if(R==null) R=M.getStartRoom();
            if(R==null) R=getStartRoom(M);
            if(R!=null) 
            	R.bringMobHere(M,false);
        }
        area.setAreaState(oldFlag);
    }

	public boolean hasASky(Room room)
	{
		if((room==null)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||((room.domainType()&Room.INDOORS)>0))
			return false;
		return true;
	}

	
	public void registerWorldObjectDestroyed(Area area, Room room, CMObject o)
	{
		if(o instanceof Deity)
			delDeity((Deity)o);
		
		if(o instanceof PostOffice)
			delPostOffice((PostOffice)o);
		
		if(o instanceof Banker)
			delBank((Banker)o);
		
		if(o instanceof Auctioneer)
			delAuctionHouse((Auctioneer)o);
		
		if(o instanceof PhysicalAgent)
		{
			PhysicalAgent AE=(PhysicalAgent)o;
			if((area == null) && (room!=null)) area = room.getArea();
			if(area == null) area =getStartArea(AE);
			delScriptHost(area, AE);
		}
	}
	
	public void registerWorldObjectLoaded(Area area, Room room, CMObject o)
	{
		if(o instanceof Deity)
			addDeity((Deity)o);
		
		if(o instanceof PostOffice)
			addPostOffice((PostOffice)o);
		
		if(o instanceof Banker)
			addBank((Banker)o);
		
		if(o instanceof Auctioneer)
			addAuctionHouse((Auctioneer)o);
		
		if(o instanceof PhysicalAgent)
		{
			PhysicalAgent AE=(PhysicalAgent)o;
			if(room == null) room = getStartRoom(AE);
			if((area == null) && (room!=null)) area = room.getArea();
			if(area == null) area = getStartArea(AE);
			addScriptHost(area, room, AE);
			if(o instanceof MOB)
			{
				MOB M=(MOB)o;
				for(int i=0;i<M.numItems();i++)
					addScriptHost(area, room, M.getItem(i));
			}
		}
	}
	
	protected void cleanScriptHosts(SLinkedList<LocatedPair> hosts, PhysicalAgent oneToDel, boolean fullCleaning)
	{
		PhysicalAgent PA;
		for(LocatedPair W : hosts)
			if(W==null)
				hosts.remove(W);
			else
			{
				PA=W.obj();
				if((PA==null)
				||(PA==oneToDel)
				||(PA.amDestroyed())
				||((fullCleaning)&&(!isAQualifyingScriptHost(PA))))
					hosts.remove(W);
			}
	}

	protected boolean isAQualifyingScriptHost(PhysicalAgent host)
	{
		if(host==null) return false;
    	for(Enumeration<Behavior> e = host.behaviors();e.hasMoreElements();)
    	{
    		Behavior B=e.nextElement();
    		if((B!=null) && B.isSavable() && (B instanceof ScriptingEngine))
    			return true;
    	}
    	for(Enumeration<ScriptingEngine> e = host.scripts();e.hasMoreElements();)
    	{
    		ScriptingEngine SE=e.nextElement();
    		if((SE!=null) && SE.isSavable() && (SE instanceof ScriptingEngine))
    			return true;
    	}
		return false;
	}
	
	protected boolean isAScriptHost(Area area, PhysicalAgent host)
	{
		if(area == null) return false;
		return isAScriptHost(scriptHostMap.get(area.Name().toUpperCase()), host);
	}
	
	protected boolean isAScriptHost(SLinkedList<LocatedPair> hosts, PhysicalAgent host)
	{
		if((hosts==null)||(host==null)||(hosts.size()==0)) return false;
		for(LocatedPair W : hosts)
			if(W.obj()==host)
				return true;
		return false;
	}
	
	protected void addScriptHost(Area area, Room room, PhysicalAgent host)
    {
    	if((area==null) || (host == null))
    		return;
    	if(!isAQualifyingScriptHost(host))
    		return;
    	synchronized(("SCRIPT_HOST_FOR: "+area.Name().toUpperCase()).intern())
    	{
    		SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name().toUpperCase());
	    	if(hosts == null)
	    	{
	    		hosts=new SLinkedList<LocatedPair>();
	    		scriptHostMap.put(area.Name().toUpperCase(), hosts);
	    	}
	    	else
	    	{
		    	cleanScriptHosts(hosts, null, false);
		    	if(isAScriptHost(hosts,host))
		    		return;
	    	}
	    	hosts.add(new LocatedPair(room, host));
    	}
    }
	
	protected void delScriptHost(Area area, PhysicalAgent oneToDel)
    {
    	if(oneToDel == null)
    		return;
    	if(area == null)
    		for(Area A : areasList)
    			if(isAScriptHost(A,oneToDel))
    			{
    				area = A;
    				break;
    			}
    	if(area == null)
    		return;
    	synchronized(("SCRIPT_HOST_FOR: "+area.Name().toUpperCase()).intern())
    	{
    		SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name().toUpperCase());
	    	if(hosts==null) return;
	    	cleanScriptHosts(hosts, oneToDel, false);
    	}
    }
	
    public Enumeration<LocatedPair> scriptHosts(Area area)
    {
    	final Vector<Iterator<LocatedPair>> V = new Vector<Iterator<LocatedPair>>();
    	if(area == null)
    		for(String areaKey : scriptHostMap.keySet())
    	    	V.add(scriptHostMap.get(areaKey.toUpperCase()).iterator());
    	else
    	{
	    	SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name().toUpperCase());
	    	if(hosts==null) return EmptyEnumeration.INSTANCE;
	    	V.add(hosts.iterator());
    	}
    	if(V.size()==0) return EmptyEnumeration.INSTANCE;
    	final MultiIterator<LocatedPair> me = new MultiIterator<LocatedPair>(V.toArray(new Iterator[0]));
    	return new Enumeration<LocatedPair>()
    	{
			public boolean hasMoreElements() { return me.hasNext();}
			public LocatedPair nextElement() {
				LocatedPair W = me.next();
				PhysicalAgent E = W.obj();
				if(((E==null) || (E.amDestroyed())) && hasMoreElements())
					return nextElement();
				return W;
			}
    	};
    }
    
    public boolean activate() 
    {
        if(thread==null)
            thread=new ThreadEngine.SupportThread("THMap"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
                    MudHost.TIME_SAVETHREAD_SLEEP, this, CMSecurity.isDebugging("SAVETHREAD"));
        if(!thread.started)
            thread.start();
        return true;
    }
    
    public boolean shutdown() {
        areasList.clear();
        deitiesList.clear();
        space.clear();
        globalHandlers.clear();
        thread.shutdown();
        return true;
    }
    
    public void run()
    {
        if((CMSecurity.isDisabled("SAVETHREAD"))
        ||(CMSecurity.isDisabled("MAPTHREAD")))
            return;
        
        boolean corpsesOnly=CMSecurity.isSaveFlag("ROOMITEMS");
        boolean noMobs=CMSecurity.isSaveFlag("ROOMMOBS");
        thread.status("expiration sweep");
        long currentTime=System.currentTimeMillis();
        boolean debug=CMSecurity.isDebugging("VACUUM");
        MOB expireM=mobCreated(null);
        try
        {
            Vector stuffToGo=new Vector();
            Item I=null;
            MOB M=null;
            Room R=null;
            Vector roomsToGo=new Vector();
            CMMsg expireMsg=CMClass.getMsg(expireM,R,null,CMMsg.MSG_EXPIRE,null);
            for(Enumeration r=rooms();r.hasMoreElements();)
            {
                R=(Room)r.nextElement();
                expireM.setLocation(R);
                expireMsg.setTarget(R);
                if((R.expirationDate()!=0)
                &&(currentTime>R.expirationDate())
                &&(R.okMessage(R,expireMsg)))
                    roomsToGo.addElement(R);
                else
                if(!R.amDestroyed())
                {
                    stuffToGo.clear();
                    for(int i=0;i<R.numItems();i++)
                    {
                        I=R.getItem(i);
                        if((I!=null)
                        &&((!corpsesOnly)||(I instanceof DeadBody))
                        &&(I.expirationDate()!=0)
                        &&(I.owner()==R)
                        &&(currentTime>I.expirationDate()))
                            stuffToGo.add(I);
                    }
                    for(int i=0;i<R.numInhabitants();i++)
                    {
                        M=R.fetchInhabitant(i);
                        if((M!=null)
                        &&(!noMobs)
                        &&(M.expirationDate()!=0)
                        &&(currentTime>M.expirationDate()))
                            stuffToGo.add(M);
                    }
                }
                if(stuffToGo.size()>0)
                {
                    boolean success=true;
                    for(int s=0;s<stuffToGo.size();s++)
                    {
                        Environmental E=(Environmental)stuffToGo.elementAt(s);
                        thread.status("expiring "+E.Name());
                        expireMsg.setTarget(E);
                        if(R.okMessage(expireM,expireMsg))
                            R.sendOthers(expireM,expireMsg);
                        else
                            success=false;
                        if(debug) Log.sysOut("UTILITHREAD","Expired "+E.Name()+" in "+getExtendedRoomID(R)+": "+success);
                    }
                    stuffToGo.clear();
                }
            }
            for(int r=0;r<roomsToGo.size();r++)
            {
                R=(Room)roomsToGo.elementAt(r);
                expireM.setLocation(R);
                expireMsg.setTarget(R);
                thread.status("expirating room "+getExtendedRoomID(R));
                if(debug)
                {
                    String roomID=getExtendedRoomID(R);
                    if(roomID.length()==0) roomID="(unassigned grid room, probably in the air)";
                    if(debug) Log.sysOut("UTILITHREAD","Expired "+roomID+".");
                }
                R.sendOthers(expireM,expireMsg);
            }
            
        }
        catch(java.util.NoSuchElementException e){}
        thread.status("title sweeping");
        List<String> playerList=CMLib.database().getUserList();
        try
        {
            for(Enumeration r=rooms();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                LandTitle T=CMLib.law().getLandTitle(R);
                if(T!=null)
                {
                    thread.status("checking title in "+R.roomID()+": "+Runtime.getRuntime().freeMemory());
                    T.updateLot(playerList);
                    thread.status("title sweeping");
                }
            }
        }catch(NoSuchElementException nse){}
        
        thread.status("cleaning scripts");
		for(String areaKey : scriptHostMap.keySet())
			cleanScriptHosts(scriptHostMap.get(areaKey), null, true);
        
        long lastDateTime=System.currentTimeMillis()-(5*TimeManager.MILI_MINUTE);
        thread.status("checking");
        try
        {
            for(Enumeration r=rooms();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                for(int m=0;m<R.numInhabitants();m++)
                {
                    MOB mob=R.fetchInhabitant(m);
                    if((mob!=null)&&(mob.lastTickedDateTime()>0)&&(mob.lastTickedDateTime()<lastDateTime))
                    {
                        boolean ticked=CMLib.threads().isTicking(mob,Tickable.TICKID_MOB);
                        boolean isDead=mob.amDead();
                        String wasFrom=((mob.getStartRoom()!=null)?mob.getStartRoom().roomID():"NULL");
                        if(!ticked)
                        {
                            if(CMLib.players().getPlayer(mob.Name())==null)
                            {
                            	if(ticked)
                            	{
                            		// we have a dead group.. let the group handler deal with it.
	                                Log.errOut(thread.getName(),mob.name()+" in room "+R.roomID()+" unticked in dead group (Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+".");
                            		continue;
                            	}
                            	else
	                                Log.errOut(thread.getName(),mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
                            }
                            else
                                Log.errOut(thread.getName(),"Player "+mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been put aside."));
                            thread.status("destroying unticked mob "+mob.name());
                            if(CMLib.players().getPlayer(mob.Name())==null) mob.destroy();
                            R.delInhabitant(mob);
                            thread.status("checking");
                        }
                    }
                }
            }
        }
        catch(java.util.NoSuchElementException e){}
        finally {
        	if(expireM!=null)
        		expireM.destroy();
        }
    }
}
