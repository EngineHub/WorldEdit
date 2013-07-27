package com.sk89q.minecraft.util.commands;

public class SuggestionContext {

    private static final SuggestionContext FOR_LAST = new SuggestionContext(null, true);
    private static final SuggestionContext FOR_HANGING = new SuggestionContext(null, false);
    
    private final Character flag;
    private final boolean forLast;

    private SuggestionContext(Character flag, boolean forLast) {
        this.flag = flag;
        this.forLast = forLast; 
    }
    
    public boolean forHangingValue() {
        return flag == null && !forLast;
    }
    
    public boolean forLastValue() {
        return flag == null && forLast;
    }

    public boolean forFlag() {
        return flag != null;
    }

    public Character getFlag() {
        return flag;
    }
    
    @Override
    public String toString() {
        return forFlag() ? ("-" + getFlag()) : (forHangingValue() ? "hanging" : "last");
    }
    
    public static SuggestionContext flag(Character flag) {
        return new SuggestionContext(flag, false);
    }
    
    public static SuggestionContext lastValue() {
        return FOR_LAST;
    }
    
    public static SuggestionContext hangingValue() {
        return FOR_HANGING;
    }

}
