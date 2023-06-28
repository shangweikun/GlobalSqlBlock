package com.swk.mybatis.driver.augmented.core;

import com.swk.mybatis.driver.augmented.param.SQLParam;
import com.swk.mybatis.driver.augmented.UnionUtil;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class ANode {

    private static final String _ANODE_PREFIX = "<todo>";
    private static final String _ANODE_SUFFIX = "</todo>";

    private XNode originXNode;

    private Node targetNode;

    private final Map<Coordinates<XmlSQLBlock>, List<SQLParam>> params = new HashMap<>();

    private final List<Coordinates<XmlSQLBlock>> SQLBlocks = new ArrayList<>();

    private void replaceTargetNode(String sqlStr) {

        sqlStr = _ANODE_PREFIX + sqlStr + _ANODE_SUFFIX;

        InputSource inputSource = new InputSource(new StringReader(sqlStr));
        Document document;
        try {
            document = UnionUtil.builder()
                    .parse(inputSource);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(
                    String.format("error !!! please connect developer : email - [%s]",
                            "shangweikun@yeah.net")
            );
        }
        Node refNode = document.getFirstChild();
        Node parentNode = targetNode.getParentNode();

        NodeList childNodes = refNode.getChildNodes();
        List<Node> deque = new LinkedList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            deque.add(childNodes.item(i));
        }

        Map<Node, Map<String, String>> nodeAttribute = saveAttribute(deque);

        for (Node node : deque) {
            targetNode.getOwnerDocument().adoptNode(node);
            parentNode.insertBefore(node, targetNode);
        }

        copyAttribute(nodeAttribute);

        parentNode.removeChild(targetNode);
    }

    private void copyAttribute(Map<Node, Map<String, String>> nodeAttribute) {
        for (Map.Entry<Node, Map<String, String>> nodeMapEntry : nodeAttribute.entrySet()) {
            Node node = nodeMapEntry.getKey();
            Map<String, String> attributesMap = nodeMapEntry.getValue();
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                for (Map.Entry<String, String> nameValueEntry : attributesMap.entrySet()) {
                    attributes.getNamedItem(nameValueEntry.getKey()).setNodeValue(nameValueEntry.getValue());
                }
            }
        }
    }

    private Map<Node, Map<String, String>> saveAttribute(List<Node> deque) {
        Map<Node, Map<String, String>> attributeCache = new HashMap<>();
        Deque<Node> copyDeque = new LinkedList<>(deque);
        while (!copyDeque.isEmpty()) {
            Node node = copyDeque.pop();
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null && attributes.getLength() > 0) {
                attributeCache.put(node, nameNodeMap2Map(attributes));
            }
            NodeList childNodes1 = node.getChildNodes();
            for (int i = 0; i < childNodes1.getLength(); i++) {
                copyDeque.add(childNodes1.item(i));
            }
        }
        return attributeCache;
    }

    private Map<String, String> nameNodeMap2Map(NamedNodeMap attributes) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            result.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
        }
        return result;
    }

    private String resolve(String sqlStr, Coordinates<XmlSQLBlock> SQLBlock) {
        String prefix = sqlStr.substring(0, SQLBlock.getLeft());
        String suffix = sqlStr.substring(SQLBlock.getRight() + 1);

        return prefix +
                SQLBlock.getValue().handle(originXNode, params.get(SQLBlock))
                + suffix;

    }

    public void setOriginXNode(XNode originXNode) {
        this.originXNode = originXNode;
    }

    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    public Map<Coordinates<XmlSQLBlock>, List<SQLParam>> getParams() {
        return params;
    }

    public List<Coordinates<XmlSQLBlock>> getSQLBlocks() {
        return SQLBlocks;
    }

    public void resolve() {

        String sqlStr = targetNode.getNodeValue();

        List<Coordinates<XmlSQLBlock>> sortedSQLBlocks = SQLBlocks.stream()
                .sorted((o1, o2) -> Integer.compare(o2.getLeft(), o1.getLeft()))
                .collect(Collectors.toList());

        for (Coordinates<XmlSQLBlock> sortedSQLBlock : sortedSQLBlocks) {
            sqlStr = resolve(sqlStr, sortedSQLBlock);
        }

        replaceTargetNode(sqlStr);
    }
}
