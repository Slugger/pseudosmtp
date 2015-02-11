# pseudoSMTP
A testing tool for apps that produce and send emails via SMTP.

## Build Status
`master`: [![Build Status](https://travis-ci.org/Slugger/pseudosmtp.svg?branch=master)](https://travis-ci.org/Slugger/pseudosmtp)

## About
Testing apps that produce and send emails can be bothersome.  How have you done it in the past?  Create a throwaway Gmail account, send email to it, login and inspect the results?  Maybe you automated the validation by accessing the emails via IMAP or POP3?  In either case, are you really exercising your application completely?  Maybe, but how difficult is it to ensure/force your app to always and only send email to this throwaway Gmail account?  And what happens when someone forgets to do that, launches full integration/regression testing, and you end up sending test emails to everyone on your team or in your department/organization/company/etc.?  Usually you get a lot of replies with a not so nice tone to them, especially when it happens more than once.

What if there was a better way!?  What if you could deploy an SMTP server in your environment that your testers (and developers) could use as the SMTP server that receives all emails and simply stores them for inspection & validation?  Wouldn't that be great?  Well that's exactly what pseudoSMTP (psmtp) is for.  Simply configure your application under test to use the deployed pseudoSMTP server as its target for all outgoing email.  Any and all email is gladly accepted and stored within a database by psmtp and it will **never, ever, ever actually deliver (or even try to deliver) any email to any listed recipients.**  Hence, the pseudo part of the app's name.  No more having to assign the same email address to every user in your app during testing and then trying to sort out which emails went to whom and whether that was correct.  Assign unique addresses to every user, point your app to the deployed psmtp instance and let it fire off emails at will!

## How?
Psmtp is implemented as a J2EE web app to be deployed in your favourite container.  Grab the latest war file, optionally configure the app, deploy it and start using it by having your development and QA teams direct their app instance to use psmtp as its SMTP server.

There is also a standalone build of psmtp avaiable that can be executed directly from the command line.  See the Install & Configure howto for more details.

## How to validate?
Ok, so how does one validate the emails that were sent?  Along with an SMTP server, psmtp also provides a web app that exposes the received email via a REST API.  Tests can query the REST API for emails of interest then download them and validate the contents.  It's that simple.  All a tester needs to know is which host the email was sent from as psmtp keeps a separate *mailbox* of emails for each IP address that it receives email from.  As long as you know who (as in which host) sent the email, you can search for it quite easily via the REST API with queries like:

```
http://mytomcat.mycompany.com:8080/psmtp/api/messages?clnt=my-app-under-test.mycompany.com
```

That REST query will return a list of all available emails on the psmtp server received from the host name `my-app-under-test.mycompany.com` where hostname my-app-under-test is housing the app being tested and sending out emails.

With the list of emails, you can then pull down the entire contents of an email by id (as a complete MIME message stream) via another REST call:

```
http://mytomcat.mycompany.com:8080/psmtp/api/messages/1200?clnt=my-app-under-test.mycompany.com
```
(where 1200 is one of the ids returned in the list from the first REST request)

From there, you parse the MIME stream with your favourite library (i.e. javamail if using Java) and then inspect the details of the email message to validate your test conditions.

### Filtering
The REST API also provides additional filtering so you can find specific email(s) of interest.  Maybe your testing that bcc recipients are properly applied so you want to make sure emails were bcc'd to `joe@bigcompany.com`:

```
http://mytomcat.mycompany.com:8080/psmtp/api/messages?clnt=my-app-under-test.mycompany.com&bcc=joe%40bigcompany.com
```

So this request will return only the list of emails that include `joe@bigcompany.com` as a bcc recipient.  There are plenty of filtering options available and they are discussed further in the [REST API wiki](https://github.com/Slugger/pseudosmtp/wiki/REST-API).

### MIME & Attachment Support
The SMTP server completely supports 8bit MIME messages and attachments.  All received attachments are stored and can be retrieved, inspected, validated, etc.

### Check out FAQ & wiki

Please checkout the FAQs and the rest of the wiki for more details about psmtp's features.
