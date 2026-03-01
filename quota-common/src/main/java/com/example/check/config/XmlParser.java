package com.example.check.config;

import com.example.check.define.*;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

/**
 * XML配置文件解析器
 *
 * 负责解析 interface-flow.xml 配置文件，将接口流程配置转换为内存对象
 *
 * 解析内容：
 *   - 接口名称
 *   - 参数校验规则
 *   - 通用处理链 (common-process)
 *   - 校验链 (check-chain)
 *   - 特殊处理链 (special-process)
 *   - 业务动作链 (biz-action)
 *
 * @see InterfaceDefine 接口定义
 */
public class XmlParser {

    /**
     * 解析XML配置文件
     *
     * @param path 配置文件路径（classpath下）
     * @return 接口定义映射，key为接口名称，value为接口定义
     */
    public static Map<String, InterfaceDefine> parse(String path) {
        Map<String, InterfaceDefine> map = new HashMap<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ClassPathResource(path).getInputStream());
            NodeList list = doc.getElementsByTagName("interface");
            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                InterfaceDefine def = new InterfaceDefine();
                def.setName(el.getAttribute("name"));

                ParamValidate pv = new ParamValidate();
                List<FieldRule> fields = new ArrayList<>();
                Element paramEl = (Element) el.getElementsByTagName("param-validate").item(0);
                NodeList fns = paramEl.getElementsByTagName("field");
                for (int j = 0; j < fns.getLength(); j++) {
                    Element fe = (Element) fns.item(j);
                    FieldRule r = new FieldRule();
                    r.setName(fe.getAttribute("name"));
                    r.setRequired(Boolean.parseBoolean(fe.getAttribute("required")));
                    r.setType(fe.getAttribute("type"));
                    r.setMin(fe.getAttribute("min"));
                    r.setLength(fe.getAttribute("length"));
                    fields.add(r);
                }
                pv.setFields(fields);
                def.setParamValidate(pv);

                def.setCommonProcess(extract(el, "common-process", "handler"));
                def.setCheckChain(extract(el, "check-chain", "check"));
                def.setSpecialProcess(extract(el, "special-process", "handler"));
                def.setBizAction(extract(el, "biz-action", "handler"));

                map.put(def.getName(), def);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * 从XML元素中提取子元素文本内容
     *
     * @param parent 父元素
     * @param pTag 父标签名
     * @param cTag 子标签名
     * @return 子元素文本内容列表
     */
    private static List<String> extract(Element parent, String pTag, String cTag) {
        List<String> list = new ArrayList<>();
        try {
            Element p = (Element) parent.getElementsByTagName(pTag).item(0);
            NodeList nodes = p.getElementsByTagName(cTag);
            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(nodes.item(i).getTextContent().trim());
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
