package com.seveks.feedmill;

public class Commands {

    public static final byte[] commandInfo = new byte[] {3,0,49};
    private static final byte[] commandToggle = new byte[] {5,0,50,0,0};
    public static final byte[] commandTurnOffAll = new byte[] {5,0,53,-1,-1};
    public static final byte[] commandTurnOnAll = new byte[] {5,0,54,-1,-1};

    public static byte[] getCommandToggle(byte outputNumber, boolean on){
        byte[] toggle = commandToggle;
        toggle[3] = outputNumber;
        toggle[4] = (byte)(on ? 0 : 1);
        return toggle;
    }

    public static byte[] getOutputStates(byte firstByte, byte secondByte){
        byte[] states = new byte[16];
        String firstEight = new StringBuilder(
                String.format("%8s", Integer.toBinaryString(firstByte & 0xFF)).replace(" ", "0"))
                .reverse().toString();
        String secondEight = new StringBuilder(
                String.format("%8s", Integer.toBinaryString(secondByte & 0xFF)).replace(" ", "0"))
                .reverse().toString();
        for (int i = 0; i < 8; i ++) {
            states[i] = Byte.parseByte(String.valueOf(firstEight.charAt(i)));
            states[i+8] = Byte.parseByte(String.valueOf(secondEight.charAt(i)));
        }
        return states;
    }

    public static byte[] getInputStates(byte firstByte, byte secondByte){
        byte[] states = new byte[16];
        String firstEight = new StringBuilder(
                String.format("%8s", Integer.toBinaryString(firstByte & 0xFF)).replace("1", "0").replace(" ", "1"))
                .reverse().toString();
        String secondEight = new StringBuilder(
                String.format("%8s", Integer.toBinaryString(secondByte & 0xFF)).replace("1", "0").replace(" ", "1"))
                .reverse().toString();
        for (int i = 0; i < 8; i ++) {
            states[i] = Byte.parseByte(String.valueOf(firstEight.charAt(i)));
            states[i+8] = Byte.parseByte(String.valueOf(secondEight.charAt(i)));
        }
        return states;
    }

    
}
