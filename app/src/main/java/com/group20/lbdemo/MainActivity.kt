package com.group20.lbdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.group20.lbdemo.network.RetrofitClient
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var tvResult: TextView
    private lateinit var btnHello: Button
    private lateinit var btnSlow: Button
    private lateinit var btnHealth: Button
    private lateinit var btnConcurrent: Button
    private lateinit var btnStartPoll: Button
    private lateinit var btnStopPoll: Button

    // CoroutineScope implementation using lifecycle
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var pollJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        btnHello = findViewById(R.id.btnHello)
        btnSlow = findViewById(R.id.btnSlow)
        btnHealth = findViewById(R.id.btnHealth)
        btnConcurrent = findViewById(R.id.btnConcurrent)
        btnStartPoll = findViewById(R.id.btnStartPoll)
        btnStopPoll = findViewById(R.id.btnStopPoll)

        btnHello.setOnClickListener {
            tvResult.text = "Calling /api/hello..."
            launch { callHelloAndShow() }
        }

        btnSlow.setOnClickListener {
            tvResult.text = "Calling /api/slow..."
            launch { callSlowAndShow() }
        }

        btnHealth.setOnClickListener {
            tvResult.text = "Calling /api/health..."
            launch { callHealthAndShow() }
        }

        btnConcurrent.setOnClickListener {
            tvResult.text = "Sending 10 concurrent /api/hello..."
            launch { callConcurrent(10) }
        }

        btnStartPoll.setOnClickListener {
            if (pollJob == null) {
                pollJob = startHealthPolling(3000L)
                tvResult.text = "Started health poll every 3s"
            } else {
                tvResult.text = "Health poll already running"
            }
        }

        btnStopPoll.setOnClickListener {
            pollJob?.cancel()
            pollJob = null
            tvResult.text = "Stopped health poll"
        }
    }

    private suspend fun callHelloAndShow() {
        try {
            val resp = RetrofitClient.api.hello()
            if (resp.isSuccessful) {
                val body = resp.body()
                tvResult.text = "OK\nServer: ${body?.server}\nMessage: ${body?.message}\nType: ${body?.type ?: "fast"}"
            } else {
                tvResult.text = "HTTP ${resp.code()} ${resp.message()}"
            }
        } catch (e: IOException) {
            tvResult.text = "Network error: ${e.localizedMessage}"
        } catch (e: HttpException) {
            tvResult.text = "HTTP exception: ${e.message}"
        } catch (e: Exception) {
            tvResult.text = "Error: ${e.localizedMessage}"
        }
    }

    private suspend fun callSlowAndShow() {
        try {
            val resp = RetrofitClient.api.slow()
            if (resp.isSuccessful) {
                val body = resp.body()
                tvResult.text = "OK (slow)\nServer: ${body?.server}\nType: ${body?.type ?: "slow"}"
            } else {
                tvResult.text = "HTTP ${resp.code()} ${resp.message()}"
            }
        } catch (e: Exception) {
            tvResult.text = "Error: ${e.localizedMessage}"
        }
    }

    private suspend fun callHealthAndShow() {
        try {
            val resp = RetrofitClient.api.health()
            if (resp.isSuccessful) {
                val body = resp.body()
                tvResult.text = "Health OK\nServer: ${body?.server}\nStatus: ${body?.status ?: "OK"}"
            } else {
                tvResult.text = "Health Fail (${resp.code()})"
            }
        } catch (e: Exception) {
            tvResult.text = "Error: ${e.localizedMessage}"
        }
    }

    private suspend fun callConcurrent(n: Int) {
        withContext(Dispatchers.IO) {
            val deferred = (1..n).map {
                async {
                    try {
                        val r = RetrofitClient.api.hello()
                        if (r.isSuccessful) {
                            r.body()?.server ?: "no-server"
                        } else {
                            "HTTP:${r.code()}"
                        }
                    } catch (e: Exception) {
                        "ERR"
                    }
                }
            }
            val results = deferred.awaitAll()
            val summary = results.groupingBy { it }.eachCount()
            withContext(Dispatchers.Main) {
                tvResult.text = "Responses: $results\nSummary: $summary"
            }
        }
    }

    private fun startHealthPolling(intervalMs: Long): Job {
        return lifecycleScope.launch {
            while (isActive) {
                try {
                    val resp = RetrofitClient.api.health()
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        tvResult.text = "Health: OK | Server: ${body?.server}"
                    } else {
                        tvResult.text = "Health: FAIL (${resp.code()})"
                    }
                } catch (e: Exception) {
                    tvResult.text = "Health: Error ${e.localizedMessage}"
                }
                delay(intervalMs)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
