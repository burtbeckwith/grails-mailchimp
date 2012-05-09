package co.happyinc

import grails.converters.JSON
import org.springframework.beans.factory.InitializingBean

//
//	Implements Mailchimp API 1.3 as per:
//	http://apidocs.mailchimp.com/api/1.3/
//
class MailchimpService implements InitializingBean {

	static transactional = false

	def grailsApplication

	private String encoding
	private String apiUrl
	private String defaultListId
	private String apiKey

	// startup
	void afterPropertiesSet() {
		apiUrl = grailsApplication.config.mailchimp.apiUrl ?: 'https://api.mailchimp.com/'
		println "--Using apiUrl ${apiUrl}"
		encoding = grailsApplication.config.mailchimp.encoding ?: 'UTF-8'
		apiKey = grailsApplication.config.mailchimp.apiKey
		defaultListId = grailsApplication.config.mailchimp.defaultListId
	}

	//	Helper method to handle HTTP and add the API key
	def call( String method, Map body = null, Closure callback ){

		println "METHOD: ${method}"
		def url = new URL(generateUrl( apiUrl, method ))
		def connection = url.openConnection()
		connection.setRequestMethod("POST")
		connection.doOutput = true

		def writer = new OutputStreamWriter(connection.outputStream)
		writeBody( writer, body )
		writer.flush()
		writer.close()
		connection.connect()

    	callback( parseResponse(connection.content.text) )
	}

	private String generateUrl(apiUrl, method) { "${apiUrl}?method=${method}" }

	/*
		#Return format is a JSON object
		http://<dc>.api.mailchimp.com/1.3/?method=listSubscribe
			&apikey=<apikey - captured from your API dashboard>
			&id=<list id - captured from lists()>
			&email_address=<email_address>
			&merge_vars[FNAME]=Firstname
			&merge_vars[LNAME]=Lastname
			&merge_vars[GROUPINGS][0][name]=Pets
			&merge_vars[GROUPINGS][0][groups]=Cat,Dog
			&merge_vars[GROUPINGS][1][name]=Food
			&merge_vars[GROUPINGS][1][groups]=Meat,Kibble
			&output=json
	*/
	private void writeBody(writer, body) {

		if ( !body ) body = [:]
		body.apikey = body.apikey ?: apiKey

		body.each { fieldKey, fieldValue ->
			encodeParams( writer, fieldKey, fieldValue )
		}
	}

	private void encodeParams(writer, prefix, value) {
		switch ( value ) {
			case Boolean:
				println("${prefix}=${value.toString().encodeAsURL()}&")
				writer.write("${prefix}=${value.toString().encodeAsURL()}&")
				break
			case Number:
				println("${prefix}=${value.toString().encodeAsURL()}&")
				writer.write("${prefix}=${value.toString().encodeAsURL()}&")
				break
			case String:
				println("${prefix}=${value.toString().encodeAsURL()}&")
				writer.write("${prefix}=${value.toString().encodeAsURL()}&")
				break
			case Map:
				value.each { subKey, subValue ->
					encodeParams( writer, "${prefix}[${subKey.encodeAsURL()}]", subValue )
				}
				break
			case List:
				value.eachWithIndex { subValue, i ->
					encodeParams( writer, "${prefix}[${i}]", subValue )
				}
				break
		}
	}

	private parseResponse(text) {
		try {
			println "Attempting to parse response: ${text}"
			def json = JSON.parse(text)
			if ( json.size() == 0 ) return text.replaceAll(/^"|"$/, "")
			return JSON.parse(text)
		} catch ( Exception e ) {
			e.printStackTrace()
			return text
		}
	}

	//
	// Below are a bunch of optional API wrappers to guide calls
	////////////////////////////////////////////////////////////


	//
	// Campaign Related Methods
	//

	// Get the content (both html and text) for a campaign either as it would appear in the campaign archive or as the raw, original content
	// http://apidocs.mailchimp.com/api/1.3/campaigncontent.func.php
	def campaignContent( String cid, Boolean for_archive, Closure callback ){
		call( 'campaignContent', [ cid:cid, for_archive:for_archive ], callback )
	}

