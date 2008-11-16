package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.RoomContent;
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
@SuppressWarnings("unchecked")
public class CMCatalog extends StdLibrary implements CatalogLibrary
{
    public String ID(){return "CMCatalog";}
    public DVector icatalog=new DVector(2);
    public DVector mcatalog=new DVector(2);

	public void setCataloged(Environmental E, boolean truefalse)
	{
		if(E==null) return;
		if(CMath.bset(E.baseEnvStats().disposition(),EnvStats.IS_CATALOGED))
		{
			if(!truefalse)
			{
				E.baseEnvStats().setDisposition(CMath.unsetb(E.baseEnvStats().disposition(),EnvStats.IS_CATALOGED));
				E.envStats().setDisposition(CMath.unsetb(E.envStats().disposition(),EnvStats.IS_CATALOGED));
			}
		}
		else
		if(truefalse)
		{
			E.baseEnvStats().setDisposition(CMath.setb(E.baseEnvStats().disposition(),EnvStats.IS_CATALOGED));
			E.envStats().setDisposition(CMath.setb(E.envStats().disposition(),EnvStats.IS_CATALOGED));
		}
	}
	
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
    
    protected void addCatalogReplace(DVector DV, Environmental E)
    {
        int start=0;
        int end=DV.size()-1;
        String name=E.Name();
        int lastStart=0;
        int lastEnd=DV.size()-1;
        int comp=-1;
        int mid=-1;
        while(start<=end)
        {
            mid=(end+start)/2;
            comp=((Environmental)DV.elementAt(mid,1)).Name().compareToIgnoreCase(name);
            if(comp==0)
                break;
            else
            if(comp>0)
            {
                lastEnd=end;
                end=mid-1;
            }
            else
            {
                lastStart=start;
                start=mid+1;
            }
        }
        if(comp==0)
        {
        	if(E instanceof DBIdentifiable)
        		((DBIdentifiable)E).setDatabaseID(((DBIdentifiable)DV.elementAt(mid,1)).databaseID());
            ((Environmental)DV.elementAt(mid,1)).destroy();
            DV.setElementAt(mid,1,E);
        }
        else
        {
            if(mid>=0)
                for(comp=lastStart;comp<=lastEnd;comp++)
                    if(((Environmental)DV.elementAt(comp,1)).Name().compareToIgnoreCase(name)>0)
                    {
                        DV.insertElementAt(comp,E,new CataDataImpl(""));
                        return;
                    }
            DV.addElement(E,new CataDataImpl(""));
        }
    }
    
    public String[] makeCatalogNames(Vector catalog){
    	String[] names=new String[catalog.size()];
    	int x=0;
    	for(Iterator i=DVector.s_iter(catalog);i.hasNext();)
    		names[x++]=((Environmental)i.next()).Name();
    	return names;
    }
    
    public String[] getCatalogItemNames() { return makeCatalogNames(icatalog.getDimensionVector(1)); }
    public String[] getCatalogMobNames() { return makeCatalogNames(mcatalog.getDimensionVector(1)); }
    
    public Item[] getCatalogItems(){
    	Vector itemsV=icatalog.getDimensionVector(1);
    	Item[] items=new Item[itemsV.size()];
    	int x=0;
    	for(Iterator i=itemsV.iterator();i.hasNext();)
    		items[x++]=(Item)i.next();
    	return items;
    }
    public MOB[] getCatalogMobs(){
    	Vector mobsV=mcatalog.getDimensionVector(1);
    	MOB[] mobs=new MOB[mobsV.size()];
    	int x=0;
    	for(Iterator i=mobsV.iterator();i.hasNext();)
    		mobs[x++]=(MOB)i.next();
    	return mobs;
    }
    
    public boolean isCatalogObj(Environmental E)
    {
        if(E instanceof MOB) return mcatalog.contains(E);
        if(E instanceof Item) return icatalog.contains(E);
        return false;
    }
    
    public boolean isCatalogObj(String name)
    {
        int index=getCatalogMobIndex(name);
        if(index<0) index=getCatalogItemIndex(name);
        return index>=0;
    }
    
    protected int getCatalogItemIndex(String called)
    { 
    	synchronized(icatalog)
    	{
	        return getGlobalIndex(icatalog.getDimensionVector(1),called);
    	}
    }
    
    protected int getCatalogMobIndex(String called)
    { 
    	synchronized(mcatalog)
    	{
	        return getGlobalIndex(mcatalog.getDimensionVector(1),called);
    	}
    }
    
