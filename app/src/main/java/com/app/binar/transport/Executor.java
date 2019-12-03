package com.app.binar.transport;

import android.content.Context;
import android.os.AsyncTask;

import com.app.binar.transport.listener.TransportListener;

/**
 * @author AKBAR <akbar.attijani@gmail.com>
 */

public class Executor extends AsyncTask<Object, Object, String[]> {
    private Context context;
    private TransportListener listener;
    private int id = -1;
    private Transporter transporter;
    private boolean isSilent = false;
    private Object[] packet;

    public Executor(Context context, TransportListener listener, int id, Transporter transporter, Object... packet) {
        this.context = context;
        this.listener = listener;
        this.id = id;
        this.transporter = transporter;
        this.packet = packet;
    }

    @Override
    protected void onPreExecute() {
       new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try{
                    synchronized (this) {
                        wait(1000);
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected String[] doInBackground(Object... objects) {
        String[] result = new String[] {"504", "Problem Komunikasi dengan Server", "Problem Komunikasi dengan Server"};
        try {
            result = transporter.execute();
            if(result[2].equalsIgnoreCase("null") || result[2].equalsIgnoreCase("[]"))
                result[2] = "{\"errorCode\":\"IB-0002\"}";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String[] result) {
        String code = result[0];
        String message = result[1];
        String body = result[2];

        try {
            int c = Integer.parseInt(code);
            if (c >= 200 && c <= 300) {
                listener.onTransportDone(c, message, body, id, packet);
            } else {
                listener.onTransportFail(c, message, body, id, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onTransportFail(code, message, body, id, packet);
        }
    }
}
