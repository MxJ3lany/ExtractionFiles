package com.undatech.opaque.proxmox;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.undatech.opaque.ConnectionSettings;
import com.undatech.opaque.RemoteClientLibConstants;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;

// Examples taken from:
// http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient
public class RestClient {

    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;

    private int responseCode;
    private String message;

    private String response;
    
    private ConnectionSettings connection;
    private Handler handler;

    private String url;
    private static HttpClient client = new DefaultHttpClient();

    public enum RequestMethod {
        GET,
        POST
    }

    public class MySSLSocketFactory extends SSLSocketFactory {
        Certificate cert = null;
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore, String certString, final Handler h)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException, CertificateException {
            super(truststore);
            if (certString != null && !certString.equals("")) {
                ByteArrayInputStream in = new ByteArrayInputStream(Base64.decode(certString, Base64.DEFAULT));
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                cert = (X509Certificate)certFactory.generateCertificate(in);
            }
            
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    
                    if (cert == null) {
                        synchronized (h) {
                            // Send a message containing the certificate to our handler.
                            Message m = new Message();
                            m.setTarget(h);
                            m.what = RemoteClientLibConstants.DIALOG_X509_CERT;
                            m.obj = chain[0];
                            h.sendMessage(m);
                            // Block indefinitely until the x509 cert is accepted.
                            while (connection.getOvirtCaData().isEmpty()) {
                                try {
                                    h.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    throw new CertificateException ("The x509 cert was not accepted.");
                                }
                            }
                        }
                    } else {
                        try {
                            PublicKey publicKey = cert.getPublicKey ();
                            chain[0].verify (publicKey);
                          } catch (Exception e) {}
                          
                          if (!cert.equals (chain[0])) {
                              throw new CertificateException ("The x509 cert does not match.");
                          }
                    }
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host,
                    port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public String getErrorMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public RestClient(ConnectionSettings connection, Handler handler) {
        this.connection = connection;
        this.handler = handler;
    }

    public void resetState(String url) {
        this.url = url;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            // TODO: Make it an option whether to trust all certificates.
            MySSLSocketFactory sslsf = new MySSLSocketFactory(trustStore, connection.getOvirtCaData().trim(), handler);

            Scheme https = new Scheme("https", sslsf, 8006);
            client.getConnectionManager().getSchemeRegistry().register(https);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public void addHeader(String name, String value) {
        headers.add(new BasicNameValuePair(name, value));
    }

    public void addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    public void execute(RequestMethod method) throws IOException {
        switch (method) {
        case GET: {
            // add parameters
            String combinedParams = "";
            if (!params.isEmpty()) {
                combinedParams += "?";
                for (NameValuePair p : params) {
                    String paramString = p.getName() + "="
                            + URLEncoder.encode(p.getValue(), "UTF-8");
                    if (combinedParams.length() > 1) {
                        combinedParams += "&" + paramString;
                    } else {
                        combinedParams += paramString;
                    }
                }
            }

            HttpGet request = new HttpGet(url + combinedParams);

            // add headers
            for (NameValuePair h : headers) {
                request.addHeader(h.getName(), h.getValue());
            }

            executeRequest(request, url);
            break;
        }
        case POST: {
            HttpPost request = new HttpPost(url);

            // add headers
            for (NameValuePair h : headers) {
                request.addHeader(h.getName(), h.getValue());
            }

            if (!params.isEmpty()) {
                request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }

            executeRequest(request, url);
            break;
        }
        }
    }

    private void executeRequest(HttpUriRequest request, String url) throws IOException {

        HttpParams params = client.getParams();

        // Setting 60 second timeouts
        HttpConnectionParams.setConnectionTimeout(params, 60 * 1000);
        HttpConnectionParams.setSoTimeout(params, 60 * 1000);

        HttpResponse httpResponse;

        httpResponse = client.execute(request);
        responseCode = httpResponse.getStatusLine().getStatusCode();
        message = httpResponse.getStatusLine().getReasonPhrase();

        HttpEntity entity = httpResponse.getEntity();

        if (entity != null) {

            InputStream instream = entity.getContent();
            response = convertStreamToString(instream);

            // Closing the input stream will trigger connection release
            instream.close();
        }
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append((line + "\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
