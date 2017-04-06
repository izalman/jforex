package com.myStrategies;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IOrder.State;
import com.dukascopy.api.drawings.IChartObjectFactory;
import com.dukascopy.api.drawings.IScreenLabelChartObject;
import com.dukascopy.api.drawings.ISignalDownChartObject;
import com.dukascopy.api.drawings.ISignalUpChartObject;
import com.dukascopy.api.feed.CreationPoint;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.util.RenkoFeedDescriptor;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




public class AFINA implements IStrategy {
    
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
    private static PrintStream out;

    private int signalUpCount = 0;
    private int signalDownCount = 0;
    private double orderAmount = 0;

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
    private final double BufferR[][] = new double[Pairs.values().length][6];
    private final double mal[][] = new double[Pairs.values().length][2];
    private final double mah[][] = new double[Pairs.values().length][2];
    
    private MockRenko renko = null;
    private MockRenko prevRenko = null;
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
        
        mal[index] = indicators.ma(renkoFeed[index], AppliedPrice.CLOSE, OfferSide.BID, maLowPeriod, maType).calculate(2, bar.getTime(), 0);

        setSignalBuy[index] = true;
        setSignalSell[index] = true;
        instrumentSellBuy[index][0] = -1;

    }
    
    
    private void getOrderCommand(int index, ITick tick) throws JFException {
        
        List<ITick> ticks = history.getTicks(instruments[index], history.getBarStart(Period.TEN_SECS, tick.getTime()) , history.getLastTick(instruments[index]).getTime());
        //Calendar a = Calendar.getInstance();
        //a.setTimeInMillis(tick.getTime());
        //println(instruments[index].name() + " TICKS COUNT = "+ticks.size()+" TIME "+a.getTime());
        int count = 0;
        for (ITick tickin: ticks) {
            count++;
            
            if (tickCount[index][0] < count) {
                tickCount[index][0]++;

                //CALCULATING
                calculateC(index, BufferK, k_pips, tickin);
                
                //calculateR(index, BufferR, GBPUSD_r_pips, tickin);
                
                //println("PREV = " +BufferR[index][5]+"  CURR = "+BufferR[index][2]);

                //FIND SYGNAL
                if (setSignalBuy[index] && setSignalSell[index]) {

                    //IBar bar = history.getBar(instruments[index], Period.ONE_HOUR, OfferSide.BID, 0);

                    //if (tick.getBid() > BufferK[index][BufferK[index].length-1] && BufferR[index][0] > BufferR[index][1]) {
                    if (tick.getBid() > BufferK[index][BufferK[index].length-1] && mal[index][1] > mal[index][0]) {
                    //if (bar.getClose() < mal[index][1] && tick.getBid() > Buffer[index][Buffer[index].length-1] && mal[index][1] > mal[index][0]) {
                    //if (bar.getHigh() > Buffer[index][Buffer[index].length-1] && tick.getBid() > Buffer[index][Buffer[index].length-1] && mal[index][1] > mal[index][0]) {
                        instrumentSellBuy[index][0] = 1;
                        if (!chartInfo) setSignalBuy[index] = false;
                    }
                    
                    //if (tick.getAsk() < BufferK[index][BufferK[index].length-1] && BufferR[index][0] < BufferR[index][1]) {
                    if (tick.getAsk() < BufferK[index][BufferK[index].length-1] && mal[index][1] < mal[index][0]) {
                    //if (bar.getClose() > mal[index][1] && tick.getAsk() < Buffer[index][Buffer[index].length-1] && mal[index][1] < mal[index][0]) {
                    //if (bar.getLow() < Buffer[index][Buffer[index].length-1] && tick.getAsk() < Buffer[index][Buffer[index].length-1] && mal[index][1] < mal[index][0]) {
                        instrumentSellBuy[index][0] = 0;
                        if (!chartInfo) setSignalSell[index] = false;
                    }

                }
                //

                //CHAR INFO
                if (chartInfo && (setSignalBuy[index] || setSignalSell[index])) {
                    if (setSignalSell[index] && instrumentSellBuy[index][0] == 0) {
                        signalDown= chartFactories[index].createSignalDown("DOWN"+String.valueOf(signalDownCount), tick.getTime(), tick.getAsk());
                        charts[index].add(signalDown);
                        signalDownCount++;
                        setSignalSell[index] = false;
                    }
                    if (setSignalBuy[index] && instrumentSellBuy[index][0]== 1) {
                        signalUp = chartFactories[index].createSignalUp("UP"+String.valueOf(signalUpCount), tick.getTime(), tick.getBid());
                        charts[index].add(signalUp);
                        signalUpCount++;
                        setSignalBuy[index] = false;
                    }
                }
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


                    while (ordersCount < maxOrders) {
                        if (instrumentSellBuy[index][0] == 1) {

                            stopLossPrice = tick.getBid() - stopPips * instruments[index].getPipValue();
                            takeProfitPrice = tick.getBid() + takePips * instruments[index].getPipValue();
                            ordersCount++;
                            if (takeProfitPrice!=0 && stopLossPrice!=0) {
                                IOrder order = engine.submitOrder("B1_"+Pairs.getPairById(index)+String.valueOf(ordersCount+System.currentTimeMillis()), instruments[index], OrderCommand.BUY, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                                if (order.getState() != State.CANCELED && ordersCount == maxOrders) instrumentSellBuy[index][0] =-1;
                            }

                        } 

                        if (instrumentSellBuy[index][0] == 0) {
                            stopLossPrice = tick.getAsk() + stopPips * instruments[index].getPipValue();
                            takeProfitPrice = tick.getAsk() - takePips * instruments[index].getPipValue();
                            ordersCount++;
                            if (takeProfitPrice!=0 && stopLossPrice!=0) {
                                IOrder order = engine.submitOrder("S1_"+Pairs.getPairById(index)+String.valueOf(ordersCount+System.currentTimeMillis()), instruments[index], OrderCommand.SELL, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                                if (order.getState() != State.CANCELED && ordersCount == maxOrders) instrumentSellBuy[index][0] =-1;
                            }

                        }
                    } 
                }
            }
        }
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
    
    
    private void calculateR(int index, double[][] buffer, int pips, ITick tick) throws JFException {
        double reversal = (pips*instruments[index].getPipValue());
        if (prevRenko==null) {
            prevRenko = new MockRenko(tick.getAsk(), reversal);
            renko = prevRenko;
            buffer[index][0] = prevRenko.getClose();
            buffer[index][1] = prevRenko.getClose();
        } else {
            
            
            if (prevRenko.high < tick.getBid()) {
                prevRenko.high = tick.getBid();
                prevRenko.close = prevRenko.high;
            }
            if (prevRenko.low > tick.getAsk()) {
                prevRenko.low = tick.getAsk();
                prevRenko.close = prevRenko.low;
            }
            //prevRenko.close = tick.getBid();
            
            buffer[index][0] = prevRenko.getClose();
            
            if(prevRenko.isComplete()) {
                prevRenko.postProcess();
                
                buffer[index][1] = prevRenko.getClose();
                
                println("GREEN COMPLETE = " +prevRenko.isGreenComplete());
                println("RED COMPLETE = " +prevRenko.isRedComplete());
            
                renko = prevRenko.getNextRenko(prevRenko);

                buffer[index][0] = renko.getClose();
                prevRenko = renko;
                
            }
        }
    }
    
    
    private void calculateR(int index, double[][] buffer, int pips, IBar bar) throws JFException {
        
        double reversal = (pips*instruments[index].getPipValue());
        if (prevRenko==null) {
            prevRenko = new MockRenko(bar.getClose(), reversal);
            renko = prevRenko;
            buffer[index][0] = prevRenko.getClose();
            buffer[index][1] = prevRenko.getClose();
        } else {
            
            
            if (prevRenko.high < bar.getClose()) {
                prevRenko.high = bar.getClose();
            }
            if (prevRenko.low > bar.getClose()) {
                prevRenko.low = bar.getClose();
            }
            prevRenko.close = bar.getClose();
            
            buffer[index][0] = renko.getClose();
            
            if(prevRenko.isComplete()) {
                prevRenko.postProcess();
                
                buffer[index][1] = prevRenko.getClose();
                
                println("GREEN COMPLETE = " +prevRenko.isGreenComplete());
                println("RED COMPLETE = " +prevRenko.isRedComplete());
            
                renko = prevRenko.getNextRenko(prevRenko);

                buffer[index][0] = renko.getClose();
                prevRenko = renko;
                
            }
        }
    }

    
    
    
class MockRenko implements IBar {

    public double open;
    public double close;
    public double low;
    public double high;

    
    private MockRenko prevRenko;
    private final double height;
    
    
    public MockRenko getNextRenko(MockRenko prevRenko) {
        MockRenko renko = new MockRenko(prevRenko.close, prevRenko.height);
        renko.prevRenko = prevRenko;
        return renko;
    }

    public MockRenko(double price, double height) {
        this.height = height;
        open = close = low = high = getRoundedPrice(price);
    }       
    
    public boolean isComplete(){
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
    
    public void postProcess(){
        //on trend change high-low difference is double the renko height - adjust it here
        if(isGreenComplete()){
            low = high - height;
        } else {
            high = low + height;
        }
        //make "solid" bricks with prices rounded to the brick height
        low = getRoundedPrice(low);
        high = getRoundedPrice(high);
        close = getRoundedPrice(close);
        open = getRoundedPrice(open);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
    
}
    
   