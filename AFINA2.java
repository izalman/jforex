package com.myStrategies;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IOrder.State;
import com.dukascopy.api.drawings.IChartObjectFactory;
import com.dukascopy.api.drawings.IRectangleChartObject;
import com.dukascopy.api.drawings.IScreenLabelChartObject;
import com.dukascopy.api.drawings.ISignalDownChartObject;
import com.dukascopy.api.drawings.ISignalUpChartObject;
import com.dukascopy.api.feed.CreationPoint;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IRenkoBar;
import com.dukascopy.api.feed.util.RenkoFeedDescriptor;
import java.awt.Color;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




public class AFINA2 implements IStrategy {
    
    @Configurable("Starting Lot")
    public double startingAmount = 0.01;
    @Configurable("Max Lot")
    public double maxLot = 20; //Test 400
    @Configurable("Orders Count")
    public int maxOrders = 1;
 
    
    @Configurable("Chart Info")
    public boolean chartInfo = false;
    
    //Trading Options
    @Configurable("Enable Trading")
    public boolean trade = false;
    
    @Configurable("Trade EURUSD")
    public boolean tradeEURUSD = false;
    @Configurable("Trade GBPUSD")
    public boolean tradeGBPUSD = false;
    @Configurable("Trade AUDUSD")
    public boolean tradeAUDUSD = false;       
    @Configurable("Trade NZDUSD")
    public boolean tradeNZDUSD = false;
    @Configurable("Trade USDCAD")
    public boolean tradeUSDCAD = false;
    @Configurable("Trade USDCHF")
    public boolean tradeUSDCHF = false;
    @Configurable("Trade USDJPY")
    public boolean tradeUSDJPY = false;
    
    //Order Options
    @Configurable("EURUSD Stop loss")
    public int EURUSDstopLossPips = 30;
    @Configurable("EURUSD Take profit")
    public int EURUSDtakeProfitPips = 20;
    
    @Configurable("GBPUSD Stop loss")
    public int GBPUSDstopLossPips = 30;
    @Configurable("GBPUSD Take profit")
    public int GBPUSDtakeProfitPips = 20;
    
    @Configurable("AUDUSD Stop loss")
    public int AUDUSDstopLossPips = 30;
    @Configurable("AUDUSD Take profit")
    public int AUDUSDtakeProfitPips = 20;
    
    @Configurable("NZDUSD Stop loss")
    public int NZDUSDstopLossPips = 30;
    @Configurable("NZDUSD Take profit")
    public int NZDUSDtakeProfitPips = 20;
    
    @Configurable("USDCAD Stop loss")
    public int USDCADstopLossPips = 30;
    @Configurable("USDCAD Take profit")
    public int USDCADtakeProfitPips = 20;
    
    @Configurable("USDCHF Stop loss")
    public int USDCHFstopLossPips = 30;
    @Configurable("USDCHF Take profit")
    public int USDCHFtakeProfitPips = 20;
    
    @Configurable("USDJPY Stop loss")
    public int USDJPYstopLossPips = 30;
    @Configurable("USDJPY Take profit")
    public int USDJPYtakeProfitPips = 20;
    
    
    @Configurable("EURUSD_R_PIPS")
    public int EURUSD_r_pips = 10;
    @Configurable("GBPUSD_R_PIPS")
    public int GBPUSD_r_pips = 10;
    @Configurable("AUDUSD_R_PIPS")
    public int AUDUSD_r_pips = 10;
    @Configurable("NZDUSD_R_PIPS")
    public int NZDUSD_r_pips = 10;
    @Configurable("USDCAD_R_PIPS")
    public int USDCAD_r_pips = 10;
    @Configurable("USDCHF_R_PIPS")
    public int USDCHF_r_pips = 10;
    @Configurable("USDJPY_R_PIPS")
    public int USDJPY_r_pips = 10;
    
    @Configurable("K_PIPS")
    public int k_pips = 10;
    
    
    //MA Indicator Parameters
    @Configurable("MA LOW Period")
    public int maLowPeriod=2;
    @Configurable("Time Period")
    public Period selectedPeriod = Period.TEN_SECS;
    @Configurable("MaType")
    public IIndicators.MaType maType = IIndicators.MaType.HMA;

    
    @Configurable("Slippage")
    public int slippage = 7;
    
    
    
    
    private final Instrument instrument0 = Instrument.EURUSD;
    private final Instrument instrument1 = Instrument.GBPUSD;
    private final Instrument instrument2 = Instrument.AUDUSD;       
    private final Instrument instrument3 = Instrument.NZDUSD;
    private final Instrument instrument4 = Instrument.USDCAD;
    private final Instrument instrument5 = Instrument.USDCHF;
    private final Instrument instrument6 = Instrument.USDJPY;
    
