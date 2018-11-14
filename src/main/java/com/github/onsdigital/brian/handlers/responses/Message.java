package com.github.onsdigital.brian.handlers.responses;

/**
 * POJO used for simple JSON responses.
 */
public class Message {

    private String mesage;

    /**
     * Costruct a new Message
     *
     * @param mesage the value of the message field you wish to set.
     */
    public Message(String mesage) {
        this.mesage = mesage;
    }

    public String getMesage() {
        return this.mesage;
    }

    public void setMesage(String mesage) {
        this.mesage = mesage;
    }
}
