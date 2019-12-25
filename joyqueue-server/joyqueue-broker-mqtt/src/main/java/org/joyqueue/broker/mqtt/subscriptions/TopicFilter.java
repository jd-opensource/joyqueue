/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.mqtt.subscriptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author majun8
 */
public class TopicFilter implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(TopicFilter.class);

    private static final long serialVersionUID = -1;

    private final String topic;

    private transient List<Token> tokens;

    private transient boolean valid;

    /**
     * Factory method
     */
    //TODO mqtt
    public static TopicFilter asTopic(String s) {
        return new TopicFilter(s);
    }

    public TopicFilter(String topic) {
        this.topic = topic;
    }

    TopicFilter(List<Token> tokens) {
        this.tokens = tokens;
//        List<String> strTokens = tokens.stream().map(Token::toString).collect(Collectors.toList());
        List<String> strTokens = new ArrayList<>();
        String tempTopic = "";
        for (Token token : tokens) {
            tempTopic += "/" + token.toString();
        }
        this.topic = tempTopic;
        this.valid = true;
    }

    public List<Token> getTokens() {
        if (tokens == null) {
            try {
                tokens = parseTopic(topic);
                valid = true;
            } catch (ParseException e) {
                valid = false;
                LOG.error("Error parsing the topic: {}, message: {}", topic, e.getMessage());
            }
        }

        return tokens;
    }

    private List<Token> parseTopic(String topic) throws ParseException {
        List<Token> res = new ArrayList<>();
        String[] splitted = topic.split("/");

        if (splitted.length == 0) {
            res.add(Token.EMPTY);
        }

        if (topic.endsWith("/")) {
            // Add a fictious space
            String[] newSplitted = new String[splitted.length + 1];
            System.arraycopy(splitted, 0, newSplitted, 0, splitted.length);
            newSplitted[splitted.length] = "";
            splitted = newSplitted;
        }

        for (int i = 0; i < splitted.length; i++) {
            String s = splitted[i];
            if (s.isEmpty()) {
                // if (i != 0) {
                // throw new ParseException("Bad format of topic, expetec topic name between
                // separators", i);
                // }
                res.add(Token.EMPTY);
            } else if (s.equals("#")) {
                // check that multi is the last symbol
                if (i != splitted.length - 1) {
                    throw new ParseException(
                            "Bad format of topic, the multi symbol (#) has to be the last one after a separator",
                            i);
                }
                res.add(Token.MULTI);
            } else if (s.contains("#")) {
                throw new ParseException("Bad format of topic, invalid subtopic name: " + s, i);
            } else if (s.equals("+")) {
                res.add(Token.SINGLE);
            } else if (s.contains("+")) {
                throw new ParseException("Bad format of topic, invalid subtopic name: " + s, i);
            } else {
                res.add(new Token(s));
            }
        }

        return res;
    }

    public Token headToken() {
        final List<Token> tokens = getTokens();
        if (tokens.isEmpty()) {
            //TODO UGLY use Optional
            return null;
        }
        return tokens.get(0);
    }

    public boolean isEmpty() {
        final List<Token> tokens = getTokens();
        return tokens == null || tokens.isEmpty();
    }

    /**
     * @return a new Topic corresponding to this less than the head token
     */
    public TopicFilter exceptHeadToken() {
        List<Token> tokens = getTokens();
        if (tokens.isEmpty()) {
            return new TopicFilter(Collections.<Token>emptyList());
        }
        List<Token> tokensCopy = new ArrayList<>(tokens);
        tokensCopy.remove(0);
        return new TopicFilter(tokensCopy);
    }

    public boolean isValid() {
        if (tokens == null)
            getTokens();

        return valid;
    }

    /**
     * Verify rule
     * @param topicFilter
     * @return
     */
    public static boolean isValid(String topicFilter) {
        return  new TopicFilter(topicFilter).isValid();
    }

    /**
     * Verify if the 2 topics matching respecting the rules of MQTT Appendix A
     *
     * @param subscriptionTopic the topic filter of the subscription
     * @return true if the two topics match.
     */
    // TODO reimplement with iterators or with queues
    public boolean match(TopicFilter subscriptionTopic) {
        List<Token> msgTokens = getTokens();
        List<Token> subscriptionTokens = subscriptionTopic.getTokens();
        int i = 0;
        for (; i < subscriptionTokens.size(); i++) {
            Token subToken = subscriptionTokens.get(i);
            if (subToken != Token.MULTI && subToken != Token.SINGLE) {
                if (i >= msgTokens.size()) {
                    return false;
                }
                Token msgToken = msgTokens.get(i);
                if (!msgToken.equals(subToken)) {
                    return false;
                }
            } else {
                if (subToken == Token.MULTI) {
                    return true;
                }
                if (subToken == Token.SINGLE) {
                    // skip a step forward
                }
            }
        }
        // if last token was a SINGLE then treat it as an empty
        // if (subToken == Token.SINGLE && (i - msgTokens.size() == 1)) {
        // i--;
        // }
        return i == msgTokens.size();
    }

    @Override
    public String toString() {
        return topic;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TopicFilter other = (TopicFilter) obj;

        return Objects.equals(this.topic, other.topic);
    }

    @Override
    public int hashCode() {
        return topic.hashCode();
    }
}
