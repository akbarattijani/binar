package com.app.binar.transport.listener;

/**
 * @author AKBAR <akbar.attijani@gmail.com>
 */
public interface TransportListener {
    void onTransportDone(Object code, Object message, Object body, int id, Object... packet);
    void onTransportFail(Object code, Object message, Object body, int id, Object... packet);
}