	// Create a new draft campaign to send.
	// http://apidocs.mailchimp.com/api/1.3/campaigncreate.func.php
	def campaignCreate( String type, Map options, Map content, Map segment_opts = null, Map type_opts = null, Closure callback ){
		call( 'campaignCreate', [ type:type, options:options, content:content, segment_opts:segment_opts, type_opts:type_opts ], callback )
	}

	// Delete a campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaigndelete.func.php
	def campaignDelete( String cid, Closure callback ){
		call( 'campaignDelete', [ cid:cid ], callback )
	}

	// Attach Ecommerce Order Information to a Campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaignecommorderadd.func.php
	def campaignEcommOrderAdd( Map order, Closure callback ){
		call( 'campaignEcommOrderAdd', [ order:order ], callback )
	}

	// Pause an AutoResponder or RSS campaign from sending
	// http://apidocs.mailchimp.com/api/1.3/campaignpause.func.php
	def campaignPause( String cid, Closure callback ){
		call( 'campaignPause', [ cid:cid ], callback )
	}

	// Replicate a campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaignreplicate.func.php
	def campaignReplicate( String cid, Closure callback ){
		call( 'campaignReplicate', [ cid:cid ], callback )
	}

	// Resume sending an AutoResponder or RSS campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignresume.func.php
	def campaignResume( String cid, Closure callback ){
		call( 'campaignResume', [ cid:cid ], callback )
	}

	// Schedule a campaign to be sent in the future
	// http://apidocs.mailchimp.com/api/1.3/campaignschedule.func.php
	def campaignSchedule( String cid, String schedule_time, String schedule_time_b, Closure callback ){
		call( 'campaignSchedule', [ cid:cid, schedule_time:schedule_time, schedule_time_b:schedule_time_b ], callback )
	}

	// Allows one to test their segmentation rules before creating a campaign using them
	// http://apidocs.mailchimp.com/api/1.3/campaignsegmenttest.func.php
	def campaignSegmentTest( String list_id, Map options, Closure callback ){
		call( 'campaignSegmentTest', [ list_id:list_id, options:options ], callback )
	}

	// Send a given campaign immediately.
	// http://apidocs.mailchimp.com/api/1.3/campaignsendnow.func.php
	def campaignSendNow( String cid, Closure callback ){
		call( 'campaignSendNow', [ cid:cid ], callback )
	}

	// Send a test of this campaign to the provided email address
	// http://apidocs.mailchimp.com/api/1.3/campaignsendtest.func.php
	def campaignSendTest( String cid, List test_emails, String send_type, Closure callback ){
		call( 'campaignSendTest', [ cid:cid, test_emails:test_emails, send_type:send_type ], callback )
	}

	// Get the URL to a customized VIP Report for the specified campaign and optionally send an email to someone with links to it.
	// http://apidocs.mailchimp.com/api/1.3/campaignsharereport.func.php
	def campaignShareReport( String cid, Map opts, Closure callback ){
		call( 'campaignShareReport', [ cid:cid, opts:opts ], callback )
	}

	// Get the HTML template content sections for a campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaigntemplatecontent.func.php
	def campaignTemplateContent( String cid, Closure callback ){
		call( 'campaignTemplateContent', [ cid:cid ], callback )
	}

	// Unschedule a campaign that is scheduled to be sent in the future
	// http://apidocs.mailchimp.com/api/1.3/campaignunschedule.func.php
	def campaignUnschedule( String cid, Closure callback ){
		call( 'campaignUnschedule', [ cid:cid ], callback )
	}

	// Update just about any setting for a campaign that has not been sent.
	// http://apidocs.mailchimp.com/api/1.3/campaignupdate.func.php
	def campaignUpdate( String cid, String name, Object value, Closure callback ){
		call( 'campaignUpdate', [ cid:cid, name:name, value:value ], callback )
	}

	// Get the list of campaigns and their details matching the specified filters
	// http://apidocs.mailchimp.com/api/1.3/campaigns.func.php
	def campaigns( Map filters = [:], def start = null, def limit = null, Closure callback ){
		call( 'campaigns', [ filters:filters, start:start, limit:limit ], callback )
	}

	//
	// Campaign Stats Methods
	//

	// Get all email addresses that complained about a given campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignabusereports.func.php
	def campaignAbuseReports( String cid, String since = null, def start = null, def limit = null, Closure callback ){
		call( 'campaignAbuseReports', [ cid:cid, since:since, start:start, limit:limit ], callback )
	}

