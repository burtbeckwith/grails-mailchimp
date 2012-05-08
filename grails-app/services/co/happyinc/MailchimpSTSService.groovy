package com.wimh

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import org.springframework.beans.factory.InitializingBean


class MailchimpSTSService implements InitializingBean {

	boolean transactional = false
    def grailsApplication
	def http
	String apikey
	
	void afterPropertiesSet() {
		http = new HTTPBuilder( grailsApplication.config.gchimp.stsUrl )
		apikey = grailsApplication.config.gchimp.apiKey
	}
	
	//
	//	Helper method to reduce some of the code duplication
	//
	def call( path, body, callback ){
		body.apikey = apikey

		http.handler.failure = { res, json ->
    		println "STS error: HTTP ${res.statusLine} ${json}"
		}
		
		http.post(
			path : path,
			body: body,
			requestContentType : URLENC
		){ res, json ->
			println "STS ok:${json}"
			callback( res, json )
		}
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/sendemail.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/SendEmail \
		     -d apikey=MY_API_KEY \
		     -d message[html]=urlencode_some_html_content
		     -d message[text]=urlencode_some_html_content
		     -d message[from_email]=verified@example.com
		     -d message[from_name]=Me
		     -d message[to_email][0]=you@example.com
		     -d message[to_email][1]=yourmom@example.com
		     -d track_opens=true
		     -d track_clicks=false
		     -d tags[0]=WelcomeEmail
	*/
	def send( message, callback ){
		
		def i = 0
		def body = [
			track_opens : message.track_opens instanceof Boolean ? message.track_opens : true,
			track_clicks : message.track_clicks instanceof Boolean ? message.track_clicks : true,
			'message[html]' : message.html,
			'message[text]' : message.text,
			'message[from_email]' : message.from_email,
			'message[from_name]' : message.from_name,
			'message[subject]' : message.subject
		]
		
		message.to.split(",").each{ email -> 
			body["message[to_email][${i++}]"] = email
		}
		
		i = 0
		message.tags?.split(",")?.each{ tag -> 
			body["tags[${i++}]"] = tag
		}
		
		call( 'SendEmail', body, callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/listverifiedemailaddresses.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/ListVerifiedEmailAddresses \
			-d apikey=MY_API_KEY
	*/
	def listVerified( callback ){
		call( 'ListVerifiedEmailAddresses', [:], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/deleteverifiedemailaddress.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/DeleteVerifiedEmailAddress \
		     -d apikey=MY_API_KEY \
		     -d email=verified@example.com
	*/
	def deleteVerified( email, callback ){
		call( 'DeleteVerifiedEmailAddress', [ email : email ], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/verifyemailaddress.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/VerifyEmailAddress \
		     -d apikey=MY_API_KEY \
		     -d email=verified@example.com
	*/
	def verify( email, callback ){
		call( 'VerifyEmailAddress', [ email : email ], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/getbounces.func.php
	*/
	def getBounces( since, callback ){
		// since is a string in "Y-m-d H:i:s" format
		call( 'GetBounces', [since:since], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/getsendstats.func.php
	*/
	def getSendStats( tag_id, since, callback ){
		// since is a string in "Y-m-d H:i:s" format
		call( 'GetSendStats', [tag_id:tag_id,since:since], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/gettags.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/GetTags \
		     -d apikey=MY_API_KEY
	*/
	def getTags( callback ){
		call( 'GetTags', [:], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/geturlstats.func.php
	*/
	def getUrlStats( url_id, since, callback ){
		// since - is a string in "Y-m-d H:i:s" format
		// url_id - is an int referencing the URL record
		call( 'GetUrlStats', [url_id:url_id,since:since], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/geturls.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/GetTags \
		     -d apikey=MY_API_KEY
	*/
	def getUrls( callback ){
		call( 'GetUrls', [:], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/getsendquota.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/GetSendQuota \
		     -d apikey=MY_API_KEY
	*/
	def getSendQuota( callback ){
		call( 'GetSendQuota', [:], callback )
	}
	
	/*
		API docs: http://apidocs.mailchimp.com/sts/1.0/getsendstatistics.func.php
		curl -l http://us1.sts.mailchimp.com/1.0/GetSendStatistics \
		     -d apikey=MY_API_KEY
	*/
	def getSendStatistics( callback ){
		call( 'GetSendStatistics', [:], callback )
	}
}

