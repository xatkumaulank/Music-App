package com.naman14.timber.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import com.naman14.timber.R
import com.naman14.timber.activities.DonateActivity
import com.naman14.timber.utils.PreferencesUtility
import java.util.*

/**
 * Created by naman on 29/10/16.
 */
open class DonateActivity() : BaseThemedActivity(), IBillingHandler {
    private var readyToPurchase = false
    var bp: BillingProcessor? = null
    private var productListView: LinearLayout? = null
    private var progressBar: ProgressBar? = null
    private var status: TextView? = null
    private var action: String? = "support"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Support development"
        action = intent.action
        productListView = findViewById<View>(R.id.product_list) as LinearLayout
        progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        status = findViewById<View>(R.id.donation_status) as TextView
        if (action != null && (action == "restore")) {
            status!!.text = "Restoring purchases.."
        }
        bp = BillingProcessor(this, getString(R.string.play_billing_license_key), this)
    }

    override fun onBillingInitialized() {
        readyToPurchase = true
        checkStatus()
        if (!(action != null && (action == "restore"))) products
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        checkStatus()
        runOnUiThread { Toast.makeText(this@DonateActivity, "Thanks for your support!", Toast.LENGTH_SHORT).show() }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        runOnUiThread { Toast.makeText(this@DonateActivity, "Unable to process purchase", Toast.LENGTH_SHORT).show() }
    }

    override fun onPurchaseHistoryRestored() {}
    public override fun onDestroy() {
        if (bp != null) bp!!.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp!!.handleActivityResult(requestCode, resultCode, data)) super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkStatus() {
        object : AsyncTask<Void?, Void?, Boolean>() {
            protected fun doInBackground(vararg voids: Void): Boolean {
                val owned = bp!!.listOwnedProducts()
                return owned != null && owned.size != 0
            }

            override fun onPostExecute(b: Boolean) {
                super.onPostExecute(b)
                if (b) {
                    PreferencesUtility.getInstance(this@DonateActivity).setFullUnlocked(true)
                    if (action != null && (action == "restore")) {
                        progressBar!!.visibility = View.GONE
                    }
                    if (supportActionBar != null) supportActionBar!!.setTitle("Support development")
                } else {
                    if (action != null && (action == "restore")) {
                        products
                    }
                }
            }

            override fun doInBackground(vararg p0: Void?): Boolean {
                TODO("Not yet implemented")
            }
        }.execute()
    }

    private val products: Unit
        private get() {
            object : AsyncTask<Void?, Void?, List<SkuDetails>?>() {
                override  fun doInBackground(vararg p0: Void?): List<SkuDetails>? {
                    val products = ArrayList<String>()
                    products.add(DONATION_1)
                    products.add(DONATION_2)
                    products.add(DONATION_3)
                    products.add(DONATION_5)
                    products.add(DONATION_10)
                    products.add(DONATION_20)
                    return bp!!.getPurchaseListingDetails(products)
                }

                override fun onPostExecute(productList: List<SkuDetails>?) {
                    super.onPostExecute(productList)
                    if (productList == null) return
                    Collections.sort(productList, Comparator { skuDetails, t1 -> if (skuDetails.priceValue >= t1.priceValue) 1 else if (skuDetails.priceValue <= t1.priceValue) -1 else 0 })
                    for (i in productList.indices) {
                        val product = productList[i]
                        val rootView = LayoutInflater.from(this@DonateActivity).inflate(R.layout.item_donate_product, productListView, false)
                        val detail = rootView.findViewById<View>(R.id.product_detail) as TextView
                        detail.text = product.priceText
                        rootView.findViewById<View>(R.id.btn_donate).setOnClickListener(object : View.OnClickListener {
                            override fun onClick(view: View) {
                                if (readyToPurchase) bp!!.purchase(this@DonateActivity, product.productId) else Toast.makeText(this@DonateActivity, "Unable to initiate purchase", Toast.LENGTH_SHORT).show()
                            }
                        })
                        productListView!!.addView(rootView)
                    }
                    progressBar!!.visibility = View.GONE
                }



            }.execute()
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val DONATION_1 = "naman14.timber.donate_1"
        private val DONATION_2 = "naman14.timber.donate_2"
        private val DONATION_3 = "naman14.timber.donate_3"
        private val DONATION_5 = "naman14.timber.donate_5"
        private val DONATION_10 = "naman14.timber.donate_10"
        private val DONATION_20 = "naman14.timber.donate_20"
    }
}