package com.myStrategies;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.*;
import com.dukascopy.api.IOrder.State;
import com.dukascopy.api.drawings.IChartObjectFactory;
import com.dukascopy.api.drawings.IRectangleChartObject;
import com.dukascopy.api.drawings.IScreenLabelChartObject;
import com.dukascopy.api.drawings.ISignalDownChartObject;
import com.dukascopy.api.drawings.ISignalUpChartObject;
import java.awt.Color;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




public class BASE1 implements IStrategy {
    
    @Configurable("Starting Lot")
    public double startingAmount = 0.01;
    @Configurable("Max Lot")
    public double maxLot = 20; //Test 400
    @Configurable("Orders Count")
    public int maxOrders = 1;
    //Trading Options
    @Configurable("Enable Trading")
    public boolean trade = false;
 
    //DRAW OPTIONS
    @Configurable("DRAW SIGNAL")
    public boolean signalOnChar = false;
    @Configurable("DRAW RENKO")
    public boolean renkoOnChart = false;
    
    //RENKO Options
    @Configurable("RENKO Reverse 2x")
    public boolean reverse2X = false; 
    @Configurable("RENKO Round PostProcess")        
    public boolean roundPostProcess = false; 
    @Configurable("RENKO Check Previous")
    public boolean checkPrevRenko = false;
    @Configurable("RENKO Update Close Price")        
    public boolean closeFollowPrice = false;
    @Configurable("RENKO Round Price")         
    public boolean roundPrice = false;
    
    
    
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
    public int EURUSD_r_pips = 20;
    @Configurable("GBPUSD_R_PIPS")
    public int GBPUSD_r_pips = 20;
    @Configurable("AUDUSD_R_PIPS")
    public int AUDUSD_r_pips = 20;
    @Configurable("NZDUSD_R_PIPS")
    public int NZDUSD_r_pips = 20;
    @Configurable("USDCAD_R_PIPS")
    public int USDCAD_r_pips = 20;
    @Configurable("USDCHF_R_PIPS")
    public int USDCHF_r_pips = 20;
    @Configurable("USDJPY_R_PIPS")
    public int USDJPY_r_pips = 20;
    

    @Configurable("Signal Period")
    public Period signalPeriod = Period.ONE_SEC;
    @Configurable("Ticks Period")
    public Period ticksPeriod = Period.TEN_SECS; //MINIMUM TEN_SECONDS
    
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
    private final int[][] instrumentSellBuy = new int[Pairs.values().length][1];
    private final int[][] tickCount = new int[Pairs.values().length][1];
    private final boolean[] setSignalBuy = new boolean[Pairs.values().length];
    private final boolean[] setSignalSell = new boolean[Pairs.values().length];
    private final boolean[] tradeInstrument = new boolean[Pairs.values().length];
    
    
    private Renko renko = null;
    
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
        Set<Instrument> subscribeToInstruments = new HashSet<>();
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

