import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;


public class SpringDependencyGraphGenerator {

    String intro = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" +
    		"<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">\r\n" +
    		"  <!--Created by yFiles for Java 2.8-->\r\n" +
    		"  <key for=\"graphml\" id=\"d0\" yfiles.type=\"resources\"/>\r\n" +
    		"  <key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>\r\n" +
    		"  <key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>\r\n" +
    		"  <key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>\r\n" +
    		"  <key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>\r\n" +
    		"  <key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>\r\n" +
    		"  <key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>\r\n" +
    		"  <key attr.name=\"Beschreibung\" attr.type=\"string\" for=\"graph\" id=\"d7\"/>\r\n" +
    		"  <key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>\r\n" +
    		"  <key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/>\r\n" +
    		"  <key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/>\r\n" +
    		"  <graph edgedefault=\"directed\" id=\"G\">\r\n" +
    		"    <data key=\"d7\"/>";

    String umlnode = "<node id=\"%s\">\r\n" +
    		"      <data key=\"d4\"/>\r\n" +
    		"      <data key=\"d5\"><![CDATA[UMLClass]]></data>\r\n" +
    		"      <data key=\"d6\">\r\n" +
    		"        <y:UMLClassNode>\r\n" +
    		"          <y:Geometry height=\"114.0\" width=\"160.0\" x=\"-1215.0\" y=\"-299.0\"/>\r\n" +
    		"          <y:Fill color=\"#%s\" transparent=\"false\"/>\r\n" +
    		"          <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\r\n" +
    		"          <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"13\" \r\n" +
    		"          fontStyle=\"bold\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"20.0\" \r\n" +
    		"          horizontalTextPosition=\"right\" iconTextGap=\"4\" image=\"2\" modelName=\"internal\" modelPosition=\"t\" textColor=\"#000000\"\r\n" +
    		"          verticalTextPosition=\"center\" visible=\"true\" width=\"50.72998046875\" x=\"54.635009765625\" \r\n" +
    		"          y=\"3.0\">%s</y:NodeLabel>\r\n" +
    		"          <y:UML clipContent=\"true\" constraint=\"\" omitDetails=\"false\" stereotype=\"\" use3DEffect=\"true\">\r\n" +
    		"            <y:AttributeLabel>%s</y:AttributeLabel>\r\n" +
    		"            <y:MethodLabel>%s</y:MethodLabel>\r\n" +
    		"          </y:UML>\r\n" +
    		"        </y:UMLClassNode>\r\n" +
    		"      </data>\r\n" +
    		"    </node>\r\n" +
    		"";

    String node = "<node id=\"%s\">\r\n" +
    		"      <data key=\"d5\"/>\r\n" +
    		"      <data key=\"d6\">\r\n" +
    		"        <y:GenericNode configuration=\"EntityRelationship_DetailedEntity\">\r\n" +
    		"          <y:Geometry height=\"95.0\" width=\"215.0\" x=\"117.0\" y=\"58.0\"/>\r\n" +
    		"          <y:Fill color=\"#E8EEF7\" color2=\"#B7C9E3\" transparent=\"false\"/>\r\n" +
    		"          <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\r\n" +
    		"          <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" backgroundColor=\"#B7C9E3\" configuration=\"DetailedEntity_NameLabelConfiguation\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasLineColor=\"false\" height=\"20.0\" horizontalTextPosition=\"right\" iconData=\"1\" iconTextGap=\"10\" modelName=\"internal\" modelPosition=\"t\" textColor=\"#000000\" verticalTextPosition=\"bottom\" visible=\"true\" width=\"194.759765625\" x=\"10.1201171875\" y=\"4.0\">%s</y:NodeLabel>\r\n" +
    		"          <y:NodeLabel alignment=\"left\" autoSizePolicy=\"content\" borderDistance=\"20.0\" configuration=\"DetailedEntity_AttributeLabelConfiguation\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"33.40234375\" modelName=\"custom\" textColor=\"#000000\" visible=\"true\" width=\"59.359375\" x=\"2.0\" y=\"32.0\">%s%s" +
    		"<y:LabelModel>\r\n" +
    		"              <y:ErdAttributesNodeLabelModel/>\r\n" +
    		"            </y:LabelModel>\r\n" +
    		"            <y:ModelParameter>\r\n" +
    		"              <y:ErdAttributesNodeLabelModelParameter/>\r\n" +
    		"            </y:ModelParameter>\r\n" +
    		"          </y:NodeLabel>\r\n" +
    		"          <y:StyleProperties>\r\n" +
    		"            <y:Property class=\"java.lang.Boolean\" name=\"shadow\" value=\"true\"/>\r\n" +
    		"          </y:StyleProperties>\r\n" +
    		"        </y:GenericNode>\r\n" +
    		"      </data>\r\n" +
    		"    </node>";