	// Retrieve the text presented in our app for how a campaign performed and any advice we may have for you - best suited for display in customized reports pages.
	// http://apidocs.mailchimp.com/api/1.3/campaignadvice.func.php
	def campaignAdvice( String cid, Closure callback ){
		call( 'campaignAdvice', [ cid:cid ], callback )
	}

	// Retrieve the Google Analytics data we've collected for this campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaignanalytics.func.php
	def campaignAnalytics( String cid, Closure callback ){
		call( 'campaignAnalytics', [ cid:cid ], callback )
	}

	// Retrieve the most recent full bounce message for a specific email address on the given campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaignbouncemessage.func.php
	def campaignBounceMessage( String cid, String email, Closure callback ){
		call( 'campaignBounceMessage', [ cid:cid, email:email ], callback )
	}

	// Retrieve the full bounce messages for the given campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaignbouncemessages.func.php
	def campaignBounceMessages( String cid, def start = null, def limit = null, String since = null, Closure callback ){
		call( 'campaignBounceMessages', [ cid:cid, start:start, limit:limit, since:since ], callback )
	}

	// Get an  of the urls being tracked, and their click counts for a given campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignclickstats.func.php
	def campaignClickStats( String cid, Closure callback ){
		call( 'campaignClickStats', [ cid:cid ], callback )
	}

	// Retrieve the Ecommerce Orders tracked by campaignEcommOrderAdd()
	// http://apidocs.mailchimp.com/api/1.3/campaignecommorders.func.php
	def campaignEcommOrders( String cid, def start = null, def limit = null, String since, Closure callback ){
		call( 'campaignEcommOrders', [ cid:cid, start:start, limit:limit, since:since ], callback )
	}

	// Retrieve the tracked eepurl mentions on Twitter
	// http://apidocs.mailchimp.com/api/1.3/campaigneepurlstats.func.php
	def campaignEepUrlStats( String cid, Closure callback ){
		call( 'campaignEepUrlStats', [ cid:cid ], callback )
	}

	// Get the top 5 performing email domains for this campaign.
	// http://apidocs.mailchimp.com/api/1.3/campaignemaildomainperformance.func.php
	def campaignEmailDomainPerformance( String cid, Closure callback ){
		call( 'campaignEmailDomainPerformance', [ cid:cid ], callback )
	}

	// Retrieve the countries and number of opens tracked for each.
	// http://apidocs.mailchimp.com/api/1.3/campaigngeoopens.func.php
	def campaignGeoOpens( String cid, Closure callback ){
		call( 'campaignGeoOpens', [ cid:cid ], callback )
	}

	// Retrieve the regions and number of opens tracked for a certain country.
	// http://apidocs.mailchimp.com/api/1.3/campaigngeoopensforcountry.func.php
	def campaignGeoOpensForCountry( String cid, String code, Closure callback ){
		call( 'campaignGeoOpensForCountry', [ cid:cid, code:code ], callback )
	}

	// DEPRECATED Get all email addresses with Hard Bounces for a given campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignhardbounces.func.php
	def campaignHardBounces( String cid, def start = null, def limit = null, Closure callback ){
		call( 'campaignHardBounces', [ cid:cid, start:start, limit:limit ], callback )
	}

	// Get all email addresses the campaign was successfully sent to (ie, no bounces)
	// http://apidocs.mailchimp.com/api/1.3/campaignmembers.func.php
	def campaignMembers( String cid, String status = null, def start = null, def limit = null, Closure callback ){
		call( 'campaignMembers', [ cid:cid, status:status, start:start, limit:limit ], callback )
	}

	// DEPRECATED Get all email addresses with Soft Bounces for a given campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignsoftbounces.func.php
	def campaignSoftBounces( String cid, def start = null, def limit = null, Closure callback ){
		call( 'campaignSoftBounces', [ cid:cid, start:start, limit:limit ], callback )
	}

	// Given a list and a campaign, get all the relevant campaign statistics (opens, bounces, clicks, etc.)
	// http://apidocs.mailchimp.com/api/1.3/campaignstats.func.php
	def campaignStats( String cid, Closure callback ){
		call( 'campaignStats', [ cid:cid ], callback )
	}

