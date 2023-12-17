package com.swk.mybatis.driver.augmented.utils;

import com.swk.mybatis.driver.augmented.core.ANode;
import com.swk.mybatis.driver.augmented.extention.BlockEnum;
import com.swk.mybatis.driver.augmented.extention.BlockMatcher;
import com.swk.mybatis.driver.augmented.extention.bind.BindBlockMatcher;
import com.swk.mybatis.driver.augmented.extention.param.ParamBlockMatcher;
import com.swk.mybatis.driver.augmented.extention.sql.GlobalSqlBlockMatcher;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public class NodeUtil {

    private static final Map<BlockEnum, BlockMatcher> ENUM2MATCHER_DIRECTORY = new HashMap<>();

    static {
        ENUM2MATCHER_DIRECTORY.put(BlockEnum.PARAM, new ParamBlockMatcher());
        ENUM2MATCHER_DIRECTORY.put(BlockEnum.BIND, new BindBlockMatcher());
        ENUM2MATCHER_DIRECTORY.put(BlockEnum.GLOBAL_SQL, new GlobalSqlBlockMatcher());
    }

    private NodeUtil() {
    }

    public static Set<ANode> analyzeXNode(XNode xnode) {

        Set<ANode> result = new HashSet<>();

        Stack<Node> textStack = new Stack<>();

        deepFindTextStack(xnode.getNode(), textStack);

        while (!textStack.isEmpty()) {
            Node node = textStack.pop();
            if (needEnhance(node)) {
                result.add(doEnhance(node));
            }
        }

        return result;
    }

    private static ANode doEnhance(Node node) {
        return null;
    }

    private static boolean needEnhance(Node node) {

        if (!node.getTextContent().contains("@")) {
            return false;
        }

        for (Map.Entry<BlockEnum, BlockMatcher> blockEnumBlockMatcherEntry : ENUM2MATCHER_DIRECTORY.entrySet()) {
            if (blockEnumBlockMatcherEntry.getValue().match(node.getTextContent())) {
                return true;
            }
        }
        return false;
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


}
