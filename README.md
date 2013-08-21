Intro
=====
The jenkins pull request notifer knows how to post messages, on behalf of a user, to github
issues (which are the same thing as pull requests).  In addition, it will update the commit
status at the tip of the pull request to change the merge status.

Why?
---
By itself this plugin might not look completely useful, but when coupled with one of a few
different solutions out there (check out the Jenkins Job DSL plugin for ideas) you can
push the results of a pull request build to github.  It's good stuff.

How?
---
You'll need to compile this hpi and push it to your Jenkins instance.  After that, go to the
*Manage Jenkins* section and enter the github api url (https://api.github.com, maybe you're
running Github Enterprise internally like https://github.mycompany.com/api) and your OAuth token.

You'll have the option to configure each job to post messages to a a specific repo and
issue number.

Example group/repo: `hnewton/cool-project`

Example pull request number: `1`

Common Problems
---------------
* The OAuth token must belong to a user that has permissions to update your repo's commit
  status or you may see a `404` during commit status update.  More info is here:
  http://developer.github.com/v3/repos/statuses/

