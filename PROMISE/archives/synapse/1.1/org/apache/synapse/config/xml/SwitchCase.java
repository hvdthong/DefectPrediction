package org.apache.synapse.config.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A SwitchCase define a case element of Switch Mediator and It has a list mediator and
 * a regex that is matched by its owning SwitchMediator for selection.
 * If any SwitchCase has selected ,Then the list mediator of it, will responsible
 * for message mediation
 */

public class SwitchCase {

    /** The regular expression pattern to be used */
    private Pattern regex = null;
    /** The list mediator for which responsible message mediation  */
    private AnonymousListMediator caseMediator;

    /**
     * To delegate message mediation to list mediator
     *
     * @param synCtx
     * @return boolean value
     */
    public boolean mediate(MessageContext synCtx) {
        if (caseMediator != null) {
            return caseMediator.mediate(synCtx);
        }
        return true;
    }

    /**
     * To get list mediator of this case element
     *
     * @return List mediator of  switch case
     */
    public AnonymousListMediator getCaseMediator() {
        return caseMediator;
    }

    /**
     * To set the list mediator
     *
     * @param caseMediator
     */
    public void setCaseMediator(AnonymousListMediator caseMediator) {
        this.caseMediator = caseMediator;
    }

    /**
     * To get the regular expression pattern
     *
     * @return Pattern
     */
    public Pattern getRegex() {
        return regex;
    }

    /**
     * To set the regular expression pattern
     *
     * @param regex
     */
    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    /**
     * To evaluate regular expression pattern to a get switch case
     *
     * @param value
     * @return boolean value
     */
    public boolean matches(String value) {
        Matcher matcher = regex.matcher(value);
        if(matcher == null){
            return false;
        }
        boolean retVal = matcher.matches();
        return retVal;
    }
}
