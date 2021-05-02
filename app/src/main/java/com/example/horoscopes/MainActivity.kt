package com.example.horoscopes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewParent
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_navigation_header.*
import org.jsoup.Jsoup
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private val URLHoroscopeTodayTomorrow: String = "https://1001goroskop.ru"
    private val URLHoroscopeWeekMonth: String = "https://horoscopes.rambler.ru"

    private var forecastToday = ""
    private var forecastTomorrow = ""
    private var forecastWeek = ""
    private var forecastMonth = ""

    var Setting: SharedPreferences? = null
    val APP_PREFERENCES: String = "horoscope"
    val APP_PREFERENCES_SELECTED_HOROSCOPE: String = "selectedHoroscope"

    var zodiac = ""

    var horoscope: Array<String> = arrayOf("Овен", "Телец", "Близнецы", "Рак", "Лев", "Дева", "Весы", "Скорпион", "Стрелец", "Козерог", "Водолей", "Рыбы")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        var navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.itemIconTintList = null

        Setting = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

        zodiac = Setting!!.getString(APP_PREFERENCES_SELECTED_HOROSCOPE, "").toString()
        
        getStartText(zodiac, URLHoroscopeTodayTomorrow)

        getForecastToday(zodiac, URLHoroscopeTodayTomorrow)
        getForecastTomorrow(zodiac, URLHoroscopeTodayTomorrow)
        getForecastWeek(zodiac, URLHoroscopeWeekMonth)

        getForecastMonth(zodiac, URLHoroscopeWeekMonth)


    }

    // это затычка
    private fun getStartText(zodiac: String, url: String) {
        thread {
            val doc = Jsoup.connect("$url?znak=$zodiac").get()
            val textElements = doc.select("div[itemprop=description]")
            //val resultText = (doc.select("div[itemprop=description]")).select("p").text()  <---- В одну строку (хуй знает работает или нет, но ошибок не дает)
            val resultText = textElements.select("p").text()
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                tvMain.text = resultText
            })
        }
    }

    private fun getForecastToday(zodiac: String, url: String) {
        thread {
            val doc = Jsoup.connect("$url?znak=$zodiac").get()
            val textElements = doc.select("div[itemprop=description]")
            val resultText = textElements.select("p").text()
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                forecastToday = resultText
            })
        }
    }

    private fun getForecastTomorrow(zodiac: String, url: String) {
        thread {
            val doc = Jsoup.connect("$url?znak=$zodiac&kn=tomorrow").get()
            val textElements = doc.select("div[itemprop=description]")
            val resultText = textElements.select("p").text()
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                forecastTomorrow = resultText
            })
        }
    }

    private fun getForecastWeek(zodiac: String, url: String) {
        thread {
            val doc = Jsoup.connect("$url/$zodiac/weekly/").get()
            val textElements = doc.select("div[itemprop=articleBody]")
            var length = textElements.select("p").size
            var resultText = StringBuilder()
            for (i in 1..length) {
                resultText.append(textElements.select("p")[i-1].text() + "\n\n")
                if (i == length) resultText.append(textElements.select("p")[i-1].text())
            }
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                forecastWeek = resultText.toString()
            })
        }
    }

    private fun getForecastMonth(zodiac: String, url: String) {
        thread {
        // TODO если ты захочешь убиться нахуй, то вот затычка хуйня и залупа но решение
        //  var negri = textElements.html()

            val doc = Jsoup.connect("$url/$zodiac/monthly/").get()
            val textElements = doc.select("div[itemprop=articleBody]")

            var divCounter = 0
            var negri = textElements.select("div")[divCounter + 1].nextElementSibling().text()

            var lengthP = textElements.select("p").size
            var lengthH2 = textElements.select("h2").size
            var length = lengthP + lengthH2 + 1
            var resultText = StringBuilder()
            var tag = "p"
            var pCounter = 0
            var h2Counter = 0
            var text = ""
            for (i in 1..length) {
                if (i == 1) {
                    resultText.append(textElements.select("p")[0].text() + "<br><br>")
                    text = textElements.select("p")[0].nextElementSibling().text()
                    tag = textElements.select("p")[0].nextElementSibling().nodeName()
                }
                if (tag.equals("p")) {
                    resultText.append(text + "<br><br>")
                } else if (tag.equals("h2")) {
                    var textMonth = "<big><b>$text</b></big>"
                    resultText.append(textMonth + "<br>")
                } else if (tag.equals("div")) {
                    text = textElements.select("div")[divCounter + 1].nextElementSibling().text()
                    tag = textElements.select("div")[divCounter + 1].nextElementSibling().nodeName()
                    divCounter += 5
                    continue
                }
                if (i != length) {
                    var tempText = text
                    text = doc.selectFirst("$tag:containsOwn($text)").nextElementSibling().text()
                    tag = doc.selectFirst("$tag:containsOwn($tempText)").nextElementSibling().tagName()
                }

                // -----------------------------
//                if (myTag.equals("p")) {
//                    resultText.append(textElements.select(myTag)[pCounter].text() + "\n\n")
//                    pCounter++
//                    myTag = textElements.select(myTag)[pCounter].nextElementSibling().nodeName()
//                } else if (myTag.equals("h2")) {
//                    var textTemp = textElements.select(myTag)[h2Counter].text()
//                    var textMonth = "<big>$textTemp</big>"
//                    resultText.append(textMonth + "\n")
//                    h2Counter++
//                    myTag = textElements.select(myTag)[h2Counter].nextElementSibling().nodeName()
//                }
              }
//            var lengthP = textElements.select("p").size
//            var lengthH2 = textElements.select("h2").size
//            var length = lengthP + lengthH2
//            var resultText = StringBuilder()
//            var myTag = ""
//            for (i in 1..length) {
//                if (textElements.select("p")[0].nextElementSibling().nodeName().equals("p")) {
//                    resultText.append(textElements.select("p")[i - 1].text() + "\n\n")
//                    myTag = "p"
//                } else if (textElements.select("p")[i - 1].nextElementSibling().nodeName().equals("h2")) {
//                    var textTemp = textElements.select("p")[i - 1].text()
//                    var textMonth = "<big>$textTemp</big>"
//                    resultText.append(textMonth + "\n")
//                    myTag = "h2"
//                }
//
//                if (myTag.equals("p")) {
//                    resultText.append(textElements.select("p")[i - 1].text() + "\n\n")
//                }
//
//                if (i == length) resultText.append(textElements.select("p")[i-1].text())
//            }
//            var length = textElements.select("p").size
//            var resultText = StringBuilder()
//            for (i in 1..length) {
//                resultText.append(textElements.select("p")[i-1].text() + "\n\n")
//                if (i == length) resultText.append(textElements.select("p")[i-1].text())
//            }
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                forecastMonth = resultText.toString()
            })
        }
    }

    fun watchToday(view: View) {
        switchColorsForBtnToday()
        tvMain.text = forecastToday
    }

    fun watchTommorow(view: View) {
        switchColorsForBtnTommorow()
        tvMain.text = forecastTomorrow
    }

    fun watchWeek(view: View) {
        switchColorsForBtnWeek()
        tvMain.text = forecastWeek
    }

    fun watchMonth(view: View) {
        switchColorsForBtnMonth()
        tvMain.text = Html.fromHtml(forecastMonth)
    }

    fun selSpinner(){
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var selItem = spinner.getItemAtPosition(position).toString()

                if(selItem.equals("Овен")) {
                    getForecastToday("aries", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("aries", URLHoroscopeTodayTomorrow)
                    getForecastWeek("aries", URLHoroscopeWeekMonth)
                    getForecastMonth("aries", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "aries")
                    editor.apply()
                }
                if(selItem.equals("Телец")){
                    getForecastToday("taurus", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("taurus", URLHoroscopeTodayTomorrow)
                    getForecastWeek("taurus", URLHoroscopeWeekMonth)
                    getForecastMonth("taurus", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "taurus")
                    editor.apply()
                }
                if(selItem.equals("Близнецы")){
                    getForecastToday("gemini", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("gemini", URLHoroscopeTodayTomorrow)
                    getForecastWeek("gemini", URLHoroscopeWeekMonth)
                    getForecastMonth("gemini", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "gemini")
                    editor.apply()
                }
                if(selItem.equals("Рак")){
                    getForecastToday("cancer", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("cancer", URLHoroscopeTodayTomorrow)
                    getForecastWeek("cancer", URLHoroscopeWeekMonth)
                    getForecastMonth("cancer", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "cancer")
                    editor.apply()
                }
                if(selItem.equals("Лев")){
                    getForecastToday("leo", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("leo", URLHoroscopeTodayTomorrow)
                    getForecastWeek("leo", URLHoroscopeWeekMonth)
                    getForecastMonth("leo", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "leo")
                    editor.apply()
                }
                if(selItem.equals("Дева")){
                    getForecastToday("virgo", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("virgo", URLHoroscopeTodayTomorrow)
                    getForecastWeek("virgo", URLHoroscopeWeekMonth)
                    getForecastMonth("virgo", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "virgo")
                    editor.apply()
                }
                if(selItem.equals("Весы")){
                    getForecastToday("libra", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("libra", URLHoroscopeTodayTomorrow)
                    getForecastWeek("libra", URLHoroscopeWeekMonth)
                    getForecastMonth("libra", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "libra")
                    editor.apply()
                }
                if(selItem.equals("Скорпион")){
                    getForecastToday("scorpio", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("scorpio", URLHoroscopeTodayTomorrow)
                    getForecastWeek("scorpio", URLHoroscopeWeekMonth)
                    getForecastMonth("scorpio", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "scorpio")
                    editor.apply()
                }
                if(selItem.equals("Стрелец")){
                    getForecastToday("sagittarius", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("sagittarius", URLHoroscopeTodayTomorrow)
                    getForecastWeek("sagittarius", URLHoroscopeWeekMonth)
                    getForecastMonth("sagittarius", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "sagittarius")
                    editor.apply()
                }
                if(selItem.equals("Козерог")){
                    getForecastToday("capricorn", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("capricorn", URLHoroscopeTodayTomorrow)
                    getForecastWeek("capricorn", URLHoroscopeWeekMonth)
                    getForecastMonth("capricorn", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "capricorn")
                    editor.apply()
                }
                if(selItem.equals("Водолей")){
                    getForecastToday("aquarius", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("aquarius", URLHoroscopeTodayTomorrow)
                    getForecastWeek("aquarius", URLHoroscopeWeekMonth)
                    getForecastMonth("aquarius", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "aquarius")
                    editor.apply()
                }
                if(selItem.equals("Рыбы")){
                    getForecastToday("pisces", URLHoroscopeTodayTomorrow)
                    getForecastTomorrow("pisces", URLHoroscopeTodayTomorrow)
                    getForecastWeek("pisces", URLHoroscopeWeekMonth)
                    getForecastMonth("pisces", URLHoroscopeWeekMonth)
                    var editor: SharedPreferences.Editor = Setting!!.edit()
                    editor.putString(APP_PREFERENCES_SELECTED_HOROSCOPE, "pisces")
                    editor.apply()
                }
            }
        }
    }
    fun openMenu(view: View) {
        Setting = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        zodiac = Setting!!.getString(APP_PREFERENCES_SELECTED_HOROSCOPE, "").toString()

        drawerLayout.openDrawer(GravityCompat.START)
        var arrayAdapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(this@MainActivity, R.layout.layout_color_spinner, horoscope)
        arrayAdapter.setDropDownViewResource(R.layout.layout_color_spinner2)
        spinner.adapter = arrayAdapter

        if(zodiac == "aries")
            spinner.setSelection(0)
        else if(zodiac == "taurus")
            spinner.setSelection(1)
        else if(zodiac == "gemini")
            spinner.setSelection(2)
        else if(zodiac == "cancer")
            spinner.setSelection(3)
        else if(zodiac == "leo")
            spinner.setSelection(4)
        else if(zodiac == "virgo")
            spinner.setSelection(5)
        else if(zodiac == "libra")
            spinner.setSelection(6)
        else if(zodiac == "scorpio")
            spinner.setSelection(7)
        else if(zodiac == "sagittarius")
            spinner.setSelection(8)
        else if(zodiac == "capricorn")
            spinner.setSelection(9)
        else if(zodiac == "aquarius")
            spinner.setSelection(10)
        else if(zodiac == "pisces")
            spinner.setSelection(11)
        else Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_SHORT).show()

        selSpinner()

    }
    private fun switchColorsForBtnToday() {
        btnToday.setBackgroundResource(R.drawable.button)
        btnToday.setTextColor(Color.WHITE)

        btnTodayDown.setBackgroundResource(R.drawable.button)
        btnTodayDown.setTextColor(Color.WHITE)
        // остальные белыми
        btnTommorow.setBackgroundResource(R.color.white_true)
        btnTommorow.setTextColor(getColor(R.color.blueApp))

        btnTommorowDown.setBackgroundResource(R.color.white_true)
        btnTommorowDown.setTextColor(getColor(R.color.blueApp))
        //
        btnWeek.setBackgroundResource(R.color.white_true)
        btnWeek.setTextColor(getColor(R.color.blueApp))

        btnWeekDown.setBackgroundResource(R.color.white_true)
        btnWeekDown.setTextColor(getColor(R.color.blueApp))
        //
        btnMonth.setBackgroundResource(R.color.white_true)
        btnMonth.setTextColor(getColor(R.color.blueApp))

        btnMonthDown.setBackgroundResource(R.color.white_true)
        btnMonthDown.setTextColor(getColor(R.color.blueApp))
    }

    private fun switchColorsForBtnTommorow() {
        btnTommorow.setBackgroundResource(R.drawable.button)
        btnTommorow.setTextColor(Color.WHITE)

        btnTommorowDown.setBackgroundResource(R.drawable.button)
        btnTommorowDown.setTextColor(Color.WHITE)

        // остальные белыми
        btnToday.setBackgroundResource(R.color.white_true)
        btnToday.setTextColor(getColor(R.color.blueApp))

        btnTodayDown.setBackgroundResource(R.color.white_true)
        btnTodayDown.setTextColor(getColor(R.color.blueApp))
        //
        btnWeek.setBackgroundResource(R.color.white_true)
        btnWeek.setTextColor(getColor(R.color.blueApp))

        btnWeekDown.setBackgroundResource(R.color.white_true)
        btnWeekDown.setTextColor(getColor(R.color.blueApp))
        //
        btnMonth.setBackgroundResource(R.color.white_true)
        btnMonth.setTextColor(getColor(R.color.blueApp))

        btnMonthDown.setBackgroundResource(R.color.white_true)
        btnMonthDown.setTextColor(getColor(R.color.blueApp))
    }

    private fun switchColorsForBtnWeek() {
        btnWeek.setBackgroundResource(R.drawable.button)
        btnWeek.setTextColor(Color.WHITE)

        btnWeekDown.setBackgroundResource(R.drawable.button)
        btnWeekDown.setTextColor(Color.WHITE)

        // остальные белыми
        btnToday.setBackgroundResource(R.color.white_true)
        btnToday.setTextColor(getColor(R.color.blueApp))

        btnTodayDown.setBackgroundResource(R.color.white_true)
        btnTodayDown.setTextColor(getColor(R.color.blueApp))
        //
        btnTommorow.setBackgroundResource(R.color.white_true)
        btnTommorow.setTextColor(getColor(R.color.blueApp))

        btnTommorowDown.setBackgroundResource(R.color.white_true)
        btnTommorowDown.setTextColor(getColor(R.color.blueApp))
        //
        btnMonth.setBackgroundResource(R.color.white_true)
        btnMonth.setTextColor(getColor(R.color.blueApp))

        btnMonthDown.setBackgroundResource(R.color.white_true)
        btnMonthDown.setTextColor(getColor(R.color.blueApp))
    }

    private fun switchColorsForBtnMonth() {
        btnMonth.setBackgroundResource(R.drawable.button)
        btnMonth.setTextColor(Color.WHITE)

        btnMonthDown.setBackgroundResource(R.drawable.button)
        btnMonthDown.setTextColor(Color.WHITE)

        // остальные белыми
        btnToday.setBackgroundResource(R.color.white_true)
        btnToday.setTextColor(getColor(R.color.blueApp))

        btnTodayDown.setBackgroundResource(R.color.white_true)
        btnTodayDown.setTextColor(getColor(R.color.blueApp))
        //
        btnTommorow.setBackgroundResource(R.color.white_true)
        btnTommorow.setTextColor(getColor(R.color.blueApp))

        btnTommorowDown.setBackgroundResource(R.color.white_true)
        btnTommorowDown.setTextColor(getColor(R.color.blueApp))
        //
        btnWeek.setBackgroundResource(R.color.white_true)
        btnWeek.setTextColor(getColor(R.color.blueApp))

        btnWeekDown.setBackgroundResource(R.color.white_true)
        btnWeekDown.setTextColor(getColor(R.color.blueApp))
    }

    fun feedback(view: View) {
        val intent = Intent(this@MainActivity, FeedBack::class.java)
        startActivity(intent)
    }

}
