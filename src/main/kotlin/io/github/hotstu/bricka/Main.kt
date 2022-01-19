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
            ChaosViewer("bricka.html", hour1[0], hour1[1], hour2[0], hour2[1], hour3.toTypedArray(), receiver.wlimit).build(receiver.startDate, receiver.endDate)
            return
        }
        j.usage()
    }
}