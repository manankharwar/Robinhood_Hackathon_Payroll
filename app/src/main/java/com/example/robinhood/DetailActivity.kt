package com.example.robinhood

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.algorand.algosdk.crypto.Address
import com.algorand.algosdk.v2.client.common.AlgodClient
import com.example.robinhood.Models.Constants
import com.example.robinhood.Models.Employee
import com.example.robinhood.databinding.ActivityDetailBinding
import kotlinx.coroutines.*
import timber.log.Timber

class DetailActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var employees = ArrayList<Employee>()

    //    private lateinit var employees: ArrayList<Employee>
    lateinit var binding: ActivityDetailBinding
    private var client: AlgodClient = AlgodClient(
        Constants.ALGOD_API_ADDR,
        Constants.ALGOD_PORT,
        Constants.ALGOD_API_TOKEN,
        Constants.ALGOD_API_TOKEN_KEY
    )
    var headers = arrayOf("X-API-Key")
    var values = arrayOf(Constants.ALGOD_API_TOKEN_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        employees = EmployeeDataSource.createEmployeeDate()
        val data = intent.getIntExtra("employee", 1)
        val employee: Employee = employees[data]
        binding.name.text = employee.name
        binding.role.text = employee.role
        binding.profileImage.setImageResource(employee.profilePics)

        binding.copy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", employee.accountAddress)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, employee.accountAddress, Toast.LENGTH_LONG).show()
        }
        binding.viewExplorer.setOnClickListener { viewExlorer() }

        val address = Address(employee.accountAddress)
        getAccountBalance(address)
    }

    private fun getAccountBalance(address: Address?) = launch {
        runOnUiThread {
            binding.progress.visibility = View.VISIBLE
        }
        withContext(Dispatchers.Default) {
            try {
                val respAcct = client.AccountInformation(address).execute(headers, values)
                if(!respAcct.isSuccessful){
                    throw java.lang.Exception(respAcct.message())
                }
                val accountInfo = respAcct.body()
                println(String.format("Account Balance: %d microAlgos", accountInfo.amount))
                runOnUiThread {
                    binding.progress.visibility = View.GONE
                    val amount = accountInfo.amount.toBigDecimal()
                    binding.salary.text = amount.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun viewExlorer() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.EXLORER))
        try {
            if (!Constants.EXLORER.startsWith("http://") && !Constants.EXLORER.startsWith(
                    "https://"
                )
            )
                Constants.EXLORER = "http://" + Constants.EXLORER;
            startActivity(browserIntent)
        } catch (e: Exception) {
            Timber.d("Host not available ${e.message}")
        }
    }
}