    String outro = "</graph> <data key=\"d0\">\r\n" +
    		"    <y:Resources>\r\n" +
    		"      <y:Resource id=\"1\">\r\n" +
    		"        <yed:ScaledIcon xScale=\"1.0\" yScale=\"1.0\">\r\n" +
    		"          <yed:ImageIcon image=\"2\"/>\r\n" +
    		"        </yed:ScaledIcon>\r\n" +
    		"      </y:Resource>\r\n" +
    		"      <y:Resource id=\"2\" type=\"java.awt.image.BufferedImage\">iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAACMklEQVR42mNgGJ6gz5KBc5Itg9lU&#13;\r\n" +
    		"e4bCKQ4MM6bYM2yaYsdwBEifBIqtmmzHEDXTmIEVQ2OnNQNvnzVD/EQ75i2Lg6Vv7sy3eHGkzvPT&#13;\r\n" +
    		"qdbAn1tbg/72lDj9m5xv/WVhsNSNfhuGNBTNzcYMyp0WzKtWxWk+ONsR+v3qpPj/F/ui/6+u8v7f&#13;\r\n" +
    		"kGn3f0GN3/9zkxL+n++O+H+yOeBHmxnDDrjman0G6VZz9l0HKt3f3p6e8v8aUPOKcp//hVHm/6cW&#13;\r\n" +
    		"e/2/MC31/6X+mP+H633ABhyu8/5bb8BwGG5AuQZD68Ys6xf3ZqX/31QX/D/N3+h/X67n/0uzsv7f&#13;\r\n" +
    		"BBoI0nSmI/T/JaCLQPSyeP3PZZoMi+AG5Coy7Lk6Mf73qoqA/wlehv8vz8v/f29eNtglVybE/r8M&#13;\r\n" +
    		"tP3GlMT/p9tD/q/JMP3fbC1yL1eZIQ5uQLwYw+rVJT6/olz0/19bVPL/3uyM/7emJf+/OTUJTIMM&#13;\r\n" +
    		"2FHi+H9BjOb/ThfptwnijOvTBBn44QaE8DHEpetLPp6WbPl/e6nT/11lzv93AumthXb/V6Ya/Z8e&#13;\r\n" +
    		"rPB/sr/svyoz4VehAoy7A3kYNFFiQIaBgdOdjaE1Xpbzbp2V0OcuN9G/vZ5i/7vdRf812wt/Lzbg&#13;\r\n" +
    		"fx0uynbdnZVhtgYDgwK2tCMMTBVGqgwMGTYMDEsdGBgOAfEZID5mz8Cw0ZyBYYosA0MkGwODHlAt&#13;\r\n" +
    		"kMnAB8TMyAZwAbEEEEsRwGJADPI7OxAzDY58AwCtoO//tOXxxAAAAABJRU5ErkJggg==</y:Resource>\r\n" +
    		"    </y:Resources>\r\n" +
    		"  </data>\r\n" +
    		"</graphml>";

    String egde = "    <edge id=\"%s\" source=\"%s\" target=\"%s\">\r\n" +
    		"      <data key=\"d10\">\r\n" +
    		"        <y:PolyLineEdge>\r\n" +
    		"          <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\r\n" +
    		"          <y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\r\n" +
    		"          <y:Arrows source=\"none\" target=\"standard\"/>\r\n" +
    		"        </y:PolyLineEdge>\r\n" +
    		"      </data>\r\n" +
    		"    </edge>\r\n" +
    		"";

    /**
     * if set, beanNames with qualifiedNames (dottet) will be shorted to the simple name (after the last dot).
     * default: true.
     */
    private boolean shortNames = true;

    /**
     * if set, bean that have no dependencies at all, will be included in the graph.
     * default: false.
     */
    private boolean showBeanWithoutDeps = false;

    /**
     * list of all known bean names.
     */
    List<String> beanNames;

