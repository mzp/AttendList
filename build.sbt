seq(webSettings :_*)

libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"

libraryDependencies += "net.databinder" %% "unfiltered-filter" % "0.5.1"

libraryDependencies += "net.databinder" %% "unfiltered-scalate" % "0.5.1"

libraryDependencies += "net.databinder" %% "dispatch-core" % "0.8.6"

libraryDependencies += "net.databinder" %% "dispatch-oauth" % "0.8.6"

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.6"

libraryDependencies +=  "javax.servlet" % "servlet-api" % "2.5"

name := "PartakeList"