            setSignalBuy[pair.getIndex()] = setSignalSell[pair.getIndex()] = true;
        }

        
        if (signalOnChar || renkoOnChart) {
            charts[Pairs.EURUSD.index] = context.getChart(instruments[Pairs.EURUSD.index]);
            if (charts[Pairs.EURUSD.index] == null) {
                signalOnChar = false;
                renkoOnChart = false;
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
                case EURUSD: if (tradeInstrument[Pairs.EURUSD.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.EURUSD.getIndex()][0] =0; break;
                case GBPUSD: if (tradeInstrument[Pairs.GBPUSD.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.GBPUSD.getIndex()][0] =0; break;
                case AUDUSD: if (tradeInstrument[Pairs.AUDUSD.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.AUDUSD.getIndex()][0] =0; break;
                case NZDUSD: if (tradeInstrument[Pairs.NZDUSD.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.NZDUSD.getIndex()][0] =0; break;
                case USDCAD: if (tradeInstrument[Pairs.USDCAD.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.USDCAD.getIndex()][0] =0; break;
                case USDCHF: if (tradeInstrument[Pairs.USDCHF.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.USDCHF.getIndex()][0] =0; break;
                case USDJPY: if (tradeInstrument[Pairs.USDJPY.getIndex()] && period.equals(ticksPeriod)) tickCount[Pairs.USDJPY.getIndex()][0] =0; break;
                default: break;
        }
        switch(instrument) {
                case EURUSD: if (tradeInstrument[Pairs.EURUSD.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.EURUSD.getIndex(), bidBar); break;
                case GBPUSD: if (tradeInstrument[Pairs.GBPUSD.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.GBPUSD.getIndex(), bidBar); break;
                case AUDUSD: if (tradeInstrument[Pairs.AUDUSD.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.AUDUSD.getIndex(), bidBar); break;
                case NZDUSD: if (tradeInstrument[Pairs.NZDUSD.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.NZDUSD.getIndex(), bidBar); break;
                case USDCAD: if (tradeInstrument[Pairs.USDCAD.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.USDCAD.getIndex(), bidBar); break;
                case USDCHF: if (tradeInstrument[Pairs.USDCHF.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.USDCHF.getIndex(), bidBar); break;
                case USDJPY: if (tradeInstrument[Pairs.USDJPY.getIndex()] && period.equals(signalPeriod)) freeSignal(Pairs.USDJPY.getIndex(), bidBar); break;
                default: break;
        }
    }
    

    @Override
    public void onAccount(IAccount account) throws JFException {}

    @Override
    public void onMessage(IMessage message) throws JFException {    }
    
    @Override
    public void onStop() throws JFException {
        if (signalOnChar || renkoOnChart) {
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
        
        setSignalBuy[index] = true;
        setSignalSell[index] = true;
        instrumentSellBuy[index][0] = -1;
        
    }
    
    
    private void getOrderCommand(int index, ITick tick) throws JFException {
        int r_pips=0;
        switch(index) {
            case 0: r_pips = EURUSD_r_pips; break;
            case 1: r_pips = GBPUSD_r_pips; break;
            case 2: r_pips = AUDUSD_r_pips; break;
            case 3: r_pips = NZDUSD_r_pips; break;
            case 4: r_pips = USDCAD_r_pips; break;
            case 5: r_pips = USDCHF_r_pips; break;
            case 6: r_pips = USDJPY_r_pips; break;
            default: break;
        }
        if (renko == null) {
            //Renko(Instrument instrument, double price, double pips, Color upBrickColor, Color downBrickColor, Price usePrice, 
            //boolean reverse2X, boolean roundPostProcess, boolean checkPrevRenko, boolean closeFollowPrice, boolean roundPrice, long time)
            renko = new Renko(instruments[index], tick.getBid(), r_pips, Color.GREEN, Color.RED, Renko.Price.ASKBID, 
                    reverse2X, roundPostProcess, checkPrevRenko, closeFollowPrice, roundPrice, tick.getTime());
            List<ITick> ticks = history.getTicks(instruments[index], history.getBarStart(Period.DAILY, history.getBar(instruments[index], Period.DAILY, OfferSide.BID, 1).getTime()) , history.getLastTick(instruments[index]).getTime());
            for (ITick tickin: ticks) {
                renko = renko.calculateRenko(renko, tickin, 0, tick.getTime());
                if (renko.getPrevRenko()!=null) break;
            }
        }
        renko = renko.calculateRenko(renko, tick, 0, tick.getTime());
        
        //DRAW RENKO
        drawRenko(renko.getPrevRenko(), index, tick);
        
        
        
        
        
        
        
        
        
        
        

        
        
        //DRAW SIGNALS
        drawSignal(index, tick);


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
                    ordCnt++;
                    if (takeProfitPrice!=0 && stopLossPrice!=0) {
                        order = engine.submitOrder("B1_"+Pairs.getPairById(index)+String.valueOf(ordCnt+System.currentTimeMillis()), instruments[index], OrderCommand.BUY, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                        if (order.getState() != State.CANCELED && ordersCount == maxOrders) instrumentSellBuy[index][0] =-1;
                    }

                } 

                if (instrumentSellBuy[index][0] == 0) {
                    stopLossPrice = tick.getAsk() + stopPips * instruments[index].getPipValue();
                    takeProfitPrice = tick.getAsk() - takePips * instruments[index].getPipValue();
                    ordersCount++;
                    ordCnt++;
                    if (takeProfitPrice!=0 && stopLossPrice!=0) {
                        order = engine.submitOrder("S1_"+Pairs.getPairById(index)+String.valueOf(ordCnt+System.currentTimeMillis()), instruments[index], OrderCommand.SELL, orderAmount, 0, slippage, stopLossPrice, takeProfitPrice);
                        if (order.getState() != State.CANCELED && ordersCount == maxOrders) instrumentSellBuy[index][0] =-1;
                    }

                }
            }
        }
    }
    
//DRAW RENKO
private void drawRenko(Renko renko, int index, ITick tick) {
    if (renkoOnChart && renko!=null && renko.isDrawRenko()) {
        if (renko.isGreenComplete(renko)) {
            signalUpCount++;
            rectangle = chartFactories[index].createRectangle("UP"+String.valueOf(signalUpCount), tick.getTime(), renko.getHigh(), renko.getTime(), renko.getLow());
            rectangle.setColor(renko.getUpBrickColor());
            rectangle.setFillColor(renko.getUpBrickColor());
            charts[index].add(rectangle);
        }
        if (renko.isRedComplete(renko)) {
            signalDownCount++;
            rectangle = chartFactories[index].createRectangle("DOWN"+String.valueOf(signalDownCount), tick.getTime(), renko.getHigh(), renko.getTime(), renko.getLow());
            rectangle.setColor(renko.getDownBrickColor());
            rectangle.setFillColor(renko.getDownBrickColor());
            charts[index].add(rectangle);
        }
        
        renko.setDrawRenko(false);
    }
}


//DRAW SIGNAL
private void drawSignal(int index, ITick tick) {
    //CHART INFO
    if (signalOnChar && (setSignalBuy[index] || setSignalSell[index])) {
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
}
    
    


    
    
//MONSTRZ RENKO
private static class Renko implements IBar {

    private double open;
    private double close;
    private double low;
    private double high;
    private long time;
    
    private Color upBrickColor;
    private Color downBrickColor;
    private Price usePrice;
    private boolean reverse2X;
    private boolean roundPostProcess;
    private boolean checkPrevRenko;
    private boolean closeFollowPrice;
    private boolean roundPrice;

    private Renko prevRenko;
    private double height;
    private boolean drawRenko = false;
    
    
    enum Price {
        ASK (0),
        BID (1),
        ASKBID (2),
        ANY (3)
        ; 
        private final int index;
        
        public int getIndex() {
            return this.index;
        }
        public static Price getPriceById(int id) {
            for (Price price: Price.values()) {
                if (id == price.index) {
                    return price;
                }
            }
            return null;
        }
        private Price(int index) {
            this.index = index;
        }
    }
    
    
    public Renko getNextRenko(Renko prevRenko, long time) {
        Renko renko = new Renko(prevRenko, time);
        renko.setPrevRenko(prevRenko);
        return renko;
    }
    
    //CONSTRUCTOR WITH PREVIOUS RENKO
    public Renko(Renko prevRenko, long time) {
        this.height = prevRenko.getHeight();
        this.time = time;
        this.open = this.close = this.low = this.high = prevRenko.getClose();
        this.upBrickColor = prevRenko.getUpBrickColor();
        this.downBrickColor = prevRenko.getDownBrickColor();
        this.usePrice = prevRenko.getUsePrice();
        this.reverse2X = prevRenko.isReverse2X();
        this.roundPostProcess = prevRenko.isRoundPostProcess();
        this.checkPrevRenko = prevRenko.isCheckPrevRenko();
        this.closeFollowPrice = prevRenko.isCloseFollowPrice();
        this.roundPrice = prevRenko.isRoundPrice();
    }
    
    //CONSTRUCTOR WHERE HEIGHT IS ALREADY CALCULATED
    public Renko(double price, double height, Color upBrickColor, Color downBrickColor, Price usePrice, 
            boolean reverse2X, boolean roundPostProcess, boolean checkPrevRenko, boolean closeFollowPrice, boolean roundPrice, long time) {
        this.height = height;
        this.time = time;
        this.open = this.close = this.low = this.high = price;
        this.upBrickColor = upBrickColor;
        this.downBrickColor = downBrickColor;
        this.usePrice = usePrice;
        this.reverse2X = reverse2X;
        this.roundPostProcess = roundPostProcess;
        this.checkPrevRenko = checkPrevRenko;
        this.closeFollowPrice = closeFollowPrice;
        this.roundPrice = roundPrice;
    }
    
    //CONSTRUCTOR WHERE PIPS ARE CALCULATED FROM INSTRUMENT
    public Renko(Instrument instrument, double price, double pips, Color upBrickColor, Color downBrickColor, Price usePrice, 
            boolean reverse2X, boolean roundPostProcess, boolean checkPrevRenko, boolean closeFollowPrice, boolean roundPrice, long time) {
        this.height = pips*instrument.getPipValue();
        this.time = time;
        this.open = this.close = this.low = this.high = price;
        this.upBrickColor = upBrickColor;
        this.downBrickColor = downBrickColor;
        this.usePrice = usePrice;
        this.reverse2X = reverse2X;
        this.roundPostProcess = roundPostProcess;
        this.checkPrevRenko = checkPrevRenko;
        this.closeFollowPrice = closeFollowPrice;
        this.roundPrice = roundPrice;
    }       
    
    public boolean isComplete(Renko renko){
        return renko.isGreenComplete(renko) || renko.isRedComplete(renko);
    }
    
    public boolean isGreenComplete(Renko renko){
        if (renko.isCheckPrevRenko() && renko.getPrevRenko()!=null) {
            return renko.getHigh() - renko.getPrevRenko().getHigh() >= renko.getHeight();
        } else {
            return renko.getHigh() - renko.getOpen() >= renko.getHeight();
        }
    }
    
    public boolean isRedComplete(Renko renko){
        if (renko.isCheckPrevRenko() && renko.getPrevRenko()!=null) {
            return renko.getPrevRenko().getLow() - renko.getLow() >= renko.getHeight();
        } else {
            return renko.getOpen() - renko.getLow() >= renko.getHeight();
        }
    }
    
    public void postProcess(Renko renko){
        //on trend change high-low difference is double the renko height - adjust it here
        if(renko.isGreenComplete(renko)){
            if (renko.isReverse2X()) renko.setLow(renko.getHigh() - renko.getHeight());
            renko.setClose(renko.getHigh());
        } else {
            if (renko.isReverse2X()) renko.setHigh(renko.getLow() + renko.getHeight());
            renko.setClose(renko.getLow());
        }
        
        //make "solid" bricks with prices rounded to the brick height
        if (renko.isRoundPostProcess()) {
            renko.setLow(getRoundedPrice(renko.getLow(), renko.getHeight()));
            renko.setHigh(getRoundedPrice(renko.getHigh(), renko.getHeight()));
            renko.setClose(getRoundedPrice(renko.getClose(), renko.getHeight()));
            renko.setOpen(getRoundedPrice(renko.getOpen(), renko.getHeight()));
        }
    }
    
    public double getRoundedPrice(double price, double height){
        //rounded to the closest pip value that is divisible with brickSize
        double delta1 = price % height;
        double delta2 = height - price % height;
        double priceRounded = delta1 <= delta2
                ? price - delta1
                : price + delta2;
        return priceRounded;
    }
    
    //CALCULATING RENKO
    public Renko calculateRenko(Renko renko, ITick tick, double price, long time) {
        switch(renko.getUsePrice()) {
            case ASK: {
                if (renko.getHigh() < tick.getAsk()) {
                    renko.setHigh(tick.getAsk());
                    if (renko.isRoundPrice()) renko.setHigh(renko.getRoundedPrice(renko.getHigh(), renko.getHeight())); 
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getHigh());
                }
                if (renko.getLow() > tick.getAsk()) {
                    renko.setLow(tick.getAsk());
                    if (renko.isRoundPrice()) renko.setLow(renko.getRoundedPrice(renko.getLow(), renko.getHeight()));
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getLow());
                }
            } break;
                
            case BID: {
                if (renko.getHigh() < tick.getBid()) {
                    renko.setHigh(tick.getBid());
                    if (renko.isRoundPrice()) renko.setHigh(renko.getRoundedPrice(renko.getHigh(), renko.getHeight())); 
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getHigh());
                }
                if (renko.getLow() > tick.getBid()) {
                    renko.setLow(tick.getBid());
                    if (renko.isRoundPrice()) renko.setLow(renko.getRoundedPrice(renko.getLow(), renko.getHeight()));
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getLow());
                }
            } break;
                
            case ASKBID: {
                if (renko.getHigh() < tick.getBid()) {
                    renko.setHigh(tick.getBid());
                    if (renko.isRoundPrice()) renko.setHigh(renko.getRoundedPrice(renko.getHigh(), renko.getHeight())); 
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getHigh());
                }
                if (renko.getLow() > tick.getAsk()) {
                    renko.setLow(tick.getAsk());
                    if (renko.isRoundPrice()) renko.setLow(renko.getRoundedPrice(renko.getLow(), renko.getHeight()));
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getLow());
                }
            } break;
                
            case ANY: {
                if (renko.getHigh() < price) {
                    renko.setHigh(price);
                    if (renko.isRoundPrice()) renko.setHigh(renko.getRoundedPrice(renko.getHigh(), renko.getHeight())); 
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getHigh());
                }
                if (renko.getLow() > price) {
                    renko.setLow(price);
                    if (renko.isRoundPrice()) renko.setLow(renko.getRoundedPrice(renko.getLow(), renko.getHeight()));
                    if (renko.isCloseFollowPrice()) renko.setClose(renko.getLow());
                }
            } break;
                
            default: break;
        }
        
        if (renko.isComplete(renko)) {
            renko.postProcess(renko);
            renko = renko.getNextRenko(renko, time);
            renko.getPrevRenko().setDrawRenko(true);
        }
        
        return renko;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUpBrickColor(Color upBrickColor) {
        this.upBrickColor = upBrickColor;
    }

    public void setDownBrickColor(Color downBrickColor) {
        this.downBrickColor = downBrickColor;
    }

    public void setReverse2X(boolean reverse2X) {
        this.reverse2X = reverse2X;
    }

    public void setPrevRenko(Renko prevRenko) {
        this.prevRenko = prevRenko;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setRoundPostProcess(boolean roundPostProcess) {
        this.roundPostProcess = roundPostProcess;
    }

    public void setUsePrice(Price usePrice) {
        this.usePrice = usePrice;
    }

    public void setCheckPrevRenko(boolean checkPrevRenko) {
        this.checkPrevRenko = checkPrevRenko;
    }

    public void setCloseFollowPrice(boolean closeFollowPrice) {
        this.closeFollowPrice = closeFollowPrice;
    }

    public void setRoundPrice(boolean roundPrice) {
        this.roundPrice = roundPrice;
    }

    public void setDrawRenko(boolean drawRenko) {
        this.drawRenko = drawRenko;
    }
    
    public Color getUpBrickColor() {
        return this.upBrickColor;
    }

    public Color getDownBrickColor() {
        return this.downBrickColor;
    }

    public boolean isReverse2X() {
        return this.reverse2X;
    }

    public Renko getPrevRenko() {
        return this.prevRenko;
    }

    public double getHeight() {
        return this.height;
    }

    public boolean isRoundPostProcess() {
        return this.roundPostProcess;
    }

    public Price getUsePrice() {
        return this.usePrice;
    }

    public boolean isCheckPrevRenko() {
        return this.checkPrevRenko;
    }

    public boolean isCloseFollowPrice() {
        return this.closeFollowPrice;
    }

    public boolean isRoundPrice() {
        return this.roundPrice;
    }

    public boolean isDrawRenko() {
        return drawRenko;
    }
    
    @Override
    public double getOpen() {
        return this.open;
    }

    @Override
    public double getClose() {
        return this.close;
    }

    @Override
    public double getLow() {
        return this.low;
    }

    @Override
    public double getHigh() {
        return this.high;
    }
    
    @Override
    public double getVolume() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public String toString() {            
        return String.format("O: %.5f C: %.5f H: %.5f L: %.5f",
                this.open, this.close, this.high, this.low);
    }
    }
    



}
    
   