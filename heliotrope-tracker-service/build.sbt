name := "heliotrope-tracker-service"

version := "0.1"

scalaVersion := "2.9.1"

seq(webSettings: _*)

seq(defaultScalariformSettings: _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= {
	val akkaVersion     = "2.0"
	val sprayVersion    = "1.0-M1"
	val salatVersion    = "0.0.8"
	val liftVersion     = "2.4"
	val specs2Version   = "1.9"
	val jettyVersion    = "8.1.0.v20120127"
	val slf4jVersion    = "1.6.4"
	val logbackVersion  = "1.0.0"
	val rogueVersion    = "1.1.5"
	val scalatraVersion = "2.0.4"
	Seq(
		"com.typesafe.akka"         %  "akka-actor"          % akkaVersion     % "compile",
		"com.typesafe.akka"         %  "akka-slf4j"          % akkaVersion,
		"cc.spray"                  %  "spray-server"        % sprayVersion    % "compile",
		"cc.spray"                  %  "spray-util"          % sprayVersion    % "compile",
		"org.specs2"                %% "specs2"              % specs2Version   % "test",
		"org.eclipse.jetty"         %  "jetty-webapp"        % jettyVersion    % "container",
		"org.slf4j"                 %  "slf4j-api"           % slf4jVersion,
		"ch.qos.logback"            %  "logback-classic"     % logbackVersion,
		"com.novus"                 %% "salat-core"          % salatVersion,
		"net.liftweb"               % "lift-json_2.9.1"      % liftVersion,
		"javax.servlet"             %  "servlet-api"         % "2.5"           % "provided->default"
	)
}

resolvers ++= Seq(
	"scalatools-releases" at "http://scala-tools.org/repo-releases",
	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "spray repository" at "http://repo.spray.cc/"
)
