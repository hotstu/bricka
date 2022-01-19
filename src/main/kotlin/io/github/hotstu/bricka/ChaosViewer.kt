package io.github.hotstu.bricka

import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.StringBuilder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max


class ChaosViewer(val outputName: String,val workStartHour: Int, val workEndHour: Int , val viewStartHour: Int , val viewEndHour: Int  , val excludeHour :Array<Int>, val rowLimit : Int) {
    private val holidays = arrayOf(
            "2021-1-1",
            "2021-2-11",
            "2021-2-12",
            "2021-2-15",
            "2021-2-16",
            "2021-2-17",
            "2021-4-5",
            "2021-5-3",
            "2021-5-4",
            "2021-5-5",
            "2021-6-14",
            "2021-9-20",
            "2021-9-21",
            "2021-10-1",
            "2021-10-4",
            "2021-10-5",
            "2021-10-6",
            "2021-10-7",

            "2022-1-1",
            "2022-1-2",
            "2022-1-3",
            "2022-1-31",
            "2022-2-1",
            "2022-2-2",
            "2022-2-3",
            "2022-2-4",
            "2022-2-5",
            "2022-2-6",
            "2022-4-3",
            "2022-4-4",
            "2022-4-5",
            "2022-4-30",
            "2022-5-1",
            "2022-5-2",
            "2022-5-3",
            "2022-5-4",
            "2022-6-3",
            "2022-6-4",
            "2022-6-5",
            "2022-9-10",
            "2022-9-11",
            "2022-9-12",
            "2022-10-1",
            "2022-10-2",
            "2022-10-3",
            "2022-10-4",
            "2022-10-5",
            "2022-10-6",
            "2022-10-7"
    )

    private val workdays = arrayOf(
            "2021-2-7",
            "2021-2-20",
            "2021-4-25",
            "2021-5-8",
            "2021-9-18",
            "2021-9-26",
            "2021-10-9",

            "2022-1-29",
            "2022-1-30",
            "2022-4-2",
            "2022-4-24",
            "2022-5-7",
            "2022-10-8",
            "2022-10-9"
    )