	// Get all unsubscribed email addresses for a given campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignstats.func.php
	def campaignUnsubscribes( String cid, def start = null, def limit = null, Closure callback ){
		call( 'campaignUnsubscribes', [ cid:cid, start:start, limit:limit ], callback )
	}

	//
	// Campaign Report Data Methods
	//

	// Return the list of email addresses that clicked on a given url, and how many times they clicked
	// http://apidocs.mailchimp.com/api/1.3/campaignclickdetailaim.func.php
	def campaignClickDetailAIM( String cid, String url, def start = null, def limit = null, Closure callback ){
		call( 'campaignClickDetailAIM', [ cid:cid, url:url, start:start, limit:limit ], callback )
	}

	// Given a campaign and email address, return the entire click and open history with timestamps, ordered by time
	// http://apidocs.mailchimp.com/api/1.3/campaignemailstatsaim.func.php
	def campaignEmailStatsAIM( String cid, String email_address, Closure callback ){
		call( 'campaignEmailStatsAIM', [ cid:cid, email_address:email_address ], callback )
	}

	// Given a campaign and correct paging limits, return the entire click and open history with timestamps, ordered by time, for every user a campaign was delivered to.
	// http://apidocs.mailchimp.com/api/1.3/campaignemailstatsaimall.func.php
	def campaignEmailStatsAIMAll( String cid, def start = null, def limit = null, Closure callback ){
		call( 'campaignEmailStatsAIMAll', [ cid:cid, start:start, limit:limit ], callback )
	}

	// Retrieve the list of email addresses that did not open a given campaign
	// http://apidocs.mailchimp.com/api/1.3/campaignnotopenedaim.func.php
	def campaignNotOpenedAIM( String cid, def start = null, def limit = null, Closure callback ){
		call( 'campaignNotOpenedAIM', [ cid:cid, start:start, limit:limit ], callback )
	}

	// Retrieve the list of email addresses that opened a given campaign with how many times they opened - note: this AIM function is free and does not actually require the AIM module to be installed
	// http://apidocs.mailchimp.com/api/1.3/campaignopenedaim.func.php
	def campaignOpenedAIM( String cid, def start = null, def limit = null, Closure callback ){
		call( 'campaignOpenedAIM', [ cid:cid, start:start, limit:limit ], callback )
	}

	//
	// Ecommerce Methods
	//

	// Import Ecommerce Order Information to be used for Segmentation.
	// http://apidocs.mailchimp.com/api/1.3/ecommorderadd.func.php
	def ecommOrderAdd( Map order = [:], Closure callback ){
		call( 'ecommOrderAdd', [ order:order ], callback )
	}

	// Delete Ecommerce Order Information used for segmentation.
	// http://apidocs.mailchimp.com/api/1.3/ecommorderdel.func.php
	def ecommOrderDel( String store_id, String order_id, Closure callback ){
		call( 'ecommOrderDel', [ store_id:store_id, order_id:order_id ], callback )
	}

	// Retrieve the Ecommerce Orders for an account
	// http://apidocs.mailchimp.com/api/1.3/ecommorders.func.php
	def ecommOrders( def start = null, def limit = null, String since = null, Closure callback ){
		call( 'ecommOrders', [ since:since, start:start, limit:limit ], callback )
	}

	//
	// Folder Related Methods
	//

	// Add a new folder to file campaigns or autoresponders in
	// http://apidocs.mailchimp.com/api/1.3/folderadd.func.php
	def folderAdd( String name, String type = null, Closure callback ){
		call( 'folderAdd', [ name:name, type:type ], callback )
	}

	// Delete a campaign or autoresponder folder.
	// http://apidocs.mailchimp.com/api/1.3/folderdel.func.php
	def folderDel( String fid, String type = null, Closure callback ){
		call( 'folderDel', [ fid:fid, type:type ], callback )
	}

	// Update the name of a folder for campaigns or autoresponders
	// http://apidocs.mailchimp.com/api/1.3/folderupdate.func.php
	def folderUpdate( String fid, String name, String type = null, Closure callback ){
		call( 'folderUpdate', [ fid:fid, name:name, type:type ], callback )
	}

	// List all the folders for a user account
	// http://apidocs.mailchimp.com/api/1.3/folders.func.php
	def folders( String type = null, Closure callback ){
		call( 'folders', [ type:type ], callback )
	}

