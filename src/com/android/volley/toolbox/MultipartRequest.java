package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class MultipartRequest extends Request<JSONObject> {

    private final Response.Listener<JSONObject> mListener;
    public final ProgressReporter mProgressReporter;
    private HttpEntity httpEntity = null;

    public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<JSONObject> listener, ProgressReporter progressReporter)
    {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mProgressReporter = progressReporter;
    }

    protected abstract HttpEntity createHttpEntity();

    private HttpEntity getHttpEntity()
    {
        if(httpEntity == null)
        {
            httpEntity = createHttpEntity();
        }
        return httpEntity;
    }

    @Override
    public String getBodyContentType()
    {
        return getHttpEntity().getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            getHttpEntity().writeTo(bos);
        }
        catch (IOException e)
        {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject jsonObject) {
        if (mListener != null)
        {
            mListener.onResponse(jsonObject);
        }
    }

    public static interface ProgressReporter
    {
        void transferred(int transferredBytes, int totalSize);
    }
}