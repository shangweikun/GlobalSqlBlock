package com.swk.mybatis.driver.augmented;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.swk.mybatis.driver.augmented.core.ANode;
import com.swk.mybatis.driver.augmented.core.Coordinates;
import com.swk.mybatis.driver.augmented.core.XmlSQLBlock;
import com.swk.mybatis.driver.augmented.param.DefaultSQLParam;
import com.swk.mybatis.driver.augmented.param.ListSQLParam;
import com.swk.mybatis.driver.augmented.param.ParamFactory;
import com.swk.mybatis.driver.augmented.param.SQLParam;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

public class UnionUtil {

    private static final Map<String, XmlSQLBlock> directory = new HashMap<>();

    private static final String _GLOBAL_SQL_PREFIX = "@sql{";
    private static final String _GLOBAL_SQL_SUFFIX = "}";

    private static final String _PARAM_PREFIX = "(";
    private static final String _PARAM_SUFFIX = ")";

    private static final String _LIST_PARAM_FLAG = "[]";

    private UnionUtil() {

    }

    private static final Transformer _TRANSFORMER;

    private static final DocumentBuilder _BUILDER;

    static {
        try {
            _TRANSFORMER = TransformerFactory.newInstance().newTransformer();
            _TRANSFORMER.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            _BUILDER = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();

        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<SQLParam> handleParams(String paramsStr) {

        List<SQLParam> result = new ArrayList<>();

        if (paramsStr != null && paramsStr.length() > 0) {

            String[] list = paramsStr.split(",");
            for (String s : list) {
                int lParamBegin = s.indexOf(_LIST_PARAM_FLAG);
                if (lParamBegin > 0) {
                    result.add(ParamFactory.newListSQLParam(s.substring(0, lParamBegin)));
                } else {
                    result.add(ParamFactory.newDefaultSQLParam(s));
                }
            }
        }

        return result;
    }

    private static void deepFindTextStack(Node node, Stack<Node> textStack) {

        if (node == null) {
            return;
        }

        if (node.getNodeType() == Node.TEXT_NODE) {
            textStack.add(node);
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            deepFindTextStack(childNodes.item(i), textStack);
        }
    }

    private static void doResolveSQLBlockDirectory(NodeList childNodes) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == 3) {
                continue;
            }

            if (!"sql".equals(item.getNodeName())) {
                continue;
            }

            NamedNodeMap attributes = item.getAttributes();
            String id = attributes.getNamedItem("id").getNodeValue();

            StringWriter writer = new StringWriter();
            try {
                UnionUtil.transformer()
                        .transform(new DOMSource(item), new StreamResult(writer));
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
            String sqlBlockStr = writer.getBuffer().toString();

            sqlBlockStr = sqlBlockStr.replaceAll("^<sql.*>", "");
            sqlBlockStr = sqlBlockStr.replaceAll("</sql>.*$", "");


            Node paramsNumberNode;
            int paramsNumber = 0;
            if ((paramsNumberNode = attributes.getNamedItem("paramNumber")) != null) {
                paramsNumber = Integer.parseInt(paramsNumberNode.getNodeValue());
            }
            XmlSQLBlock sqlBlockEntity = new XmlSQLBlock().generate(sqlBlockStr, id, paramsNumber);

            directory.put(id, sqlBlockEntity);
        }
    }

    static void init() {

        NodeList childNodes;
        try (InputStream inputStream = GlobalSqlBlockMyBatisDriver.class.getClassLoader().getResourceAsStream("rules.mapper.enhanced.xml")) {
            Document doc = UnionUtil.builder().parse(inputStream);
            Element roots = doc.getDocumentElement();
            childNodes = roots.getChildNodes();
            doResolveSQLBlockDirectory(childNodes);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(String.format("error !!! please connect developer : email - [%s]", "shangweikun@yeah.net"));
        }


    }

    public static Set<ANode> analyzeXNode(XNode xnode) {

        Set<ANode> result = new HashSet<>();

        Stack<Node> textStack = new Stack<>();

        deepFindTextStack(xnode.getNode(), textStack);

        while (!textStack.isEmpty()) {
            Node node = textStack.pop();
            String value = node.getNodeValue();
            int begin = -1;
            int end;
            ANode aNode = new ANode();
            aNode.setOriginXNode(xnode);
            aNode.setTargetNode(node);

            do {
                end = -1;
                begin = value.indexOf(_GLOBAL_SQL_PREFIX, begin + 1);

                if (begin > 0) {
                    end = value.indexOf(_GLOBAL_SQL_SUFFIX, begin);
                }

                if (end > 0 && end > begin) {

                    Node namedItem = xnode.getNode().getAttributes().getNamedItem("id");

                    String blockStr = value.substring(begin + 5, end);
                    String globalSqlId;
                    List<SQLParam> params = new ArrayList<>();
                    XmlSQLBlock xmlSQLBlock;

                    int pBegin = blockStr.indexOf(_PARAM_PREFIX);
                    if (pBegin < 0) {
                        globalSqlId = blockStr;
                        xmlSQLBlock = directory.get(globalSqlId);
                    } else {
                        int pEnd = blockStr.indexOf(_PARAM_SUFFIX, pBegin);
                        if (pEnd < 0) {
                            throw new RuntimeException(String.format("[%s] has global sql block , but param's end flag is not find, please check your mapper xml file", namedItem.getNodeValue()));
                        }
                        globalSqlId = blockStr.substring(0, pBegin);
                        xmlSQLBlock = directory.get(globalSqlId);
                        params.addAll(handleParams(blockStr.substring(pBegin + 1, pEnd)));
                    }

                    if (xmlSQLBlock == null) {
                        throw new RuntimeException(String.format("[%s] has global sql block , but sql block id [%s]do not find, please check you global sql xml file", namedItem.getNodeValue(), globalSqlId));
                    }
                    Coordinates<XmlSQLBlock> xmlSQLBlockCoordinates = new Coordinates<>();

                    xmlSQLBlockCoordinates.setLeft(begin);
                    xmlSQLBlockCoordinates.setRight(end);
                    xmlSQLBlockCoordinates.setValue(xmlSQLBlock);

                    aNode.getSQLBlocks().add(xmlSQLBlockCoordinates);

                    aNode.getParams().put(xmlSQLBlockCoordinates, params);

                    result.add(aNode);
                }

            } while (end > 0 && value.indexOf(_GLOBAL_SQL_PREFIX, end) > 0);

        }

        return result;

    }

    public static Transformer transformer() {
        return _TRANSFORMER;
    }

    public static DocumentBuilder builder() {
        return _BUILDER;
    }
}