    /**
     * list of egdes in graph.
     */
    ArrayList<Edge> egdes = new ArrayList<SpringDependencyGraphGenerator.Edge>();

    /**
     * list of nodes (beans) for the graph
     */
    ArrayList<Node> nodes = new ArrayList<SpringDependencyGraphGenerator.Node>();

    /**
     * stores list of locations for spring context resources.
     */
    private String[] locations;

    private String outfileName;

    /**
     * map with bean defs.
     */
    LinkedHashMap<String, BeanDefinition> beans = new LinkedHashMap<String, BeanDefinition>();

    private static class Edge {
        String from;
        String to;
        public Edge(String from, String to) {
            super();
            this.from = from;
            this.to = to;
        }
    }
    private static class Node {
        String name;
        String markup;
        public Node(String name, String markup) {
            super();
            this.name = name;
            this.markup = markup;
        }
    }

    public static void main(String[] args) {
        SpringDependencyGraphGenerator instance = new SpringDependencyGraphGenerator();
        String[] l = new String[] {
                "springconfig/trinity-oneandone-mgmt-legacy-beans.xml",
                "springconfig/trinity-oneandone-mgmt-legacy-db-beans.xml",
                "springconfig/trinity-oneandone-mgmt-legacy-jobs-beans.xml",
                "springconfig/trinity-oneandone-mgmt-legacy-resources-beans.xml",
        };
        instance.setLocations(l);
        instance.setOutfileName("C:/Users/srinke/AbschaltungAltsysteme/1.graphml");
        instance.run();
    }

    /**
     * @param args
     */
    public void run() {

        loadProps();

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        for (String loc : locations) {
            reader.loadBeanDefinitions(new ClassPathResource(loc));
        }

        beanNames = Arrays.asList(factory.getBeanDefinitionNames());
        for (String name : beanNames) {
            BeanDefinition def = factory.getBeanDefinition(name);
            beans.put(name, def);

            MutablePropertyValues props = def.getPropertyValues();
            ConstructorArgumentValues ctorArgs = def.getConstructorArgumentValues();

            if( def.getParentName() != null && beanNames.contains(def.getParentName())) {
                addEdge(name, def.getParentName());
            }

            StringBuilder sbc = new StringBuilder();
            for (ValueHolder valHolder : ctorArgs.getGenericArgumentValues()) {
                sbc.append(valHolder.getName()!=null?valHolder.getName():"");
                if( valHolder.getValue() instanceof RuntimeBeanReference ) {
                    RuntimeBeanReference ref = (RuntimeBeanReference)valHolder.getValue();
                    sbc.append("C: ref("+makeLabel(ref.getBeanName())+")");
                }
                checkReferences(name, valHolder.getValue());
                sbc.append("\r\n");
            }

            StringBuilder sbp = new StringBuilder();
            for (PropertyValue propVal : props.getPropertyValueList()) {
                checkReferences(name, propVal.getValue());
                if( propVal.getValue() instanceof RuntimeBeanReference ) {
                    RuntimeBeanReference ref = (RuntimeBeanReference)propVal.getValue();
                    sbp.append(propVal.getName()+": ref("+makeLabel(ref.getBeanName())+")\r\n");
                } else if(propVal.getValue() instanceof BeanDefinitionHolder) {
                    BeanDefinitionHolder holder = (BeanDefinitionHolder) propVal.getValue();
                    String facBean = holder.getBeanDefinition().getFactoryBeanName();
                    if( facBean != null ) {
                        sbp.append(propVal.getName()+": fac("+makeLabel(facBean)+")\r\n");
                    }
                } else {
                    sbp.append(propVal.getName()+"\r\n");
                }
            }

            nodes.add( new Node( name,  String.format(umlnode, name, getColor(name), makeLabel(name), sbc.toString(), sbp.toString() )));
        }

        try {
            renderGraph();
        } catch( IOException e) {
            e.printStackTrace();
        }
    }

    private static class NodeDef {
        String color;
        Pattern namePattern;
    }

    ArrayList<NodeDef> nodeDefs = new ArrayList<SpringDependencyGraphGenerator.NodeDef>();

    Properties colorProps = new Properties();

    private String getColor(String name) {
        for (NodeDef nd : nodeDefs) {
            if( nd.namePattern.matcher(name).matches() ) {
                return nd.color;
            }
        }
        return colorProps.getProperty("defaultColor","B7C9E3");
    }

