/*
 * Google Ontology N4 Module - Gradle Build Script
 *
 * This build script uses Tridium's official Niagara Gradle plugins
 */

import com.tridium.gradle.plugins.module.util.ModulePart.RuntimeProfile.*

plugins {
  // The Niagara Module plugin configures the "moduleManifest" extension and the
  // "jar" and "moduleTestJar" tasks.
  id("com.tridium.niagara-module")

  // The signing plugin configures the correct signing of modules. It requires
  // that the plugin also be applied to the root project.
  id("com.tridium.niagara-signing")

  // The niagara_home repositories convention plugin configures !bin/ext and
  // !modules as flat-file Maven repositories so that projects in this build can
  // depend on already-installed Niagara modules.
  id("com.tridium.convention.niagara-home-repositories")

  // The Annotation processors plugin adds default dependencies on ":nre"
  // for the "annotationProcessor" and "moduleTestAnnotationProcessor"
  // configurations.
  id("com.tridium.niagara-annotation-processors")
}

description = "Google Digital Buildings Ontology mapper for Niagara 4"

moduleManifest {
  moduleName.set("googleOntology")
  runtimeProfile.set(rt)
  preferredSymbol.set("g")
  vendor.set("Custom")
}

// See documentation at module://docDeveloper/doc/build.html#dependencies for the supported
// dependency types
dependencies {
  // NRE dependencies
  nre(":nre")

  // Niagara module dependencies
  api(":baja")
  api(":control-rt")
  api(":web-rt")
  api(":box-rt")

  // Servlet API for HTTP endpoints
  uberjar("javax.servlet:javax.servlet-api:3.0.1")

  // Smile Machine Learning library
  // Pure Java, no native dependencies, perfect for Niagara!
  uberjar("com.github.haifengl:smile-core:3.1.1")
}