	//
	// Golden Monkeys Methods
	//

	// Retrieve all Activity (opens/clicks) for Golden Monkeys over the past 10 days
	// http://apidocs.mailchimp.com/api/1.3/gmonkeyactivity.func.php
	def gmonkeyActivity( Closure callback ){
		call( 'gmonkeyActivity', callback )
	}

	// Add Golden Monkey(s)
	// http://apidocs.mailchimp.com/api/1.3/gmonkeyadd.func.php
	def gmonkeyAdd( String id, List email_addresses, Closure callback ){
		call( 'gmonkeyAdd', [ id:id, email_addresses:email_addresses ], callback )
	}

	// Remove Golden Monkey(s)
	// http://apidocs.mailchimp.com/api/1.3/gmonkeydel.func.php
	def gmonkeyDel( String id, List email_addresses, Closure callback ){
		call( 'gmonkeyDel', [ id:id, email_addresses:email_addresses ], callback )
	}

	// Retrieve all Golden Monkey(s) for an account
	// http://apidocs.mailchimp.com/api/1.3/gmonkeymembers.func.php
	def gmonkeyMembers( Closure callback ){
		call( 'gmonkeyMembers', callback )
	}

	//
	// Helper Methods
	//

	// Retrieve all Campaigns Ids a member was sent
	// http://apidocs.mailchimp.com/api/1.3/campaignsforemail.func.php
	def campaignsForEmail( String email_address, Map options = null, Closure callback ){
		call( 'campaignsForEmail', [ email_address:email_address, options:options ], callback )
	}

	// Return the current Chimp Chatter messages for an account.
	// http://apidocs.mailchimp.com/api/1.3/chimpchatter.func.php
	def chimpChatter( Closure callback ){
		call( 'chimpChatter', callback )
	}

	// Have HTML content auto-converted to a text-only format.
	// http://apidocs.mailchimp.com/api/1.3/generatetext.func.php
	def generateText( String type, String content, Closure callback ){
		call( 'generateText', [ type:type, content:content ], callback )
	}

	// Retrieve lots of account information including payments made, plan info, some account stats, installed modules, contact info, and more.
	// http://apidocs.mailchimp.com/api/1.3/getaccountdetails.func.php
	def getAccountDetails( Closure callback ){
		call( 'getAccountDetails', callback )
	}

	// Send your HTML content to have the CSS inlined and optionally remove the original styles.
	// http://apidocs.mailchimp.com/api/1.3/inlinecss.func.php
	def inlineCss( String html, Boolean strip_css = null, Closure callback ){
		call( 'inlineCss', [ html:html, strip_css:strip_css ], callback )
	}

	// Retrieve all List Ids a member is subscribed to.
	// http://apidocs.mailchimp.com/api/1.3/listsforemail.func.php
	def listsForEmail( String email_address, Closure callback ){
		call( 'listsForEmail', [ email_address:email_address ], callback )
	}

	// "Ping" the MailChimp API - a simple method you can call that will return a constant value as long as everything is good.
	// http://apidocs.mailchimp.com/api/1.3/ping.func.php
	def ping( Closure callback ){
		call( 'ping', callback )
	}

	//
	// List Related Methods
	//

	// Get all email addresses that complained about a given campaign
	// http://apidocs.mailchimp.com/api/1.3/listabusereports.func.php
	def listAbuseReports( String id, def start = null, def limit = null, String since = null, Closure callback ){
		call( 'listAbuseReports', [ id:id, since:since, start:start, limit:limit ], callback )
	}

	// Access up to the previous 180 days of daily detailed aggregated activity stats for a given list
	// http://apidocs.mailchimp.com/api/1.3/listactivity.func.php
	def listActivity( String id, Closure callback ){
		call( 'listActivity', [ id:id ], callback )
	}

	// Subscribe a batch of email addresses to a list at once.
	// http://apidocs.mailchimp.com/api/1.3/listbatchsubscribe.func.php
	def listBatchSubscribe( String id, List batch, Boolean double_optin = null, Boolean update_existing = null, Boolean replace_interests = null, Closure callback ){
		call( 'listBatchSubscribe', [ id:id, batch:batch, double_optin:double_optin, update_existing:update_existing, replace_interests:replace_interests ], callback )
	}

