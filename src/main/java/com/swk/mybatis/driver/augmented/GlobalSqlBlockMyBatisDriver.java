package com.swk.mybatis.driver.augmented;

import com.swk.mybatis.driver.augmented.core.ANode;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.Set;

public class GlobalSqlBlockMyBatisDriver extends XMLLanguageDriver {

    static {
        UnionUtil.init();
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration,
                                     XNode xNode,
                                     Class<?> parameterType) {

        Set<ANode> ANodes = UnionUtil.analyzeXNode(xNode);
        if (!ANodes.isEmpty()) {
            for (ANode aNode : ANodes) {
                aNode.resolve();
            }
        }

        return super.createSqlSource(configuration, xNode, parameterType);
    }
}
