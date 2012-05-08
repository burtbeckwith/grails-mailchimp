
A simple API wrapper for the Mailchimp API and Mailchimp STS API.

Exposes a service for each API:

Configuration
-------------

In your Config.groovy you need the following lines:

		mailchimp.apiUrl = 'YOUR API endpoint' // e.g. 'http://us2.api.mailchimp.com/1.3/' but this depends on which datacentre your API key is valid for
		mailchimp.apiKey = 'YOUR API KEY'
		mailchimp.defaultListId = 'YOUR DEFAULT LIST ID'

Example
-------

		mailchimpService.lists { res, json ->
			res.headers.each{ println it }
			println res.data
			println json
		}		


