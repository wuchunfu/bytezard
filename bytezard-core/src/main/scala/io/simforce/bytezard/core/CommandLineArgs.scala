package io.simforce.bytezard.core

case class CommandLineArgs(deployMode: String = "client",
                           configFile: String = "application.conf",
                           testConfig: Boolean = false)
