package org.apache.camel.component.irc;

import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 *
 * @version $Revision: 655516 $
 */
public class IrcEndpoint extends DefaultEndpoint<IrcExchange> {
    private IrcBinding binding;
    private IrcConfiguration configuration;
    private IrcComponent component;

    public IrcEndpoint(String endpointUri, IrcComponent component, IrcConfiguration configuration) {
        super(endpointUri, component);
        this.component = component;
        this.configuration = configuration;
    }

    public boolean isSingleton() {
        return true;
    }

    public IrcExchange createExchange(ExchangePattern pattern) {
        return new IrcExchange(getCamelContext(), pattern, getBinding());
    }

    public IrcExchange createOnPrivmsgExchange(String target, IRCUser user, String msg) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("PRIVMSG", target, user, msg));
    }

    public IrcExchange createOnNickExchange(IRCUser user, String newNick) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("NICK", user, newNick));
    }

    public IrcExchange createOnQuitExchange(IRCUser user, String msg) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("QUIT", user, msg));
    }

    public IrcExchange createOnJoinExchange(String channel, IRCUser user) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("JOIN", channel, user));
    }

    public IrcExchange createOnKickExchange(String channel, IRCUser user, String whoWasKickedNick, String msg) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("KICK", channel, user, whoWasKickedNick, msg));
    }

    public IrcExchange createOnModeExchange(String channel, IRCUser user, IRCModeParser modeParser) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("MODE", channel, user, modeParser.getLine()));
    }

    public IrcExchange createOnPartExchange(String channel, IRCUser user, String msg) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("PART", channel, user, msg));
    }

    public IrcExchange createOnTopicExchange(String channel, IRCUser user, String topic) {
        return new IrcExchange(getCamelContext(), getExchangePattern(), getBinding(), new IrcMessage("TOPIC", channel, user, topic));
    }

    public IrcProducer createProducer() throws Exception {
        return new IrcProducer(this, component.getIRCConnection(configuration));
    }

    public IrcConsumer createConsumer(Processor processor) throws Exception {
        return new IrcConsumer(this, processor, component.getIRCConnection(configuration));
    }

    public IrcComponent getComponent() {
        return component;
    }

    public void setComponent(IrcComponent component) {
        this.component = component;
    }

    public IrcBinding getBinding() {
        if (binding == null) {
            binding = new IrcBinding();
        }
        return binding;
    }

    public void setBinding(IrcBinding binding) {
        this.binding = binding;
    }

    public IrcConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IrcConfiguration configuration) {
        this.configuration = configuration;
    }
}

