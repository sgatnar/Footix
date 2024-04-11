package com.example.footixappbachelorarbeit

internal object ActivityConstants {

    const val CONNECTION_KEY = "CONNECTION_KEY"
    const val AUTO_CONNECT = "AUTO_CONNECT"
    const val CONNECTED = "CONNECTEd"
    const val LOGGING_KEY = "LOGGING_ENABLED"

    /** Property name for the history field in [Connection] object for use with [java.beans.PropertyChangeEvent]  */
    const val historyProperty = "history"

    /** Property name for the connection status field in [Connection] object for use with [java.beans.PropertyChangeEvent]  */
    const val ConnectionStatusProperty = "connectionStatus"

    /** Empty String for comparisons  */
    const val empty = ""
}