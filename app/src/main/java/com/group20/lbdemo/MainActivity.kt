package com.group20.lbdemo

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.group20.lbdemo.network.RetrofitClient
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var tvResult: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var btnHello: Button
    private lateinit var btnSlow: Button
    private lateinit var btnHealth: Button
    private lateinit var btnConcurrent: Button
    private lateinit var btnStartPoll: Button
    private lateinit var btnStopPoll: Button
    private lateinit var btnClearLog: Button

    // CoroutineScope implementation using lifecycle
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var pollJob: Job? = null
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        scrollView = findViewById(R.id.scrollView)
        btnHello = findViewById(R.id.btnHello)
        btnSlow = findViewById(R.id.btnSlow)
        btnHealth = findViewById(R.id.btnHealth)
        btnConcurrent = findViewById(R.id.btnConcurrent)
        btnStartPoll = findViewById(R.id.btnStartPoll)
        btnStopPoll = findViewById(R.id.btnStopPoll)
        btnClearLog = findViewById(R.id.btnClearLog)

        btnHello.setOnClickListener {
            appendLog("Calling /api/hello ...")
            launch { callHelloAndShow() }
        }

        btnSlow.setOnClickListener {
            appendLog("Calling /api/slow ...")
            launch { callSlowAndShow() }
        }

        btnHealth.setOnClickListener {
            appendLog("Calling /api/health ...")
            launch { callHealthAndShow() }
        }

        btnConcurrent.setOnClickListener {
            appendLog("Sending 10 concurrent /api/hello ...")
            launch { callConcurrent(10) }
        }

        btnStartPoll.setOnClickListener {
            if (pollJob == null) {
                pollJob = startHealthPolling(3000L)
                appendLog("Started health poll every 3s")
            } else {
                appendLog("Health poll already running")
            }
        }

        btnStopPoll.setOnClickListener {
            pollJob?.cancel()
            pollJob = null
            appendLog("Stopped health poll")
        }

        btnClearLog.setOnClickListener {
            lifecycleScope.launch {
                tvResult.text = "" // clear log hiển thị trên UI
                Toast.makeText(this@MainActivity, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun callHelloAndShow() {
        try {
            val resp = RetrofitClient.api.hello()
            if (resp.isSuccessful) {
                val body = resp.body()
                appendLog("/hello → OK | Server: ${body?.server} | Msg: ${body?.message}")
            } else {
                appendLog("/hello → HTTP ${resp.code()} ${resp.message()}")
            }
        } catch (e: Exception) {
            appendLog("/hello error: ${e.localizedMessage}")
        }
    }

    private suspend fun callSlowAndShow() {
        try {
            val resp = RetrofitClient.api.slow()
            if (resp.isSuccessful) {
                val body = resp.body()
                appendLog("/slow OK | Server: ${body?.server}")
            } else {
                appendLog("/slow fail (${resp.code()})")
            }
        } catch (e: Exception) {
            appendLog("/slow error: ${e.localizedMessage}")
        }
    }

    private suspend fun callHealthAndShow() {
        try {
            val resp = RetrofitClient.api.health()
            if (resp.isSuccessful) {
                val body = resp.body()
                appendLog("Health OK | Server: ${body?.server} | Status: ${body?.status}")
            } else {
                appendLog("Health FAIL (${resp.code()})")
            }
        } catch (e: Exception) {
            appendLog("Health error: ${e.localizedMessage}")
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
                appendLog("Concurrent results: $summary")
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
                        appendLog("Health poll OK | Server: ${body?.server}")
                    } else {
                        appendLog("Health poll FAIL (${resp.code()})")
                    }
                } catch (e: Exception) {
                    appendLog("Health poll error: ${e.localizedMessage}")
                }
                delay(intervalMs)
            }
        }
    }

    private fun appendLog(msg: String) {
        val ts = timeFmt.format(Date())
        tvResult.append("[$ts] $msg\n")
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
