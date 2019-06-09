package hudson.plugins.dimensionsscm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.tools.ant.types.selectors.SelectorUtils;

class DefaultPathMatcher implements PathMatcher {
    private final Collection<String> excludeStrings;
    private final Collection<String> includeStrings;

    DefaultPathMatcher(String[] pathsToExclude, String[] pathsToInclude) {
        if (Values.isNullOrEmpty(pathsToExclude)) {
            excludeStrings = Collections.emptyList();
        } else {
            excludeStrings = Arrays.asList(pathsToExclude);
        }
        if (Values.isNullOrEmpty(pathsToInclude)) {
            includeStrings = Collections.emptyList();
        } else {
            includeStrings = Arrays.asList(pathsToInclude);
        }
    }

    @Override
    public boolean match(final String matchText) {
        final String path = (matchText == null) ? "" : matchText;
        boolean rejected = false;
        for (String pattern : excludeStrings) {
            if (SelectorUtils.matchPath(pattern, path)) {
                rejected = true;
                break;
            }
        }
        boolean included = true;
        if (!rejected && !includeStrings.isEmpty()) {
            included = false;
            for (String pattern : includeStrings) {
                if (SelectorUtils.matchPath(pattern, path)) {
                    included = true;
                    break;
                }
            }
        }
        return !rejected && included;
    }

    @Override
    public String toString() {
        return "DefaultPathMatcher(" + Values.toString(excludeStrings) + ", " + Values.toString(includeStrings) + ")";
    }
}
