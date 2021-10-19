package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.BrowserSwitchResult;
import com.braintreepayments.api.PayPalCheckoutRequest;
import com.braintreepayments.api.PayPalClient;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.example.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 7171;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private PayPalClient payPalClient;
    private BraintreeClient braintreeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        braintreeClient = new BraintreeClient(this, "ASjvaoZirsMUqtoiGPzf7WsDFYxT3HD5UveqNwT7wmJG5LiG_ugkQrFk0o2Ut6YyqTyfnJtiteOMw8hR");
        payPalClient = new PayPalClient(braintreeClient);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTokenizePayPalAccountWithCheckoutMethod();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    protected void onResume() {
        super.onResume();

        BrowserSwitchResult browserSwitchResult = braintreeClient.deliverBrowserSwitchResult(this);
        Log.d("browserSwitchResult" ,"browserSwitchResult: " + browserSwitchResult);
        if (browserSwitchResult != null) {
            payPalClient.onBrowserSwitchResult(browserSwitchResult, (payPalAccountNonce, error) -> {
                Log.d("PaypalData" ,"Data: " + payPalAccountNonce.getString());
            });
        }
    }

    protected void myTokenizePayPalAccountWithCheckoutMethod() {
        PayPalCheckoutRequest request = new PayPalCheckoutRequest("1.00");
        request.setCurrencyCode("USD");
        payPalClient.tokenizePayPalAccount(this, request, (error) -> {
            Log.d("tokenizePayPalAccount", "Iniside: " + error);
        });
    }
    public void onBraintreeSubmit(View v) {
        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey("k7vpgxgq9wdr5k3k");
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                // use the result to update your UI and send the payment method nonce to your server
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }
}