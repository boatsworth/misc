package ben.misc.jsoup;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

public class TemplateSpecProcessor {

    final Map<String,Fragment> fragmentMap;
    final List<String> topLevelFragments;
    final Element root;

    public TemplateSpecProcessor(Element rootDocument) {
        this.root = rootDocument;
        fragmentMap = Maps.newLinkedHashMap();
        topLevelFragments = Lists.newArrayList();
    }

    public static TemplateSpecProcessor fromResource(String pathToResource) throws IOException {
        URL url = Resources.getResource(pathToResource);
        String sourceHtml = Resources.toString(url, Charsets.UTF_8);
        Document document = Jsoup.parse(sourceHtml);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        return new TemplateSpecProcessor(document);
    }

    public void process() {
        validate();

        Elements elements = root.select("> fragment");
        for (Element element : elements) {
            Fragment fragment = getFragment(element);
            topLevelFragments.add(fragment.name);
        }

        processFragmentsRecursively();
    }

    protected void processFragmentsRecursively() {
        Elements allFragments = root.select("fragment");
        if (allFragments.isEmpty()) {
            return;
        }

        for (Element fragment : allFragments) {
            boolean hasSubFragments = fragment.select("> fragment").size() > 0;
            if (!hasSubFragments) {
                processFragment(fragment);
            }
        }

        // keep processing until all <fragment> elements have been transformed
        processFragmentsRecursively();
    }

    public Map<String, Fragment> getFragmentMap() {
        return fragmentMap;
    }

    protected void processFragment(Element e) {
        // save fragment's HTML
        Fragment f = getFragment(e);
        f.content = e.html();

        // modify fragment's tag, attributes, and HTML
        transformFragment(e, f.name);

        // update hierarchy
        Element parent = getParentFragment(e);
        if (parent != null) {
            Fragment p = getFragment(parent);
            p.children.add(f.name);
        }
    }

    protected void transformFragment(Element e, String fragmentName) {
        e.tagName("span");
        e.attr("id", fragmentName);
        e.removeAttr("name");
        e.html("{{{"+fragmentName+"}}}");
    }

    protected Element getParentFragment(Element e) {
        Element parent = e.parent();
        while (parent != null) {
            if (parent.tagName().equals("fragment")) {
                return parent;
            }

            parent = parent.parent();
        }
        return null;
    }

    protected Fragment getFragment(Element e) {
        String name = e.attr("name");
        Fragment f = fragmentMap.get(name);
        if (f == null) {
            f = new Fragment();
            f.name = name;
            fragmentMap.put(name, f);
        }
        return f;
    }

    protected void validate() {
        Elements fragments = root.select("fragment");
        Elements fragmentsWithNameAttr = root.select("fragment[name]");

        checkState(fragments.size() == fragmentsWithNameAttr.size(),
                "Fragment must have a 'name' attribute.");

    }


}
