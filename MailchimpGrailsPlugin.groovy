import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import co.happyinc.MailchimpService

class MailchimpGrailsPlugin {
	// the plugin version
	def version = "0.0.2"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.3.7 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
    def loadAfter = ['services']
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
			"grails-app/views/error.gsp"
	]

	// TODO Fill in these fields
	def author = "Richard Marr"
	def authorEmail = ""
	def title = "Grails Mailchimp Plugin"
	def description = '''\\
Simple API wrapper for the Mailchimp API 1.3 and Mailchimp STS API 1.0
'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/mailchimp"

	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional), this event occurs before 
	}

	def doWithSpring = {
	}

	def doWithDynamicMethods = { ctx ->
		// TODO Implement registering dynamic methods to classes (optional)
	}

	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)
	}

	def onChange = { event ->
		// TODO Implement code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}
}
