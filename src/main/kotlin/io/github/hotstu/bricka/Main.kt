package io.github.hotstu.bricka

import com.beust.jcommander.JCommander

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val receiver = Args()
        val j = JCommander.newBuilder()
                .addObject(receiver)
                .args(args)
                .build()
        println(receiver)

        j.programName = "java -jar bricka.jar"

        if (receiver.help) {
            j.usage()
            return
        }
        if (receiver.daemon) {
            ChaosProducer("bricka").start()
            return
        }
        if (args.size >= 2) {
            val hour1 = receiver.hourFromTo.split("-").map { it.toInt() }
            val hour2 = receiver.viewHourFromTo.split("-").map { it.toInt() }
            val hour3 = receiver.excludeHours.split(",").map { it.toInt() }
            ChaosViewer(
                    dbname = "bricka",
                    outputName = "build/bricka.html",
                    workStartHour = hour1[0],
                    workEndHour = hour1[1],
                    viewStartHour = hour2[0],
                    viewEndHour = hour2[1],
                    excludeHour = hour3.toTypedArray(),
                    rowLimit = receiver.wlimit
            ).build(receiver.startDate, receiver.endDate)
            return
        }
        j.usage()
    }
}