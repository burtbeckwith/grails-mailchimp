h1. Grails Mailchimp Plugin

A thin API wrapper for the *Mailchimp API 1.3* and *Mailchimp STS API 1.0*.

Exposes a service for each API:

h2. Configuration

In your Config.groovy you need the following lines:

		mailchimp.apiUrl = 'YOUR API endpoint' // e.g. 'https://us2.api.mailchimp.com/1.3/' but this depends on which datacentre your API key is valid for
		mailchimp.apiKey = 'YOUR API KEY'
		mailchimp.defaultListId = 'YOUR DEFAULT LIST ID'

h3. API reference:

There are a lot of API methods so I suggest looking them up in the Mailchimp docs and checking in the appropriate Service method

 * Mailchimp API 1.3: http://apidocs.mailchimp.com/api/1.3/
 * Mailchimp STS API 1.0: http://apidocs.mailchimp.com/sts/1.0/

h2. Example

API Endpoints are then exposed as service methods, each expecting at least a closure (depending on the required arguments of that method)

h3. Simple call

		mailchimpService.lists { res, json ->
			println res.data
			println json
		}		

h3. With an argument

		mailchimpService.campaignsForEmail "some.email@address.com", { res, json ->
			println res.data
			println json
		}

