package com.myStrategies;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder.State;
import com.dukascopy.api.indicators.IIndicator;

public class DailyMartinGale implements IStrategy {
    private IEngine engine;
    private IConsole console;
    private IContext context;
    private IAccount account;
    private IHistory history;
    private IIndicators indicators;
    private SimpleDateFormat sdf;

    private ArrayList<String> orderLabelList    = new ArrayList<String>();

    @Configurable("Currency")
    public Instrument currencyInstrument = Instrument.EURUSD;
    @Configurable("Amount")
    public double amount = 0.2;
    @Configurable("Take profit")
    public int takeProfit    = 10;
    @Configurable("Stop Loss")
    public int stopLoss = 5;
    @Configurable("Multiplier")
    public int multiplier = 2;
    @Configurable("Trade start hour GMT")
    public String startTime = "10:00";
    @Configurable("Trade close hour GMT")
    public String endTime = "22:55";
    
    private double amountDelta = 0;
    private int counter = 0;
    String label;

    private double takeProfitPrice;
    private double stopLossPrice;
    private String parsedStartTime;
    private String parsedEndTime;
    
    
    
    enum SRLevels {
        FIRST(0),
        SECOND(1),
        THIRD(2)
        ;
        
        public final int index;

        private SRLevels(int index) {
            this.index = index;
        }
    }
    
    
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
    }

    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.context = context;
        history = this.context.getHistory();
        indicators = this.context.getIndicators();
        amountDelta = amount;
        sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        parsedStartTime = startTime.replace(":", "");
        parsedEndTime = endTime.replace(":", "");
    }

    public void onAccount(IAccount account) throws JFException {
        this.account = account;
    }

    public void onMessage(IMessage message) throws JFException {
    }

    public void onStop() throws JFException {
        print(" Stop stragegy");
        for (IOrder order : engine.getOrders()) {
            order.close();
        }
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (instrument.equals(currencyInstrument) && isValidTime(tick.getTime())) {
            double[][] fastDouble = new double[1][120];
            double[][] slowDouble = new double[1][120];
            double[][] highLevels = new double[1][3];
            double[][] lowLevels = new double[1][3];
            
            fastDouble[0] = calcMA(currencyInstrument);
            slowDouble[0] = calcMA(currencyInstrument);
            
            calcSRLevels(fastDouble, slowDouble, highLevels, lowLevels);
            
            monitorOrder(instrument, tick,  highLevels, lowLevels);
        }
    }

    private void monitorOrder(Instrument instrument, ITick tick, double[][] highLevels, double[][] lowLevels) throws JFException { 
        double useOfLeverage = account.getUseOfLeverage();
        //if ((useOfLeverage < 100) ) {
            // Create first order
            if (orderLabelList.size() < 1) {
                createFirstOrders(tick, instrument, amountDelta , highLevels, lowLevels);
            }
            recreateOrderIfClosed(instrument, tick);
        //} else {
        //    print(" Maximum use of Leverage exceed " + useOfLeverage + " strategy stopping");
            //context.stop();
        //}
    }

    private void recreateOrderIfClosed(Instrument instrument, ITick tick) throws JFException {
        double currentBidPrice = tick.getBid();
        double currentAskPrice = tick.getAsk();
        IOrder prevOrder = engine.getOrder(orderLabelList.get(0));
        
        // If any order was created during strategy run
        if (prevOrder != null) {
            if (prevOrder.isLong()) {
                recreateOrderAfterBuy(instrument, tick, currentAskPrice, prevOrder);
            } else {
                recreateOrderAfterSell(instrument, tick, currentBidPrice, prevOrder);
            }
        }
    }

    private void recreateOrderAfterSell(Instrument instrument, ITick tick, double currentPrice, IOrder order) throws JFException {
        

        if ((currentPrice < takeProfitPrice)) {
            takeProfitPrice = tick.getAsk() - (instrument.getPipValue() * takeProfit);
            stopLossPrice = tick.getAsk() + (instrument.getPipValue() * stopLoss);
            print(String.valueOf(currentPrice > takeProfit));
            print( "Current price: " + currentPrice + "; Take profit: " + takeProfitPrice + " is triggered, next order amount(default): " + amount);
            deleteOrderFromList(instrument, tick, order.getLabel());
            amountDelta = amount;
            createOrders(tick, instrument, amountDelta, IEngine.OrderCommand.SELL);
        } else if (currentPrice >= stopLossPrice) {
            takeProfitPrice = tick.getBid() + (instrument.getPipValue() * takeProfit);
            stopLossPrice = tick.getBid() - (instrument.getPipValue() * stopLoss);
            print( "Amount " + amountDelta + " multiplier: " + multiplier);
            print("Current price: " + currentPrice + " ;Stop Loss: " + stopLossPrice + " is triggered, next order amount: " + amountDelta);
            deleteOrderFromList(instrument, tick, order.getLabel());
            amountDelta *= multiplier;
            createOrders(tick, instrument, amountDelta, IEngine.OrderCommand.BUY);
        }
    }

    private void recreateOrderAfterBuy(Instrument instrument, ITick tick, double currentPrice, IOrder order) throws JFException {
        double currentBidPrice = tick.getBid();
        double currentAskPrice = tick.getAsk();

        if ((currentPrice > takeProfitPrice)) {
            takeProfitPrice = tick.getBid() + (instrument.getPipValue() * takeProfit);
            stopLossPrice = tick.getBid() - (instrument.getPipValue() * stopLoss);
            deleteOrderFromList(instrument, tick, order.getLabel());
            amountDelta = amount;
            print( "Current price: " + currentPrice + "; Take profit: " + takeProfitPrice + " is triggered, next order amount(default): " + amountDelta);
            createOrders(tick, instrument, amountDelta, IEngine.OrderCommand.BUY);
        } else if (currentPrice <= stopLossPrice) {
            takeProfitPrice = tick.getAsk() - (instrument.getPipValue() * takeProfit);
            stopLossPrice = tick.getAsk() + (instrument.getPipValue() * stopLoss);
            deleteOrderFromList(instrument, tick, order.getLabel());
            amountDelta *= multiplier;
            print("Current price: " + currentPrice + " ;Stop Loss: " + stopLossPrice + " is triggered, next order amount: " + amountDelta);
            createOrders(tick, instrument, amountDelta, IEngine.OrderCommand.SELL);
        }
    }

    private void createFirstOrders(ITick tick, Instrument instrument, double orderAmount , double[][] highLevels, double[][] lowLevels) throws JFException {
        
        if (history.getBar(instrument, Period.ONE_HOUR, OfferSide.BID, 0).getLow() < lowLevels[0][0]) {
            createOrders(tick, instrument, orderAmount, IEngine.OrderCommand.SELL);
        } else if (history.getBar(instrument, Period.ONE_HOUR, OfferSide.BID, 0).getHigh() > highLevels[0][0]) {
            createOrders(tick, instrument, orderAmount, IEngine.OrderCommand.BUY);
        }
    }

    private void deleteOrderFromList(Instrument instrument, ITick tick, String orderLabel) throws JFException {
        IOrder order  = engine.getOrder(orderLabel);
        order.close();
        orderLabelList.remove(orderLabel);

        
    }
    
    private void createOrders(ITick tick, Instrument instrument, double orderAmount, OrderCommand orderCommand) throws JFException {
        String label = getLabel(instrument);
        if (orderCommand ==OrderCommand.SELL) {
            engine.submitOrder(label, instrument, OrderCommand.SELL, orderAmount, 0, 20, stopLossPrice, takeProfitPrice);
        } else {
            engine.submitOrder(label, instrument, OrderCommand.BUY, orderAmount, 0, 20, stopLossPrice, takeProfitPrice);
        }
        print("Created order " + label + " price " + tick.getBid() + " amount: " + orderAmount);
        orderLabelList.add(label);
        
        
    }

    protected String getLabel(Instrument instrument) {
        String label = instrument.name();
        label = label + (counter ++);
        label = label.toUpperCase();
        return label;
    }
    
    
     private double[] calcMA(Instrument instrument) throws JFException {

        return indicators.supportResistance(instrument, Period.ONE_HOUR, OfferSide.BID, Filter.WEEKENDS, 120, history.getTimeForNBarsBack(Period.ONE_HOUR, history.getLastTick(instrument).getTime(), 3), 0)[0];

    }
     
         private void calcSRLevels(double[][] fastDouble, double[][] slowDouble, double[][] highLevels, double[][] lowLevels) {
            int highCount = 0;
            for (int i = fastDouble[0].length-1; i >=0; i--) {
                if (!Double.isNaN(fastDouble[0][i]) && fastDouble[0][i] != 0) {
                     
                    if (highLevels[0][SRLevels.FIRST.index] == 0 && highCount == SRLevels.FIRST.index) {
                        highLevels[0][SRLevels.FIRST.index] = fastDouble[0][i];
                        highCount ++;
                    } else if (highLevels[0][SRLevels.FIRST.index]!= fastDouble[0][i] && highLevels[0][SRLevels.SECOND.index] == 0 && highCount ==SRLevels.SECOND.index) {
                        highLevels[0][SRLevels.SECOND.index] = fastDouble[0][i];
                        highCount ++; 
                    } else if (highLevels[0][SRLevels.SECOND.index]!= fastDouble[0][i] && highLevels[0][SRLevels.THIRD.index] == 0 && highCount ==SRLevels.THIRD.index) {
                        highLevels[0][SRLevels.THIRD.index] = fastDouble[0][i];
                        highCount ++;
                    }  
                }
                if (highCount == SRLevels.values().length) break;
            }
            int lowCount = 0;
            for (int i = slowDouble[0].length-1; i >=0; i--) {
                if (!Double.isNaN(slowDouble[0][i]) && slowDouble[0][i] !=0) {
            
                    if (lowLevels[0][SRLevels.FIRST.index] == 0 && lowCount == SRLevels.FIRST.index) {
                        lowLevels[0][SRLevels.FIRST.index] = slowDouble[0][i];
                        lowCount ++;
                    } else if (lowLevels[0][SRLevels.FIRST.index]!= slowDouble[0][i] && lowLevels[0][SRLevels.SECOND.index] == 0 && lowCount ==SRLevels.SECOND.index) {
                        lowLevels[0][SRLevels.SECOND.index] = slowDouble[0][i];
                        lowCount ++; 
                    }  else if (lowLevels[0][SRLevels.SECOND.index]!= slowDouble[0][i] && lowLevels[0][SRLevels.THIRD.index] == 0 && lowCount ==SRLevels.THIRD.index) {
                        lowLevels[0][SRLevels.THIRD.index] = slowDouble[0][i];
                        lowCount ++;
                    }
                }
                if (lowCount == SRLevels.values().length) break;
            } 
    }
    
    /**
     * 
     * @param tickTime market tick time in milliseconds
     * @return
     */
    private boolean isValidTime(long tickTime) {
        
        String formattedTickTime = sdf.format(tickTime); 
        formattedTickTime = formattedTickTime.replace(":", "");
        
        int tickTimeValue = Integer.parseInt(formattedTickTime);
        int startTimeValue = Integer.parseInt(parsedStartTime);
        int endTimeValue = Integer.parseInt(parsedEndTime);
        
        if (startTimeValue < endTimeValue){
            if ((tickTimeValue > startTimeValue) && (tickTimeValue < endTimeValue)){
                return true;
            }
        // Else swap time range and calculate valid time 
        } else {
            int tmpTimeValue = startTimeValue;
            startTimeValue = endTimeValue;
            endTimeValue = tmpTimeValue;
            if ((tickTimeValue < startTimeValue) || (tickTimeValue >= endTimeValue)){
                return true;
            }
        }
        return false;
    }
    private void print(String message) {
        console.getOut().println(message);
    }
}