	// Unsubscribe a batch of email addresses to a list
	// http://apidocs.mailchimp.com/api/1.3/listbatchunsubscribe.func.php
	def listBatchUnsubscribe( String id, List emails, Boolean delete_member = null, Boolean send_goodbye = null, Boolean send_notify = null, Closure callback ){
		call( 'listBatchUnsubscribe', [ id:id, emails:emails, delete_member:delete_member, send_goodbye:send_goodbye, send_notify:send_notify ], callback )
	}

	// Retrieve the clients that the list's subscribers have been tagged as being used based on user agents seen.
	// http://apidocs.mailchimp.com/api/1.3/listclients.func.php
	def listClients( String id, Closure callback ){
		call( 'listClients', [ id:id ], callback )
	}

	// Access the Growth History by Month for a given list.
	// http://apidocs.mailchimp.com/api/1.3/listgrowthhistory.func.php
	def listGrowthHistory( String id, Closure callback ){
		call( 'listGrowthHistory', [ id:id ], callback )
	}

	// Add a single Interest Group - if interest groups for the List are not yet enabled, adding the first group will automatically turn them on.
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupadd.func.php
	def listInterestGroupAdd( String id, String group_name, def grouping_id = null, Closure callback ){
		call( 'listInterestGroupAdd', [id:id, group_name:group_name, grouping_id:grouping_id ], callback )
	}

	// Delete a single Interest Group - if the last group for a list is deleted, this will also turn groups for the list off.
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupdel.func.php
	def listInterestGroupDel( String id, String group_name, def grouping_id = null, Closure callback ){
		call( 'listInterestGroupDel', [ id:id, group_name:group_name, grouping_id:grouping_id ], callback )
	}

	// Change the name of an Interest Group
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupupdate.func.php
	def listInterestGroupUpdate( String id, String old_name, String new_name, def grouping_id = null, Closure callback ){
		call( 'listInterestGroupUpdate', [ id:id, old_name:old_name, new_name:new_name, grouping_id:grouping_id ], callback )
	}

	// Add a new Interest Grouping - if interest groups for the List are not yet enabled, adding the first grouping will automatically turn them on.
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupingadd.func.php
	def listInterestGroupingAdd( String id, String name, String type, List groups, Closure callback ){
		call( 'listInterestGroupingAdd', [ id:id, name:name, type:type, groups:groups ], callback )
	}

	// Delete an existing Interest Grouping - this will permanently delete all contained interest groups and will remove those selections from all list members
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupingdel.func.php
	def listInterestGroupingDel( Integer grouping_id, Closure callback ){
		call( 'listInterestGroupingDel', [ grouping_id:grouping_id ], callback )
	}

	// Update an existing Interest Grouping
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupingupdate.func.php
	def listInterestGroupingUpdate( Integer grouping_id, String name, String value, Closure callback ){
		call( 'listInterestGroupingUpdate', [ grouping_id:grouping_id, name:name, value:value ], callback )
	}

	// Get the list of interest groupings for a given list, including the label, form information, and included groups for each
	// http://apidocs.mailchimp.com/api/1.3/listinterestgroupings.func.php
	def listInterestGroupings( String id,  apikey, Closure callback ){
		call( 'listInterestGroupings', [ id:id ], callback )
	}

	// Retrieve the locations (countries) that the list's subscribers have been tagged to based on geocoding their IP address
	// http://apidocs.mailchimp.com/api/1.3/listlocations.func.php
	def listLocations( String id, Closure callback ){
		call( 'listLocations', [ id:id ], callback )
	}

	// Get the most recent 100 activities for particular list members (open, click, bounce, unsub, abuse, sent to )
	// http://apidocs.mailchimp.com/api/1.3/listmemberactivity.func.php
	def listMemberActivity( String id, String email_address, Closure callback ){
		call( 'listMemberActivity', [ id:id, email_address:email_address ], callback )
	}

	// Get all the information for particular members of a list
	// http://apidocs.mailchimp.com/api/1.3/listmemberinfo.func.php
	def listMemberInfo( String id, String email_address, Closure callback ){
		call( 'listMemberInfo', [ id:id, email_address:email_address ], callback )
	}

