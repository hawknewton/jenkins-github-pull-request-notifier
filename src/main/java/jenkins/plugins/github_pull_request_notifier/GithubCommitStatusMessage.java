package jenkins.plugins.github_pull_request_notifier;

import net.sf.json.JSONObject;
import hudson.model.Result;
import hudson.model.AbstractBuild;

public class GithubCommitStatusMessage {
    private AbstractBuild<?, ?> build;

    public GithubCommitStatusMessage(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public String toJson() {
        JSONObject obj = new JSONObject();
        obj.put("target_url", getBuildUrl());
        obj.put("state", getBuildStatus());
        obj.put("description", getBuildDescription());

        return obj.toString();
    }

    private String getBuildStatus() {
        String status;

        if(build.isBuilding()) {
            status = "pending";
        } else {
            Result result = build.getResult();

            if(result == Result.SUCCESS) {
                status = "success";
            } else if(result == Result.UNSTABLE) {
                status = "failure";
            } else {
                status = "error";
            }
        }

        return status;
    }

    private String getBuildDescription() {
        String description;

        if(build.isBuilding()) {
            description = "is running.  Started " + build.getTime();
        } else {
            Result result = build.getResult();
            if(result == Result.SUCCESS) {
                description = "completed successfully";
            } else if(result == Result.FAILURE) {
                description = "failed";
            } else if(result == Result.ABORTED) {
                description = "was aborted";
            } else if(result == Result.UNSTABLE) {
                description = "was unstable";
            } else {
                description = "errored out";
            }

            description += " after " + build.getDurationString();
        }

        return build.getFullDisplayName() + " " + description;
    }

    @SuppressWarnings("deprecation")
    private String getBuildUrl() {
        return build.getAbsoluteUrl();
    }
}
