package com.ai.runner.center.pay.web.system.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
 
 /**   
  * 解析xml
  * Title: PAYMENT-PLATFORM <br>
  * Description: <br>
  * Date: 2015年1月9日 <br>
  * Copyright (c) 2015 AILK <br>
  * 
  * @author LiangMeng
  */
 public final class XMLUtil { 
     
     private static final Logger LOG = LogManager.getLogger(XMLUtil.class);
     
     private XMLUtil() {
         
     }
     
     /**
      * xml报文转化成map(一级)
      * @param xml
      * @return
      * @author LiangMeng
      */
     public static Map<String,String> readStringXmlOut(String xml) {
         Map<String,String> map = new HashMap<String,String>();
         Document doc = null;
         try {
             // 将字符串转为XML
             doc = DocumentHelper.parseText(xml); 
             // 获取根节点
             Element rootElt = doc.getRootElement(); 
             // 拿到根节点的名称
             LOG.info("根节点：" + rootElt.getName());
             // 获取根节点下的子节点head
             Iterator iters = rootElt.elementIterator(); 
             // 遍历head节点
             while (iters.hasNext()) {
                 Element itemEle = (Element) iters.next();
                 String eleName = itemEle.getName();
                 map.put(eleName, itemEle.getTextTrim());
             }
         } catch (DocumentException e) {
             LOG.error(e.getMessage(), e);
         } catch (Exception e) {
             LOG.error(e.getMessage(), e);
         }
         return map;
     }
     
 }