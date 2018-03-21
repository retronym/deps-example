scalaVersion := "2.12.4"

version in ThisBuild := "0.1-SNAPSHOT"

val a = project

val b = project.dependsOn(a)

val c = project.dependsOn(a)

val d = project.dependsOn(a)