import sbt._

object Version {
  final val Akka                     = "2.4.4"
  final val AkkaHttpJson             = "1.6.0"
  final val AkkaLog4j                = "1.1.3"
  final val AkkaPersistenceCassandra = "0.13"
  final val AkkaPersistenceInmemory  = "1.2.12"
  final val AkkaSse                  = "1.7.3"
  final val Circe                    = "0.4.1"
  final val CommonsValidator         = "1.5.0"
  final val Constructr               = "0.13.2"
  final val Log4j                    = "2.5"
  final val Scala                    = "2.11.8"
  final val ScalaTest                = "2.2.6"
}

object Library {
  val akkaClusterSharding        = "com.typesafe.akka"        %% "akka-cluster-sharding"        % Version.Akka
  val akkaClusterTools           = "com.typesafe.akka"        %% "akka-cluster-tools"           % Version.Akka
  val akkaHttp                   = "com.typesafe.akka"        %% "akka-http-experimental"       % Version.Akka
  val akkaHttpCirce              = "de.heikoseeberger"        %% "akka-http-circe"              % Version.AkkaHttpJson
  val akkaHttpTestkit            = "com.typesafe.akka"        %% "akka-http-testkit"            % Version.Akka
  val akkaLog4j                  = "de.heikoseeberger"        %% "akka-log4j"                   % Version.AkkaLog4j
  val akkaPersistenceCassandra   = "com.typesafe.akka"        %% "akka-persistence-cassandra"   % Version.AkkaPersistenceCassandra
  val akkaPersistenceInmemory    = "com.github.dnvriend"      %% "akka-persistence-inmemory"    % Version.AkkaPersistenceInmemory
  val akkaSse                    = "de.heikoseeberger"        %% "akka-sse"                     % Version.AkkaSse
  val akkaTestkit                = "com.typesafe.akka"        %% "akka-testkit"                 % Version.Akka
  val circeGeneric               = "io.circe"                 %% "circe-generic"                % Version.Circe
  val commonsValidator           = "commons-validator"        %  "commons-validator"            % Version.CommonsValidator
  val constructrAkka             = "de.heikoseeberger"        %% "constructr-akka"              % Version.Constructr
  val constructrCoordinationEtcd = "de.heikoseeberger"        %% "constructr-coordination-etcd" % Version.Constructr
  val log4jCore                  = "org.apache.logging.log4j" %  "log4j-core"                   % Version.Log4j
  val log4jSlf4jImpl             = "org.apache.logging.log4j" %  "log4j-slf4j-impl"             % Version.Log4j
  val scalaTest                  = "org.scalatest"            %% "scalatest"                    % Version.ScalaTest
}