    private fun dateSeq(startYear: Int, startMonth: Int, startDate: Int, endYear: Int, endMonth: Int, endDate: Int): Sequence<Calendar> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, startYear)
        calendar.set(Calendar.MONTH, startMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, startDate)
        val end = Calendar.getInstance()
        end.set(Calendar.YEAR, endYear)
        end.set(Calendar.MONTH, endMonth - 1)
        end.set(Calendar.DAY_OF_MONTH, endDate)
        return generateSequence(calendar) {
            val next = Calendar.getInstance()
            next.time = it.time
            next.add(Calendar.DAY_OF_MONTH, 1)
            if (next > end) {
                null
            } else {
                next
            }
        }
    }

    private fun hourSeq(startHour: Int, endHour: Int, excludeHour: Array<Int>): Sequence<String> {
        return generateSequence("$startHour") {
            var toInt = it.toInt() + 1
            while (excludeHour.contains(toInt)) {
                toInt += 1
            }
            if (toInt > endHour) {
                null
            } else {
                "$toInt"
            }
        }
    }
    private fun Calendar.formatDate(): String {
        return "${this.get(Calendar.YEAR)}-${this.get(Calendar.MONTH) + 1}-${this.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun String.toDate(): Triple<Int, Int, Int> {
        val split = this.split("-")
        return Triple(split[0].toInt(), split[1].toInt(), split[2].toInt())
    }

    private fun isNormalDay(dateStr: String): Boolean {
        val (year, month, date) = dateStr.toDate()
        val instance = Calendar.getInstance()
        instance.set(Calendar.YEAR, year)
        instance.set(Calendar.MONTH, month - 1)
        instance.set(Calendar.DAY_OF_MONTH, date)
        val week = instance.get(Calendar.DAY_OF_WEEK)
        if (week == Calendar.SATURDAY || week == Calendar.SUNDAY) {
            return workdays.contains(dateStr)
        } else {
            return !holidays.contains(dateStr)
        }
    }

    private fun isNormalHour(hour: String): Boolean {
        val toInt = hour.toInt()
        return (toInt >= workStartHour) && (toInt < workEndHour)
    }

    private fun queryAll(): List<Entry> {
        return runBlocking {
            var connection: Connection? = null
            val list = ArrayList<Entry>()
            try {
                // create a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/input.db")
                connection.createStatement().use { statement ->
                    statement.queryTimeout = 30
                    val rs = statement.executeQuery("SELECT * FROM peach")
                    do {
                        list.add(Entry.from(rs))
                    } while (rs.next())
                }
            } catch (e: SQLException) {
                System.err.println(e.message)
            } finally {
                try {
                    connection?.close()
                } catch (e: SQLException) {
                    System.err.println(e.message)
                }
            }
            list

        }
    }
    fun build(start: String, end: String) {
        val step = 16
        val initHori = 0
        val initVertical = 48

        var cursor = initHori
        var typicalHeight = 0
        val list = queryAll()
        val totalTime = list.size
        var normalJbTime = 0
        var holidayJbTime = 0
        val (year, month, day) = start.toDate()
        val (eyear, emonth, eday) = end.toDate()

        var offsetVertical = 0
        val sb = StringBuilder()
        dateSeq(year, month, day, eyear, emonth, eday)
                .forEach { date ->
                    val key = date.formatDate()
                    val dayList = list.filter { it.date == key }
                    sb.append("<g transform=\"translate(${cursor}, ${offsetVertical})\">")
                    sb.append("\n")
                    val vStep = 16
                    var vCursor = initVertical
                    val normal = isNormalDay(key)
                    hourSeq(viewStartHour, viewEndHour, excludeHour).forEach { hour ->
                        val hourType = when (normal) {
                            true -> {
                                if (isNormalHour(hour)) {
                                    0
                                } else {
                                    1
                                }
                            }
                            else -> {
                                2
                            }
                        }
                        val hourList = dayList.filter { it.hour == hour }
                        val size = hourList.size

                        when (hourType) {
                            0 -> {
                            }
                            1 -> {
                                normalJbTime += size
                            }
                            2 -> {
                                holidayJbTime += size
                            }
                        }

                        val level = when {
                            size <= 0 -> {
                                0
                            }
                            size < 30 -> {
                                1
                            }
                            size < 60 -> {
                                2
                            }
                            else -> {
                                3
                            }
                        }
                        sb.append("<rect class=\"square\" width=\"11\" height=\"11\" x=\"16\" y=\"${vCursor}\"  rx=\"2\" ry=\"2\" data-date=\"${key}\" data-hour=\"${hour}\" data-type=\"${hourType}\" data-level=\"${level}\" data-count=\"${size}\"></rect>\n")
                        vCursor += vStep
                        if (hour.toInt() == 11|| hour.toInt() == 17) {
                            vCursor += 4
                        }
                    }
                    typicalHeight = max(typicalHeight, vCursor + vStep)
                    sb.append("</g>")
                    sb.append("\n")
                    cursor += step
                    if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        cursor += 8
                        if (rowLimit > 0 && cursor >= rowLimit) {
                            offsetVertical += typicalHeight
                            offsetVertical -= 32
                            cursor = initHori
                        }
                    }

                }
        sb.append(" <text  x=\"16\" y=\"12\" class=\"title\">${start}~${end}</text>")

        sb.append(" <text  x=\"16\" y=\"32\" class=\"title\">工作总时长${totalTime}分钟, 加班时长${normalJbTime + holidayJbTime}分钟, 非工作日${holidayJbTime}分钟</text>")
        sb.insert(0, "<svg width=\"${max(cursor + step, rowLimit)}\" height=\"${typicalHeight + offsetVertical}\" >\n")
        sb.append("</svg>")
        val tpl = this.javaClass.classLoader.getResource("TPL.html").readText()
        File(outputName).writeText(tpl.replace("{{svg}}", sb.toString()))
        println("生成统计成功！")
    }



}




data class Entry(val date: String, val hour: String, val min: String) {
    companion object {
        fun from(rs: ResultSet): Entry {
            return Entry(rs.getString(1), rs.getString(2), rs.getString(3))
        }
    }
}




fun main() {
    val chaos = ChaosViewer("test.html",   workStartHour = 9,  workEndHour = 18,  viewStartHour = 9,  viewEndHour = 21 ,  excludeHour  = arrayOf(12), rowLimit = 1080)
    chaos.build("2021-12-20", "2022-4-10")
}


