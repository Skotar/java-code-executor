package org.slf4j.impl

import org.apache.log4j.Logger

class Log4jLoggerAdapterFactory {

    companion object {
        fun getLogger(logger: Logger): org.slf4j.Logger =
                Log4jLoggerAdapter(logger)
    }
}