package com.bionworks.bionsmartlockplugin;

import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import androidx.annotation.NonNull;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.app.Activity.RESULT_OK;
import static org.apache.cordova.CordovaActivity.TAG;

public class BionSmartLockPlugin extends CordovaPlugin {

    private CredentialsClient mCredentialsClient;
    private CredentialRequest.Builder mCredentialRequest;
    private Credential credential;
    private CallbackContext  callbackContext;
    private static final int RC_SAVE = 1;
    private static final int RC_READ = 3;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        mCredentialsClient = Credentials.getClient(cordova.getActivity());
        if (action.equals("retrievePassword")) {
            mCredentialRequest  = new CredentialRequest.Builder()
                    .setPasswordLoginSupported(true);
            retrivePassword(callbackContext);
        } else if(action.equals("storePassword")) {
            String email = args.getString(0);
            String password = args.getString(1);
            credential = new Credential.Builder(email)
                    .setPassword(password)
                    .build();
            savePassword(callbackContext);
        } else if(action.equals("deletePassword")) {
            String email = args.getString(0);
            String password = args.getString(1);
            credential = new Credential.Builder(email)
                    .setPassword(password)
                    .build();
            deletePassword(callbackContext);
        } else {
           JSONObject response = new JSONObject();
           response.put("status","ERROR");
           response.put("data",null);
           response.put("message","Invalid Action");
           this.callbackContext.error(response);
        }
        return true;
    }

    private void deletePassword(CallbackContext callbackContext) throws JSONException{
        Task<Void> deleteTask = mCredentialsClient.delete(credential);
        try {
            Void task = Tasks.await(deleteTask, 1000, TimeUnit.MILLISECONDS);
            Log.d(TAG,"credentials deleted SuccessFully");
            JSONObject response = new JSONObject();
            response.put("status","SUCCESS");
            response.put("data",null);
            response.put("message","Credential Deleted Successfully");
            callbackContext.success(response);
        } catch (ExecutionException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        } catch (InterruptedException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        } catch (TimeoutException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        }
    }

    private void savePassword(CallbackContext callbackContext) throws JSONException {
        Task<Void> saveTask = mCredentialsClient.save(credential);
        try {
            Void task = Tasks.await(saveTask, 1000, TimeUnit.MILLISECONDS);
            Log.d(TAG,"credentials save SuccessFully");
            JSONObject response = new JSONObject();
            response.put("status","SUCCESS");
            response.put("data",null);
            response.put("message","Credential saved successfully");
            callbackContext.success(response);
        } catch (ExecutionException exp) {
            Exception e = saveTask.getException();
            if (e instanceof ResolvableApiException) {
                // This is most likely the case where the user has multiple saved
                // credentials and needs to pick one. This requires showing UI to
                // resolve the read request.
                ResolvableApiException rae = (ResolvableApiException) e;
                resolveResult(rae, RC_SAVE);
            } else if (e instanceof ApiException) {
                // The user must create an account or sign in manually.
                Log.d(TAG,"status code debug--->"+((ApiException) e).getStatusCode());
                JSONObject response = new JSONObject();
                response.put("status","ERROR");
                response.put("data",null);
                response.put("message","No Google Account Signed in or Enable smartlock in Google account");
                callbackContext.error(response);
            }
        } catch (InterruptedException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        } catch (TimeoutException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        }
    }

    private void retrivePassword(CallbackContext callbackContext) throws JSONException{
        Task<CredentialRequestResponse> task = mCredentialsClient.request(mCredentialRequest.build());
        try {
            CredentialRequestResponse response = Tasks.await(task, 1000, TimeUnit.MILLISECONDS);
            onCredentialRetrieved(response.getCredential(),callbackContext);
        } catch (ExecutionException exp) {
            Exception e = task.getException();
            if (e instanceof ResolvableApiException) {
                // This is most likely the case where the user has multiple saved
                // credentials and needs to pick one. This requires showing UI to
                // resolve the read request.
                ResolvableApiException rae = (ResolvableApiException) e;
                if(rae.getStatusCode() == 6) {
                    resolveResult(rae, RC_READ);
                } else {
                    JSONObject response = new JSONObject();
                    response.put("status","SUCCESS");
                    response.put("data",null);
                    response.put("message","No saved Credentials Found");
                    callbackContext.success(response);
                }
            } else if (e instanceof ApiException) {
                // The user must create an account or sign in manually.
                JSONObject response = new JSONObject();
                response.put("status","ERROR");
                response.put("data",null);
                response.put("message","No Google Account Signed in or Enable smartlock in Google account");
                callbackContext.error(response);
            }
        } catch (InterruptedException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        } catch (TimeoutException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            callbackContext.error(response);
        }
    }
    private void resolveResult(ResolvableApiException rae, int requestCode) throws JSONException{
        try {
            Log.d(TAG,"resolving req");
            cordova.setActivityResultCallback(this);
            rae.startResolutionForResult(cordova.getActivity(),requestCode);
        } catch (IntentSender.SendIntentException exp) {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data",null);
            response.put("message","Exception:"+exp.getMessage());
            this.callbackContext.error(response);
        }
    }

    private void onCredentialRetrieved(Credential credential,CallbackContext callbackContext) throws JSONException {
        String accountType = credential.getAccountType();
        Log.d(TAG,"accountType->"+accountType+"->"+credential.getId()+credential.getPassword());
        if (accountType == null) {
            // Sign the user in with information from the Credential.
            JSONObject data= new JSONObject();
            data.put("userName",credential.getId());
            data.put("password",credential.getPassword());

            JSONObject response = new JSONObject();
            response.put("status","SUCCESS");
            response.put("data", data);
            response.put("message","Credential retrieved successfully");
            callbackContext.success(response);
        } else {
            JSONObject response = new JSONObject();
            response.put("status","ERROR");
            response.put("data", null);
            response.put("message","Something went wrong account type is not null");
            callbackContext.error(response);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_READ:
                Log.d(TAG,"Reading ->"+RESULT_OK);
                if (resultCode == RESULT_OK) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    try {
                        onCredentialRetrieved(credential,this.callbackContext);
                    } catch (Exception exp) {
                        Log.e(TAG,"response cannot send.Exception:"+exp.getMessage());
                    }
                } else {
                    try {
                        JSONObject response = new JSONObject();
                        response.put("status","ERROR");
                        response.put("data",null);
                        response.put("message","Credential Read failed");
                        this.callbackContext.error(response);
                    } catch (Exception e) {
                        Log.e(TAG,"response cannot send exception"+e.getMessage());
                    }
                }
                break;
            case RC_SAVE:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Credential Save: OK");
                    try{
                        JSONObject response = new JSONObject();
                        response.put("status","SUCCESS");
                        response.put("data",null);
                        response.put("message","Credential saved successfully");
                        this.callbackContext.success(response);
                    } catch (Exception e) {
                        Log.e(TAG,"response cannot send.exception:"+e.getMessage());
                    }
                } else {
                    try{
                        JSONObject response = new JSONObject();
                        response.put("status","ERROR");
                        response.put("data",null);
                        response.put("message","Credential save failed");
                        this.callbackContext.error(response);
                    } catch (Exception e) {
                        Log.e(TAG,"response cannot send.exception:"+e.getMessage());
                    }
                }
                break;
        }
    }
}
