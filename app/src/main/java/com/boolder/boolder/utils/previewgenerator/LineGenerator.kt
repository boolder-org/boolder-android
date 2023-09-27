package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.Line

fun dummyLine() = Line(
    id = 0,
    problemId = 0,
    topoId = 0,
    coordinates = """
        [
            {"x":0.76,"y":0.7533},
            {"x":0.7575,"y":0.5933},
            {"x":0.7362,"y":0.47},
            {"x":0.695,"y":0.435},
            {"x":0.635,"y":0.4367},
            {"x":0.585,"y":0.4617},
            {"x":0.5188,"y":0.505},
            {"x":0.4537,"y":0.5167},
            {"x":0.3625,"y":0.5083},
            {"x":0.2913,"y":0.485},
            {"x":0.2325,"y":0.3967},
            {"x":0.195,"y":0.28},
            {"x":0.19,"y":0.195},
            {"x":0.1975,"y":0.1267}
        ]
    """.trimIndent()
)
