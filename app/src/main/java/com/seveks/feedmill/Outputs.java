package com.seveks.feedmill;

public class Outputs {
    private String outputDescription, inputDescription;
    private int number, inId, outId;
    private boolean outputState, inputState, isLoading;

    /*public Outputs(String outputDescription, String inputDescription, int number, boolean outputState, boolean inputState) {
        this.outputDescription = outputDescription;
        this.inputDescription = inputDescription;
        this.number = number;
        this.outputState = outputState;
        this.inputState = inputState;
    }*/

    public Outputs(int inId, int outId, String outputDescription, String inputDescription, int number, boolean outputState, boolean inputState) {
        this.inId = inId;
        this.outId = outId;
        this.outputDescription = outputDescription;
        this.inputDescription = inputDescription;
        this.number = number;
        this.outputState = outputState;
        this.inputState = inputState;
    }

    public int getInId() {
        return inId;
    }

    public int getOutId() {
        return outId;
    }

    public int getNumber() {
        return number;
    }

    public String getOutputDescription() {
        return outputDescription;
    }

    public void setInputDescription(String inputDescription) {
        this.inputDescription = inputDescription;
    }

    public String getInputDescription() {
        return inputDescription;
    }

    public void setOutputDescription(String outputDescription) {
        this.outputDescription = outputDescription;
    }

    public boolean getOutputState() {
        return outputState;
    }

    public void setOutputState(boolean outputState) {
        this.outputState = outputState;
    }

    public boolean getInputState() {
        return inputState;
    }

    public void setInputState(boolean inputState) {
        this.inputState = inputState;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}
