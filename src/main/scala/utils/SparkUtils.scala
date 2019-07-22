package utils

import org.apache.spark.SparkConf


object SparkUtils {

    def getConfProperty(sparkconf: SparkConf, prop: String): String = {
        assert(sparkconf.contains(prop), s"'$prop' is not defined.")
        sparkconf.get(prop)
    }

    def getConfPropertyOrElse(sparkconf: SparkConf, prop: String, default: String): String =
        if (!sparkconf.contains(prop)) default else sparkconf.get(prop)

}
