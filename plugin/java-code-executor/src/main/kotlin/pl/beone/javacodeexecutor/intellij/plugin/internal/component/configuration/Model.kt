package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

data class Model(var host: String = "localhost",
                 var path: String = "/alfresco",
                 var port: String = "8080",
                 var username: String = "admin",
                 var password: String = "admin")