	// Get all of the list members for a list that are of a particular status.
	// http://apidocs.mailchimp.com/api/1.3/listmembers.func.php
	def listMembers( String id, String status, String since = null, def start = null, def limit = null, Closure callback ){
		call( 'listMembers', [ id:id, status:status, since:since, start:start, limit:limit ], callback )
	}

	// Add a new merge tag to a given list
	// http://apidocs.mailchimp.com/api/1.3/listmergevaradd.func.php
	def listMergeVarAdd( String id, String tag, String name, Map options = [:], Closure callback ){
		call( 'listMergeVarAdd', [ id:id, tag:tag, name:name, options:options ], callback )
	}

	// Delete a merge tag from a given list and all its members.
	// http://apidocs.mailchimp.com/api/1.3/listmergevardel.func.php
	def listMergeVarDel( String id, String tag, Closure callback ){
		call( 'listMergeVarDel', [ id:id, tag:tag ], callback )
	}

	// Update most parameters for a merge tag on a given list.
	// http://apidocs.mailchimp.com/api/1.3/listmergevarupdate.func.php
	def listMergeVarUpdate( String id, String tag, Map options, Closure callback ){
		call( 'listMergeVarUpdate', [ id:id, tag:tag, options:options ], callback )
	}

	// Get the list of merge tags for a given list, including their name, tag, and required setting
	// http://apidocs.mailchimp.com/api/1.3/listmergevars.func.php
	def listMergeVars( String id, Closure callback ){
		call( 'listMergeVars', [ id:id ], callback )
	}

	// Save a segment against a list for later use.
	// http://apidocs.mailchimp.com/api/1.3/liststaticsegmentadd.func.php
	def listStaticSegmentAdd( String id,  String name, Closure callback ){
		call( 'listStaticSegmentAdd', [ id:id, name:name ], callback )
	}

	// Delete a static segment.
	// http://apidocs.mailchimp.com/api/1.3/liststaticsegmentdel.func.php
	def listStaticSegmentDel( String id, Integer seg_id, Closure callback ){
		call( 'listStaticSegmentDel', [], callback )
	}

	// Add list members to a static segment.
	// http://apidocs.mailchimp.com/api/1.3/liststaticsegmentmembersadd.func.php
	def listStaticSegmentMembersAdd( String id, Integer seg_id, List batch, Closure callback ){
		call( 'listStaticSegmentMembersAdd', [ id:id, seg_id:seg_id, batch:batch ], callback )
	}

	// Remove list members from a static segment.
	// http://apidocs.mailchimp.com/api/1.3/liststaticsegmentmembersdel.func.php
	def listStaticSegmentMembersDel( String id, Integer seg_id, List batch, Closure callback ){
		call( 'listStaticSegmentMembersDel', [ id:id, seg_id:seg_id, batch:batch ], callback )
	}

	// Resets a static segment - removes all members from the static segment.
	// http://apidocs.mailchimp.com/api/1.3/liststaticsegmentreset.func.php
	def listStaticSegmentReset( String id, Integer seg_id, Closure callback ){
		call( 'listStaticSegmentReset', [ id:id, seg_id:seg_id ], callback )
	}

	// Retrieve all of the Static Segments for a list.
	// http://apidocs.mailchimp.com/api/1.3/liststaticsegments.func.php
	def listStaticSegments( String id, Closure callback ){
		call( 'listStaticSegments', [ id:id ], callback )
	}

	// Subscribe the provided email to a list.
	// http://apidocs.mailchimp.com/api/1.3/listsubscribe.func.php
	def listSubscribe( String id, String email_address, Map merge_vars, String email_type, Boolean double_optin, Boolean update_existing, Boolean replace_interests, Boolean send_welcome, Closure callback ){
		call( 'listSubscribe', [ id:id, email_address:email_address, merge_vars:merge_vars, email_type:email_type, double_optin:double_optin, update_existing:update_existing, replace_interests:replace_interests, send_welcome:send_welcome ], callback )
	}

	// Unsubscribe the given email address from the list
	// http://apidocs.mailchimp.com/api/1.3/listunsubscribe.func.php
	def listUnsubscribe( String id, String email_address, Boolean delete_member, Boolean send_goodbye, Boolean send_notify, Closure callback ){
		call( 'listUnsubscribe', [ id:id, email_address:email_address, delete_member:delete_member, send_goodbye:send_goodbye, send_notify:send_notify ], callback )
	}

