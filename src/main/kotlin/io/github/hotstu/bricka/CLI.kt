package io.github.hotstu.bricka

import com.beust.jcommander.Parameter
import java.util.*


class Args {
    @Parameter
     var parameters: List<String> = ArrayList()

    @Parameter(names = ["-h","--help"], description = "查看帮助")
     var help = false

    @Parameter(names = ["-d", "--daemon"], description = "daemon mode")
     var daemon = false

    @Parameter(names = ["-s", "--startDate"], description = "起始日期,格式2020-1-1")
     var startDate = "2021-12-20"

    @Parameter(names = ["-e", "--endDate"], description = "结束日期,格式2020-1-1")
     var endDate = "2022-4-10"

    @Parameter(names = ["-wh", "--workHour"], description = "工作时间,例如 9-18")
     var hourFromTo = "9-18"

    @Parameter(names = ["-vh", "--viewHour"], description = "想要展示的时间,例如 9-20")
     var viewHourFromTo = "9-21"

    @Parameter(names = ["-eh", "--excludeHour"], description = "想要在展示时排除的时间,格式12,13, 默认12")
     var excludeHours = "12"

    @Parameter(names = ["-rowLimit"], description = "单行最大宽度,超过将换行，默认0无限制")
    var wlimit = 0
}