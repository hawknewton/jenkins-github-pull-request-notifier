package jenkins.plugins.github_pull_request_notifier;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.PrintStream;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class GithubClient {
    private GithubPullRequestNotifier notifier;
    private EnvVars env;
    private PrintStream logger;

    public GithubClient(GithubPullRequestNotifier notifier, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        this.notifier = notifier;
        this.env = build.getEnvironment(listener);
        this.logger = listener.getLogger();
    }

    public int postMessage(GithubIssueMessage message) throws IOException, InterruptedException{
        String url = getCommentsUrl();
        PostMethod post = new PostMethod(url);

        String json = message.toJson();
        StringRequestEntity requestEntity = new StringRequestEntity(json, "application/json","UTF-8");
        post.setRequestEntity(requestEntity);

        return execute(post);
    }

    public int postMessage(GithubCommitStatusMessage message) throws IOException, InterruptedException {
        String url = getCommitUrl();
        PostMethod post = new PostMethod(url);

        String json = message.toJson();
        StringRequestEntity requestEntity = new StringRequestEntity(json, "application/json","UTF-8");
        post.setRequestEntity(requestEntity);

        return execute(post);
    }

    private int execute(HttpMethod method) throws IOException {
        Header authHeader = new Header("Authorization", "token " + getOauthToken());
        method.addRequestHeader(authHeader);

        HttpClient client = new HttpClient();
        try {
            int status = client.executeMethod(method);

            // cache the response body so we can close the connection
            method.getResponseBodyAsString();
            return status;
        } finally {
            method.releaseConnection();
        }
    }


    private String getOauthToken() {
        String result = notifier.getDescriptor().getOauthToken();
        if(result == null) {
            throw new IllegalArgumentException("Github OAuth token isn't defined.  Define it in your global config");
        }

        return result;
    }

    private String getCommentsUrl() throws IOException, InterruptedException {
        return getGithubUrl() + "/v3/repos/" + getGroupRepo() + "/issues/" + getPullRequestNumber() + "/comments";
    }

    private String getGithubUrl() {
        String result = notifier.getDescriptor().getGithubUrl();
        if (result == null) {
            throw new IllegalArgumentException("Github API URL isn't defined.  Define it in your global config");
        }

        return result;
    }

    private String getGroupRepo() {
        return env.expand(notifier.getGroupRepo());
    }

    private String getPullRequestNumber() {
        return env.expand(notifier.getPullRequestNumber());
    }

    private String getCommitUrl() throws IOException {
        String url = getGithubUrl();

        GetMethod get = new GetMethod(url + "/v3/repos/" + getGroupRepo() + "/pulls/" + getPullRequestNumber());
        int status = execute(get);

        if(status != 200) {
            throw new IllegalStateException("Got non-200 trying to retrieve commit at the tip of the pull request: " + status);
        }

        JSONObject obj = JSONObject.fromObject(get.getResponseBodyAsString());
        String sha = obj.getJSONObject("head").getString("sha");
        return getGithubUrl() + "/v3/repos/" + getGroupRepo() + "/statuses/" + sha;
    }
}
