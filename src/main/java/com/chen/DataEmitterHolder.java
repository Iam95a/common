package com.chen;

/**
 * @author goldgreat
 * @Date 2021-12-02
 */
public class DataEmitterHolder {
    private DataEmitter dataEmitter;

    public void register(DataEmitter de) {
        this.dataEmitter = de;
    }

    public void dataEmit(String value) {
        if (dataEmitter == null) {
            return;
        } else {
            dataEmitter.receiveData(value);
        }
    }


    public void complete() {
        dataEmitter.complete();
    }
}