    public Item getCatalogItem(String called)
    { 
        try
        {
        	synchronized(icatalog)
        	{
	            return (Item)icatalog.elementAt(getCatalogItemIndex(called),1);
        	}
        }
        catch(Exception e)
        {
            return null;
        }
    }
    public MOB getCatalogMob(String called)
    { 
        try
        {
        	synchronized(mcatalog)
        	{
	            return (MOB)mcatalog.elementAt(getCatalogMobIndex(called),1);
        	}
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public CataData getCatalogItemData(String called)
    { 
        try
        {
        	synchronized(icatalog)
        	{
	            return (CataData)icatalog.elementAt(getCatalogItemIndex(called),2);
        	}
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public CataData getCatalogMobData(String called)
    { 
        try
        {
        	synchronized(mcatalog)
        	{
	            return (CataData)mcatalog.elementAt(getCatalogMobIndex(called),2);
        	}
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public Vector<RoomContent> roomContent(Room R) 
    {
		Item I=null;
		MOB M=null;
		Environmental E=null;
		ShopKeeper SK=null;
        Vector shops=null;
        Vector shopItems=null;
        Environmental shopItem=null;
        Vector<RoomContent> content =new Vector<RoomContent>();
		if(R!=null)
		{
			if(R==null) return content;
            shops=CMLib.coffeeShops().getAllShopkeepers(R,null);
            for(int s=0;s<shops.size();s++)
            {
                E=(Environmental)shops.elementAt(s);
                if(E==null) continue;
                SK=CMLib.coffeeShops().getShopKeeper(E);
                if(SK==null) continue;
                shopItems=SK.getShop().getStoreInventory();
                for(int b=0;b<shopItems.size();b++)
                {
                    shopItem=(Environmental)shopItems.elementAt(b);
                    content.addElement(new RoomContentImpl(shopItem,SK));
                }
            }
			for(int i=0;i<R.numItems();i++)
			{
				I=R.fetchItem(i);
				if(I!=null) content.addElement(new RoomContentImpl(I));
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
			    M=R.fetchInhabitant(m);
			    if(M==null) continue;
                for(int i=0;i<M.inventorySize();i++)
                {
                    I=M.fetchInventory(i);
    				if(I!=null) content.addElement(new RoomContentImpl(I,M));
                }
				content.addElement(new RoomContentImpl(M));
			}
		}
		return content;
	}

    public void updateRoomContent(String roomID, Vector<RoomContent> content)
    {
    	Vector<Environmental> updatables=new Vector<Environmental>();
    	Vector<Environmental> deletables=new Vector<Environmental>();
    	for(RoomContent C : content)
    	{
    		if(C.deleted())
    		{
				if(C.holder()!=null)
				{
					if(!updatables.contains(C.holder()))
						updatables.addElement(C.holder());
				}
				else
				if(!updatables.contains(C.E()))
					deletables.add(C.E());
    		}
    		else
    		if(C.isDirty())
    		{
				if(C.holder()!=null)
				{
					if(!updatables.contains(C.holder()))
						updatables.addElement(C.holder());
				}
				else
				if(!updatables.contains(C.E()))
					updatables.add(C.E());
    		}
    	}
    	for(Environmental E : deletables)
    	{
    		updatables.remove(E);
    		if((!(E instanceof DBIdentifiable))
    		||(!((DBIdentifiable)E).canSaveDatabaseID())
    		||(((DBIdentifiable)E).databaseID().trim().length()==0))
    			continue;
    		if(E instanceof MOB)
	    		CMLib.database().DBDeleteMOB(roomID,(MOB)E);
    		else
	    		CMLib.database().DBDeleteItem(roomID,(Item)E);
    	}
    	for(Environmental E : updatables)
    	{
    		if(E instanceof ShopKeeper)
    		{
    			ShopKeeper SK=(ShopKeeper)E;
                DVector addBacks=new DVector(3);
                Vector shopItems=SK.getShop().getStoreInventory();
                for(int b=0;b<shopItems.size();b++)
                {
                    Environmental shopItem=(Environmental)shopItems.elementAt(b);
                    int num=SK.getShop().numberInStock(shopItem);
                    int price=SK.getShop().stockPrice(shopItem);
                    addBacks.addElement(shopItem,new Integer(num),new Integer(price));
                }
                SK.getShop().emptyAllShelves();
                for(int a=0;a<addBacks.size();a++)
                    SK.getShop().addStoreInventory(
                            (Environmental)addBacks.elementAt(a,1),
                            ((Integer)addBacks.elementAt(a,2)).intValue(),
                            ((Integer)addBacks.elementAt(a,3)).intValue(),
                            SK);
    		}
    	}
    	for(Environmental E : updatables)
    	{
    		if((!(E instanceof DBIdentifiable))
    		||(!((DBIdentifiable)E).canSaveDatabaseID())
    		||(((DBIdentifiable)E).databaseID().trim().length()==0))
    			continue;
    		if(E instanceof MOB)
	    		CMLib.database().DBUpdateMOB(roomID,(MOB)E);
    		else
	    		CMLib.database().DBUpdateItem(roomID,(Item)E);
    	}
    }
    
    public void delCatalog(Environmental E)
    {
        if(E==null) return;
        CataData data=getCatalogData(E);
        if(E instanceof Item)
        {
        	synchronized(icatalog)
        	{
	            icatalog.removeElement((Item)E);
        	}
        }
        else
        if(E instanceof MOB)
        {
        	synchronized(mcatalog)
        	{
	            mcatalog.removeElement((MOB)E);
        	}
        }
        if(data!=null)
        {
        	Vector<Room> rooms=new Vector<Room>();
        	for(Enumeration<Environmental> e=data.enumeration();e.hasMoreElements();)
        	{
        		Environmental E2=e.nextElement();
        		Room R=CMLib.map().getStartRoom(E2);
        		if((R!=null)&&(!rooms.contains(R)))
        			rooms.addElement(R);
        		changeCatalogUsage(E2,false);
        	}
        	for(Room R : rooms)
        	{
        		Vector<RoomContent> contents=roomContent(R);
        		for(RoomContent content : contents)
        		{
        			if((CMLib.flags().isCataloged(content.E()))&&(content.E().Name().equalsIgnoreCase(E.Name())))
        			{
            			if(((E instanceof MOB)&&(content.E() instanceof MOB))
            			||((E instanceof Item)&&(content.E() instanceof Item)))
            				setCataloged(E,false);
        			}
        		}
        		R=CMLib.coffeeMaker().makeNewRoomContent(R);
        		contents=roomContent(R);
        		boolean dirty=false;
        		for(RoomContent content : contents)
        		{
        			if((CMLib.flags().isCataloged(content.E()))&&(content.E().Name().equalsIgnoreCase(E.Name())))
        			{
            			if(((E instanceof MOB)&&(content.E() instanceof MOB))
            			||((E instanceof Item)&&(content.E() instanceof Item)))
            			{
            				setCataloged(E,false);
            				content.flagDirty();
            				dirty=true;
            			}
        			}
        		}
            	if(dirty) updateRoomContent(R.roomID(),contents);
            	R.destroy();
        	}
        }
    }
    
    public boolean addCatalogReplace(Environmental E)
    {
        if((E==null)||(!(E instanceof DBIdentifiable))||(!((DBIdentifiable)E).canSaveDatabaseID())) 
        	return false;
    	synchronized(getSync(E).intern())
    	{
	        setCataloged(E,true);
    		E=(Environmental)E.copyOf();
	        setCataloged(E,false);
	        E.text(); // to get cataloged status into xml
	        if(E instanceof Item)
	        {
	        	synchronized(icatalog)
	        	{
		            addCatalogReplace(icatalog,(Item)E);
	        	}
	        }
	        else
	        if(E instanceof MOB)
	        {
	        	synchronized(mcatalog)
	        	{
		            addCatalogReplace(mcatalog,(MOB)E);
	        	}
	        }
    	}
        return true;
    }
    
    public boolean shutdown()
    {
        icatalog=new DVector(2);
        mcatalog=new DVector(2);
        return true;
    }
    
    public void updateCatalog(Environmental E)
    {
    	synchronized(getSync(E).intern())
    	{
	        Environmental cataE=CMLib.catalog().getCatalogMatch(E);
	        if(cataE==null) return;
	        cataE.setMiscText(E.text());
	        setCataloged(cataE,false);
	        if(E instanceof MOB)
	            CMLib.database().DBUpdateMOB("CATALOG_MOBS",(MOB)cataE);
	        else
	            CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)cataE);
	        setCataloged(E,true);
    	}
    }
    
    public void changeCatalogUsage(Environmental E, boolean toCataloged)
    {
    	synchronized(getSync(E).intern())
    	{
            if((E!=null)&&(E.baseEnvStats()!=null)&&(!E.amDestroyed()))
            {
                if(toCataloged)
            	{
	                setCataloged(E,true);
                	CataData data=getCatalogData(E);
                    if(data!=null) data.addReference(E);
            	}
                else
            	if(CMLib.flags().isCataloged(E))
            	{
                    setCataloged(E,false);
                	CataData data=getCatalogData(E);
                    if(data!=null) data.delReference(E);
            	}
            }
    	}
    }
    
    private void propogateShopChange(ShopKeeper SK, HashSet<Environmental> ignored, Environmental cataE)
    {
        boolean isMob=(cataE instanceof MOB);
        Environmental E=null;
        int i=0;
        Vector V=SK.getShop().getStoreInventory();
        for(i=0;i<V.size();i++)
        {
            E=(Environmental)V.elementAt(i);
            if(!ignored.contains(E))
            {
	            if((isMob)&&(E instanceof MOB)
	            &&(CMLib.flags().isCataloged(E))
	            &&(cataE.Name().equalsIgnoreCase(E.Name())))
	                E.setMiscText(E.text());
	            if((!isMob)&&(E instanceof Item)
	            &&(CMLib.flags().isCataloged(E))
	            &&(cataE.Name().equalsIgnoreCase(E.Name())))
	                E.setMiscText(E.text());
            }
        }
    }
    
    public void propogateCatalogChange(Environmental cataE)
    {
    	synchronized(getSync(cataE).intern())
    	{
    		CataData data = getCatalogData(cataE);
    		HashSet<Environmental> ignored=null;
    		if(data!=null)
    			ignored=CMParms.makeHashSet(data.enumeration());
    		else
    			ignored=new HashSet<Environmental>(1);
    		for(Iterator<Environmental> i=ignored.iterator();i.hasNext();)
    		{
    			Environmental E=i.next();
    			if((!E.amDestroyed())
                &&(CMLib.flags().isCataloged(E))
                &&(E.Name().equalsIgnoreCase(cataE.Name())))
                    E.setMiscText(E.text());
    		}
    		
	        boolean isMob=(cataE instanceof MOB);
	        MOB M=null;
	        Room R=null;
	        Item I=null;
	        ShopKeeper SK=null;
	        int m=0,i=0;
	        for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
	        {
	            Area A=(Area)e.nextElement();
	            SK=CMLib.coffeeShops().getShopKeeper(A);
	            if(SK!=null) propogateShopChange(SK,ignored,cataE);
	        }
	        for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
	        {
	            R=(Room)e.nextElement();
	            if(!isMob)
	            for(i=0;i<R.numItems();i++)
	            {
	                I=R.fetchItem(i);
	                if((CMLib.flags().isCataloged(I))
	                &&(!ignored.contains(I))
	                &&(cataE.Name().equalsIgnoreCase(I.Name())))
	                    I.setMiscText(I.text());
	            }
	            for(m=0;m<R.numInhabitants();m++)
	            {
	                M=R.fetchInhabitant(m);
	                if(!M.isMonster()) continue;
	                if((isMob)
	                &&(!ignored.contains(M))
	                &&(CMLib.flags().isCataloged(M))
	                &&(cataE.Name().equalsIgnoreCase(M.Name())))
	                    M.setMiscText(M.text());
	                if(!isMob)
	                {
	                    for(i=0;i<M.inventorySize();i++)
	                    {
	                        I=M.fetchInventory(i);
	                        if((CMath.bset(I.baseEnvStats().disposition(),EnvStats.IS_CATALOGED))
        	                &&(!ignored.contains(I))
	                        &&(cataE.Name().equalsIgnoreCase(I.Name())))
	                            I.setMiscText(I.text());
	                    }
	                    SK=CMLib.coffeeShops().getShopKeeper(M);
	                    if(SK!=null) propogateShopChange(SK,ignored,cataE);
	                }
	            }
	            SK=CMLib.coffeeShops().getShopKeeper(R);
	            if(SK!=null) propogateShopChange(SK,ignored,cataE);
	        }
	        if(!isMob)
	        for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
	        {
	            M=(MOB)e.nextElement();
	            for(i=0;i<M.inventorySize();i++)
	            {
	                I=M.fetchInventory(i);
	                if((CMath.bset(I.baseEnvStats().disposition(),EnvStats.IS_CATALOGED))
	                &&(!ignored.contains(I))
	                &&(cataE.Name().equalsIgnoreCase(I.Name())))
	                {
	                    I.setMiscText(I.text());
	                    if(M.playerStats()!=null)
	                        M.playerStats().setLastUpdated(0);
	                }
	            }
	            SK=CMLib.coffeeShops().getShopKeeper(M);
	            if(SK!=null) propogateShopChange(SK,ignored,cataE);
	        }
    	}
    }
    
    public CataData getCatalogData(Environmental E) {
    	if(E==null) return null;
    	return (E instanceof MOB)?getCatalogMobData(E.Name()):getCatalogItemData(E.Name());
    }
    
    public Environmental getCatalogMatch(Environmental E) {
    	if(E==null) return null;
    	return (E instanceof MOB)?getCatalogMob(E.Name()):getCatalogItem(E.Name());
    }
    
    public void updateCatalogIntegrity(Environmental E)
    {
    	synchronized(getSync(E).intern())
    	{
	    	if(checkCatalogIntegrity(E)==null) return;
	    	setCataloged(E,false);
	    	E.text();
    	}
    }

    private final String getSync(Environmental E) { return ((E instanceof MOB)?"CATASYNC_MOB_":"CATASYNC_ITEM_")+E.Name().toUpperCase();}
    
    public StringBuffer checkCatalogIntegrity(Environmental E) 
    {
    	if(E==null) return null;
    	synchronized(getSync(E).intern())
    	{
            if(CMLib.flags().isCataloged(E))
    		{
            	CataData data=getCatalogData(E);
            	Environmental cataE=getCatalogMatch(E);
            	if((cataE==null)||(data==null))
            	{
                	if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)) 
                		return null; // if catalog isn't fully loaded, this can be a false correction
                	if(data!=null)
    	            	data.delReference(E);
                	setCataloged(E,false);
                	E.text();
                	return null;
            	} 
            	else 
            	{
            		StringBuffer diffs=null;
                	setCataloged(E,false);
                	if(!cataE.sameAs(E))
                	{
                		diffs=new StringBuffer("");
                        for(int i=0;i<cataE.getStatCodes().length;i++)
                            if((!cataE.getStat(cataE.getStatCodes()[i]).equals(E.getStat(cataE.getStatCodes()[i]))))
                                diffs.append(cataE.getStatCodes()[i]+",");
                	}
                	setCataloged(E,true);
                	if(data!=null)
                		data.addReference(E);
                	return diffs;
            	}
    		}
            else
            {
            	CataData data=getCatalogData(E);
            	if(data!=null)
	            	data.delReference(E);
            	return null;
            }
    	}
    }
    
    public Item getDropItem(MOB M, boolean live)
    {
        if(M==null) return null;
        CatalogLibrary.CataData data=null;
        Vector selections=null;
        synchronized(icatalog)
        {
        	try
        	{
		        for(int d=0;d<icatalog.size();d++)
		        {
		            data=(CatalogLibrary.CataData)icatalog.elementAt(d,3);
		            if((data.getWhenLive()==live)
		            &&(data.getRate()>0.0)
		            &&(data.getMaskV()!= null)
		            &&(Math.random() <= data.getRate())
		            &&(CMLib.masking().maskCheck(data.getMaskV(),M,true)))
		            {
		                if(selections==null)
		                    selections=new Vector();
		                selections.addElement(icatalog.elementAt(d,1));
		            }
		        }
        	} catch(IndexOutOfBoundsException e) {}
        }
        if(selections==null) return null;
        Item I=(Item)selections.elementAt(CMLib.dice().roll(1,selections.size(),-1));
        I=(Item)I.copyOf();
        changeCatalogUsage(I,true);
        return I;
    }
    
    public CataData sampleCataData(String xml) {return new CataDataImpl(xml);}
    
    public static class RoomContentImpl implements RoomContent
    {
    	private Environmental obj=null;
    	private boolean dirty=false;
    	private Environmental holder=null;
    	public RoomContentImpl(Environmental E){ obj=E;}
    	public RoomContentImpl(Environmental E, Environmental E2){ obj=E; holder=E2;}
    	public Environmental E(){return obj;}
    	public void flagDirty(){ dirty=true;}
    	public Environmental holder(){ return holder;}
    	public boolean isDirty(){ return dirty;}
    	public boolean deleted(){ return obj.amDestroyed();}
    }
    
    public static class CataDataImpl implements CataData 
    {
        public Vector lmaskV=null;
        public String lmaskStr=null;
        public boolean live=false;
        public double rate=0.0;
        public Vector<WeakReference> refs=new Vector<WeakReference>(1);
        
        public CataDataImpl(String catadata)
        {
            build(catadata);
        }
        
        private Vector<Environmental> makeVector() {
        	Vector<Environmental> V=new Vector<Environmental>(refs.size());
        	WeakReference R=null;
        	for(int r=0;r<refs.size();r++)
        	{
        		R=refs.elementAt(r);
        		if(R!=null)
        		{
        			Environmental o=(Environmental)R.get();
        			if((o!=null)
        			&&(!o.amDestroyed())
        			&&(CMath.bset(o.baseEnvStats().disposition(),EnvStats.IS_CATALOGED)))
        				V.addElement((Environmental)o);
        		}
        	}
        	return V;
        }
        
        public int numReferences() {
        	int num=0;
        	for(int r=0;r<refs.size();r++)
        		if(refs.elementAt(r).get()!=null)
        			num++;
        	return num;
        }
        
        public Enumeration<Environmental> enumeration() { return makeVector().elements();}
        
        public synchronized void addReference(Environmental E) {
        	if(isReference(E)) return;
        	Environmental o=null;
        	for(int r=0;r<refs.size();r++)
        	{
        		o=(Environmental)refs.elementAt(r).get();
        		if(o==null)
        		{
	    			refs.setElementAt(new WeakReference(E),r);
	    			return;
        		}
        	}
        	refs.addElement(new WeakReference(E));
        }
        
        public boolean isReference(Environmental E) {
        	for(int r=0;r<refs.size();r++)
        		if(refs.elementAt(r).get()==E) return true;
        	return false;
        }
        
        public synchronized void delReference(Environmental E) {
        	if(!isReference(E)) return;
        	Environmental o=null;
        	for(int r=0;r<refs.size();r++)
        	{
        		o=(Environmental)refs.elementAt(r).get();
        		if(o==E)
        		{
        			refs.removeElementAt(r);
	    			return;
        		}
        	}
        }
        
        public CataDataImpl(String _lmask, String _rate, boolean _live)
        {
            this(_lmask,CMath.s_pct(_rate),_live);
        }
        
        public CataDataImpl(String _lmask, double _rate, boolean _live)
        {
            live=_live;
            lmaskStr=_lmask;
            lmaskV=null;
            if(lmaskStr.length()>0)
                lmaskV=CMLib.masking().maskCompile(lmaskStr);
            rate=_rate;
        }
        
        public Vector getMaskV(){return lmaskV;}
        public String getMaskStr(){return lmaskStr;}
        public boolean getWhenLive(){return live;}
        public double getRate(){return rate;}
        public void setMaskStr(String s){
        	lmaskStr=s;
        	if(s.trim().length()==0)
        		lmaskV=null;
        	else
        		lmaskV=CMLib.masking().maskCompile(s);
        }
        public void setWhenLive(boolean l){live=l;}
        public void setRate(double r){rate=r;}
        
        public String data() 
        {
            StringBuffer buf=new StringBuffer("");
            buf.append("<CATALOGDATA>");
            buf.append("<RATE>"+CMath.toPct(rate)+"</RATE>");
            buf.append("<LMASK>"+CMLib.xml().parseOutAngleBrackets(lmaskStr)+"</LMASK>");
            buf.append("<LIVE>"+live+"</LIVE>");
            buf.append("</CATALOGDATA>");
            return buf.toString();
        }
        
        public void build(String catadata)
        {
            Vector V=null;
            if((catadata!=null)&&(catadata.length()>0))
            {
                V=CMLib.xml().parseAllXML(catadata);
                XMLLibrary.XMLpiece piece=CMLib.xml().getPieceFromPieces(V,"CATALOGDATA");
                if((piece!=null)&&(piece.contents!=null)&&(piece.contents.size()>0))
                {
                    lmaskStr=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(piece.contents,"LMASK"));
                    String ratestr=CMLib.xml().getValFromPieces(piece.contents,"RATE");
                    rate=CMath.s_pct(ratestr);
                    lmaskV=null;
                    if(lmaskStr.length()>0)
                        lmaskV=CMLib.masking().maskCompile(lmaskStr);
                    live=CMath.s_bool(CMLib.xml().getValFromPieces(piece.contents,"LIVE"));
                }
            }
            else
            {
                lmaskV=null;
                lmaskStr="";
                live=false;
                rate=0.0;
            }
        }
    }
}
