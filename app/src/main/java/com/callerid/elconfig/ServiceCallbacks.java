package com.callerid.elconfig;

// Simple interface to allow for possible callbacks between service and activity.
public interface ServiceCallbacks{
    void display(String rString, byte[] rArray);
}
