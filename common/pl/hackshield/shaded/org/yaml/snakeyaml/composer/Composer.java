/*
 * Decompiled with CFR 0.150.
 */
package pl.hackshield.shaded.org.yaml.snakeyaml.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.hackshield.shaded.org.yaml.snakeyaml.LoaderOptions;
import pl.hackshield.shaded.org.yaml.snakeyaml.composer.ComposerException;
import pl.hackshield.shaded.org.yaml.snakeyaml.error.Mark;
import pl.hackshield.shaded.org.yaml.snakeyaml.error.YAMLException;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.AliasEvent;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.Event;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.MappingStartEvent;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.NodeEvent;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.ScalarEvent;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.SequenceStartEvent;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.MappingNode;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.Node;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.NodeId;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.NodeTuple;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.ScalarNode;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.SequenceNode;
import pl.hackshield.shaded.org.yaml.snakeyaml.nodes.Tag;
import pl.hackshield.shaded.org.yaml.snakeyaml.parser.Parser;
import pl.hackshield.shaded.org.yaml.snakeyaml.resolver.Resolver;

public class Composer {
    protected final Parser parser;
    private final Resolver resolver;
    private final Map<String, Node> anchors;
    private final Set<Node> recursiveNodes;
    private int nonScalarAliasesCount = 0;
    private final LoaderOptions loadingConfig;

    public Composer(Parser parser, Resolver resolver) {
        this(parser, resolver, new LoaderOptions());
    }

    public Composer(Parser parser, Resolver resolver, LoaderOptions loadingConfig) {
        this.parser = parser;
        this.resolver = resolver;
        this.anchors = new HashMap<String, Node>();
        this.recursiveNodes = new HashSet<Node>();
        this.loadingConfig = loadingConfig;
    }

    public boolean checkNode() {
        if (this.parser.checkEvent(Event.ID.StreamStart)) {
            this.parser.getEvent();
        }
        return !this.parser.checkEvent(Event.ID.StreamEnd);
    }

    public Node getNode() {
        this.parser.getEvent();
        Node node = this.composeNode(null);
        this.parser.getEvent();
        this.anchors.clear();
        this.recursiveNodes.clear();
        return node;
    }

    public Node getSingleNode() {
        this.parser.getEvent();
        Node document = null;
        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            document = this.getNode();
        }
        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            Event event = this.parser.getEvent();
            Mark contextMark = document != null ? document.getStartMark() : null;
            throw new ComposerException("expected a single document in the stream", contextMark, "but found another document", event.getStartMark());
        }
        this.parser.getEvent();
        return document;
    }

    private Node composeNode(Node parent) {
        Node node;
        if (parent != null) {
            this.recursiveNodes.add(parent);
        }
        if (this.parser.checkEvent(Event.ID.Alias)) {
            AliasEvent event = (AliasEvent)this.parser.getEvent();
            String anchor = event.getAnchor();
            if (!this.anchors.containsKey(anchor)) {
                throw new ComposerException(null, null, "found undefined alias " + anchor, event.getStartMark());
            }
            node = this.anchors.get(anchor);
            if (!(node instanceof ScalarNode)) {
                ++this.nonScalarAliasesCount;
                if (this.nonScalarAliasesCount > this.loadingConfig.getMaxAliasesForCollections()) {
                    throw new YAMLException("Number of aliases for non-scalar nodes exceeds the specified max=" + this.loadingConfig.getMaxAliasesForCollections());
                }
            }
            if (this.recursiveNodes.remove(node)) {
                node.setTwoStepsConstruction(true);
            }
        } else {
            NodeEvent event = (NodeEvent)this.parser.peekEvent();
            String anchor = event.getAnchor();
            node = this.parser.checkEvent(Event.ID.Scalar) ? this.composeScalarNode(anchor) : (this.parser.checkEvent(Event.ID.SequenceStart) ? this.composeSequenceNode(anchor) : this.composeMappingNode(anchor));
        }
        this.recursiveNodes.remove(parent);
        return node;
    }

    protected Node composeScalarNode(String anchor) {
        Tag nodeTag;
        ScalarEvent ev = (ScalarEvent)this.parser.getEvent();
        String tag = ev.getTag();
        boolean resolved = false;
        if (tag == null || tag.equals("!")) {
            nodeTag = this.resolver.resolve(NodeId.scalar, ev.getValue(), ev.getImplicit().canOmitTagInPlainScalar());
            resolved = true;
        } else {
            nodeTag = new Tag(tag);
        }
        ScalarNode node = new ScalarNode(nodeTag, resolved, ev.getValue(), ev.getStartMark(), ev.getEndMark(), ev.getScalarStyle());
        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }
        return node;
    }

    protected Node composeSequenceNode(String anchor) {
        Tag nodeTag;
        SequenceStartEvent startEvent = (SequenceStartEvent)this.parser.getEvent();
        String tag = startEvent.getTag();
        boolean resolved = false;
        if (tag == null || tag.equals("!")) {
            nodeTag = this.resolver.resolve(NodeId.sequence, null, startEvent.getImplicit());
            resolved = true;
        } else {
            nodeTag = new Tag(tag);
        }
        ArrayList<Node> children = new ArrayList<Node>();
        SequenceNode node = new SequenceNode(nodeTag, resolved, children, startEvent.getStartMark(), null, startEvent.getFlowStyle());
        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }
        while (!this.parser.checkEvent(Event.ID.SequenceEnd)) {
            children.add(this.composeNode(node));
        }
        Event endEvent = this.parser.getEvent();
        node.setEndMark(endEvent.getEndMark());
        return node;
    }

    protected Node composeMappingNode(String anchor) {
        Tag nodeTag;
        MappingStartEvent startEvent = (MappingStartEvent)this.parser.getEvent();
        String tag = startEvent.getTag();
        boolean resolved = false;
        if (tag == null || tag.equals("!")) {
            nodeTag = this.resolver.resolve(NodeId.mapping, null, startEvent.getImplicit());
            resolved = true;
        } else {
            nodeTag = new Tag(tag);
        }
        ArrayList<NodeTuple> children = new ArrayList<NodeTuple>();
        MappingNode node = new MappingNode(nodeTag, resolved, children, startEvent.getStartMark(), null, startEvent.getFlowStyle());
        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }
        while (!this.parser.checkEvent(Event.ID.MappingEnd)) {
            this.composeMappingChildren(children, node);
        }
        Event endEvent = this.parser.getEvent();
        node.setEndMark(endEvent.getEndMark());
        return node;
    }

    protected void composeMappingChildren(List<NodeTuple> children, MappingNode node) {
        Node itemKey = this.composeKeyNode(node);
        if (itemKey.getTag().equals(Tag.MERGE)) {
            node.setMerged(true);
        }
        Node itemValue = this.composeValueNode(node);
        children.add(new NodeTuple(itemKey, itemValue));
    }

    protected Node composeKeyNode(MappingNode node) {
        return this.composeNode(node);
    }

    protected Node composeValueNode(MappingNode node) {
        return this.composeNode(node);
    }
}