    enum Pairs {
        EURUSD (0),
        GBPUSD (1),
        AUDUSD (2),
        NZDUSD (3),
        USDCAD (4),
        USDCHF (5),
        USDJPY (6)
        ; 
        private final int index;
        
        public int getIndex() {
            return this.index;
        }
        
        public static Pairs getPairById(int id) {
            for (Pairs pair: Pairs.values()) {
                if (id == pair.index) {
                    return pair;
                }
            }
            return null;
        }

        private Pairs(int index) {
            this.index = index;
        }
    }
    private IAccount account;
    private IEngine engine;
    private IHistory history;
    private IIndicators indicators;
    private static IConsole console;
    private ISignalUpChartObject signalUp;
    private ISignalDownChartObject signalDown;
    IRectangleChartObject rectangle;
    private static PrintStream out;

    private int signalUpCount = 0;
    private int signalDownCount = 0;
    private double orderAmount = 0;
    private int ordCnt = 0;

    private final IChart[] charts = new IChart[Pairs.values().length];
    private final IScreenLabelChartObject[][] labels = new IScreenLabelChartObject[Pairs.values().length][10000];
    private final IChartObjectFactory[] chartFactories = new IChartObjectFactory[Pairs.values().length];
    private final Instrument[] instruments = new Instrument[Pairs.values().length];
    private final IFeedDescriptor[] renkoFeed = new IFeedDescriptor[Pairs.values().length];
    private final int[][] instrumentSellBuy = new int[Pairs.values().length][1];
    private final int[][] tickCount = new int[Pairs.values().length][1];
    private final boolean[] setSignalBuy = new boolean[Pairs.values().length];
    private final boolean[] setSignalSell = new boolean[Pairs.values().length];
    private final boolean[] tradeInstrument = new boolean[Pairs.values().length];
    
    private final double BufferK[][] = new double[Pairs.values().length][200];
    private final double BufferR[][] = new double[Pairs.values().length][10];
    private final double mal[][] = new double[Pairs.values().length][2];
    private final double mah[][] = new double[Pairs.values().length][10];
    
    private MockRenko renkoRB = null;
    private MockRenko prevRenkoRB = null;
    private MockRenko renkoRA = null;
    private MockRenko prevRenkoRA = null;
    private MockRenko renkoB = null;
    private MockRenko prevRenkoB = null;
    private IRenkoBar prevFeedData2 = null;
    private IRenkoBar prevFeedData1 = null;
    private IRenkoBar lastFeedData = null;
    
    IOrder order;
    
    //Method prints message to console
    private static void println(String message) {
        out.println(message);
    }
    
     //Method prints message to console
    private static void print(String message) {
        out.print(message);
    }
    
    
    @Override
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.history = context.getHistory();
        this.indicators = context.getIndicators();
        account = context.getAccount();
        console = context.getConsole();
      
        out = console.getOut();
        
        instruments[Pairs.EURUSD.getIndex()] = instrument0;
        instruments[Pairs.GBPUSD.getIndex()] = instrument1;
        instruments[Pairs.AUDUSD.getIndex()] = instrument2;
        instruments[Pairs.NZDUSD.getIndex()] = instrument3;
        instruments[Pairs.USDCAD.getIndex()] = instrument4;
        instruments[Pairs.USDCHF.getIndex()] = instrument5;
        instruments[Pairs.USDJPY.getIndex()] = instrument6;
        
        //SUBSCRIBING TO INSTRUMENTS
        Set<Instrument> subscribeToInstruments = new HashSet<Instrument>();
        if (tradeEURUSD) subscribeToInstruments.add(instrument0);
        if (tradeGBPUSD) subscribeToInstruments.add(instrument1);
        if (tradeAUDUSD) subscribeToInstruments.add(instrument2);
        if (tradeNZDUSD) subscribeToInstruments.add(instrument3);
        if (tradeUSDCAD) subscribeToInstruments.add(instrument4);
        if (tradeUSDCHF) subscribeToInstruments.add(instrument5);
        if (tradeUSDJPY) subscribeToInstruments.add(instrument6);
               
