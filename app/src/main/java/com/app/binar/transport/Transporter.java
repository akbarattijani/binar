package com.app.binar.transport;

import android.content.Context;
import android.util.Log;

import com.app.binar.transport.body.BodyBuilder;
import com.app.binar.transport.body.Multipart;
import com.app.binar.transport.listener.TransportListener;
import com.app.binar.transport.model.MediaType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author AKBAR <akbar.attijani@gmail.com>
 */

public class Transporter {

    private Context context;
    private TransportListener listener;
    private int id = -1;
    private String url = "";
    private String port = "";
    private String route = "";
    private BodyBuilder body;
    private Map<String, String> headers;
    private String method = "";
    private Object[] packet;
    private final String LINE_FEED = "\r\n";

    public Transporter() {
        headers = new HashMap<>();
    }

    public Transporter context(Context context) {
        this.context = context;
        return this;
    }

    public Transporter listener(TransportListener listener) {
        this.listener = listener;
        return this;
    }

    public Transporter id(int id) {
        this.id = id;
        return this;
    }

    public Transporter url(String url) {
        this.url = url;
        return this;
    }

    public Transporter port(String port) {
        this.port = port;
        return this;
    }

    public Transporter route(String route) {
        this.route = route;
        return this;
    }

    public Transporter body(BodyBuilder body) {
        this.body = body;
        return this;
    }

    public Transporter header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Transporter packet(Object... packet) {
        this.packet = packet;
        return this;
    }

    public Executor post() {
        this.method = "POST";
        return new Executor(context, listener, id, this, packet);
    }

    public Executor put() {
        this.method = "PUT";
        return new Executor(context, listener, id, this, packet);
    }

    public Executor gets() {
        body = new BodyBuilder().setMediaType(MediaType.JSON.toString());
        this.method = "GET";
        return new Executor(context, listener, id, this, packet);
    }

    private void validate() throws Exception {
        if (url == null || url.equals("")) {
            throw new Exception("URL is empty");
        } else if (route == null && route.equals("")) {
            throw new Exception("Route is empty");
        } else if (listener == null) {
            throw new Exception("TransportListener not set");
        } else if (context == null) {
            throw new Exception("Context not set");
        } else if (method == null || method.equals("")) {
            throw new Exception("Method not set");
        }
    }

    String[] execute() throws Exception {
        validate();

        URL urlToRequest = new URL(url + (port != null && !port.equals("") ? ":" + port : "") + route);
        HttpsURLConnection urlConnection = (HttpsURLConnection) urlToRequest.openConnection();

        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        urlConnection.setSSLSocketFactory(sc.getSocketFactory());
        urlConnection.setConnectTimeout(180000); // 3 menit
        urlConnection.setRequestMethod(this.method);
        urlConnection.setRequestProperty("Content-Type", body.getMediaType());

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
        }

        // Print out url and route
        Log.d("URL Request", urlToRequest.toString());

        //Send request
        if (this.method.equals("POST") || this.method.equals("PUT")) {
//            urlConnection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
            Object object = body.getBody();
            String boundary = body.getBoundary();
            if (object instanceof Multipart) {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                Multipart multipart = (Multipart) object;
                for (Map.Entry<String, Object> entry : multipart.getParams().entrySet()) {
                    Object data = entry.getValue();
                    if (data instanceof File) {
                        File uploadFile = (File) data;
                        String fileName = uploadFile.getName();
                        writer.append("--" + boundary).append(LINE_FEED);
                        writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
                        writer.append("Content-ReceiptHistory-Encoding: binary").append(LINE_FEED);
                        writer.append(LINE_FEED);
                        writer.flush();

                        final FileInputStream inputStream = new FileInputStream(uploadFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead = -1;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        inputStream.close();

                        writer.append(LINE_FEED);
                        writer.flush();
                    } else {
                        writer.append("--" + boundary).append(LINE_FEED);
                        writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append(LINE_FEED);
                        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
                        writer.append(LINE_FEED);
                        writer.append(String.valueOf(data)).append(LINE_FEED);
                        writer.flush();
                    }
                }
            } else {
                outputStream.writeBytes(String.valueOf(object));
                outputStream.flush();
            }
            outputStream.close();
        }

        //Get Response
        InputStream is = urlConnection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder response = new StringBuilder();
        while((line = rd.readLine()) != null) {
            response.append(line);
        }

        rd.close();
        is.close();

        String code = String.valueOf(urlConnection.getResponseCode());
        String message = urlConnection.getResponseMessage();
        urlConnection.disconnect();
        return new String[] {code, message, response.toString()};
    }
}
