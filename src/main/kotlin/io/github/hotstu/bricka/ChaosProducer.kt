package io.github.hotstu.bricka

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class ChaosProducer(val dbname: String) {
    fun start() {
        println("BRICKA starts as daemon...")
        runBlocking {
            var connection: Connection? = null
            try {
                // create a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/${dbname}.db")
                connection.createStatement().use {statement ->
                    statement.queryTimeout = 30 // set timeout to 30 sec.
                    //statement.executeUpdate("drop table if exists peach")
                    statement.executeUpdate("create table if not exists peach (date string, hour string, min string)")
                    while (isActive) {
                        delay(60 * 1000)
                        val instance = Calendar.getInstance()
                        instance.timeInMillis = System.currentTimeMillis()
                        val date = "${instance.get(Calendar.YEAR)}-${instance.get(Calendar.MONTH) + 1}-${instance.get(Calendar.DAY_OF_MONTH)}"
                        val hour = "${instance.get(Calendar.HOUR_OF_DAY)}"
                        val min = "${instance.get(Calendar.MINUTE)}"
                        println("当前时间：${date} ${hour}:${min}")
                        statement.executeUpdate("insert into peach values('${date}', '${hour}', '${min}')")
                    }
                }
            } catch (e: SQLException) {
                // if the error message is "out of memory",
                // it probably means no database file is found
                System.err.println(e.message)
            } finally {
                try {
                    connection?.close()
                } catch (e: SQLException) {
                    // connection close failed.
                    System.err.println(e.message)
                }
            }

        }
        println("BRICKA exits normally")
    }
}