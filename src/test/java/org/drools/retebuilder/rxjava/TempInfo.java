package org.drools.retebuilder.rxjava;

import java.util.Random;

public class TempInfo {

    public static final Random random = new Random();

    private final String town;
    private final int temp;

    public TempInfo(String town, int temp) {
        this.town = town;
        this.temp = temp;
    }

    public static TempInfo fetch(String town) {
        return new TempInfo(town, random.nextInt(70) - 20);
    }

    @Override
    public String toString() {
        return String.format(town + " : " + temp);
    }

    public int getTemp() {
        return temp;
    }

    public String getTown() {
        return town;
    }
}