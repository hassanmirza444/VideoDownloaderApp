package com.upscapesoft.videodownloaderapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.data.Constants;
import com.upscapesoft.videodownloaderapp.helper.AdController;

import java.util.ArrayList;
import java.util.List;

public class PremiumActivity extends AppCompatActivity {

    private static final String TAG = "payment";
    RelativeLayout oneTimePurchase;
    ImageView backBtn;
    LinearLayout container;

    private BillingClient billingClient;
    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            // To be implemented in a later section.
            Log.d(TAG, "onPurchasesUpdated: 1");
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                Log.d(TAG, "onPurchasesUpdated: size  " + purchases.size());
                if (!purchases.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handlePurchase(purchases.get(0));
                        }
                    });
                }
                for (Purchase purchase : purchases) {
                    Toast.makeText(PremiumActivity.this, "purchase", Toast.LENGTH_SHORT).show();

                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };

    void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.


        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        //

        //sessionManager.saveBooleanValue(Const.isVip, true);

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {


                // Handle the success of the consume operation.
            }
        };

        billingClient.consumeAsync(consumeParams, listener);

    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_premium);

        oneTimePurchase = findViewById(R.id.oneTimePurchase);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "onBillingSetupFinished: ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initView();
                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onBillingServiceDisconnected: ");
            }
        });

        /*admob*/
        container = findViewById(R.id.banner_container);
        AdController.loadBannerAd(PremiumActivity.this, container);
        AdController.loadInterAd(PremiumActivity.this);

    }

    private void initView() {
        oneTimePurchase.setOnClickListener(v -> {
            if (billingClient.isReady()) {
                setUpSku();
            } else {
                Log.d("TAG", "paymentMethod: bp not init");
            }
        });

    }

    private void setUpSku() {
        List<String> skuList = new ArrayList<>();
        skuList.add("premium");
        skuList.add(Constants.PURCHASE_PRODUCT_ID);


        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    // Process the result.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d(TAG, "run: " + skuDetailsList.size());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (skuDetailsList.isEmpty()) {
                                Toast.makeText(PremiumActivity.this, "Purchase error", Toast.LENGTH_SHORT).show();
                                return;

                            } else {
                                launchPayment(skuDetailsList.get(0));
                            }

                        }
                    });

                });
    }

    private void launchPayment(SkuDetails s) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(s)
                .build();
        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();

        Log.d(TAG, "launchPayment: " + responseCode);
    }

    @Override
    public void onBackPressed() {
        AdController.adCounter++;
        if (AdController.adCounter == AdController.adDisplayCounter) {
            AdController.showInterAd(PremiumActivity.this, null, 0);
        } else {
            super.onBackPressed();
        }
    }

}