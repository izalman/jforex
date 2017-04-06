package com.myStrategies;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IOrder.State;
import com.dukascopy.api.drawings.IChartObjectFactory;
import com.dukascopy.api.drawings.IScreenLabelChartObject;
import com.dukascopy.api.drawings.ISignalDownChartObject;
import com.dukascopy.api.drawings.ISignalUpChartObject;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IFeedListener;
import com.dukascopy.api.feed.IRenkoBar;
import com.dukascopy.api.feed.util.RenkoFeedDescriptor;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




public class BestFuture2 implements IStrategy, IFeedListener  {
    
    @Configurable("Starting Lot")
    public double startingAmount = 0.01;
    @Configurable("Max Lot")
    public double maxLot = 20; //Test 400
    
 
    @Configurable("Enable Trading")
    public boolean trade = false;
    @Configurable("Chart Info")
    public boolean chartInfo = false;
    
    
    //order parameters
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
    public int EURUSD_r_pips = 15;
    @Configurable("GBPUSD_R_PIPS")
    public int GBPUSD_r_pips = 15;
    @Configurable("AUDUSD_R_PIPS")
    public int AUDUSD_r_pips = 15;
    @Configurable("NZDUSD_R_PIPS")
    public int NZDUSD_r_pips = 15;
    @Configurable("USDCAD_R_PIPS")
    public int USDCAD_r_pips = 15;
    @Configurable("USDCHF_R_PIPS")
    public int USDCHF_r_pips = 15;
    @Configurable("USDJPY_R_PIPS")
    public int USDJPY_r_pips = 15;
    
    @Configurable("K_PIPS")
    public int k_pips = 5; //Always
    
    
    //MA Indicator Parameters
    @Configurable("MA HIGH Period")
    public int maHighPeriod=2; //2 or 3
    @Configurable("MA LOW Period")
    public int maLowPeriod=2; //2 or 3
    @Configurable("Time Period")
    public Period selectedPeriod = Period.ONE_HOUR;
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

    private int orderCounter = 0;
    private int signalUpCount = 0;
    private int signalDownCount = 0;
    private double stopLossPrice = 0;
    private double takeProfitPrice = 0;
    private double orderAmount = 0;

    private final IChart[] charts = new IChart[Pairs.values().length];
    private final IScreenLabelChartObject[][] labels = new IScreenLabelChartObject[Pairs.values().length][10000];
    private final IChartObjectFactory[] chartFactories = new IChartObjectFactory[Pairs.values().length];
    private final Instrument[] instruments = new Instrument[Pairs.values().length];
    private final IFeedDescriptor[] renkoFeed = new IFeedDescriptor[Pairs.values().length];
    private final int[][] instrumentSellBuy = new int[Pairs.values().length][1]; 
    private final long[] prevBarStart = new long[Pairs.values().length];
    private final boolean[] setSignalBuy = new boolean[Pairs.values().length];
    private final long[] lastBarStart = new long[Pairs.values().length];
    private final boolean[] setSignalSell = new boolean[Pairs.values().length];
    private final boolean[] tradeInstrument = new boolean[Pairs.values().length];
    
    private final double Buffer[][] = new double[Pairs.values().length][200];
    
