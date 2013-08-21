package jenkins.plugins.github_pull_request_notifier;

import net.sf.json.JSONObject;
import hudson.model.Result;
import hudson.model.AbstractBuild;

public class GithubIssueMessage {
    private final AbstractBuild<?, ?> build;


    public GithubIssueMessage(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public String toJson() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");

        buf.append(build.getFullDisplayName());
        buf.append("] (");
        buf.append(getBuildUrl());
        buf.append(") **");
        buf.append(getBuildResult());
        buf.append("** after ");
        buf.append(build.getDurationString());

        JSONObject obj = new JSONObject();
        obj.put("body", buf.toString());

        return obj.toString();

    }

    @SuppressWarnings("deprecation")
    private String getBuildUrl() {
        return build.getAbsoluteUrl();
    }

    private String getBuildResult() {
        String result;
        if (build.getResult() == Result.SUCCESS) {
            result = "succeeded";
        } else if (build.getResult() == Result.ABORTED) {
            result = "aborted";
        } else {
            // Unstable, "not built" (whatever that means), failed
            result = "FAILED";
        }

        return result;
    }
}
