package com.seveks.feedmill.DataBase;

public class Preset {
    private int presetId;
    private String presetName;

    public Preset(int presetId, String presetName){
        this.presetId = presetId;
        this.presetName = presetName;
    }

    public int getPresetId() {
        return presetId;
    }
    public String getPresetName() {
        return presetName;
    }
}
