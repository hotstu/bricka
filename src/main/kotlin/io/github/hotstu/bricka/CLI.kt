package io.github.hotstu.bricka

import com.beust.jcommander.Parameter
import java.util.*


class Args {
    @Parameter
     var parameters: List<String> = ArrayList()

    @Parameter(names = ["-h","--help"], description = "daemon mode")
     var help = false

    @Parameter(names = ["-d"], description = "daemon mode")
     var daemon = false

    @Parameter(names = ["-s", "--startDate"], description = "start date")
     var startDate = "2021-12-20"

    @Parameter(names = ["-e", "--endDate"], description = "end date")
     var endDate = "2022-4-10"

    @Parameter(names = ["-wh", "--workhour"], description = "work hour from to in format like 9-18")
     var hourFromTo = "9-18"

    @Parameter(names = ["-vh", "--viewHour"], description = "view hours from to in format like 9-20")
     var viewHourFromTo = "9-21"

    @Parameter(names = ["-eh", "--excludeHour"], description = " hours exclude from to in format like 12,13, default is 12")
     var excludeHours = "12"


}