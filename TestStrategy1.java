/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package com.myStrategies;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import static com.dukascopy.api.Period.DAILY;
import static com.dukascopy.api.Period.FIFTEEN_MINS;
import static com.dukascopy.api.Period.FIVE_MINS;
import static com.dukascopy.api.Period.FOUR_HOURS;
import static com.dukascopy.api.Period.MONTHLY;
import static com.dukascopy.api.Period.ONE_HOUR;
import static com.dukascopy.api.Period.ONE_MIN;
import static com.dukascopy.api.Period.TEN_MINS;
import static com.dukascopy.api.Period.TEN_SECS;
import static com.dukascopy.api.Period.THIRTY_MINS;
import static com.dukascopy.api.Period.WEEKLY;
import org.neuroph.core.learning.SupervisedLearning;

 
/**
 *
 * @author Xitrix
 */
@RequiresFullAccess
@Library("neuroph-core-2.9.jar")
public class TestStrategy1 implements IStrategy {

    private IEngine engine = null;
    private IIndicators indicators = null;
    private IHistory history = null;
    private IContext context = null;
    private IDataService dataService = null;
    private int tagCounter = 0;
    private double[] cci1H = new double[Instrument.EURUSD.values().length];
    private double[] rsi1H = new double[Instrument.EURUSD.values().length];
    private double[] sma1H = new double[Instrument.EURUSD.values().length];
    private double[] sma11H = new double[Instrument.EURUSD.values().length];
    private double[] sma21H = new double[Instrument.EURUSD.values().length];
    private double[] sma31H = new double[Instrument.EURUSD.values().length];
    private double[][] fractal1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] bbands1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] stoch1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] emaEnv1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] ich1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] donch1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[] ema1H = new double[Instrument.EURUSD.values().length];
    private double[] ema11H = new double[Instrument.EURUSD.values().length];
    private double[][] aw1 = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] aw2 = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] aw3 = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] ac1 = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] ac2 = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[][] minMax1H = new double[Instrument.EURUSD.values().length][Instrument.EURUSD.values().length];
    private double[] zig1H = new double[Instrument.EURUSD.values().length];
    private static IConsole console;
    private boolean start = false;
    Calendar from = Calendar.getInstance();
    Calendar to = Calendar.getInstance();
    
    private OfferSide offerSide = OfferSide.BID;
    
    private Period[] periods = new Period[]{
             TEN_SECS, ONE_MIN, FIVE_MINS, TEN_MINS, FIFTEEN_MINS, THIRTY_MINS, ONE_HOUR, FOUR_HOURS, DAILY, WEEKLY, MONTHLY
    };
     
    NeuralNetwork BuySellNet;
    NeuralNetwork BuySellNet20;
    
    
    //Method prints message to console
    public static void println(String message) {
        console.getOut().println(message);
        //System.out.println(message);
    }
    
     //Method prints message to console
    public static void print(String message) {
        console.getOut().print(message);
        //System.out.print(message);
    }
    
    public void onStart(IContext context) throws JFException {
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.history = context.getHistory();
        this.dataService = context.getDataService();
        this.context = context;
        this.indicators = context.getIndicators();
        
        
        //this.userInterface = context.getUserInterface();
        
        
        
        //subscribe an instrument:
        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(Instrument.EURUSD);                    
        context.setSubscribedInstruments(instruments, true);
        console.getOut().println("Started");
        
       
        println("Hello World! Console OUT.");
       
    }

    
    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        
        if (!instrument.equals(Instrument.EURUSD)) return;
        
        //ITick tick = history.getLastTick(instrument);
        //IBar bar = offerSide.equals(offerSide.ASK) ? askBar : bidBar;
        
        if (!start) {
            start = true;

            from.setTimeInMillis((System.currentTimeMillis() -15724800000L));
            from.set(Calendar.MILLISECOND, 0);
            from.set(Calendar.SECOND, 0);
            from.set(Calendar.MINUTE, 0);
            from.set(Calendar.HOUR, 0);

            to.setTimeInMillis(history.getLastTick(instrument.EURUSD).getTime());
            //to.set(Calendar.MILLISECOND, 0);
            //to.set(Calendar.SECOND, 0);
            //to.set(Calendar.MINUTE, 0);

            
            List<IBar> bars1H  = history.getBars(instrument.EURUSD, ONE_HOUR, OfferSide.BID, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()), 0);

            
            //rsi1H = indicators.rsi(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 5, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            
            //rsi1H = indicators.willr(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 5, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);

            
            //fractal1H = indicators.fractal(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 10, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()), -10);

            zig1H = indicators.zigzag(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 2, 5, 15, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()), 0);
            //sma1H = indicators.sma(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 2, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //sma11H = indicators.sma(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 5, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //sma21H = indicators.sma(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 8, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //sma31H = indicators.sma(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 13, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            cci1H = indicators.cci(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 10, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            
            donch1H = indicators.donchian(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 12, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //minMax1H = indicators.minMax(instrument.EURUSD, ONE_HOUR, OfferSide.BID, AppliedPrice.CLOSE, 20, Filter.WEEKENDS,  7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //ich1H = indicators.ichimoku(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 1, 2, 20, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()), 0);
            //donch1H = indicators.macd(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 12, 26, 9, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //ema1H = indicators.ema(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 5, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //ema11H = indicators.ema(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.OPEN, 5, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            stoch1H = indicators.stoch(instrument.EURUSD, ONE_HOUR, OfferSide.BID, 14, 3, IIndicators.MaType.SMA, 3, IIndicators.MaType.SMA, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //bbands1H = indicators.bbands(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 10, 2, 2, IIndicators.MaType.EMA, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //ema1H = indicators.emaEnvelope(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 2, 0.50, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //aw1 = indicators.awesome(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 5, IIndicators.MaType.SMA, 34, IIndicators.MaType.SMA, Filter.WEEKENDS,  7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //aw2 = indicators.awesome(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 10, IIndicators.MaType.SMA, 34, IIndicators.MaType.SMA, Filter.WEEKENDS,  7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //aw3 = indicators.awesome(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 17, IIndicators.MaType.SMA, 34, IIndicators.MaType.SMA, Filter.WEEKENDS,  7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            
            //ac1 = indicators.ac(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, 5, 34, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //ac2 = indicators.ac(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, 10, 34, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            //ac3 = indicators.ac(instrument.EURUSD, ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, 17, 34, Filter.WEEKENDS, 7240, history.getBarStart(Period.ONE_HOUR, to.getTimeInMillis()),0);
            
            int last1Hbar = bars1H.size()-1; //Last Bar Data
            //int fractalLength = fractal1H[0].length-1;
            int zigZagLength = zig1H.length-1;

            /**
            for (int i = fractalLength; i > 1000; i--) {
                print(String.valueOf(fractal1[0][i]));
            }
            **/
            //for (int i = zigZagLength; i > zigZagLength - 50; i--) {
            //    print(String.valueOf(zig1H[i]));
            //}



            println("\nBars 1H Length: "+last1Hbar);
            println("Last 1H Price: "+bars1H.get(last1Hbar).getHigh());

            //println("Fractal Length: "+fractalLength);
            println("ZigZag Length: "+zigZagLength);
            int patternsCount = 0;
            
            //#################
            int inputCount = 6;
            //#################
            DataSet trainBuySellSet = new DataSet(inputCount, 2);
            BuySellNet = NeuralNetwork.createFromFile("E:\\Documents\\NetBeansProjects\\Neuroph project\\Neural Networks\\TestNet.nnet");
            
            DataSet trainBuySellSet20 = new DataSet(inputCount, 2);
            BuySellNet20 = NeuralNetwork.createFromFile("E:\\Documents\\NetBeansProjects\\Neuroph project\\Neural Networks\\TestNet.nnet");
            
            
            while (Double.isNaN(zig1H[zigZagLength])) {
                    zigZagLength--;
            }
            zigZagLength--;
            
            while (last1Hbar > 100) { 
                int pos1 = 0;
                int pos2 = 0;
                int pos3 = 0;
                int pos4 = 0;
                int pos5 = 0;
                double rsiDv1 = 0;
                double rsiDv2 = 0;
                double cciDv1 = 0;
                double cciDv2 = 0;
                double donchDv1 = 0;
                double donchDv2 = 0;
                double donchDv3 = 0;
                double donchDv4 = 0;
                double donchDv5 = 0;
                double donchDv6 = 0;
                double donchDv7 = 0;
                double donchDv8 = 0;
                double donchDv9 = 0;
                double minMaxDv1 = 0;
                double minMaxDv2 = 0;
                double minMaxDv3 = 0;
                double minMaxDv4 = 0;
                double minMaxDv5 = 0;
                double minMaxDv6 = 0;
                double minMaxDv7 = 0;
                double minMaxDv8 = 0;
                double minMaxDv9 = 0;
                double stoch1 = 0;
                double stoch2 = 0;
                double stoch3 = 0;
                double stochDv1 = 0;
                double stochDv2 = 0;
                double stochDv3 = 0;
                double emaDv1 = 0;
                double emaDv2 = 0;
                double emaDv10 = 0;
                double emaDv20 = 0;
                double emaDv12 = 0;
                double emaDv22 = 0;
                double emaDv13 = 0;
                double emaDv23 = 0;
                double sma1Dv = 0;
                double sma11Dv = 0;
                double sma21Dv = 0;
                double sma31Dv = 0;
                double sma1_1 = 0;
                double sma1_2 = 0;
                double sma1_3 = 0;
                double ichDv1 = 0;
                double ichDv2 = 0;
                double ichDv3 = 0;
                double ichDv4 = 0;
                double ichDv5 = 0;
                double ichDv6 = 0;
                double pipsDv1 = 0;
                double pipsDv2 = 0;
                double pipsDv3 = 0;
                double lenDv1 = 0;
                double lenDv2 = 0;
                double lenDV3 = 0;
                
                
                
                
                int pipsRes = 0;
                double pipsLine1 = 0;
                double pipsLine2 = 0;
                
                double[] trainBuySellInp = new double [inputCount];
                double[] trainBuySellOut = new double[2];
                
                double[] trainBuySellInp20 = new double [inputCount];
                double[] trainBuySellOut20 = new double[2];


                
                while (Double.isNaN(zig1H[zigZagLength])) {
                    zigZagLength--;
                }
                pos1 = zigZagLength;
                zigZagLength--;
                
                while (Double.isNaN(zig1H[zigZagLength])) {
                    zigZagLength--;
                }
                pos2 = zigZagLength;
                //zigZagLength--;
                /**
                while (Double.isNaN(zig1H[zigZagLength])) {
                    zigZagLength--;
                }
                pos3 = zigZagLength;
                zigZagLength--;
                while (Double.isNaN(zig1H[zigZagLength])) {
                    zigZagLength--;
                }
                pos4 = zigZagLength;
                zigZagLength--;
                 while (Double.isNaN(zig1H[zigZagLength])) {
                    zigZagLength--;
                }
                pos5 = zigZagLength;
                zigZagLength--;
                * */
                
                
                //println("pos1 = "+pos1);
                //println("pos2 = "+pos2);
                //println("pos3 = "+pos3);
                //println("pos4 = "+pos4);
                
                pipsRes = Double.valueOf((zig1H[pos1] - zig1H[pos2])*10000).intValue();
                
                pos2+=2;
                
                stochDv1 = (double)(Double.valueOf(stoch1H[0][pos2] - stoch1H[1][pos2]).intValue())/1000;
                stochDv2 = (double)(Double.valueOf(stoch1H[0][pos2-1] - stoch1H[1][pos2-1]).intValue())/1000;
                //rsiDv1 = (double)(Double.valueOf(rsi1H[pos2] - rsi1H[pos3]).intValue())/1000;
                //rsiDv2 = (double)(Double.valueOf(rsi1H[pos3] - rsi1H[pos4]).intValue())/1000;
                //pipsDv1 = (double)Double.valueOf((bars1H.get(pos2).getOpen() - bars1H.get(pos3).getOpen())*10000/(pos2-pos3)).intValue()/100;
                //pipsDv2 = (double)Double.valueOf((bars1H.get(pos3).getOpen() - bars1H.get(pos4).getOpen())*10000/(pos3-pos4)).intValue()/100;
                //pipsDv3 = (double)Double.valueOf((bars1H.get(pos4).getOpen() - bars1H.get(pos5).getOpen())*10000/(pos4-pos5)).intValue()/100;
                donchDv1 = (double)(Double.valueOf((donch1H[0][pos2] - donch1H[0][pos2-1])*10000).intValue())/1000;
                donchDv2 = (double)(Double.valueOf((donch1H[1][pos2] - donch1H[1][pos2-1])*10000).intValue())/1000;
                donchDv3 = (double)(Double.valueOf((donch1H[0][pos2-1] - donch1H[0][pos2-2])*10000).intValue())/1000;
                donchDv4 = (double)(Double.valueOf((donch1H[1][pos2-1] - donch1H[1][pos2-2])*10000).intValue())/1000;
                //minMaxDv1 = (double)(Double.valueOf((minMax1H[0][pos2] - donch1H[0][pos2])*10000).intValue())/1000;
                //minMaxDv2 = (double)(Double.valueOf((minMax1H[1][pos2] - donch1H[1][pos2])*10000).intValue())/1000;
                //minMaxDv3 = (double)(Double.valueOf((minMax1H[0][pos2] - bars1H.get(pos2).getHigh())*10000).intValue())/1000;
                //minMaxDv4 = (double)(Double.valueOf((minMax1H[1][pos2] - bars1H.get(pos2).getLow())*10000).intValue())/1000;
                //minMaxDv5 = (double)(Double.valueOf((minMax1H[0][pos3] - bars1H.get(pos3).getHigh())*10000).intValue())/1000;
                //minMaxDv6 = (double)(Double.valueOf((minMax1H[1][pos3] - bars1H.get(pos3).getLow())*10000).intValue())/1000;
                
                trainBuySellInp[0] = stochDv1;
                trainBuySellInp[1] = stochDv2;
                trainBuySellInp[2] = donchDv1;
                trainBuySellInp[3] = donchDv2;
                trainBuySellInp[4] = donchDv3;
                trainBuySellInp[5] = donchDv4;
                //trainBuySellInp[6] = minMaxDv1;
                //trainBuySellInp[7] = minMaxDv2;
                //trainBuySellInp[8] = minMaxDv3;
                //trainBuySellInp[9] = minMaxDv4;
                //trainBuySellInp[10] = minMaxDv5;
                //trainBuySellInp[11] = minMaxDv6;


                trainBuySellInp20[0] = stochDv1;
                trainBuySellInp20[1] = stochDv2;
                trainBuySellInp20[2] = donchDv1;
                trainBuySellInp20[3] = donchDv2;
                trainBuySellInp20[4] = donchDv3;
                trainBuySellInp20[5] = donchDv4;
                //trainBuySellInp20[6] = minMaxDv1;
                //trainBuySellInp20[7] = minMaxDv2;
                //trainBuySellInp20[8] = minMaxDv3;
                //trainBuySellInp20[9] = minMaxDv4;
                //trainBuySellInp20[10] = minMaxDv5;
                //trainBuySellInp20[11] = minMaxDv6;
                
                
                if (pipsRes > 0) { //########## BUY PATTERN
                
                    trainBuySellOut[0] = 1;
                    trainBuySellOut20[0] = 1;
                    
                } else if (pipsRes < 0) { //####### SELL PATTERN
                    
                    trainBuySellOut[1] = 1;
                    trainBuySellOut20[1] = 1;
                     
                }
                
                if (patternsCount < 350 ) { //&& (pipsRes >= 60 || pipsRes <= -60)
                    trainBuySellSet.addRow(trainBuySellInp, trainBuySellOut);
                }
                if (pipsRes!=0) {
                    trainBuySellSet20.addRow(trainBuySellInp20, trainBuySellOut20);
                    patternsCount++;
                }
                
                pos2-=2;
                
                zigZagLength = pos2;
                last1Hbar = pos2;
                
                
                



            }
            println("#######################################  LEARN PATTERNS = "+patternsCount + " #######################################");
            
            SupervisedLearning learningRule = (SupervisedLearning)BuySellNet.getLearningRule();
            //KohonenLearning learningRule = (KohonenLearning)BuySellNet.getLearningRule();
            learningRule.setLearningRate(0.1);
            //learningRule.setMaxError(0.01);
            //learningRule.setIterations(0, 1000);
            learningRule.setMaxIterations(1000);
            BuySellNet.learn(trainBuySellSet, learningRule);

            //testNeuralNetwork(BuySellNet, trainBuySellSet);
            testNeuralNetwork20(BuySellNet, trainBuySellSet20);
            
            
            
            
            int pos2 = 0;
            int pos3 = 0;
            int pos4 = 0;
            int pos5 = 0;
            double rsiDv1 = 0;
            double rsiDv2 = 0;
            double cciDv1 = 0;
            double cciDv2 = 0;
            double donchDv1 = 0;
            double donchDv2 = 0;
            double donchDv3 = 0;
            double donchDv4 = 0;
            double donchDv5 = 0;
            double donchDv6 = 0;
            double donchDv7 = 0;
            double donchDv8 = 0;
            double donchDv9 = 0;
            double minMaxDv1 = 0;
            double minMaxDv2 = 0;
            double minMaxDv3 = 0;
            double minMaxDv4 = 0;
            double minMaxDv5 = 0;
            double minMaxDv6 = 0;
            double minMaxDv7 = 0;
            double minMaxDv8 = 0;
            double minMaxDv9 = 0;
            double stoch1 = 0;
            double stoch2 = 0;
            double stoch3 = 0;
            double stochDv1 = 0;
            double stochDv2 = 0;
            double stochDv3 = 0;
            double emaDv1 = 0;
            double emaDv2 = 0;
            double emaDv10 = 0;
            double emaDv20 = 0;
            double emaDv12 = 0;
            double emaDv22 = 0;
            double emaDv13 = 0;
            double emaDv23 = 0;
            double sma1Dv = 0;
            double sma11Dv = 0;
            double sma21Dv = 0;
            double sma31Dv = 0;
            double sma1_1 = 0;
            double sma1_2 = 0;
            double sma1_3 = 0;
            double ichDv1 = 0;
            double ichDv2 = 0;
            double ichDv3 = 0;
            double ichDv4 = 0;
            double ichDv5 = 0;
            double ichDv6 = 0;
            double pipsDv1 = 0;
            double pipsDv2 = 0;
            double pipsDv3 = 0;
            double lenDv1 = 0;
            double lenDv2 = 0;
            double lenDV3 = 0;
            
            
            double pipsLine1 = 0;
            double pipsLine2 = 0;
            
            trainBuySellSet = new DataSet(inputCount, 2);
            double[] trainBuySellInp = new double [inputCount];
            double[] trainBuySellOut = new double [2];

            
            last1Hbar = bars1H.size()-1; //Last Bar Data
            //fractalLength = fractal1H[0].length-1;
            zigZagLength = zig1H.length-1;
            
            //Searching Input pattern
            
            pos2 = last1Hbar;
            
            /*
            while (Double.isNaN(zig1H[zigZagLength])) {
                zigZagLength--;
            }
            zigZagLength--;
            
            while (Double.isNaN(zig1H[zigZagLength])) {
                zigZagLength--;
            }
            pos3 = zigZagLength;
            zigZagLength--;
            while (Double.isNaN(zig1H[zigZagLength])) {
                zigZagLength--;
            }
            pos4 = zigZagLength;
            zigZagLength--;
            while (Double.isNaN(zig1H[zigZagLength])) {
                zigZagLength--;
            }
            pos5 = zigZagLength;
            zigZagLength--;
            **/
            
            //println("pos2 = "+pos2);
            //println("pos3 = "+pos3);
            //println("pos4 = "+pos4);


                stochDv1 = (double)(Double.valueOf(stoch1H[0][pos2-1] - stoch1H[1][pos2-1]).intValue())/1000;
                stochDv2 = (double)(Double.valueOf(stoch1H[0][pos2-2] - stoch1H[1][pos2-2]).intValue())/1000;
                //rsiDv1 = (double)(Double.valueOf(rsi1H[pos2] - rsi1H[pos3]).intValue())/1000;
                //rsiDv2 = (double)(Double.valueOf(rsi1H[pos3] - rsi1H[pos4]).intValue())/1000;
                //pipsDv1 = (double)Double.valueOf((bars1H.get(pos2).getOpen() - bars1H.get(pos3).getOpen())*10000/(pos2-pos3)).intValue()/100;
                //pipsDv2 = (double)Double.valueOf((bars1H.get(pos3).getOpen() - bars1H.get(pos4).getOpen())*10000/(pos3-pos4)).intValue()/100;
                //pipsDv3 = (double)Double.valueOf((bars1H.get(pos4).getOpen() - bars1H.get(pos5).getOpen())*10000/(pos4-pos5)).intValue()/100;
                donchDv1 = (double)(Double.valueOf((donch1H[0][pos2-1] - donch1H[0][pos2-2])*10000).intValue())/1000;
                donchDv2 = (double)(Double.valueOf((donch1H[1][pos2-1] - donch1H[1][pos2-2])*10000).intValue())/1000;
                donchDv3 = (double)(Double.valueOf((donch1H[0][pos2-2] - donch1H[0][pos2-3])*10000).intValue())/1000;
                donchDv4 = (double)(Double.valueOf((donch1H[1][pos2-2] - donch1H[1][pos2-3])*10000).intValue())/1000;
                //minMaxDv1 = (double)(Double.valueOf((minMax1H[0][pos2] - donch1H[0][pos2])*10000).intValue())/1000;
                //minMaxDv2 = (double)(Double.valueOf((minMax1H[1][pos2] - donch1H[1][pos2])*10000).intValue())/1000;
                //minMaxDv3 = (double)(Double.valueOf((minMax1H[0][pos2] - bars1H.get(pos2).getHigh())*10000).intValue())/1000;
                //minMaxDv4 = (double)(Double.valueOf((minMax1H[1][pos2] - bars1H.get(pos2).getLow())*10000).intValue())/1000;
                //minMaxDv5 = (double)(Double.valueOf((minMax1H[0][pos3] - bars1H.get(pos3).getHigh())*10000).intValue())/1000;
                //minMaxDv6 = (double)(Double.valueOf((minMax1H[1][pos3] - bars1H.get(pos3).getLow())*10000).intValue())/1000;


            trainBuySellInp[0] = stochDv1;
            trainBuySellInp[1] = stochDv2;
            trainBuySellInp[2] = donchDv1;
            trainBuySellInp[3] = donchDv2;
            trainBuySellInp[4] = donchDv3;
            trainBuySellInp[5] = donchDv4;
            //trainBuySellInp[6] = minMaxDv1;
            //trainBuySellInp[7] = minMaxDv2;
            //trainBuySellInp[8] = minMaxDv3;
            //trainBuySellInp[9] = minMaxDv4;
            //trainBuySellInp[10] = minMaxDv5;
            //trainBuySellInp[11] = minMaxDv6;
            
            
           
            
            trainBuySellSet.addRow(trainBuySellInp, trainBuySellOut);
            BuySellNet.setInput(trainBuySellSet.getRows().get(0).getInput());
            BuySellNet.calculate();
            double[] out = BuySellNet.getOutput();
            
            println(" \nInput: " + Arrays.toString(trainBuySellSet.getRows().get(0).getInput())+ " \nOut: "+ Arrays.toString(out) +"\n");
            
            
            if (out[0] > 0.7) {
                for (IOrder order : engine.getOrders(instrument)) {
                    if (order.getState() == IOrder.State.FILLED && !order.isLong()) {
                        order.close();
                    }
                }
                if (positionsTotal(instrument.EURUSD) == 0) {
                    println("BUY 20");
                    engine.submitOrder(getLabel(instrument.EURUSD), instrument.EURUSD, IEngine.OrderCommand.BUY, 0.01, 0, 0, tick.getBid()
                        - instrument.EURUSD.getPipValue() * 80, tick.getBid() + instrument.EURUSD.getPipValue() * 160);
                }
            } else if (out[1] > 0.7) {
                for (IOrder order : engine.getOrders(instrument)) {
                    if (order.getState() == IOrder.State.FILLED && order.isLong()) {
                        order.close();
                    }
                }
                if (positionsTotal(instrument.EURUSD) == 0) {
                    println("SELL 20");
                    engine.submitOrder(getLabel(instrument.EURUSD), instrument.EURUSD, IEngine.OrderCommand.SELL, 0.01, 0, 0, tick.getAsk()
                        + instrument.EURUSD.getPipValue() * 80, tick.getAsk() - instrument.EURUSD.getPipValue() * 160);
                }
            }
            
            
            
            
            start = false;
        }
       
        
       
        
    }

    public static void testNeuralNetwork(NeuralNetwork nnet, DataSet testSet) {
         int error =0;
         int cnt =0;
        for(DataSetRow dataRow : testSet.getRows()) {
            cnt++;
            nnet.setInput(dataRow.getInput());
            nnet.calculate();
            Neuron[] neurons = nnet.getOutputNeurons();
            double[] out = nnet.getOutput();
            
            print(" \nInput: " + Arrays.toString(dataRow.getInput())+ " \nOut: "+ Arrays.toString(dataRow.getDesiredOutput()));
            //println("\nNeuron Output: " + Arrays.toString(out)); 
            if (out[0] > 0.7) {
                //println("BUY ");
            }
            if (out[1] > 0.7) {
                //println("SELL ");
            }
            if (dataRow.getDesiredOutput()[0] == 1 && (neurons[0].getOutput() < 0.7 || neurons[1].getOutput() > 0.7)) {
                error++;
                println("\nERROR BUY Neuron Output: " + Arrays.toString(out));
            } 
            if (dataRow.getDesiredOutput()[1] == 1 && (neurons[1].getOutput() < 0.7 || neurons[0].getOutput() > 0.7)) {
                error++;
                println("\nERROR SELL Neuron Output: " + Arrays.toString(out));
            }
        }
        println(" BuySell Input count: " + cnt+ " Errors: "+error);
    }
    
    public static void testNeuralNetwork20(NeuralNetwork nnet, DataSet testSet) {
         int error =0;
         int cnt =0;
        for(DataSetRow dataRow : testSet.getRows()) {
            cnt++;
            nnet.setInput(dataRow.getInput());
            nnet.calculate();
            Neuron[] neurons = nnet.getOutputNeurons();
            double[] out = nnet.getOutput();
            
            //print(" \nInput: " + Arrays.toString(dataRow.getInput())+ " \nOut: "+ Arrays.toString(dataRow.getDesiredOutput()));
            //println("\nNeuron Output: " + Arrays.toString(out)); 
            if (out[0] > 0.7) {
                //println("BUY 20");
            }
            if (out[1] > 0.7) {
                //println("SELL 20");
            }
            if (dataRow.getDesiredOutput()[0] == 1 && (neurons[0].getOutput() < 0.7 || neurons[1].getOutput() > 0.7)) {
                error++;
                println("\nERROR BUY 20 Neuron Output: " + Arrays.toString(out));
            } 
            if (dataRow.getDesiredOutput()[1] == 1 && (neurons[1].getOutput() < 0.7 || neurons[0].getOutput() > 0.7)) {
                error++;
                println("\nERROR SELL 20 Neuron Output: " + Arrays.toString(out));
            }
        }
        println(" \n BuySell( 20 pips ) Input count: " + cnt+ " Errors: "+error);
    }
     

     
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        
        
    }


    public void onMessage(IMessage message) throws JFException {
        
    }


    public void onAccount(IAccount account) throws JFException {
       
    }


    public void onStop() throws JFException {
        
    }
    
    
     //count open positions
    protected int positionsTotal(Instrument instrument) throws JFException {
        int counter = 0;
        for (IOrder order : engine.getOrders(instrument)) {
            if (order.getState() == IOrder.State.FILLED) {
                counter++;
            }
        }
        return counter;
    }
    
    protected String getLabel(Instrument instrument) {
        String label = instrument.name();
        label = label.substring(0, 2) + label.substring(3, 5);
        label = label + (tagCounter++);
        label = label.toLowerCase();
        return label;
    }
    
}