    private void loadProps() {
        if( colorProps.isEmpty() ) {
            try {
                colorProps.load(new FileInputStream("colors.properties"));

            } catch( IOException e)  {
                e.printStackTrace();
            }
        }
        Enumeration<String> names = (Enumeration<String>) colorProps.propertyNames();
        while( names.hasMoreElements() ) {
            String key = names.nextElement();
            if( key.startsWith("pattern.")) {
                String name = key.substring(8);
                NodeDef nd = new NodeDef();
                nd.namePattern = Pattern.compile(colorProps.getProperty(key));
                nd.color = colorProps.getProperty("color."+name);
                nodeDefs.add(nd);
            }
        }


    }

    private void renderGraph() throws IOException {
        PrintStream out = new PrintStream(new File(outfileName));

        out.print(intro);

        for (Node n : nodes) {
            if( showBeanWithoutDeps  || hasEdges( n.name ) ) {
                out.print(n.markup);
            }
        }

        int edgeId = 0;
        for (Edge e : egdes) {
            out.print(String.format(egde, String.valueOf(edgeId), e.from, e.to));
            edgeId++;
        }
        out.print(outro);
        out.close();
    }

    /**
     * adds an edge from node to node
     * @param from id of 'from' node
     * @param to id of 'to' node
     */
    private void addEdge(String from, String to) {
        egdes.add( new Edge(from, to));
    }

    /**
     * recursively checks dependecies in bead definition. egde is only added for runtime bean refs (direct or in maps,
     * set, lists, arrays or props) that are know (contained in beanNames).
     * @param name name of actual bean
     * @param value value of property or ctor arg, that will be inspected
     */
    @SuppressWarnings("rawtypes")
    private void checkReferences(String name, Object value) {
        if( value instanceof RuntimeBeanReference ) {
            RuntimeBeanReference ref = (RuntimeBeanReference) value;
            if( beanNames.contains(ref.getBeanName()) ) {
                addEdge(name, ref.getBeanName());
            }
        } else if ( value instanceof ManagedMap ) {
            ManagedMap m = (ManagedMap)value;
            for(Object v : m.values() ) {
                checkReferences(name, v);
            }
        } else if ( value instanceof ManagedArray ) {
            ManagedArray a = (ManagedArray)value;
            for(Object v : a ) {
                checkReferences(name, v);
            }
        } else if ( value instanceof ManagedList ) {
            ManagedList a = (ManagedList)value;
            for(Object v : a ) {
                checkReferences(name, v);
            }
        } else if ( value instanceof ManagedSet ) {
            ManagedSet a = (ManagedSet)value;
            for(Object v : a ) {
                checkReferences(name, v);
            }
        } else if ( value instanceof ManagedProperties ) {
            ManagedProperties a = (ManagedProperties)value;
            for(Entry<Object, Object> v : a.entrySet() ) {
                checkReferences(name, v.getValue());
            }
        } else if (value instanceof BeanDefinitionHolder) {
            BeanDefinitionHolder holder = (BeanDefinitionHolder) value;
            String facBean = holder.getBeanDefinition().getFactoryBeanName();
            if( facBean != null ) addEdge(name,facBean);
        }
    }

    /**
     * create a label for a node with respect to {@link #shortNames} flag.
     * @param in input
     * @return label
     */
    private String makeLabel(String in) {
        if( shortNames ) {
            int i = in.lastIndexOf(".");
            return i==-1?in:in.substring(i+1);
        } else {
            return in;
        }
    }

    /**
     * check if node 'name' has any edges (from or to).
     * @param name name of the node.
     * @return true if node has edges.
     */
    private boolean hasEdges(String name) {
        for (Edge e : egdes) {
            if( e.to.equals(name) || e.from.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the locations.
     *
     * @return the locations
     */
    public String[] getLocations() {
        return locations;
    }

    /**
     * Sets the locations.
     *
     * @param locations the new locations
     */
    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    /**
     * Gets the outfile name.
     *
     * @return the outfile name
     */
    public String getOutfileName() {
        return outfileName;
    }

    /**
     * Sets the outfile name.
     *
     * @param outfileName the new outfile name
     */
    public void setOutfileName(String outfileName) {
        this.outfileName = outfileName;
    }

}
