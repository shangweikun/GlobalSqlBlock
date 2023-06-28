package com.swk.mybatis.driver.augmented.core;

import com.swk.mybatis.driver.augmented.param.ParamFactory;
import com.swk.mybatis.driver.augmented.param.SQLParam;
import org.apache.ibatis.parsing.XNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlSQLBlock {

    private static final String paramPatternStr = ":[0-9]+";

    private String id;

    private String sql;

    private final List<Coordinates<String>> params = new ArrayList<>();

    public String handle(XNode xNode, List<SQLParam> sqlParams) {

        if (sqlParams.size() != params.size()) {
            throw new RuntimeException(String.format(
                    "[%s] use global sql [%s], but params size do not match, pleas check you mapper file",
                    xNode.getNode().getAttributes().getNamedItem("id").getNodeValue(), id));
        }

        String result = this.sql;

        for (int i = 0; i < sqlParams.size(); i++) {
            SQLParam sqlParam = sqlParams.get(i);
            Coordinates<String> param = params.get(i);
            result = ParamFactory.handle(sqlParam, result, param.getLeft(), param.getRight());
        }

        return result;
    }

    public XmlSQLBlock generate(String sqlBlockStr, String id, int paramsNum) {

        this.sql = sqlBlockStr;
        this.id = id;

        Pattern paramPattern = Pattern.compile(paramPatternStr);
        Matcher paramMatcher = paramPattern.matcher(sqlBlockStr);

        while (paramMatcher.find()) {

            if (paramsNum == 0) {
                throw new RuntimeException(String.format(
                        "[%s] global sql block do not have param attribute, but found, please check your rule file",
                        id
                ));
            }

            Coordinates<String> coordinates = new Coordinates<>();
            String group = paramMatcher.group();

            int left = sql.indexOf(group);
            coordinates.setLeft(left);
            coordinates.setRight(left + group.length() - 1);
            coordinates.setValue(group);
            this.params.add(coordinates);
        }

        if (paramsNum != params.size()) {
            throw new RuntimeException(String.format(
                    "[%s] global sql block  have param attribute, but the value do not match with sql block, please check your rule file",
                    id
            ));
        }

        return this;
    }
}
