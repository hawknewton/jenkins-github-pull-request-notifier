package jenkins.plugins.github_pull_request_notifier;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.PrintStream;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class GithubPullRequestNotifier extends Notifier {
    private final String pullRequestNumber;
    private final String groupRepo;

    @DataBoundConstructor
    public GithubPullRequestNotifier(String groupRepo, String pullRequestNumber) {
        this.groupRepo = groupRepo;
        this.pullRequestNumber = pullRequestNumber;
    }

    public String getPullRequestNumber() {
        return pullRequestNumber;
    }

    public String getGroupRepo() {
        return groupRepo;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener)  {
        PrintStream logger = listener.getLogger();

        try {
            GithubClient client = new GithubClient(this, build, listener);
            logger.println(postCommitStatus(client, build));

            return true;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Couldn't set commit status: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't set commit status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        GithubClient client = new GithubClient(this, build, listener);

        PrintStream logger = listener.getLogger();

        logger.println(postIssueMessage(client, build));
        logger.println(postCommitStatus(client, build));

        return true;
    }

    private String postCommitStatus(GithubClient client, AbstractBuild<?, ?> build) throws InterruptedException, IOException {
        GithubCommitStatusMessage message = new GithubCommitStatusMessage(build);
        int status = client.postMessage(message);
        String logMessage;

        if(status == 201) {
            logMessage = "Successfully updated commit status";
        } else if(status == 404) {
            logMessage = "Couldn't post commit status.  I got a 404 which can happen if the user doesn't have access to the repo";
        } else {
            logMessage = "Couldn't post commit status, got [" + status + "]";
        }

        return logMessage;
    }

    private String postIssueMessage(GithubClient client, AbstractBuild<?, ?> build) throws InterruptedException, IOException {
        GithubIssueMessage issueMessage = new GithubIssueMessage(build);

        int issueStatus = client.postMessage(issueMessage);

        String logMessage;
        if(issueStatus == 201) {
            logMessage = "Successfully commented on github issue";
        } else {
            logMessage = "Failed to push comment to github issue" + issueStatus;
        }

        return logMessage;
    }



    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends
            BuildStepDescriptor<Publisher> {
        private String githubUrl;
        private String oauthToken;

        public DescriptorImpl() {
            super(GithubPullRequestNotifier.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Github Pull Request Notifier";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData)
                throws hudson.model.Descriptor.FormException {
            githubUrl = formData.getString("githubUrl");
            oauthToken = formData.getString("oauthToken");

            save();
            return super.configure(req, formData);
        }

        public String getGithubUrl() {
            return githubUrl;
        }

        public void setGithubUrl(String githubUrl) {
            this.githubUrl = githubUrl;
        }

        public String getOauthToken() {
            return oauthToken;
        }

        public void setOauthToken(String oauthToken) {
            this.oauthToken = oauthToken;
        }
    }
}
