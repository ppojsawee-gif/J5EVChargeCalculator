package com.j5ev.chargecalculator

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
    private val bg = 0xFF101512.toInt()
    private val panel = 0xFF1B241F.toInt()
    private val green = 0xFF35D07F.toInt()
    private val white = 0xFFF2F6F3.toInt()
    private val muted = 0xFFAAB8AF.toInt()
    private val fmt = DecimalFormat("0.0")
    private lateinit var start: EditText
    private lateinit var target: EditText
    private lateinit var battery: EditText
    private lateinit var current: EditText
    private lateinit var price: EditText
    private lateinit var money: EditText
    private lateinit var ampSpinner: Spinner
    private lateinit var wallSpinner: Spinner
    private lateinit var mode: Spinner
    private lateinit var result: TextView
    private lateinit var resultTitle: TextView
    private lateinit var endTime: TextView
    private lateinit var amount: TextView
    private lateinit var moneyLabel: TextView
    private lateinit var priceLabel: TextView
    private lateinit var powerLabel: TextView
    private lateinit var timeLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
        calculate()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(28))
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(text("J5 EV CHARGE", 12, green, true))
        root.addView(text("Calculator", 30, white, true))
        root.addView(text("คำนวณการชาร์จสำหรับ JAECOO 5 EV • ใช้งานออฟไลน์", 13, muted))
        root.addView(space(18))

        val modeCard = card(root, "ประเภทเครื่องชาร์จ")
        mode = spinner(listOf("สายชาร์จฉุกเฉิน (AC)", "Wall Charger (AC)", "ตู้นอกบ้าน (DC)"))
        modeCard.addView(mode)
        mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateMode()
                calculate()
            }
        }

        val batteryCard = card(root, "ข้อมูลแบตเตอรี่")
        batteryCard.addView(label("แบตเตอรี่ปัจจุบัน (%)"))
        start = edit("46", "เช่น 46", true); batteryCard.addView(start)
        batteryCard.addView(label("ต้องการให้ถึง (%)"))
        target = edit("100", "เช่น 80 หรือ 100", true); batteryCard.addView(target)
        batteryCard.addView(label("ความจุแบตเตอรี่ที่ใช้คำนวณ (kWh)"))
        current = edit("58.9", "ปรับได้ตามสเปกรถ", false); batteryCard.addView(current)
        batteryCard.addView(text("ค่าเริ่มต้น 58.9 kWh เป็นค่าประมาณ สามารถแก้ไขได้", 11, muted))

        val powerCard = card(root, "กำลังไฟและราคา")
        powerLabel = label("กระแสไฟ")
        powerCard.addView(powerLabel)
        ampSpinner = spinner(listOf("8A", "10A", "13A", "16A"))
        powerCard.addView(ampSpinner)
        timeLabel = label("กำลัง Wall Charger")
        powerCard.addView(timeLabel)
        wallSpinner = spinner(listOf("3.5 kW", "7 kW", "11 kW"))
        powerCard.addView(wallSpinner)
        priceLabel = label("ราคาต่อหน่วย (บาท/kWh)")
        powerCard.addView(priceLabel)
        price = edit("6.60", "เช่น 6.60", false); powerCard.addView(price)
        moneyLabel = label("งบประมาณ (บาท)")
        powerCard.addView(moneyLabel)
        money = edit("100", "เช่น 100", false); powerCard.addView(money)

        val scheduleCard = card(root, "กำหนดเวลา")
        scheduleCard.addView(label("เริ่มชาร์จเวลา (HH:MM)"))
        val now = Calendar.getInstance()
        startTimeDefault = String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
        startTime = edit(startTimeDefault, "22:30", false); scheduleCard.addView(startTime)
        scheduleCard.addView(label("ต้องการทราบผล ณ เวลา (HH:MM)"))
        targetTime = edit("06:30", "06:30", false); scheduleCard.addView(targetTime)
        scheduleCard.addView(text("สำหรับโหมดตู้ DC ระบบใช้กำลังเฉลี่ย 60 kW ตามที่เลือกไว้", 11, muted))

        val button = Button(this).apply {
            text = "คำนวณการชาร์จ"
            setTextColor(0xFF07130C.toInt())
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(green))
            textSize = 16f
            setOnClickListener { calculate() }
        }
        root.addView(button, lp(match, dp(54)).apply { topMargin = dp(16) })

        val out = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(panel)
        }
        val outLp = lp(match, wrap).apply { topMargin = dp(18) }
        root.addView(out, outLp)
        out.addView(text("ผลการคำนวณ", 14, green, true))
        resultTitle = text("แบตเตอรี่ที่คาดว่าจะได้", 13, muted); out.addView(resultTitle)
        result = text("--%", 38, white, true); out.addView(result)
        endTime = text("เวลาที่คาดว่าจะเต็ม: --", 14, white); out.addView(endTime)
        amount = text("พลังงาน/ค่าใช้จ่าย: --", 13, muted); out.addView(amount)
        out.addView(space(8))
        out.addView(text("หมายเหตุ: เป็นค่าประมาณ ไม่ใช่ข้อมูลจาก BMS รถจริง โดย AC คิดประสิทธิภาพ 90% และ DC คิด 92%; กำลัง DC จริงเปลี่ยนตาม SOC อุณหภูมิ และตู้ชาร์จ", 11, muted))
        val footer = text("J5 EV Charge Calculator  •  v1.0", 11, muted)
        footer.gravity = Gravity.CENTER
        root.addView(footer, lp(match, wrap).apply { topMargin = dp(20) })

        listOf(start, target, current, price, money, startTime, targetTime).forEach {
            it.setOnEditorActionListener { _, _, _ -> calculate(); false }
        }
        ampSpinner.onItemSelectedListener = simpleListener { calculate() }
        wallSpinner.onItemSelectedListener = simpleListener { calculate() }
        updateMode()
    }

    private lateinit var startTime: EditText
    private lateinit var targetTime: EditText
    private var startTimeDefault = "22:00"

    private fun updateMode() {
        val m = mode.selectedItemPosition
        ampSpinner.visibility = if (m == 0) View.VISIBLE else View.GONE
        wallSpinner.visibility = if (m == 1) View.VISIBLE else View.GONE
        powerLabel.visibility = if (m == 0) View.VISIBLE else View.GONE
        timeLabel.visibility = if (m == 1) View.VISIBLE else View.GONE
        priceLabel.visibility = if (m == 2) View.VISIBLE else View.GONE
        price.visibility = if (m == 2) View.VISIBLE else View.GONE
        moneyLabel.visibility = if (m == 2) View.VISIBLE else View.GONE
        money.visibility = if (m == 2) View.VISIBLE else View.GONE
        targetTime.hint = if (m == 2) "เวลาที่ต้องการออก (HH:MM)" else "เช่น 06:30"
    }

    private fun calculate() {
        try {
            val s = number(start, 46.0).coerceIn(0.0, 100.0)
            val t = number(target, 100.0).coerceIn(s, 100.0)
            val cap = number(current, 58.9).coerceAtLeast(1.0)
            val modeIndex = if (::mode.isInitialized) mode.selectedItemPosition else 0
            val requiredKwh = cap * (t - s) / 100.0
            val efficiency = if (modeIndex == 2) 0.92 else 0.90
            val kw = when (modeIndex) {
                0 -> 220.0 * listOf(8.0,10.0,13.0,16.0)[ampSpinner.selectedItemPosition] / 1000.0
                1 -> listOf(3.5,7.0,11.0)[wallSpinner.selectedItemPosition]
                else -> 60.0
            }
            val actualKw = kw * efficiency
            val st = parseTime(startTime.text.toString()) ?: 0
            val tt = parseTime(targetTime.text.toString()) ?: (st + 480)
            val durationMinutes: Double
            val deliveredKwh: Double
            var cost = 0.0
            if (modeIndex == 2) {
                val unitPrice = number(price, 6.6).coerceAtLeast(0.01)
                val budget = number(money, 100.0).coerceAtLeast(0.0)
                deliveredKwh = budget / unitPrice
                val added = deliveredKwh * 100.0 / cap * 0.92
                val finalSoc = min(100.0, s + added)
                durationMinutes = deliveredKwh / kw * 60.0
                cost = budget
                resultTitle.text = "แบตเตอรี่หลังใช้เงิน ${fmt.format(budget)} บาท"
                result.text = "${fmt.format(finalSoc)}%"
                val finish = addMinutes(st, durationMinutes)
                endTime.text = "ชาร์จตามงบเสร็จประมาณ ${formatTime(finish)}"
                amount.text = "พลังงานจากตู้ ${fmt.format(deliveredKwh)} kWh • ราคา ${fmt.format(unitPrice)} บาท/kWh"
            } else {
                val hoursAvailable = max(0.0, (if (tt < st) tt + 1440 else tt) - st) / 60.0
                val kwhByDeadline = actualKw * hoursAvailable
                val socByDeadline = min(100.0, s + kwhByDeadline * 100.0 / cap)
                durationMinutes = requiredKwh / actualKw * 60.0
                deliveredKwh = requiredKwh
                resultTitle.text = "แบตเตอรี่ ณ เวลาที่กำหนด (${formatTime(tt)})"
                result.text = "${fmt.format(socByDeadline)}%"
                val finish = addMinutes(st, durationMinutes)
                endTime.text = "ต้องเริ่ม ${formatTime(st)} • ถึง ${fmt.format(t)}% ใช้ประมาณ ${formatDuration(durationMinutes)}"
                amount.text = "ต้องการพลังงานเข้่าแบต ${fmt.format(requiredKwh)} kWh • ใช้ไฟจากปลั๊กประมาณ ${fmt.format(requiredKwh / efficiency)} kWh"
            }
        } catch (e: Exception) {
            resultTitle.text = "กรุณาตรวจสอบข้อมูลที่กรอก"
            result.text = "--%"
            endTime.text = "ตรวจสอบเวลาและตัวเลขอีกครั้ง"
            amount.text = ""
        }
    }

    private fun number(e: EditText, default: Double) = e.text.toString().trim().replace(",", "").toDoubleOrNull() ?: default
    private fun parseTime(v: String): Int? = try {
        val p = v.trim().split(":"); if (p.size != 2) null else {
            val h = p[0].toInt(); val m = p[1].toInt()
            if (h in 0..23 && m in 0..59) h * 60 + m else null
        }
    } catch (_: Exception) { null }
    private fun addMinutes(start: Int, duration: Double) = (start + ceil(duration).toInt()) % 1440
    private fun formatTime(mins: Int) = String.format("%02d:%02d", (mins % 1440) / 60, mins % 60)
    private fun formatDuration(minutes: Double): String {
        val n = ceil(minutes).toInt()
        return "${n / 60} ชม. ${n % 60} นาที"
    }

    private fun simpleListener(action: () -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { action() }
    }
    private fun card(parent: LinearLayout, title: String): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(16))
            setBackgroundColor(panel)
        }
        parent.addView(c, lp(match, wrap).apply { topMargin = dp(14) })
        c.addView(text(title, 16, white, true))
        c.addView(space(8))
        return c
    }
    private fun text(value: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(3), 0, dp(3))
    }
    private fun label(value: String) = text(value, 12, muted)
    private fun edit(value: String, hintText: String, decimal: Boolean): EditText = EditText(this).apply {
        setText(value); hint = hintText; textSize = 16f; setTextColor(white); setHintTextColor(muted)
        inputType = if (decimal) InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        setSingleLine(true)
        backgroundTintList = android.content.res.ColorStateList.valueOf(green)
        setPadding(dp(8), dp(4), dp(8), dp(4))
    }
    private fun spinner(items: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
    private fun space(h: Int) = View(this).apply { layoutParams = lp(match, dp(h)) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun lp(w: Int, h: Int) = LinearLayout.LayoutParams(w, h)
    companion object { const val match = ViewGroup.LayoutParams.MATCH_PARENT; const val wrap = ViewGroup.LayoutParams.WRAP_CONTENT }
}