    private final double mal[][] = new double[Pairs.values().length][2];
    private final double mah[][] = new double[Pairs.values().length][2];
    
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
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(EURUSD_r_pips), OfferSide.BID);
                    renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                    context.subscribeToFeed(renkoFeed[pair.getIndex()], this);
                }} break;
                case GBPUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(GBPUSD_r_pips), OfferSide.BID);
                    //renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;
                case AUDUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(AUDUSD_r_pips), OfferSide.BID);
                    //renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;    
                case NZDUSD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(NZDUSD_r_pips), OfferSide.BID);
                    //renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;
                case USDCAD: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(USDCAD_r_pips), OfferSide.BID);
                    //renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;   
                case USDCHF: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(USDCHF_r_pips), OfferSide.BID);
                    //renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;    
                case USDJPY: {if (tradeInstrument[pair.getIndex()]) 
                {
                    renkoFeed[pair.getIndex()] = new RenkoFeedDescriptor(instruments[pair.getIndex()], PriceRange.valueOf(USDJPY_r_pips), OfferSide.BID);
                    //renkoFeed[pair.getIndex()].setPeriod(Period.ONE_HOUR);
                    renkoFeed[pair.getIndex()].setFilter(Filter.WEEKENDS);
                }} break;    
                default: break;
            }
            
            
            //setSignalBuy[pair.getIndex()] = setSignalSell[pair.getIndex()] = true;
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
    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
        try {
            
            if(!(feedData instanceof IBar)){
                return;
            }
            Instrument instrument = feedDescriptor.getInstrument();
            switch(instrument) {
                case EURUSD: if (tradeInstrument[Pairs.EURUSD.getIndex()]) getCommand(Pairs.EURUSD.getIndex(), feedData); break;
                case GBPUSD: if (tradeInstrument[Pairs.GBPUSD.getIndex()]) getCommand(Pairs.GBPUSD.getIndex(), feedData); break;
                case AUDUSD: if (tradeInstrument[Pairs.AUDUSD.getIndex()]) getCommand(Pairs.AUDUSD.getIndex(), feedData); break;
                case NZDUSD: if (tradeInstrument[Pairs.NZDUSD.getIndex()]) getCommand(Pairs.NZDUSD.getIndex(), feedData); break;
                case USDCAD: if (tradeInstrument[Pairs.USDCAD.getIndex()]) getCommand(Pairs.USDCAD.getIndex(), feedData); break;
                case USDCHF: if (tradeInstrument[Pairs.USDCHF.getIndex()]) getCommand(Pairs.USDCHF.getIndex(), feedData); break;
                case USDJPY: if (tradeInstrument[Pairs.USDJPY.getIndex()]) getCommand(Pairs.USDJPY.getIndex(), feedData); break;
                default: break;
            }
            
         } catch (Exception ex) {
            println("ERROR ON FEED DATA ERROR: "+ex);
        }
    }
    
    @Override
    public void onTick(Instrument instrument, ITick tick) {
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
            println("ERROR ON TICK ERROR: "+ex);
        }
    }
    
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}

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
    
    private void getCommand(int index, ITimedData feedData) throws JFException{
        mal[index] = indicators.ma(renkoFeed[index], AppliedPrice.CLOSE, OfferSide.BID, maLowPeriod, maType).calculate(2, history.getBarStart(selectedPeriod, feedData.getTime()), 0);
        setSignalBuy[index] = true;
        setSignalSell[index] = true;
        instrumentSellBuy[index][0] = -1;
    }
    
    private void getOrderCommand(int index, ITick tick) throws JFException{
        /*
        if (setSignalBuy[index] && setSignalSell[index]) {
            //mal[index] = indicators.ma(instruments[index], selectedPeriod, OfferSide.BID, AppliedPrice.LOW, maLowPeriod, maType, Filter.WEEKENDS, 2, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            mal[index] = indicators.ma(renkoFeed[index], AppliedPrice.CLOSE, OfferSide.BID, maLowPeriod, maType).calculate(2, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            //mah[index] = indicators.ma(renkoFeed[index], AppliedPrice.HIGH, OfferSide.BID, maHighPeriod, maType).calculate(2, history.getBarStart(selectedPeriod, tick.getTime()), 0); 
            lastBarStart[index] = history.getBarStart(selectedPeriod, tick.getTime());
            prevBarStart[index] = lastBarStart[index];
            setSignalBuy[index] = true;
            setSignalSell[index] = true;
            instrumentSellBuy[index][0] = -1;
            
        } else if (prevBarStart[index]!=lastBarStart[index]) {
            //mal[index] = indicators.ma(instruments[index], selectedPeriod, OfferSide.BID, AppliedPrice.LOW, maLowPeriod, maType, Filter.WEEKENDS, 2, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            //mah[index] = indicators.ma(instruments[index], selectedPeriod, OfferSide.BID, AppliedPrice.HIGH, maLowPeriod, maType, Filter.WEEKENDS, 2, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            mal[index] = indicators.ma(renkoFeed[index], AppliedPrice.CLOSE, OfferSide.BID, maLowPeriod, maType).calculate(2, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            //mah[index] = indicators.ma(renkoFeed[index], AppliedPrice.HIGH, OfferSide.BID, maHighPeriod, maType).calculate(2, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            prevBarStart[index] = lastBarStart[index];
            setSignalBuy[index] = true;
            setSignalSell[index] = true;
            instrumentSellBuy[index][0] = -1;
        }
        lastBarStart[index] = history.getBarStart(selectedPeriod, tick.getTime());
        */
        //CALCULATING KAGI
        List<IBar> bars = calculateC(index, Buffer, k_pips, tick);
        
        if (bars!=null) {

            //FIND SYGNAL
            if (setSignalBuy[index] && setSignalSell[index]) {
                
                int updown = -1;
                for (int i = Buffer[index].length-1; i>1; i--) {
                    if (Buffer[index][i-1] > Buffer[index][i]) {
                        updown = 0;
                        break;
                    } else if (Buffer[index][i-1] < Buffer[index][i]){
                        updown =1;
                        break;
                    }
                }
                
                IBar lastBar = bars.get(bars.size()-1);

                if (lastBar.getHigh() > Buffer[index][Buffer[index].length-1] && mal[index][1] > mal[index][0]) {
                //if (lastBar.getHigh() > Buffer[index][Buffer[index].length-1] && mal[index][1] > mal[index][0] && mah[index][1] > mah[index][0]) {
                    instrumentSellBuy[index][0] = 1;
                    if (!chartInfo) setSignalBuy[index] = false;
                }
                if (lastBar.getLow() < Buffer[index][Buffer[index].length-1] && mal[index][1] < mal[index][0]) {
                //if (lastBar.getLow() < Buffer[index][Buffer[index].length-1] && mal[index][1] < mal[index][0] && mah[index][1] < mah[index][0]) {
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

                int stopLossPips  = 30;
                int takeProfitPips = 20;
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
                    case 0: stopLossPips = EURUSDstopLossPips; takeProfitPips = EURUSDtakeProfitPips; break;
                    case 1: stopLossPips = GBPUSDstopLossPips; takeProfitPips = GBPUSDtakeProfitPips; break;
                    case 2: stopLossPips = AUDUSDstopLossPips; takeProfitPips = AUDUSDtakeProfitPips; break;
                    case 3: stopLossPips = NZDUSDstopLossPips; takeProfitPips = NZDUSDtakeProfitPips; break;
                    case 4: stopLossPips = USDCADstopLossPips; takeProfitPips = USDCADtakeProfitPips; break;
                    case 5: stopLossPips = USDCHFstopLossPips; takeProfitPips = USDCHFtakeProfitPips; break;
                    case 6: stopLossPips = USDJPYstopLossPips; takeProfitPips = USDJPYtakeProfitPips; break;
                    default: break;
                }


                if (ordersCount < 2) {
                    if (instrumentSellBuy[index][0] == 1) {

                        stopLossPrice = history.getLastTick(instruments[index]).getBid() - stopLossPips * instruments[index].getPipValue();
                        takeProfitPrice = history.getLastTick(instruments[index]).getBid() + takeProfitPips * instruments[index].getPipValue();
                        orderCounter++;
                        if (takeProfitPrice!=0 && stopLossPrice!=0) {
                            IOrder order = engine.submitOrder("B"+Pairs.getPairById(index)+String.valueOf(orderCounter+System.currentTimeMillis()), instruments[index], OrderCommand.BUY, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                            if (order.getState() != State.CANCELED) instrumentSellBuy[index][0] =-1;
                        }
                        
                    } 

                    if (instrumentSellBuy[index][0] == 0) {
                        stopLossPrice = history.getLastTick(instruments[index]).getAsk() + stopLossPips * instruments[index].getPipValue();
                        takeProfitPrice = history.getLastTick(instruments[index]).getAsk() - takeProfitPips * instruments[index].getPipValue();
                        orderCounter++;
                        if (takeProfitPrice!=0 && stopLossPrice!=0) {
                            IOrder order = engine.submitOrder("S"+Pairs.getPairById(index)+String.valueOf(orderCounter+System.currentTimeMillis()), instruments[index], OrderCommand.SELL, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                            if (order.getState() != State.CANCELED) instrumentSellBuy[index][0] =-1;
                        }

                    }
                } 
            }
        }
    }
    
    

    private List<IBar> calculateC(int index, double[][] buffer, int pips, ITick tick) throws JFException {
        int endIndex = buffer[index].length-1;
        int shift = endIndex;
        int startIndex = 0;

        double reversal = (pips*instruments[index].getPipValue());
        if (buffer[index][0] == 0) {
            List<IBar> bars  = history.getBars(instruments[index], selectedPeriod, OfferSide.BID, Filter.WEEKENDS, endIndex+1, history.getBarStart(selectedPeriod, tick.getTime()), 0);

            buffer[index][endIndex] = bars.get(endIndex).getClose();
            for (int i = endIndex; i >= startIndex; i--) {

                while (shift >= startIndex && Math.abs(bars.get(shift).getClose()-bars.get(i).getClose()) < reversal) {
                    buffer[index][shift] = bars.get(i).getClose(); 
                    shift--;
                }

                if (shift>=startIndex) {

                    if (i <=endIndex) {
                        buffer[index][shift] = bars.get(i).getClose();
                    } else { buffer[index][shift] = bars.get(i).getClose(); }

                    i=shift;
                    shift--;
                } else {
                    break;
                }  
            }
            return bars;
            
        } else {

            List<IBar> bars  = history.getBars(instruments[index], selectedPeriod, OfferSide.BID, Filter.WEEKENDS, 3, history.getBarStart(selectedPeriod, tick.getTime()), 0);
            if (!bars.isEmpty()) {
                int b = buffer[index].length-1;

                if (Math.abs(buffer[index][b] - bars.get(bars.size()-1).getClose()) > reversal) {
                        buffer[index][b-1]=buffer[index][b];
                        buffer[index][b] = bars.get(bars.size()-1).getClose();
                }
                return bars;
            } else {
                return null;
            }
            
        }
    }
    
    
    
}
    
   