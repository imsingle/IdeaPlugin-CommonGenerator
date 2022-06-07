package com.github.imsingle.settergenerator.config

import com.github.imsingle.settergenerator.Parameters

object GeneratorConstants {
    val typeGeneratedMap: HashMap<String?, String?> = object : HashMap<String?, String?>() {
        init {
            put("boolean", "false")
            put("java.lang.Boolean", "false")
            put("int", "0")
            put("byte", "(byte)0")
            put("java.lang.Byte", "(byte)0")
            put("java.lang.Integer", "0")
            put("java.lang.String", "\"\"")
            put("java.math.BigDecimal", "new BigDecimal(\"0\")")
            put("java.lang.Long", "0L")
            put("long", "0L")
            put("short", "(short)0")
            put("java.lang.Short", "(short)0")
            put("java.util.Date", "new Date()")
            put("float", "0.0F")
            put("java.lang.Float", "0.0F")
            put("double", "0.0D")
            put("java.lang.Double", "0.0D")
            put("java.lang.Character", "\'\'")
            put("char", "\'\'")
            put("java.time.LocalDateTime", "LocalDateTime.now()")
            put("java.time.LocalDate", "LocalDate.now()")
        }
    }

    val typeGeneratedImport: HashMap<String?, String?> = object : HashMap<String?, String?>() {
        init {
            put("java.math.BigDecimal", "java.math.BigDecimal")
            put("java.util.Date", "java.util.Date")
            put("java.time.LocalDateTime", "java.time.LocalDateTime")
            put("java.time.LocalDate", "java.time.LocalDate")
        }
    }

    val defaultPacakgeValues: java.util.HashMap<String?, String?> = object : java.util.HashMap<String?, String?>() {
        init {
            put("java.sql.Date", "new Date(new java.util.Date().getTime())")
            put(
                "java.sql.Timestamp",
                "new Timestamp(new java.util.Date().getTime())"
            )
        }
    }

    val defaultImportMap: java.util.HashMap<String?, String?> = object : java.util.HashMap<String?, String?>() {
        init {
            put("List", "java.util.ArrayList")
            put("Map", "java.util.HashMap")
            put("Set", "java.util.HashSet")
        }
    }

    var defaultCollections: java.util.HashMap<String?, String?> = object : java.util.HashMap<String?, String?>() {
        init {
            put("List", "ArrayList")
            put("Map", "HashMap")
            put("Set", "HashSet")
        }
    }

    fun appendCollectNotEmpty(
        builder: StringBuilder,
        paramInfo: Parameters, defaultImpl: String?,
        newImportList: MutableSet<String>
    ) {
        builder.append("new $defaultImpl<")
        if (paramInfo.params!!.isNotEmpty() == true) {
            for (i in 0 until paramInfo.params!!.size) {
                builder.append(paramInfo.params!!.get(i).realName)
                newImportList.add(paramInfo.params!!.get(i).realPackage!!)
                if (i != paramInfo.params?.size!! - 1) {
                    builder.append(",")
                }
            }
        }
        builder.append(">()")
    }
}
