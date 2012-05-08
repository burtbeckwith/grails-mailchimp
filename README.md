
A simple API wrapper for the *Mailchimp API 1.3* and *Mailchimp STS API 1.0*.

Exposes a service for each API:

Configuration
-------------

In your Config.groovy you need the following lines:

		mailchimp.apiUrl = 'YOUR API endpoint' // e.g. 'http://us2.api.mailchimp.com/1.3/' but this depends on which datacentre your API key is valid for
		mailchimp.apiKey = 'YOUR API KEY'
		mailchimp.defaultListId = 'YOUR DEFAULT LIST ID'

Mailchimp API reference:
------------------------

 * Mailchimp API 1.3: http://apidocs.mailchimp.com/api/1.3/
 * Mailchimp STS API 1.0: http://apidocs.mailchimp.com/sts/1.0/

Example
-------

API Endpoints are then exposed as service methods, each expecting at least a closure (depending on the required arguments of that method)

		mailchimpService.lists { res, json ->
			res.headers.each{ println it }
			println res.data
			println json
		}		


