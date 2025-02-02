package modules.reddit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import constants.RegexPatterns;
import core.restclient.RestClient;
import modules.PostBundle;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;

public class RedditDownloader {

    public CompletableFuture<Optional<RedditPost>> retrievePost(long guildId, String input, boolean nsfwAllowed) {
        String[] inputExt = extractSubredditAndOrderBy(input);
        if (inputExt != null) {
            return RestClient.WEBCACHE.getClient(input).get("reddit/single/" + guildId + "/" + nsfwAllowed + "/" + inputExt[0] + "/" + inputExt[1])
                    .thenApply(response -> {
                        if (response.getCode() / 100 == 5) {
                            throw new CompletionException(new IOException("Reddit retrieval error"));
                        }

                        String content = response.getBody();
                        if (content.startsWith("{")) {
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.registerModule(new JavaTimeModule());
                                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                RedditPost redditPost = mapper.readValue(content, RedditPost.class);
                                return Optional.of(redditPost);
                            } catch (JsonProcessingException e) {
                                throw new CompletionException(e);
                            }
                        } else {
                            return Optional.empty();
                        }
                    });
        } else {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    public CompletableFuture<Optional<PostBundle<RedditPost>>> retrievePostsBulk(String input, String args) {
        String[] inputExt = extractSubredditAndOrderBy(input);
        if (inputExt != null) {
            return RestClient.WEBCACHE.getClient(input).get("reddit/bulk/" + inputExt[0] + "/" + inputExt[1])
                    .thenApply(response -> {
                        if (response.getCode() / 100 == 5) {
                            throw new CompletionException(new IOException("Reddit retrieval error"));
                        }

                        String content = response.getBody();
                        if (content.startsWith("[")) {
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.registerModule(new JavaTimeModule());
                                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                List<RedditPost> redditPosts = mapper.readerForListOf(RedditPost.class)
                                        .readValue(content);
                                if (redditPosts.size() > 0) {
                                    PostBundle<RedditPost> postBundle = PostBundle.create(redditPosts, args, RedditPost::getId);
                                    return Optional.of(postBundle);
                                } else {
                                    return Optional.empty();
                                }
                            } catch (JsonProcessingException e) {
                                throw new CompletionException(e);
                            }
                        } else {
                            return Optional.empty();
                        }
                    });
        } else {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private String[] extractSubredditAndOrderBy(String input) {
        Matcher matcher = RegexPatterns.SUBREDDIT.matcher(input.replace(" ", "_"));
        if (matcher.matches()) {
            String subreddit = matcher.group("subreddit");
            String orderBy = matcher.group("orderby");
            if (orderBy == null) {
                orderBy = "hot";
            }
            return new String[] { subreddit, orderBy };
        } else {
            return null;
        }
    }

}
