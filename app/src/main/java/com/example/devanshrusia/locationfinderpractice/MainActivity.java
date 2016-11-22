package com.example.devanshrusia.locationfinderpractice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private AlertDialog mAlertDialog;
    private String mcc, mnc;
    private int cid, lac;

    private double lat = 12.932833;
    private double lng = 77.622923;
    private int accuracy = 0;

    private StringBuilder mString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.originalTextView);
    }

    public void fetchCellValue(View view) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        String networkOperator = telephonyManager.getNetworkOperator();

        if (!networkOperator.isEmpty()) {
//            Log.i("Cell info ", networkOperator);
            mcc = networkOperator.substring(0, 3);
            mnc = networkOperator.substring(3);
        }

        cid = cellLocation.getCid();
        lac = cellLocation.getLac();

        mString = new StringBuilder("Cell values:- \n\nMcc: " + mcc + "\n" + "Mnc: " + mnc + "\n" + "Cid " + cid + "\n" + "Lac: " + lac + "\n\n");
        mTextView.setText(mString);
//        Log.i("Data gathered", mString.toString());

        //TODO Call to get Lat long
        apiRequest();
    }

    private void apiRequest() {

        showAlertDialog("Fetching Lat long....");
        JSONObject reqObject = new JSONObject();
        try {
            reqObject.put("token", "98c3934d66429e");
            reqObject.put("radio", "gsm");
            reqObject.put("mcc", mcc);
            reqObject.put("mnc", mnc);

            JSONArray jsonArray = new JSONArray();
            JSONObject cellIdJsonObject = new JSONObject();
            cellIdJsonObject.put("lac", lac);
            cellIdJsonObject.put("cid", cid);
            jsonArray.put(cellIdJsonObject);

            reqObject.put("cells", jsonArray);
            reqObject.put("address", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean checkCell = false;
        if (!checkCell) {
            hideAlertDialog();
            triggerIntent();
        } else {
            Log.i("req obj ", reqObject.toString());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST,
                    "https://ap1.unwiredlabs.com/v2/process.php", reqObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("Response recieved", response.toString());
                            if (response != null) {
                                hideAlertDialog();
                                Log.i("LocFinder", "Response obj " + response.toString());
                                try {
                                    mString.append("Lat: " + response.get("lat") + "\n" + "Long: " + response.get("lon") + "\n");
                                    mString.append("Address: ").append(response.get("address"));

                                    lng = (double) response.get("lon");
                                    lat = (double) response.get("lat");
                                    accuracy = (int) response.get("accuracy");
//                                Log.i("LocFinder", mString.toString());
//                                Log.i("LocFinder", "Main Lat : " + lat + " long : " + lng);
                                    mTextView.setText(mString);

                                    triggerIntent();
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Something went wrong ", Toast.LENGTH_LONG).show();
                                    hideAlertDialog();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Check your internet connection ", Toast.LENGTH_LONG).show();
                    hideAlertDialog();
                    VolleyLog.e("Error: ", error.getMessage());
                    error.printStackTrace();
                }
            });


            // add the request object to the queue to be executed
            jsObjRequest.setShouldCache(false);
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ((CellInfoApplicationController) getApplication()).addToRequestQueue(jsObjRequest,
                    null);
            Log.i("LocFinder", jsObjRequest.toString());
        }
    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please wait...");
        builder.setMessage(message);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void hideAlertDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
        }
    }

    private void triggerIntent() {

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.latConstant, lat);
        intent.putExtra(MapsActivity.longConstant, lng);
        intent.putExtra(MapsActivity.accuracyConstant, accuracy);
        startActivity(intent);
    }
}