	// Edit the email address, merge fields, and interest groups for a list member.
	// http://apidocs.mailchimp.com/api/1.3/listupdatemember.func.php
	def listUpdateMember( String id, String email_address, Map merge_vars, String email_type = null, Boolean replace_interests = null, Closure callback ){
		call( 'listUpdateMember', [ id:id, email_address:email_address, merge_vars:merge_vars, email_type:email_type, replace_interests:replace_interests ], callback )
	}

	// Add a new Webhook URL for the given list
	// http://apidocs.mailchimp.com/api/1.3/listwebhookadd.func.php
	def listWebhookAdd( String id, String url, Map actions = null, Map sources = null, Closure callback ){
		call( 'listWebhookAdd', [ id:id, url:url, actions:actions, sources:sources ], callback )
	}

	// Delete an existing Webhook URL from a given list
	// http://apidocs.mailchimp.com/api/1.3/listwebhookdel.func.php
	def listWebhookDel( String id, String url, Closure callback ){
		call( 'listWebhookDel', [ id:id, url:url ], callback )
	}

	// Return the Webhooks configured for the given list
	// http://apidocs.mailchimp.com/api/1.3/listwebhooks.func.php
	def listWebhooks( String id, Closure callback ){
		call( 'listWebhooks', [ id:id ], callback )
	}

	// Retrieve all of the lists defined for your user account
	// http://apidocs.mailchimp.com/api/1.3/lists.func.php
	def lists( def filters = [:], def start = null, def limit = null, Closure callback ){
		call( 'lists', [ filters:filters, start:start, limit:limit ], callback )
	}

	//
	// Security Related Methods
	//

	// Add an API Key to your account.
	// http://apidocs.mailchimp.com/api/1.3/apikeyadd.func.php
	def apikeyAdd( String username, String password, String apikey, Closure callback ){
		call( 'apikeyAdd', [ username:username, password:password, apikey:apikey ], callback )
	}

	// Expire a Specific API Key.
	// http://apidocs.mailchimp.com/api/1.3/apikeyexpire.func.php
	def apikeyExpire( String username, String password,  apikey, Closure callback ){
		call( 'apikeyExpire', [ username:username, password:password, apikey:apikey ], callback )
	}

	// Retrieve a list of all MailChimp API Keys for this User
	// http://apidocs.mailchimp.com/api/1.3/apikeys.func.php
	def apikeys( String username, String password, Boolean expired = null, Closure callback ){
		call( 'apikeys', [ username:username, password:password, expired:expired ], callback )
	}

	//
	// Template Related Methods
	//

	// Create a new user template, NOT campaign content.
	// http://apidocs.mailchimp.com/api/1.3/templateadd.func.php
	def templateAdd( String name, String html, Closure callback ){
		call( 'templateAdd', [ name:name, html:html ], callback )
	}

	// Delete (deactivate) a user template
	// http://apidocs.mailchimp.com/api/1.3/templatedel.func.php
	def templateDel( Integer id, Closure callback ){
		call( 'templateDel', [ id:id ], callback )
	}

	// Pull details for a specific template to help support editing
	// http://apidocs.mailchimp.com/api/1.3/templateinfo.func.php
	def templateInfo( Integer tid, String type = null, Closure callback ){
		call( 'templateInfo', [ tid:tid, type:type ], callback )
	}

	// Undelete (reactivate) a user template
	// http://apidocs.mailchimp.com/api/1.3/templateundel.func.php
	def templateUndel( Integer id, Closure callback ){
		call( 'templateUndel', [ id:id ], callback )
	}

	// Replace the content of a user template, NOT campaign content.
	// http://apidocs.mailchimp.com/api/1.3/templateupdate.func.php
	def templateUpdate( Integer id, Map values, Closure callback ){
		call( 'templateUpdate', [ id:id, values:values ], callback )
	}

	// Retrieve various templates available in the system, allowing some thing similar to our template gallery to be created.
	// http://apidocs.mailchimp.com/api/1.3/templates.func.php
	def templates( Map types = [:], Map inactives = [:], String category, Closure callback ){
		call( 'templates', [ types:types, inactives:inactives, category:category ], callback )
	}
}
