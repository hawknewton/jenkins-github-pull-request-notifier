package jenkins.plugin.github_issue_notifier;


import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import jenkins.plugins.github_pull_request_notifier.GithubIssueMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractBuild.class)

public class GithubIssueMessageTest {
    private AbstractBuild<?, ?> mockBuild;

    @Before
    public void setupMocks() {
        mockBuild = mock(AbstractBuild.class);

        when(mockBuild.getFullDisplayName()).thenReturn("full_display_name");
        when(mockBuild.getDurationString()).thenReturn("duration_string");
        when(mockBuild.getAbsoluteUrl()).thenReturn("absolute_url");
    }

    @Test
    public void testSuccessBuildMessage() {
        when(mockBuild.getResult()).thenReturn(Result.SUCCESS);
        String json = new GithubIssueMessage(mockBuild).toJson();

        Assert.assertEquals(json, "{\"body\":\"[full_display_name] (absolute_url) **succeeded** after duration_string\"}");
    }

    @Test
    public void testFailBuildMessage() {
        when(mockBuild.getResult()).thenReturn(Result.FAILURE);
        String json = new GithubIssueMessage(mockBuild).toJson();

        Assert.assertEquals(json, "{\"body\":\"[full_display_name] (absolute_url) **FAILED** after duration_string\"}");
    }

    @Test
    public void testAboredBuildMessage() {
        when(mockBuild.getResult()).thenReturn(Result.ABORTED);

        String json = new GithubIssueMessage(mockBuild).toJson();
        Assert.assertEquals(json, "{\"body\":\"[full_display_name] (absolute_url) **aborted** after duration_string\"}");
    }
}
