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


class ChaosViewer(val outputName: String,val workStartHour: Int, val workEndHour: Int , val viewStartHour: Int , val viewEndHour: Int  , val excludeHour :Array<Int>) {
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
        val sb = StringBuilder()
        val step = 16
        var cursor = 0
        var typicalHeight = 0
        val list = queryAll()
        val totalTime = list.size
        var normalJbTime = 0
        var holidayJbTime = 0
        val (year, month, day) = start.toDate()
        val (eyear, emonth, eday) = end.toDate()

        dateSeq(year, month, day, eyear, emonth, eday)
                .forEach { date ->
                    val key = date.formatDate()
                    val dayList = list.filter { it.date == key }
                    sb.append("<g transform=\"translate(${cursor}, 0)\">")
                    sb.append("\n")
                    val vStep = 16
                    var vCursor = 32
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
                    if (date.get(Calendar.DAY_OF_WEEK) == Calendar.DAY_OF_WEEK) {
                        cursor += 8
                    }
                }

        sb.append(" <text  x=\"16\" y=\"16\" class=\"title\">工作总时长${totalTime}分钟, 加班时长${normalJbTime + holidayJbTime}分钟, 非工作日${holidayJbTime}分钟</text>")
        sb.insert(0, "<svg width=\"${cursor + step}\" height=\"${typicalHeight}\" >\n")
        sb.insert(0, """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>chaosviewer1.0.0</title>
            <style>
            :root {
            --color-scale-black: #010409;
            --color-scale-white: #f0f6fc;
            --color-scale-gray-0: #f0f6fc;
            --color-scale-gray-1: #c9d1d9;
            --color-scale-gray-2: #b1bac4;
            --color-scale-gray-3: #8b949e;
            --color-scale-gray-4: #6e7681;
            --color-scale-gray-5: #484f58;
            --color-scale-gray-6: #30363d;
            --color-scale-gray-7: #21262d;
            --color-scale-gray-8: #161b22;
            --color-scale-gray-9: #0d1117;
            --color-scale-blue-0: #cae8ff;
            --color-scale-blue-1: #a5d6ff;
            --color-scale-blue-2: #79c0ff;
            --color-scale-blue-3: #58a6ff;
            --color-scale-blue-4: #388bfd;
            --color-scale-blue-5: #1f6feb;
            --color-scale-blue-6: #1158c7;
            --color-scale-blue-7: #0d419d;
            --color-scale-blue-8: #0c2d6b;
            --color-scale-blue-9: #051d4d;
            --color-scale-green-0: #aff5b4;
            --color-scale-green-1: #7ee787;
            --color-scale-green-2: #56d364;
            --color-scale-green-3: #3fb950;
            --color-scale-green-4: #2ea043;
            --color-scale-green-5: #238636;
            --color-scale-green-6: #196c2e;
            --color-scale-green-7: #0f5323;
            --color-scale-green-8: #033a16;
            --color-scale-green-9: #04260f;
            --color-scale-yellow-0: #f8e3a1;
            --color-scale-yellow-1: #f2cc60;
            --color-scale-yellow-2: #e3b341;
            --color-scale-yellow-3: #d29922;
            --color-scale-yellow-4: #bb8009;
            --color-scale-yellow-5: #9e6a03;
            --color-scale-yellow-6: #845306;
            --color-scale-yellow-7: #693e00;
            --color-scale-yellow-8: #4b2900;
            --color-scale-yellow-9: #341a00;
            --color-scale-orange-0: #ffdfb6;
            --color-scale-orange-1: #ffc680;
            --color-scale-orange-2: #ffa657;
            --color-scale-orange-3: #f0883e;
            --color-scale-orange-4: #db6d28;
            --color-scale-orange-5: #bd561d;
            --color-scale-orange-6: #9b4215;
            --color-scale-orange-7: #762d0a;
            --color-scale-orange-8: #5a1e02;
            --color-scale-orange-9: #3d1300;
            --color-scale-red-0: #ffdcd7;
            --color-scale-red-1: #ffc1ba;
            --color-scale-red-2: #ffa198;
            --color-scale-red-3: #ff7b72;
            --color-scale-red-4: #f85149;
            --color-scale-red-5: #da3633;
            --color-scale-red-6: #b62324;
            --color-scale-red-7: #8e1519;
            --color-scale-red-8: #67060c;
            --color-scale-red-9: #490202;
            --color-scale-purple-0: #eddeff;
            --color-scale-purple-1: #e2c5ff;
            --color-scale-purple-2: #d2a8ff;
            --color-scale-purple-3: #bc8cff;
            --color-scale-purple-4: #a371f7;
            --color-scale-purple-5: #8957e5;
            --color-scale-purple-6: #6e40c9;
            --color-scale-purple-7: #553098;
            --color-scale-purple-8: #3c1e70;
            --color-scale-purple-9: #271052;
            --color-scale-pink-0: #ffdaec;
            --color-scale-pink-1: #ffbedd;
            --color-scale-pink-2: #ff9bce;
            --color-scale-pink-3: #f778ba;
            --color-scale-pink-4: #db61a2;
            --color-scale-pink-5: #bf4b8a;
            --color-scale-pink-6: #9e3670;
            --color-scale-pink-7: #7d2457;
            --color-scale-pink-8: #5e103e;
            --color-scale-pink-9: #42062a;
            --color-scale-coral-0: #FFDDD2;
            --color-scale-coral-1: #FFC2B2;
            --color-scale-coral-2: #FFA28B;
            --color-scale-coral-3: #F78166;
            --color-scale-coral-4: #EA6045;
            --color-scale-coral-5: #CF462D;
            --color-scale-coral-6: #AC3220;
            --color-scale-coral-7: #872012;
            --color-scale-coral-8: #640D04;
            --color-scale-coral-9: #460701;
        }


        .square[data-type="0"][data-level="0"] {
            fill: var(--color-scale-gray-2);
            opacity: .5;
        }
        .square[data-type="0"][data-level="1"] {
            fill: var(--color-scale-green-0);
        }
        .square[data-type="0"][data-level="2"] {
            fill: var(--color-scale-green-5);
        }
        .square[data-type="0"][data-level="3"] {
            fill: var(--color-scale-green-5);
        }

        .square[data-type="1"][data-level="0"] {
            fill: var(--color-scale-gray-1);
            opacity: .3;
        }
        .square[data-type="1"][data-level="1"] {
            fill: var(--color-scale-pink-1);
        }
        .square[data-type="1"][data-level="2"] {
            fill: var(--color-scale-pink-5);
        }
        .square[data-type="1"][data-level="3"] {
            fill: var(--color-scale-pink-5);
        }
        .square[data-type="2"][data-level="0"] {
            fill: var(--color-scale-gray-1);
            opacity: .3;
        }
        .square[data-type="2"][data-level="1"] {
            fill: var(--color-scale-red-1);
        }
        .square[data-type="2"][data-level="2"] {
            fill: var(--color-scale-red-5);
        }
        .square[data-type="2"][data-level="3"] {
            fill: var(--color-scale-red-5);
        }
    </style>
        </head>

        <body>
    """.trimIndent())
        sb.append("</svg>")
        sb.append("""
            <div id="desc" style="margin-left:16px">点击方块查看详情</div>
            <script>
                var ${'$'} = document.querySelectorAll.bind(document)
                ${'$'}(".square").forEach((it)=>{it.addEventListener("click",(e)=>{
                    console.log(e)
                    var date = e.target.attributes["data-date"].value
                    var hour = e.target.attributes["data-hour"].value
                    var type = e.target.attributes["data-type"].value
                    var count = e.target.attributes["data-count"].value
                    console.log(date, type, count)
                    var typedesc = "正常上班"
                    if(type == "1") {
                        typedesc = "工作日加班"
                    }
                    if(type == "2") {
                        typedesc = "节假日加班"
                    }

                    document.getElementById("desc").innerText = "" + date + " " +hour +"~" +(parseInt(hour) + 1)+"点, " + typedesc + ", 时长:" + count +"分钟" 
                }) })
            </script>
        </body>
        </html>
    """.trimIndent())
        File(outputName).writeText(sb.toString())
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
    val chaos = ChaosViewer("test.html",   workStartHour = 9,  workEndHour = 18,  viewStartHour = 9,  viewEndHour = 21 ,  excludeHour  = arrayOf(12))
    chaos.build("2021-12-20", "2022-4-10")
}


