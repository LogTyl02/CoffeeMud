package com.planet_ink.coffee_mud.Libraries.interfaces;
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
@SuppressWarnings("unchecked")
public interface MoneyLibrary extends CMLibrary
{
    public static final String defaultCurrencyDefinition=
        "=1 gold coin(s);100 golden note(s);10000 whole note(s);1000000 Archon note(s)";
    public static final String goldStandard=
        "GOLD=0.01 copper piece(s) (cp);0.1 silver piece(s) (sp);1.0 gold piece(s) (gp);5.0 platinum piece(s) (pp)";
    public static final String copperStandard=
        "COPPER=1 copper bit(s) (cc);10 silver bit(s) (sc);100 gold bit(s) (gc);500 platinum bit(s) (pc)";
    
    public static final int DEBT_DEBTOR=1;
    public static final int DEBT_OWEDTO=2;
    public static final int DEBT_AMTDBL=3;
    public static final int DEBT_REASON=4;
    public static final int DEBT_DUELONG=5;
    public static final int DEBT_INTDBL=6;
    
    public void unloadCurrencySet(String currency);
    public DVector createCurrencySet(String currency);
    public DVector getCurrencySet(String currency);
    public Vector getAllCurrencies();
    public Vector getDenominationNameSet(String currency);
    public double lowestAbbreviatedDenomination(String currency);
    public double lowestAbbreviatedDenomination(String currency, double absoluteAmount);
    public double abbreviatedRePrice(MOB shopkeeper, double absoluteAmount);
    public double abbreviatedRePrice(String currency, double absoluteAmount);
    public String abbreviatedPrice(MOB shopkeeper, double absoluteAmount);
    public String abbreviatedPrice(String currency, double absoluteAmount);
    public String getDenominationShortCode(String currency, double denomination);
    public double getLowestDenomination(String currency);
    public String getDenominationName(String currency);
    public String getDenominationName(String currency,  double denomination, long number);
    public double getBestDenomination(String currency, double absoluteValue);
    public double getBestDenomination(String currency, int numberOfCoins, double absoluteValue);
    public Vector getBestDenominations(String currency, double absoluteValue);
    public String getConvertableDescription(String currency, double denomination);
    public String getDenominationName(String currency, double denomination);
    public String nameCurrencyShort(MOB mob, double absoluteValue);
    public String nameCurrencyShort(MOB mob, int absoluteValue);
    public String nameCurrencyShort(String currency, double absoluteValue);
    public String nameCurrencyLong(MOB mob, double absoluteValue);
    public String nameCurrencyLong(MOB mob, int absoluteValue);
    public String nameCurrencyLong(String currency, double absoluteValue);
    public Coins makeBestCurrency(MOB mob,  double absoluteValue, Environmental owner, Item container);
    public Coins makeBestCurrency(String currency,  double absoluteValue, Environmental owner, Item container);
    public Coins makeBestCurrency(MOB mob, double absoluteValue);
    public Coins makeCurrency(String currency, double denomination, long numberOfCoins);
    public Coins makeBestCurrency(String currency, double absoluteValue);
    public Vector makeAllCurrency(String currency, double absoluteValue);
    public void addMoney(MOB customer, int absoluteValue);
    public void addMoney(MOB customer, double absoluteValue);
    public void addMoney(MOB customer, String currency,int absoluteValue);
    public void addMoney(MOB mob, String currency, double absoluteValue);
    public void giveSomeoneMoney(MOB recipient, double absoluteValue);
    public void giveSomeoneMoney(MOB recipient, String currency, double absoluteValue);
    public void giveSomeoneMoney(MOB banker, MOB customer, double absoluteValue);
    public void giveSomeoneMoney(MOB banker, MOB customer, String currency, double absoluteValue);
    public void bankLedger(String bankName, String owner, String explanation);
    public boolean modifyBankGold(String bankName,  String owner, String explanation, String currency, double absoluteAmount);
    public boolean modifyThisAreaBankGold(Area A,  HashSet triedBanks, String owner, String explanation, String currency, double absoluteAmount);
    public boolean modifyLocalBankGold(Area A, String owner, String explanation, String currency, double absoluteAmount);
    public void subtractMoneyGiveChange(MOB banker, MOB mob, int absoluteAmount);
    public void subtractMoneyGiveChange(MOB banker, MOB mob, double absoluteAmount);
    public void subtractMoneyGiveChange(MOB banker, MOB mob, String currency, double absoluteAmount);
    public void setMoney(MOB mob, double absoluteAmount);
    public void setMoney(MOB mob, String currency, double absoluteAmount);
    public void subtractMoney(MOB mob, double absoluteAmount);
    public void subtractMoney(MOB mob, String currency, double absoluteAmount);
    public int getMoney(MOB mob);
    public void setMoney(MOB mob, int amount);
    public void clearZeroMoney(MOB mob, String currency);
    public void clearInventoryMoney(MOB mob, String currency);
    public void subtractMoney(MOB mob, double denomination, double absoluteAmount);
    public void subtractMoney(MOB mob, String currency, double denomination, double absoluteAmount);
    public Vector getStandardCurrency(MOB mob, String currency);
    public long getNumberOfCoins(MOB mob, String currency, double denomination);
    public String getCurrency(Environmental E);
    public double getTotalAbsoluteValue(MOB mob, String currency);
    public double getTotalAbsoluteNativeValue(MOB mob);
    public double getTotalAbsoluteShopKeepersValue(MOB mob, MOB shopkeeper);
    public double getTotalAbsoluteValueAllCurrencies(MOB mob);
	public DVector getDebt(String name);
	public DVector getDebt(String name, String owedTo);
	public DVector getDebtOwed(String owedTo);
	public double getDebtOwed(String name, String owedTo);
	public void adjustDebt(String name, String owedTo, double adjustAmt, String reason, double interest, long due);
	public void delAllDebt(String name, String owedTo);
}
