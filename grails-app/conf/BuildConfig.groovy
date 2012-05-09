grails.project.work.dir = 'target'
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
	}

	dependencies {
		compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.0') {
			excludes "commons-logging", "xml-apis", "groovy"
		}
	}

	plugins {
		build(':release:2.0.0', ':rest-client-builder:1.0.2') {
			export = false
		}
	}
}