        context.setSubscribedInstruments(subscribeToInstruments, true);

        
        tradeInstrument[Pairs.EURUSD.getIndex()] = tradeEURUSD;
        tradeInstrument[Pairs.GBPUSD.getIndex()] = tradeGBPUSD;
        tradeInstrument[Pairs.AUDUSD.getIndex()] = tradeAUDUSD;
        tradeInstrument[Pairs.NZDUSD.getIndex()] = tradeNZDUSD;
        tradeInstrument[Pairs.USDCAD.getIndex()] = tradeUSDCAD;
        tradeInstrument[Pairs.USDCHF.getIndex()] = tradeUSDCHF;
        tradeInstrument[Pairs.USDJPY.getIndex()] = tradeUSDJPY;
        
        
        for (Pairs pair: Pairs.values()) {
            
            switch(pair) {
                case EURUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(EURUSD_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;
                case GBPUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(GBPUSD_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;
                case AUDUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(AUDUSD_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;    
                case NZDUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(NZDUSD_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;
                case USDCAD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(USDCAD_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;   
                case USDCHF: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(USDCHF_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;    
                case USDJPY: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(USDJPY_r_pips), OfferSide.BID, Period.ONE_MIN, CreationPoint.CLOSE);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;    
                default: break;
            }
            
            
            setSignalBuy[pair.getIndex()] = setSignalSell[pair.getIndex()] = true;
        }

        
        if (chartInfo) {
            charts[Pairs.EURUSD.index] = context.getChart(instruments[Pairs.EURUSD.index]);
            if (charts[Pairs.EURUSD.index] == null) {
                chartInfo = false;
            } else {
                chartFactories[Pairs.EURUSD.index] = charts[Pairs.EURUSD.index].getChartObjectFactory();
                for (Pairs pair: Pairs.values()) {
                    if (pair.getIndex() != Pairs.EURUSD.index) {
                        charts[pair.getIndex()] = context.getChart(instruments[pair.getIndex()]);
                        if (charts[pair.getIndex()] != null) {
                            chartFactories[pair.getIndex()] = charts[pair.getIndex()].getChartObjectFactory();
                        }
                    }
                } 
            }
        }
    }
    
    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        
        try {
            switch(instrument) {
                case EURUSD: if (tradeInstrument[Pairs.EURUSD.getIndex()]) getOrderCommand(Pairs.EURUSD.getIndex(), tick); break;
                case GBPUSD: if (tradeInstrument[Pairs.GBPUSD.getIndex()]) getOrderCommand(Pairs.GBPUSD.getIndex(), tick); break;
                case AUDUSD: if (tradeInstrument[Pairs.AUDUSD.getIndex()]) getOrderCommand(Pairs.AUDUSD.getIndex(), tick); break;
                case NZDUSD: if (tradeInstrument[Pairs.NZDUSD.getIndex()]) getOrderCommand(Pairs.NZDUSD.getIndex(), tick); break;
                case USDCAD: if (tradeInstrument[Pairs.USDCAD.getIndex()]) getOrderCommand(Pairs.USDCAD.getIndex(), tick); break;
                case USDCHF: if (tradeInstrument[Pairs.USDCHF.getIndex()]) getOrderCommand(Pairs.USDCHF.getIndex(), tick); break;
                case USDJPY: if (tradeInstrument[Pairs.USDJPY.getIndex()]) getOrderCommand(Pairs.USDJPY.getIndex(), tick); break;
                default: break;
            }
        } catch (Exception ex) {
            println("ERROR: "+ex);
        }

    }
    
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        switch(instrument) {
                case EURUSD: if (tradeInstrument[Pairs.EURUSD.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.EURUSD.getIndex()][0] =0; break;
                case GBPUSD: if (tradeInstrument[Pairs.GBPUSD.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.GBPUSD.getIndex()][0] =0; break;
                case AUDUSD: if (tradeInstrument[Pairs.AUDUSD.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.AUDUSD.getIndex()][0] =0; break;
                case NZDUSD: if (tradeInstrument[Pairs.NZDUSD.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.NZDUSD.getIndex()][0] =0; break;
                case USDCAD: if (tradeInstrument[Pairs.USDCAD.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.USDCAD.getIndex()][0] =0; break;
                case USDCHF: if (tradeInstrument[Pairs.USDCHF.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.USDCHF.getIndex()][0] =0; break;
                case USDJPY: if (tradeInstrument[Pairs.USDJPY.getIndex()] && period.equals(Period.TEN_SECS)) tickCount[Pairs.USDJPY.getIndex()][0] =0; break;
                default: break;
        }
        switch(instrument) {
                case EURUSD: if (tradeInstrument[Pairs.EURUSD.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.EURUSD.getIndex(), bidBar); break;
                case GBPUSD: if (tradeInstrument[Pairs.GBPUSD.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.GBPUSD.getIndex(), bidBar); break;
                case AUDUSD: if (tradeInstrument[Pairs.AUDUSD.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.AUDUSD.getIndex(), bidBar); break;
                case NZDUSD: if (tradeInstrument[Pairs.NZDUSD.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.NZDUSD.getIndex(), bidBar); break;
                case USDCAD: if (tradeInstrument[Pairs.USDCAD.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.USDCAD.getIndex(), bidBar); break;
                case USDCHF: if (tradeInstrument[Pairs.USDCHF.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.USDCHF.getIndex(), bidBar); break;
                case USDJPY: if (tradeInstrument[Pairs.USDJPY.getIndex()] && period.equals(selectedPeriod)) freeSignal(Pairs.USDJPY.getIndex(), bidBar); break;
                default: break;
        }
    }
    

    @Override
    public void onAccount(IAccount account) throws JFException {}

    @Override
    public void onMessage(IMessage message) throws JFException {    }
    
    @Override
    public void onStop() throws JFException {
        if (chartInfo) {
            for (Pairs pair: Pairs.values()) {
                for (IScreenLabelChartObject label: labels[pair.index]) {
                    if (label!=null) {
                        charts[pair.index].remove(label);
                    }
                }     
            }
        }
        //close all orders
        /*
        for (Pairs pair: Pairs.values()) {
            List<IOrder> orders = engine.getOrders(instruments[pair.index]);
            for (IOrder order :orders) {
                if (order.getState() == IOrder.State.FILLED) order.close();
            }
        }
        */
    }
    
    
    private void freeSignal (int index, IBar bar) throws JFException{
        
        //mal[index] = indicators.ma(renkoFeed[index], AppliedPrice.CLOSE, OfferSide.BID, maLowPeriod, maType).calculate(2, bar.getTime(), 0);
        /*
        prevFeedData2 = (IRenkoBar)history.getFeedData(renkoFeed[index], 2);
        prevFeedData1 = (IRenkoBar)history.getFeedData(renkoFeed[index], 1);
        lastFeedData = (IRenkoBar)history.getFeedData(renkoFeed[index], 0);
        println(String.format("PREV RENKO2: O: %.5f C: %.5f H: %.5f L: %.5f",prevFeedData2.getOpen(),prevFeedData2.getClose(), prevFeedData2.getHigh(), prevFeedData2.getLow()));
        println(String.format("PREV RENKO1: O: %.5f C: %.5f H: %.5f L: %.5f",prevFeedData1.getOpen(),prevFeedData1.getClose(), prevFeedData1.getHigh(), prevFeedData1.getLow()));
        println(String.format("LAST RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",lastFeedData.getOpen(),lastFeedData.getClose(), lastFeedData.getHigh(), lastFeedData.getLow()));
        println(String.format("BAR: O: %.5f C: %.5f H: %.5f L: %.5f",bar.getOpen(),bar.getClose(), bar.getHigh(), bar.getLow()));
        */
        setSignalBuy[index] = true;
        setSignalSell[index] = true;
        instrumentSellBuy[index][0] = -1;
        
        //println(String.format("RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",renkoRB.getOpen(),renkoRB.getClose(), renkoRB.getHigh(), renkoRB.getLow()));
        
        
        
        //if (renkoB!=null && renkoB.prevRenko!=null) println(String.format("BAR MOCK RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",renkoB.prevRenko.getOpen(),renkoB.prevRenko.getClose(), renkoB.prevRenko.getHigh(), renkoB.prevRenko.getLow()));
        //if (renkoRB!=null && renkoRB.prevRenko!=null) println(String.format("BID MOCK RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",renkoRB.prevRenko.getOpen(),renkoRB.prevRenko.getClose(), renkoRB.prevRenko.getHigh(), renkoRB.prevRenko.getLow()));
        //if (renkoRA!=null && renkoRA.prevRenko!=null) println(String.format("ASK MOCK RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",renkoRA.prevRenko.getOpen(),renkoRA.prevRenko.getClose(), renkoRA.prevRenko.getHigh(), renkoRA.prevRenko.getLow()));

    }
    
    
    private void getOrderCommand(int index, ITick tick) throws JFException {
        if (renkoRB == null) {
            List<ITick> ticks = history.getTicks(instruments[index], history.getBarStart(Period.DAILY, history.getBar(instruments[index], Period.DAILY, OfferSide.BID, 1).getTime()) , history.getLastTick(instruments[index]).getTime());
            for (ITick tickin: ticks) {
                calculateRB(index, BufferR, GBPUSD_r_pips, tickin);
                if (renkoRB.prevRenko!=null) break;
            }
        }
        //double cci1H = indicators.cci(instruments[index], Period.ONE_HOUR, OfferSide.BID, maLowPeriod,0);
        //mal[index] = indicators.ma(instruments[index], Period.TEN_SECS, OfferSide.BID, AppliedPrice.CLOSE, maLowPeriod, maType, Filter.WEEKENDS, 5, tick.getTime(), 0);
        //double[][] stoch1H = indicators.stoch(instruments[index], Period.ONE_HOUR, OfferSide.BID, 5, 3, IIndicators.MaType.SMA, 3, IIndicators.MaType.EMA, Filter.WEEKENDS, 1, tick.getTime(),0);
        //double[] fib = indicators.fibPivot(instruments[index], Period.DAILY, OfferSide.BID, Period.DAILY, 0);
        
        //double dema = indicators.dema(instruments[index], selectedPeriod, OfferSide.BID, AppliedPrice.CLOSE, maLowPeriod, 1);
      
        //calculateR(index, BufferR, GBPUSD_r_pips, dema, tick.getTime());
        //calculateRB(index, BufferR, GBPUSD_r_pips, tick);
        //double cci1H = indicators.sar(instruments[index], Period.ONE_HOUR, OfferSide.BID, 0.1, 0.4, 1);
        List<ITick> ticks = history.getTicks(instruments[index], history.getBarStart(Period.TEN_SECS, tick.getTime()) , history.getLastTick(instruments[index]).getTime());
        
        //Calendar a = Calendar.getInstance();
        //a.setTimeInMillis(tick.getTime());
        //println(instruments[index].name() + " TICKS COUNT = "+ticks.size()+" TIME "+a.getTime());
        //List<IOrder> ordersl = history.getOrdersHistory(instruments[index], history.getBarStart(Period.WEEKLY, tick.getTime()), history.getLastTick(instruments[index]).getTime());
        //println("ORDERS COUNT = "+ordersl.size());
        int count = 0;
        for (ITick tickin: ticks) {
            count++;
            
            if (tickCount[index][0] < count) {
                tickCount[index][0]++;

                //CALCULATING
                //calculateC(index, BufferK, k_pips, tickin);

                if (renkoRB!=null) {
                    //calculateRB(index, BufferR, GBPUSD_r_pips, tickin);
                }
                
                
            }
        }   
                //println("PREV = " +BufferR[index][5]+"  CURR = "+BufferR[index][2]);

                //FIND SYGNAL
                if (setSignalBuy[index] && setSignalSell[index]) {

                    //IBar bar = history.getBar(instruments[index], Period.ONE_HOUR, OfferSide.BID, 0);
                    if (tick.getBid() > renkoRB.getHigh()) {
                    //if (!Double.isNaN(cci1H) && tick.getBid() > BufferK[index][BufferK[index].length-1] && cci1H < tick.getBid() && BufferK[index][BufferK[index].length-1] > BufferK[index][BufferK[index].length-2]) {
                    //if (bar.getClose() < mal[index][1] && tick.getBid() > Buffer[index][Buffer[index].length-1] && mal[index][1] > mal[index][0]) {
                    //if (bar.getHigh() > Buffer[index][Buffer[index].length-1] && tick.getBid() > Buffer[index][Buffer[index].length-1] && mal[index][1] > mal[index][0]) {
                        instrumentSellBuy[index][0] = 1;
                        if (!chartInfo) setSignalBuy[index] = false;
                    }

                    if (tick.getAsk() < renkoRB.getLow()) {
                    //if (!Double.isNaN(cci1H) && tick.getAsk() < BufferK[index][BufferK[index].length-1] && cci1H > tick.getBid() && BufferK[index][BufferK[index].length-1] < BufferK[index][BufferK[index].length-2]) {
                    //if (bar.getClose() > mal[index][1] && tick.getAsk() < Buffer[index][Buffer[index].length-1] && mal[index][1] < mal[index][0]) {
                    //if (bar.getLow() < Buffer[index][Buffer[index].length-1] && tick.getAsk() < Buffer[index][Buffer[index].length-1] && mal[index][1] < mal[index][0]) {
                        instrumentSellBuy[index][0] = 0;
                        if (!chartInfo) setSignalSell[index] = false;
                    } 

                }
                //
                calculateRB(index, BufferR, GBPUSD_r_pips, tick);
                //CHAR INFO
                /*
                if (chartInfo && (setSignalBuy[index] || setSignalSell[index])) {
                    if (setSignalSell[index] && instrumentSellBuy[index][0] == 0) {
                        signalDown= chartFactories[index].createSignalDown("DOWN"+String.valueOf(signalDownCount), tick.getTime(), tick.getAsk());
                        charts[index].add(signalDown);
                        signalDownCount++;
                        setSignalSell[index] = false;
                        instrumentSellBuy[index][0] =-1;
                    }
                    if (setSignalBuy[index] && instrumentSellBuy[index][0]== 1) {
                        signalUp = chartFactories[index].createSignalUp("UP"+String.valueOf(signalUpCount), tick.getTime(), tick.getBid());
                        charts[index].add(signalUp);
                        signalUpCount++;
                        setSignalBuy[index] = false;
                        instrumentSellBuy[index][0] =-1;
                    }
                }
                */
                //


                //TRADE
                if (trade && instrumentSellBuy[index][0]!=-1) {

                    long multiplier = (long)account.getBalance()/1000;
                    if (multiplier > 0) {
                        orderAmount = startingAmount*multiplier;
                    } else {
                        orderAmount = startingAmount;
                    }
                    if (orderAmount > maxLot) orderAmount = maxLot;

                    List<IOrder> orders = engine.getOrders(instruments[index]);
                    int ordersCount = orders.size();

                    for (IOrder order :orders) {
                        if (order.getState() == State.FILLED 
                            && !order.isLong() && instrumentSellBuy[index][0] == 1) {
                            order.close();
                            ordersCount--;
                        } 
                        if (order.getState() == State.FILLED 
                            && order.isLong() && instrumentSellBuy[index][0] == 0) {
                            order.close();
                            ordersCount--;
                        } 
                    }

                    double stopLossPrice  = 0;
                    double takeProfitPrice = 0;
                    double stopPips = 0;
                    double takePips = 0;
                    /*
                    EURUSD (0),
                    GBPUSD (1),
                    AUDUSD (2),
                    NZDUSD (3),
                    USDCAD (4),
                    USDCHF (5),
                    USDJPY (6)
                    */
                    switch(index) {
                        case 0: stopPips = EURUSDstopLossPips; takePips = EURUSDtakeProfitPips; break;
                        case 1: stopPips = GBPUSDstopLossPips; takePips = GBPUSDtakeProfitPips; break;
                        case 2: stopPips = AUDUSDstopLossPips; takePips = AUDUSDtakeProfitPips; break;
                        case 3: stopPips = NZDUSDstopLossPips; takePips = NZDUSDtakeProfitPips; break;
                        case 4: stopPips = USDCADstopLossPips; takePips = USDCADtakeProfitPips; break;
                        case 5: stopPips = USDCHFstopLossPips; takePips = USDCHFtakeProfitPips; break;
                        case 6: stopPips = USDJPYstopLossPips; takePips = USDJPYtakeProfitPips; break;
                        default: break;
                    }


                    //while (ordersCount < maxOrders) {
                        if (instrumentSellBuy[index][0] == 1) {

                            stopLossPrice = tick.getBid() - stopPips * instruments[index].getPipValue();
                            takeProfitPrice = tick.getBid() + takePips * instruments[index].getPipValue();
                            //stopLossPrice = tick.getBid() - stopPips * instruments[index].getPipValue();
                            //takeProfitPrice = tick.getBid() + takePips * instruments[index].getPipValue();
                            ordersCount++;
                            ordCnt++;
                            if (takeProfitPrice!=0 && stopLossPrice!=0) {
                                order = engine.submitOrder("B1_"+Pairs.getPairById(index)+String.valueOf(ordCnt+System.currentTimeMillis()), instruments[index], OrderCommand.BUY, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                                if (order.getState() != State.CANCELED && ordersCount == maxOrders) instrumentSellBuy[index][0] =-1;
                            }

                        } 

                        if (instrumentSellBuy[index][0] == 0) {
                            stopLossPrice = tick.getAsk() + stopPips * instruments[index].getPipValue();
                            takeProfitPrice = tick.getAsk() - takePips * instruments[index].getPipValue();
                            //stopLossPrice = tick.getAsk() + stopPips * instruments[index].getPipValue();
                            //takeProfitPrice = tick.getAsk() - takePips * instruments[index].getPipValue();
                            ordersCount++;
                            ordCnt++;
                            
                            if (takeProfitPrice!=0 && stopLossPrice!=0) {
                                order = engine.submitOrder("S1_"+Pairs.getPairById(index)+String.valueOf(ordCnt+System.currentTimeMillis()), instruments[index], OrderCommand.SELL, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                                if (order.getState() != State.CANCELED && ordersCount == maxOrders) instrumentSellBuy[index][0] =-1;
                            }

                        }
                    //} 
                }
            //}
        //}
    }
    
    

    private void calculateC(int index, double[][] buffer, int pips, ITick tick) throws JFException {
        int endIndex = buffer[index].length-1;
        int shift = endIndex;
        int startIndex = 0;

        double reversal = (pips*instruments[index].getPipValue());
        if (buffer[index][buffer[index].length-1] == 0) {
            List<IBar> bars  = history.getBars(instruments[index], selectedPeriod, OfferSide.BID, Filter.WEEKENDS, endIndex+1, history.getBarStart(selectedPeriod, tick.getTime()), 0);

            buffer[index][endIndex] = bars.get(endIndex).getClose();
            for (int i = endIndex; i >= startIndex; i--) {

                while (shift >= startIndex && Math.abs(bars.get(shift).getClose()-bars.get(i).getClose()) <= reversal) {
                    buffer[index][shift] = bars.get(i).getClose(); 
                    shift--;
                }

                if (shift>startIndex) {

                    if (i <=endIndex) {
                        buffer[index][shift] = bars.get(i).getClose();
                    } else { buffer[index][shift] = bars.get(i).getClose(); }

                    i=shift;
                    shift--;
                } else {
                    break;
                }  
            }
            
        } else {
            int b = buffer[index].length-1;

            if (tick.getBid() - buffer[index][b] > reversal) {
                    buffer[index][b-1]=buffer[index][b];
                    buffer[index][b] = tick.getBid();
            } else if (tick.getAsk() - buffer[index][b] < -reversal){
                    buffer[index][b-1]=buffer[index][b];
                    buffer[index][b] = tick.getAsk();
            } 
            
        }  
    }
    
    
    private void calculateRB(int index, double[][] buffer, int pips, ITick tick) throws JFException {
        double reversal = (pips*instruments[index].getPipValue());
        if (renkoRB==null) {
            renkoRB = new MockRenko(tick.getBid(), reversal, tick.getTime());
        } else {
            
            if (renkoRB.high < tick.getBid()) {
                renkoRB.high = tick.getBid();//renkoRB.getRoundedPrice(tick.getBid());
                renkoRB.close = renkoRB.high;
                }
            if (renkoRB.low > tick.getAsk()) {
                renkoRB.low = tick.getAsk();//renkoRB.getRoundedPrice(tick.getAsk());
                renkoRB.close = renkoRB.low;
            }
            
            if(renkoRB.isComplete()) {
                //renkoRB.postProcess();
                
                int cnt = 0;
                double lastPrice = buffer[index][0];
                double prevPrice = 0;
                buffer[index][0] = renkoRB.getClose();
                while(cnt<buffer[index].length-1) {
                    cnt++;
                    prevPrice = buffer[index][cnt];
                    buffer[index][cnt] = lastPrice;
                    lastPrice = prevPrice;
                }
                /*
                println("START");
                for (double a: buffer[index]) println(String.valueOf(a));
                println("END");
                
                */
                /*
                if (renkoRB.isGreenComplete()) {
                    println(String.format("GREEN RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",renkoRB.getOpen(),renkoRB.getClose(), renkoRB.getHigh(), renkoRB.getLow()));
                }
                if (renkoRB.isRedComplete()) {
                    println(String.format("RED RENKO: O: %.5f C: %.5f H: %.5f L: %.5f",renkoRB.getOpen(),renkoRB.getClose(), renkoRB.getHigh(), renkoRB.getLow()));
                }
                */
                
                if (renkoRB.isGreenComplete()) {
                    signalUpCount++;
                    rectangle = chartFactories[index].createRectangle("UP"+String.valueOf(signalUpCount), tick.getTime(), renkoRB.getHigh(), renkoRB.getTime(), renkoRB.getLow());
                    rectangle.setColor(Color.GREEN);
                    rectangle.setFillColor(Color.GREEN);
                    charts[index].add(rectangle);
                }
                if (renkoRB.isRedComplete()) {
                    signalDownCount++;
                    rectangle = chartFactories[index].createRectangle("DOWN"+String.valueOf(signalDownCount), tick.getTime(), renkoRB.getHigh(), renkoRB.getTime(), renkoRB.getLow());
                    rectangle.setColor(Color.RED);
                    rectangle.setFillColor(Color.RED);
                    charts[index].add(rectangle);
                }
                renkoRB = renkoRB.getNextRenko(renkoRB, tick.getTime());

            }
        }
    }
    
    
    
    private void calculateRA(int index, double[][] buffer, int pips, ITick tick) throws JFException {
        double reversal = (pips*instruments[index].getPipValue());
        if (renkoRA==null) {
            renkoRA = new MockRenko(tick.getBid(), reversal, tick.getTime());
        } else {
            
            
            if (renkoRA.high < tick.getAsk()) {
                renkoRA.high = tick.getAsk();
                renkoRA.close = tick.getAsk();
            }
            if (renkoRA.low > tick.getAsk()) {
                renkoRA.low = tick.getAsk();
                renkoRA.close = tick.getAsk();
            }
            
            if(renkoRA.isComplete()) {
                renkoRA.postProcess();
                
                int cnt = 0;
                double lastPrice = buffer[index][0];
                double prevPrice = 0;
                buffer[index][0] = renkoRA.getClose();
                while(cnt<buffer[index].length-1) {
                    cnt++;
                    prevPrice = buffer[index][cnt];
                    buffer[index][cnt] = lastPrice;
                    lastPrice = prevPrice;
                }
                /*
                println("START");
                for (double a: buffer[index]) println(String.valueOf(a));
                println("END");
                
                if (renkoRA.isGreenComplete()) println("###### GREEN COMPLETE ######");
                if (renkoRA.isRedComplete()) println("###### RED COMPLETE ######");
                */
                renkoRA = renkoRA.getNextRenko(renkoRA, tick.getTime());

            }
        }
    }
    
    private void calculateR(int index, double[][] buffer, int pips, double price, long time) throws JFException {
        
        double reversal = (pips*instruments[index].getPipValue());
        if (renkoB==null) {
            renkoB = new MockRenko(price, reversal, time);
        } else {
            
            
            if (renkoB.high < price) {
                renkoB.high = price;
                renkoB.close = renkoB.high;
            }
            if (renkoB.low > price) {
                renkoB.low = price;
                renkoB.close = renkoB.low;
            }
            
            if(renkoB.isComplete()) {
                renkoB.postProcess();
                
                int cnt = 0;
                double lastPrice = buffer[index][0];
                double prevPrice = 0;
                buffer[index][0] = renkoB.getClose();
                while(cnt<buffer[index].length-1) {
                    cnt++;
                    prevPrice = buffer[index][cnt];
                    buffer[index][cnt] = lastPrice;
                    lastPrice = prevPrice;
                }
                /*
                println("START");
                for (double a: buffer[index]) println(String.valueOf(a));
                println("END");
                
                if (renkoB.isGreenComplete()) println("###### GREEN COMPLETE ######");
                if (renkoB.isRedComplete()) println("###### RED COMPLETE ######");
                */
                renkoB = renkoB.getNextRenko(renkoB, time);

            }
        }
    }
    
    
    private void calculateR(int index, double[][] buffer, int pips, IBar bar) throws JFException {
        
        double reversal = (pips*instruments[index].getPipValue());
        if (renkoB==null) {
            renkoB = new MockRenko(bar.getClose(), reversal, bar.getTime());
        } else {
            
            
            if (renkoB.high < bar.getClose()) {
                renkoB.high = bar.getClose();
                renkoB.close = renkoB.high;
            }
            if (renkoB.low > bar.getClose()) {
                renkoB.low = bar.getClose();
                renkoB.close = renkoB.low;
            }
            
            if(renkoB.isComplete()) {
                renkoB.postProcess();
                
                int cnt = 0;
                double lastPrice = buffer[index][0];
                double prevPrice = 0;
                buffer[index][0] = renkoB.getClose();
                while(cnt<buffer[index].length-1) {
                    cnt++;
                    prevPrice = buffer[index][cnt];
                    buffer[index][cnt] = lastPrice;
                    lastPrice = prevPrice;
                }
                /*
                println("START");
                for (double a: buffer[index]) println(String.valueOf(a));
                println("END");
                
                if (renkoB.isGreenComplete()) println("###### GREEN COMPLETE ######");
                if (renkoB.isRedComplete()) println("###### RED COMPLETE ######");
                */
                renkoB = renkoB.getNextRenko(renkoB, bar.getTime());

            }
        }
    }

    
    
    
class MockRenko implements IBar {

    private double open;
    private double close;
    private double low;
    private double high;
    private long time;

    
    private MockRenko prevRenko;
    private double height;
    
    
    public MockRenko getNextRenko(MockRenko prevRenko, long time) {
        MockRenko renko = new MockRenko(prevRenko.close, prevRenko.height, time);
        renko.prevRenko = prevRenko;
        return renko;
    }

    public MockRenko(MockRenko prevRenko) {
        this.prevRenko = prevRenko;
    }
    

    public MockRenko(double price, double height, long time) {
        this.height = height;
        this.time = time;
        //open = close = low = high = price;//getRoundedPrice(price);
        open = close = price;
        low = price-height;
        high = price+height;
    }       
    
    private boolean isComplete(){
        return isGreenComplete() || isRedComplete();
    }
    
    private boolean isGreenComplete(){
        return prevRenko == null 
            ? high - open >= height
            : high - prevRenko.high >= height;
    }
    
    private boolean isRedComplete(){
        return prevRenko == null 
            ? open - low >= height
            : prevRenko.low - low >= height;
    }
    
    private void postProcess(){
        //on trend change high-low difference is double the renko height - adjust it here
        /*
        if(isGreenComplete()){
            low = high - height;
        } else {
            high = low + height;
        }
        */
        if(isGreenComplete()){
            //high = (high + low)/2;
            close = high;
        } else {
            //low = (high + low)/2;
            close = low;
        }
        
        //make "solid" bricks with prices rounded to the brick height
        /*
        low = getRoundedPrice(low);
        high = getRoundedPrice(high);
        close = getRoundedPrice(close);
        open = getRoundedPrice(open);
        */
    }
    
    private double getRoundedPrice(double price){
        //rounded to the closest pip value that is divisible with brickSize
        double delta1 = price % height;
        double delta2 = height - price % height;
        double priceRounded = delta1 <= delta2
                ? price - delta1
                : price + delta2;
        return priceRounded;
    }

    @Override
    public double getOpen() {
        return open;
    }

    @Override
    public double getClose() {
        return close;
    }

    @Override
    public double getLow() {
        return low;
    }

    @Override
    public double getHigh() {
        return high;
    }


    @Override
    public String toString() {            
        return String.format("O: %.5f C: %.5f H: %.5f L: %.5f",
                open, close, high, low);
    }

    @Override
    public double getVolume() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getTime() {
        return time;
    }
    
}
    
}
    
   